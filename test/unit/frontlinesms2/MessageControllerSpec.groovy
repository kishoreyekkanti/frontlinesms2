package frontlinesms2

import grails.plugin.spock.*

class MessageControllerSpec extends ControllerSpec {

	def setup() {
		mockDomain Fmessage
		registerMetaClass(Fmessage)
		Fmessage.metaClass.'static'.countAllMessages = {isStarred -> [inbox:0,pending:0,deleted:0,sent:0]}
		mockParams.messageText = "text"
		controller.messageSendService = new MessageSendService()
		def sahara = new Group(name: "Sahara", members: [new Contact(primaryMobile: "12345"),new Contact(primaryMobile: "56484")])
		def thar = new Group(name: "Thar", members: [new Contact(primaryMobile: "12121"), new Contact(primaryMobile: "22222")])
		mockDomain Group, [sahara, thar]
		mockConfig('''
			pagination.max = 10
		''')

	}

	def "should send message to all the members in a group"() {
		setup:
			mockParams.groups = "Sahara"
		when:
			assert Fmessage.count() == 0
			controller.send()
		then:
			Fmessage.list()*.dst.containsAll(["12345","56484"])
	}

	def "should send message to all the members in multiple groups"() {
		setup:
			mockParams.groups = ["Sahara", "Thar"]
		when:
			assert Fmessage.count() == 0
			controller.send()
		then:
			Fmessage.list()*.dst.containsAll(["12345","56484","12121","22222"])
	}
	
	def "should send a message to the given address"() {
		setup:
			mockParams.addresses = "+919544426000"
		when:
			assert Fmessage.count() == 0
			controller.send()
		then:
			Fmessage.count() == 1
	}

	def "should eliminate duplicate address if present"() {
		setup:
			mockParams.addresses = "12345"
			mockParams.groups = "Sahara"
		when:
			assert Fmessage.count() == 0
			controller.send()
		then:
			Fmessage.count() == 2
	}

	def "should send message to each recipient in the list of address"() {
		setup:
			def addresses = ["+919544426000", "+919004030030", "+1312456344"]
			mockParams.addresses = addresses
		when:
			assert Fmessage.count() == 0
			controller.send()
		then:
			Fmessage.list()*.dst.containsAll(addresses)
			Fmessage.count() == 3
	}
	
	def "should display flash message on successful message sending"() {
		setup:
			def addresses = ["+919544426000", "+919004030030", "+1312456344"]
			mockParams.addresses = addresses
		when:
			assert Fmessage.count() == 0
			controller.send()
		then:
			controller.flash.message == "Message has been queued to send to +919544426000, +919004030030, +1312456344"
			
	}

	def "should fetch starred inbox messages"() {
		def isStarred = true

		expect:
		setupDataAndAssert(isStarred, 5, 1, {fmessage ->
			Fmessage.metaClass.'static'.countInboxMessages = {starred ->
				assert isStarred == starred
				return 2
			}

			Fmessage.metaClass.'static'.getInboxMessages = { starred, max, offset ->
				if(isStarred && max == mockParams.max && offset == mockParams.offset)
					[fmessage]
			}

			controller.inbox()
		})
	}




	def "should fetch all inbox messages"() {
		def isStarred = false
		expect:
			setupDataAndAssert(isStarred, null, null, { fmessage ->

				Fmessage.metaClass.'static'.getInboxMessages = {starred, max, offset ->
					assert isStarred == starred
					assert max == 10
					assert offset == 0
					[fmessage]
				}

				Fmessage.metaClass.'static'.countInboxMessages = {starred ->
					assert isStarred == starred
					2
				}
				controller.inbox()
		})
	}


	def "should fetch starred pending messages"() {
		def isStarred = true
		expect:
			setupDataAndAssert(isStarred, 3, 4, {fmessage ->
				Fmessage.metaClass.'static'.getPendingMessages = {starred, max, offset ->
					assert starred == isStarred
					assert max == mockParams.max
					assert offset == mockParams.offset
					return [fmessage]
				}

				Fmessage.metaClass.'static'.countPendingMessages = {starred ->
					assert isStarred == starred
					2
				}

				controller.pending()
		})
	}

	def "should fetch all pending messages"() {
		def isStarred = false
		expect:
			setupDataAndAssert(isStarred, null, null, {fmessage ->
				Fmessage.metaClass.'static'.getPendingMessages = {starred, max, offset ->
					assert isStarred == starred
					assert max == 10
					assert offset == 0
					return [fmessage]
				}

				Fmessage.metaClass.'static'.countPendingMessages = {starred ->
					assert isStarred == starred
					2
				}

				controller.pending()
		})
	}

	def "should fetch all poll messages"() {
		def isStarred = false
		expect:
			setupDataAndAssert(isStarred, null, null, {fmessage ->
				def poll = new Poll(id: 2L, responses: [new PollResponse()])
				mockParams.ownerId = 2L
				mockDomain Poll, [poll]
				poll.metaClass.getMessages = {starred, max, offset ->
					assert starred == isStarred
					assert max == 10
					assert offset == 0
					[fmessage]
				}

				poll.metaClass.countMessages = {starred ->
					assert isStarred == starred
					2
				}
				controller.poll()

		})
	}

