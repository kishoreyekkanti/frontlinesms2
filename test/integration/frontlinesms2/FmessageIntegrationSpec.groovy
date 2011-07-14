package frontlinesms2

import frontlinesms2.enums.MessageStatus

class FmessageIntegrationSpec extends grails.plugin.spock.IntegrationSpec {

	def 'get deleted messages gets all messages with deleted flag'() {
		setup:
				(1..3).each {
				    new Fmessage(deleted:true).save(flush:true)
				}
				(1..2).each {
					new Fmessage(deleted:false).save(flush:true)
				}
		when:
			def deletedMessages = Fmessage.getDeletedMessages(false)
		then:
			deletedMessages.size == 3
	}
	
	def "should return all message counts"() {
		setup:
			Fmessage inboxMessage = new Fmessage(status:MessageStatus.INBOUND, deleted:false, text:'An inbox message').save(flush:true)
			Fmessage anotherInboxMessage = new Fmessage(status:MessageStatus.INBOUND,deleted:false, text:'Another inbox message').save(flush:true)
			
			Fmessage sentMessage = new Fmessage(status:MessageStatus.SENT, deleted:false, text:'A sent message').save(flush:true)
			Fmessage anotherSentMessage = new Fmessage(status:MessageStatus.SENT,deleted:false, text:'Another sent message').save(flush:true)
			Fmessage deletedSentMessage = new Fmessage(status:MessageStatus.SENT,deleted:true, text:'Deleted sent message').save(flush:true)
			
			Fmessage sentFailedMessage = new Fmessage(status:MessageStatus.SEND_FAILED, deleted:false, text:'A sent failed message').save(flush:true)
			Fmessage sentPendingMessage = new Fmessage(status:MessageStatus.SEND_PENDING,deleted:false, text:'A pending message').save(flush:true)
			
		when:
			def messageCounts = Fmessage.countAllMessages(false)
		then:
			messageCounts['inbox'] == 2
			messageCounts['sent'] == 2
			messageCounts['pending'] == 2
			messageCounts['deleted'] == 1
	}
	
	def "should return unread messages count"() {
		setup:
			Fmessage readMessage = new Fmessage(status:MessageStatus.INBOUND, deleted:false, text:'A read message', read:true).save(flush:true)
			Fmessage unreadMessage = new Fmessage(status:MessageStatus.INBOUND,deleted:false, text:'An unread message', read:false).save(flush:true)
		when:
			def unreadMessageCount = Fmessage.countUnreadMessages()
		then:
			unreadMessageCount == 1
	}

        def "can filter messages between given dates by dateReceived"() {
                setup:   
                        Fmessage message1 = new Fmessage(dateReceived: new Date("2011/10/11")).save()
                        Fmessage message2 = new Fmessage(dateReceived: new Date("2011/10/12")).save()
                        Fmessage message3 = new Fmessage(dateReceived: new Date("2011/10/13")).save()
                when:
                        def startDate = new Date("2011/10/12")
                        def endDate   = new Date("2011/10/13")
                        def messages = Fmessage.allMessagesReceivedBetween(startDate, endDate)
                then:
                        messages == [message3, message2]
        }
}
