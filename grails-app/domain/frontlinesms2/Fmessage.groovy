package frontlinesms2

import groovy.time.*
import frontlinesms2.enums.MessageStatus

class Fmessage {
	String src
	String dst
	String text
	String displaySrc
	Date dateCreated
	Date dateReceived
	boolean contactExists
	MessageStatus status
	boolean read
	boolean deleted
	boolean starred
	static belongsTo = [messageOwner:MessageOwner]
	static transients = ['displaySrc']
	static mapping = {
		sort dateCreated:'desc'
		sort dateReceived:'desc'
	}

	static constraints = {
		src(nullable:true)
		dst(nullable:true)
		text(nullable:true)
		messageOwner(nullable:true)
		dateReceived(nullable:true)
		status(nullable:true)
	}
	
	static namedQueries = {
			inbox { isStarred ->
				and {
					eq("deleted", false)
					if(isStarred)
						eq("starred", true)
					eq("status", MessageStatus.INBOUND)
					isNull("messageOwner")
				}
			}
			sent { isStarred ->
				and {
					eq("deleted", false)
					eq("status", MessageStatus.SENT)
					isNull("messageOwner")
					if(isStarred)
						eq("starred", true)
				}
			}
			pending { isStarred ->
				and {
					eq("deleted", false)
					isNull("messageOwner")
					'in'("status", [MessageStatus.SEND_PENDING, MessageStatus.SEND_FAILED])
					if(isStarred)
						eq('starred', true)
				}
			}
			deleted { isStarred ->
				and {
					eq("deleted", true)
					if(isStarred)
						eq('starred', true)
				}
			}
			owned { isStarred, responses ->
				and {
					eq("deleted", false)
					'in'("messageOwner", responses)
					if(isStarred)
						eq("starred", true)
				}
			}
			unread {
				and {
					eq("deleted", false)
					eq("status", MessageStatus.INBOUND)
					eq("read", false)
					isNull("messageOwner")
				}
			}
                        receivedBetween { startDate, endDate ->
                                         between("dateReceived", startDate, endDate)
                        }
	}

	def getDisplayText() {
		def p = PollResponse.withCriteria {
			messages {
				eq('deleted', false)
				eq('id', this.id)
			}
		}

		p?.size()?"${p[0].value} (\"${this.text}\")":this.text
	}
	
	def updateDisplaySrc() {
		if(src) {
			def c = Contact.findByPrimaryMobile(src)
			displaySrc = c? c.name: src
			contactExists = c? true: false
		}
	}
	
	def toDelete() {
		this.deleted = true
		this
	}

	def addStar() {
		this.starred = true
		this
	}
	
	def removeStar() {
		this.starred = false
		this
	}
	static def getFolderMessages(folderId) {
		def folder = Folder.get(folderId)
		def messages = Fmessage.owned(folder).list(sort:"dateReceived", order:"desc")
		messages
	}

	static def getInboxMessages(isStarred) {
		def messages = Fmessage.inbox(isStarred).list(sort:"dateReceived", order:"desc")
		messages
	}

	static def getSentMessages(isStarred) {
		def messages = Fmessage.sent(isStarred).list(sort:"dateReceived", order:"desc")
		messages
	}
		
	static def getPendingMessages(isStarred) {
		def messages = Fmessage.pending(isStarred).list(sort:"dateReceived", order:"desc")
		messages
	}

	static def getDeletedMessages(isStarred) {
		def messages = Fmessage.deleted(isStarred).list(sort:"dateReceived", order:"desc")
		messages
	}
	
	static def countInboxMessages(isStarred) {
		def messageCount = Fmessage.inbox(isStarred).count()
		messageCount
	}
	
	static def countSentMessages(isStarred) {
		def messageCount = Fmessage.sent(isStarred).count()
		messageCount
	}
	
	static def countPendingMessages(isStarred) {
		def messageCount = Fmessage.pending(isStarred).count()
		messageCount
	}
	
	static def countDeletedMessages(isStarred) {
		def messageCount = Fmessage.deleted(isStarred).count()
		messageCount
	}
	
	static def countUnreadMessages(isStarred) {
		def messageCount = Fmessage.unread().count()
		messageCount
	}
	
	static def countAllMessages(isStarred) {
		def inboxCount = Fmessage.countInboxMessages()
		def sentCount = Fmessage.countSentMessages()
		def pendingCount = Fmessage.countPendingMessages()
		def deletedCount = Fmessage.countDeletedMessages()
		[inbox: inboxCount, sent: sentCount, pending: pendingCount, deleted: deletedCount]
	}

        static def allMessagesReceivedBetween(startDate, endDate) {
		def messages = Fmessage.receivedBetween(startDate, endDate).list(sort:"dateReceived", order:"desc")
		messages
        }
	
	static def search(String searchString=null, Group groupInstance=null, Collection<MessageOwner> messageOwner=[]) {
		if(searchString) {
			def groupPrimaryMobile = groupInstance?.getMembers()*.primaryMobile
			if(!groupPrimaryMobile) {
				groupPrimaryMobile = "null"
			}
			
			def results = Fmessage.createCriteria().list {
				ilike("text", "%${searchString}%")
				and{
					if(groupInstance) {
						'in'("src",  groupPrimaryMobile)
					}
					if(messageOwner) {
						'in'("messageOwner", messageOwner)
					}
					eq('deleted', false)
				}
				order('dateReceived', 'desc')
				order('dateCreated', 'desc')
			}
			results*.updateDisplaySrc()
			results
		} else {
			[]
		}
	}
}
