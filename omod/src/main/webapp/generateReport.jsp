<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/module/birt/report.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="birt.runner.wizard.title"/></h2>	

<br />
<b class="boxHeader"><spring:message code="birt.runner.wizard.generateReport.title"/></b>
<form id="" action="" method="post" class="box">

	<input type="hidden" name="_page4" value="true"/>
	<input type="hidden" name="_finish" value="true"/>
	
	<h3><spring:message code="birt.runner.wizard.generateReport.title"/></h3>
	
    <input type="submit" class="button" name="save" value="Continue"/>
    <input type="submit" class="button" name="cancel" value="Cancel"/>	
	
</form>