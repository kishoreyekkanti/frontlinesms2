<%@ page contentType="text/html;charset=UTF-8" %>
<html>
	<head>
		<title><g:layoutTitle default="Messages"/></title>
		<g:render template="/css"/>
		<link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
		<g:javascript library="jquery" plugin="jquery"/>
		<jqui:resources />
		<g:javascript src="application.js"/>
		<g:javascript src="popup.js"></g:javascript>
        <g:layoutHead />
    </head>
	<body>
		<g:render template="/tabs"/>
		<g:render template="quick_message"/>
		<g:remoteLink controller="quickMessage" action="create" onSuccess="launchWizard('Quick Message', data);" class="quick_message">
			Quick Message
		</g:remoteLink>
        <g:render template="/flash"/>
		<div id="main">
			<g:render template="menu"/>
			<g:render template="message_list"/>
			<g:layoutBody/>
			Show:
			<g:link action="${messageSection}" params="${params.findAll({it.key != 'max' && it.key != 'offset'}) + [starred: true]}" >Starred</g:link>
			<g:link action="${messageSection}" params="${params.findAll({it.key != 'starred' && it.key != 'max' && it.key != 'offset'})}">All</g:link>
		</div>
	    <div id="footer">
			<g:paginate next="Forward" prev="Back"
					 max="${grailsApplication.config.pagination.max}" controller="message"
					action="${messageSection}" total="${messageInstanceTotal}" params= "${params}"/>
		</div>
	</body>
</html>
