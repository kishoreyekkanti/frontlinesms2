package frontlinesms2

import grails.converters.JSON

class PollController {
	def index = {
		 redirect(action: "create", params: params)
	}

	def create = {
		def pollInstance = new Poll()
		pollInstance.properties = params
		[pollInstance: pollInstance]
	}

	def save = {
		def responseList = params.responses.tokenize()
		def pollInstance = Poll.createPoll(params.title, responseList)
		
		if (pollInstance.save(flush: true)) {
			flash.message = "${message(code: 'default.created.poll', args: [message(code: 'poll.label', default: 'Poll'), pollInstance.id])}"
			redirect(controller: "message", action:'inbox', params:[flashMessage: flash.message])
		} else {
			render(view: "create", model: [pollInstance: pollInstance])
		}
	}

    def plot = {
        def ownerInstance = Poll.get(params.ownerId)
        def stats = ownerInstance.responseStats
        render ownerInstance.responseStats as JSON
    }
}
