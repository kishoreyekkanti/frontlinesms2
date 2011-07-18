<%@ page contentType="text/html;charset=UTF-8" %>
<div  id ="poll-actions">
  <h2>Categorize Response</h2>
    <div id="container"></div>
  <ol>


	  <g:each in="${responseList}" status="i" var="r">
		  <li>
			  <g:link action="changeResponse" params="[ownerId: ownerInstance.id, responseId: r.id, messageId: messageInstance.id]">${r.value}</g:link>
		  </li>
	  </g:each>
  </ol>
</div>
