<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/module/birt/report.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>
 
 
<h2><spring:message code="birt.downloadReportDesign.title"/></h2>	

<!-- <b><spring:message code="birt.list.title"/></b> -->
<b class="boxHeader"><spring:message code="birt.downloadReportDesign.title"/></b>
<div class="box">
	<div id="reportDesignList">
		
		<div class="error">
			${model.error }
		</div>
	</div>
</div>		




<%@ include file="/WEB-INF/template/footer.jsp" %>