package frontlinesms2

import grails.plugin.spock.ControllerSpec

class QuickMessageControllerSpec extends ControllerSpec {
	def 'create returns the contact and group list'() {
		setup:
			def jim = new Contact(name:"jim", address:"12345")
			def mohave = new Group(name:"Mojave", members: [jim])
			mockDomain Contact, [jim]
			mockDomain Group, [mohave]
		when:
			def result = controller.create()
		then:
			result['contactList'] == [jim]
			result['groupList'] == ["Mojave": 1]
	}
}