package frontlinesms2.poll

import frontlinesms2.*
import frontlinesms2.enums.MessageStatus

class PollGebSpec extends grails.plugin.geb.GebSpec {
	static createTestPolls() {
		[Poll.createPoll('Football Teams', ['manchester', 'barcelona']),
				Poll.createPoll('Shampoo Brands', ['pantene', 'oriele']),
				Poll.createPoll('Rugby Brands', ['newzealand', 'britain'])]*.save(failOnError:true, flush:true)
	}

	static createTestMessages() {
		[new Fmessage(src:'Bob', dst:'+254987654', text:'I like manchester', dateReceived: new Date() - 4, starred: true),
			new Fmessage(src:'Alice', dst:'+2541234567', text:'go manchester', dateReceived: new Date() - 3),
				new Fmessage(src:'Joe', dst:'+254112233', text:'pantene is the best',  dateReceived: new Date() - 2),
				new Fmessage(src:'Jill', dst:'+234234', text:'I fell down the hill',  dateReceived: new Date() - 1)].each() {
					it.status = MessageStatus.INBOUND
					it.save(failOnError:true, flush:true)
				}

		[PollResponse.findByValue('manchester').addToMessages(Fmessage.findBySrc('Bob')),
				PollResponse.findByValue('manchester').addToMessages(Fmessage.findBySrc('Alice')),
				PollResponse.findByValue('pantene').addToMessages(Fmessage.findBySrc('Joe'))]*.save(failOnError:true, flush:true)
	}

	static createTestFolders() {
		['Work', 'Projects'].each {
			new Folder(name: it).save(failOnError:true, flush:true)
		}
	}
	
	static deleteTestPolls() {
		Poll.findAll().each() {
			it.refresh()
			it.delete(failOnError:true, flush:true)
		}
	}

	static deleteTestMessages() {
		Fmessage.findAll().each() {
			it.refresh()
			it.delete(failOnError:true, flush:true)
		}
	}
	
	static deleteTestFolders() {
		Folder.findAll().each() {
			it.refresh()
			it.delete(failOnError:true, flush:true)
		}
	}
}

