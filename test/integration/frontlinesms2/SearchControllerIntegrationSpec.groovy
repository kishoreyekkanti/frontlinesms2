package frontlinesms2

import frontlinesms2.enums.MessageStatus

class SearchControllerIntegrationSpec extends grails.plugin.spock.IntegrationSpec {
	def controller
	def firstContact, secondContact
	def group
	def folder

	def setup() {
		controller = new SearchController()
		firstContact = new Contact(name:'Alex', primaryMobile:'+254987654').save(failOnError:true)
		secondContact = new Contact(name:'Mark', primaryMobile:'+254333222').save(failOnError:true)
		group = new Group(name:'test').save(failOnError:true)
		
		[new Fmessage(src:'+254987654', dst:'+254987654', text:'work at 11.00'),
			new Fmessage(src:'+254111222', dst:'+254937634', text:'work is awesome'),
			new Fmessage(src:'Bob', dst:'+254987654', text:'hi Bob'),
				new Fmessage(src:'Michael', dst:'+2541234567', text:'Can we get meet in 5 minutes')].each() {
					it.status = MessageStatus.INBOUND
					it.save(failOnError:true)
				}

		def chickenMessage = new Fmessage(src:'Barnabus', dst:'+12345678', text:'i like chicken', status:MessageStatus.INBOUND)
		def liverMessage = new Fmessage(src:'Minime', dst:'+12345678', text:'i like liver', status: MessageStatus.INBOUND)
		def liverMessage2 = new Fmessage(src:'+254333222', dst:'+12345678', text:'liver for lunch?', status:MessageStatus.INBOUND)
		def chickenResponse = new PollResponse(value:'chicken')
		def liverResponse = new PollResponse(value:'liver')
		liverResponse.addToMessages(liverMessage)
		liverResponse.addToMessages(liverMessage2)
		chickenResponse.addToMessages(chickenMessage)
		new Poll(title:'Miauow Mix', responses:[chickenResponse, liverResponse]).save(failOnError:true, flush:true)
	}

	def cleanup() {
		Group.findAll()*.delete(flush:true, failOnError:true)
		Contact.findAll()*.delete(flush:true, failOnError:true)
		Poll.findAll()*.delete(flush:true, failOnError:true)
		MessageOwner.list()*.delete(flush:true, failOnError:true)
		Fmessage.findAll()*.delete(flush:true, failOnError:true)
	}
	
	private def makeGroupMember() {
		firstContact.addToGroups(group)
		secondContact.addToGroups(group)
		firstContact.save(flush: true)
		secondContact.save(flush: true)
		assert(Contact.get(firstContact.id).isMemberOf(Group.get(group.id)))
		assert(Contact.get(secondContact.id).isMemberOf(Group.get(group.id)))
	}
	
	def "blank search does not return a list of messages"() {
		when:
			controller.params.searchString = ""
			def model = controller.result()
		then:
			model.messageInstanceList.isEmpty()
	}
	
	def "message searches can be restricted to a poll"() {
		when:
			controller.params.searchString = "chicken"
			controller.params.activityId = "poll-${Poll.findByTitle('Miauow Mix').id}"
			def model = controller.result()
		then:
			model.messageInstanceList == [Fmessage.findBySrc('Barnabus')]
	}
	
	def "message searches can be restricted to a folder"() {
		when:
			folder = new Folder(name: 'work').save(failOnError:true, flush:true)
			folder.addToMessages(Fmessage.findBySrc('+254111222')).save(failOnError: true, flush:true)
			controller.params.searchString = "work"
			controller.params.activityId = "folder-${folder.id}"
			def model = controller.result()
		then:
			model.messageInstanceList == [Fmessage.findBySrc('+254111222')]
	}
	
	def "message searches can be restricted to a contact group"() {
		given:
			makeGroupMember()
		when:
			controller.params.searchString = "liver"
			controller.params.groupId = Group.findByName('test').id
			def model = controller.result()
		then:
			model.messageInstanceList == [Fmessage.findBySrc('+254333222')]
	}
	
	def "groups without contacts do not return messages"() {
		when:
			controller.params.searchString = "test"
			controller.params.groupId = Group.findByName('test').id
			def model = controller.result()
		then:
			model.messageInstanceList.isEmpty()
	}
	
	def "message searches can be restricted to both contact groups and polls"() {
		given:
			makeGroupMember()
		when:
			controller.params.searchString = "liver"
			controller.params.activityId = "poll-${Poll.findByTitle('Miauow Mix').id}"
			controller.params.groupId = Group.findByName('test').id
			def model = controller.result()
		then:
			model.messageInstanceList == [Fmessage.findBySrc('+254333222')]
	}
	
	def "deleted messages do not appear in search results"() {
		when:
			controller.params.searchString = "liver"
			controller.params.activityId = "poll-${Poll.findByTitle('Miauow Mix').id}"
			Fmessage.findBySrc("+254333222").toDelete().save(flush: true)
			def model = controller.result()
		then:
			model.messageInstanceList == [Fmessage.findBySrc('Minime')]
	}
}