	//FIXME: Need to  replace it with 'setupDataAndAssert' method.
	def "should fetch starred poll messages"() {
		setup:
			registerMetaClass(Poll)
			def starredFmessage = new Fmessage(starred: true)
			def unstarredFmessage = new Fmessage(starred: false)
			def poll = new Poll(id: 2L, responses: [new PollResponse()])
			mockParams.starred = true
			mockParams.ownerId = 2L
			mockParams.max = 2
			mockParams.offset =3
			mockDomain Folder
			mockDomain Poll, [poll]
			mockDomain Fmessage, [starredFmessage, unstarredFmessage]
			poll.metaClass.getMessages = {isStarred, max, offset ->
				assert max == 2
				assert offset == 3
				isStarred ? [starredFmessage] : [starredFmessage, unstarredFmessage]
			}

			poll.metaClass.countMessages = {isStarred ->
				2
			}

		when:
			def results = controller.poll()
		then:
			results['messageInstanceList'] == [starredFmessage]
	}

	def "should fetch all folder messages"() {
		def isStarred = false
		expect:
			setupDataAndAssert(isStarred, null, null, {fmessage ->
				def folder = new Folder(id: 2L, messages: [fmessage])
				mockParams.ownerId = 2L
				mockDomain Folder, [folder]
				folder.metaClass.getFolderMessages = {starred, max, offset->
						assert starred == isStarred
						assert max == 10
						assert offset == 0
						[fmessage]
				}

				folder.metaClass.countMessages = {starred ->
					assert isStarred == starred
					2
				}
				controller.folder()
			})
	}

	def "should fetch starred folder messages"() {
		expect:
			def isStarred = true
			setupDataAndAssert (isStarred, 3, 2, {fmessage ->
				def folder = new Folder(id: 2L, messages: [fmessage])
				mockParams.ownerId = 2L
				mockDomain Folder, [folder]
				folder.metaClass.getFolderMessages = {starred, max, offset->
					assert starred == isStarred
					assert max == mockParams.max
					assert offset == mockParams.offset
					[fmessage]
				}
				folder.metaClass.countMessages = {starred ->
					assert isStarred == starred
					2
				}
				controller.folder()
			})
	}


	def "should fetch starred trash messages"() {
		expect:
			def isStarred = true
			setupDataAndAssert (isStarred, 3, 4, {fmessage ->
				Fmessage.metaClass.'static'.getDeletedMessages = {starred, max, offset->
					assert starred == starred;
					assert max == mockParams.max;
					assert offset == mockParams.offset;
					[fmessage]
				}

				Fmessage.metaClass.'static'.countDeletedMessages = {starred ->
					assert isStarred == starred
					2
				}

				controller.trash()
			})
	}

	def "should fetch all trash messages"() {
		expect:
			def isStarred = false
			setupDataAndAssert (isStarred, null, null, {fmessage ->
				Fmessage.metaClass.'static'.getDeletedMessages = {starred, max, offset ->
					assert starred == isStarred
					assert max == 10
					assert offset == 0
					[fmessage]
				}

				Fmessage.metaClass.'static'.countDeletedMessages = {starred ->
					assert isStarred == starred
					2
				}
				
				controller.trash()
			})
	}

	def "should show the starred sent messages"() {
		expect:
			def isStarred = true
			setupDataAndAssert (isStarred, 3, 4, {fmessage ->
				Fmessage.metaClass.'static'.getSentMessages = {starred, max, offset ->
					assert starred == isStarred
					assert max == 3
					assert offset == 4
					[fmessage]
				}

				Fmessage.metaClass.'static'.countSentMessages = {starred ->
					assert isStarred == starred
					2
				}

				controller.sent()
			})
	}

	def "should show all the  sent messages"() {
		expect:
			def isStarred = false
			setupDataAndAssert (isStarred,null, null, {fmessage ->
				Fmessage.metaClass.'static'.getSentMessages = {starred, max, offset ->
					assert starred == isStarred
					assert max == 10
					assert offset == 0
					[fmessage]
				}

				Fmessage.metaClass.'static'.countSentMessages = {starred ->
					assert isStarred == starred
					2
				}
				controller.sent()
			})
	}

     private void setupDataAndAssert(boolean isStarred, Integer max, Integer offset, Closure closure)  {
			registerMetaClass(Fmessage)
			def fmessage = new Fmessage(src: "src1", starred: isStarred)
			mockDomain Folder
			mockDomain Poll
			mockDomain Contact
			mockParams.starred = isStarred
			mockParams.max = max
			mockParams.offset = offset

			def results = closure.call(fmessage)

			assert results['messageInstanceList'] == [fmessage]
			assert results['messageInstanceTotal'] == 2
			assert results['messageInstance'] == fmessage
			assert results['messageInstanceList']*.contactExists == [false]
			assert results['messageInstanceList']*.displaySrc == ["src1"]
    }
}