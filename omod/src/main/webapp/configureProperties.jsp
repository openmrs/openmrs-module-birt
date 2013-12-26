<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:require privilege="Manage Global Properties" otherwise="/login.htm" redirect="/module/birt/configureProperties.htm" />
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp"%>

<openmrs:htmlInclude file="/dwr/interface/DWRAdministrationService.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<h2><spring:message code="birt.configure.title"/></h2>	


<b class="boxHeader"><spring:message code="birt.configure.title"/></b>
<div class="box">
	<div id="configureProperties">	
		<openmrs:portlet url="globalProperties" id="globalPropertyEditSection" parameters="propertyPrefix=birt.|excludePrefix=birt.started|hidePrefix=true" />
	</div>
</div>
<%@ include file="/WEB-INF/template/footer.jsp"%>
