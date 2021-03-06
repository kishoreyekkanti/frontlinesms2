package frontlinesms2.message

import frontlinesms2.*

class FolderListSpec extends frontlinesms2.folder.FolderGebSpec {
	def 'folder message list is displayed'() {
		given:
			createTestFolders()
			createTestMessages()
		when:
			to FolderListPage
			def folderMessageSources = $('#messages tbody tr td:nth-child(2)')*.text()
		then:
			at FolderListPage
			folderMessageSources == ['Jane', 'Max']
		cleanup:
			deleteTestFolders()
			deleteTestMessages()
	}
	
	def "message's folder details are shown in list"() {
		given:
			createTestFolders()
			createTestMessages()
		when:
			"message/folder/${Folder.findByName('Work').id}/show/${Fmessage.findBySrc('Max')}"
			def rowContents = $('#messages tbody tr:nth-child(1) td')*.text()
		then:
			rowContents[1] == 'Jane'
			rowContents[2] == 'Meeting at 10 am'
			rowContents[3] ==~ /[0-9]{2}-[A-Z][a-z]{2}-[0-9]{4} [0-9]{2}:[0-9]{2}/
		cleanup:
			deleteTestFolders()
			deleteTestMessages()
	}

	def 'selected folder is highlighted'() {
		given:
			createTestFolders()
			createTestMessages()
		when:
			"message/folder/${Folder.findByName('Work').id}/show/${Fmessage.findBySrc('Max')}"
			def selectedMenuItem = $('#messages-menu .selected')
		then:
			selectedMenuItem.text() == 'Work'
		cleanup:
			deleteTestFolders()
			deleteTestMessages()
	}

	def "should be able to reply for messages listed in the folder section"() {
		setup:
			createTestFolders()
			createTestMessages()
		when:
			def folder = Folder.findByName("Work")
			def messages = folder.getMessages() as List
			def message = messages[0]
			go "message/folder/${folder.id}/show/${message.id}"
		then:
			$("#reply-dropdown").value('reply')
			waitFor {$('div#tabs-1').displayed}
		when:
			$("div#tabs-1 .next").click()
		then:
			$('input', value: message.src).getAttribute('checked')
		cleanup:
			deleteTestFolders()
			deleteTestMessages()
	}

	def "should filter folder messages for starred and unstarred messages"() {
		given:
			createTestFolders()
			createTestMessages()
		when:
			go "message/folder/${Folder.findByName('Work').id}/show/${Fmessage.findBySrc('Max').id}"
		then:
			$("#messages tbody tr").size() == 2
		when:
			$('a', text:'Starred').click()
			waitFor {$("#messages tbody tr").size() == 1}
		then:
			$("#messages tbody tr")[0].find("td:nth-child(2)").text() == 'Max'
		when:
			$('a', text:'All').click()
			waitFor {$("#messages tbody tr").size() == 2}
		then:
			$("#messages tbody tr").collect {it.find("td:nth-child(2)").text()}.containsAll(['Jane', 'Max'])
		cleanup:
			deleteTestFolders()
			deleteTestMessages()

	}
	
	def "should autopopulate the message body when 'forward' is clicked"() {
		setup:
			createTestFolders()
			createTestMessages()
		when:
			def folder = Folder.findByName("Work")
			go "message/folder/${folder.id}/show/${Fmessage.findBySrc('Max').id}"
		then:
			$("#reply-dropdown").value('forward')
			waitFor {$('div#tabs-1').displayed}
			$('textArea', name:'messageText').text() == "I will be late"
		cleanup:
			deleteTestFolders()
			deleteTestMessages()
	}
	
	def 'folder message list should also display message counts'() {
		given:
			createTestFolders()
			createTestMessages()
		when:
			"message"
		then:
			$("#activities-submenu li")*.text() == ['Work (2)', 'Projects (2)']
		cleanup:
			deleteTestFolders()
			deleteTestMessages()
	}
}

class FolderListPage extends geb.Page {
 	static url = "message/folder/${Folder.findByName('Work').id}/show/${Fmessage.findBySrc('Max').id}"
	static at = {
		title.endsWith('Folder')
	}
	static content = {
		messagesList { $('#messages-submenu') }
	}
}