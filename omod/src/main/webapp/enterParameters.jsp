<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/module/birt/report.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="birt.runner.wizard.title"/></h2>	

<br />
<b class="boxHeader"><spring:message code="birt.runner.wizard.enterParameters.title"/></b>
<form id="" action="" method="post" class="box">

	<input type="hidden" name="_page3" value="true"/>
	<input type="hidden" name="_target4" value="true"/>
	
	<h3><spring:message code="birt.runner.wizard.enterParameters.title"/></h3>
	
	<c:if test="${empty report.reportParameters}">
		<spring:message code="birt.reporParameters.error"/>
	</c:if>
	<c:forEach var="parameter" items="${report.reportParameters}">
		param: ${parameter}<br/>
	</c:forEach>

	
    <input type="submit" class="button" name="save" value="Continue"/>
    <input type="submit" class="button" name="cancel" value="Cancel"/>	
	
</form>