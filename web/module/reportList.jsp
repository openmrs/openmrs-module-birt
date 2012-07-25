<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/module/birt/report.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>
 
 
<h2><spring:message code="birt.manage.title"/></h2>	

<br />

<style>
#reportList { 
	font-size: 1.1em; 
	padding: 0px; 
}

#reportListTable { 
	width: 100%;
	background-color: white; 
	padding: 0px;
	margin: 0px;
} 
#reportListTable td { 
	padding-left: 10px; 
}
#reportListTable tr { 
	height: 30px;
}

</style>

<%--
<b class="boxHeader"><spring:message code="Report.upload.title"/></b>
<form method="post" action="reportUpload.form" enctype="multipart/form-data" class="box">
	<div>
		<spring:message code="general.file" />
		<input type="file" name="birtReport" size="50" />
		<input type="submit" value='<spring:message code="general.upload" />' />
	</div>
</form>

<br/>
--%>
<!-- <b><spring:message code="birt.list.title"/></b> -->
<b class="boxHeader"><spring:message code="birt.list.title"/></b>
<div class="box">
	<div id="reportList">
		<table id="reportFilterTable" width="100%" cellpadding="0" cellspacing="0">		
			<tr>
				<td align="center">
					<form method="get">				
						<input type="text" name="filter" size="40"/>
						<input type="submit" value="Filter" /> 
						<a href="report.list">Show all reports</a>
					</form>					
				</td>
			</tr>
		</table>
	
	
		<table id="reportListTable" cellspacing="0" cellpadding="0" border="0">
			<c:forEach var="report" items="${reportList}" varStatus="varStatus">
				<c:if test="${varStatus.first}">
					<tr class="evenRow">		
						<th align="center"><spring:message code="birt.generateReport.header"/></th>						
						<th align="center"><spring:message code="birt.editReport.header"/></th>
						<th></th>
						<th align="left"><spring:message code="birt.reportTitle.header" /></th>
					</tr>
				</c:if>
				
				<c:set var="rowClass" scope="page">
					<c:choose><c:when test="${varStatus.index % 2 == 0}">oddRow</c:when><c:otherwise>evenRow</c:otherwise></c:choose>
				</c:set>
				<tr class="${rowClass}">

					<td valign="middle" align="center" width="3%">					
						<a href="generateReport.form?reportId=${report.reportDefinition.id}"><img src="${pageContext.request.contextPath}/images/play.gif" 
								border="0" alt="<spring:message code="Report.generate"/>" /></a>						
					</td>
					<td valign="middle" align="center" width="3%">
						<a href="report.form?reportId=${report.reportDefinition.id}"><img src="${pageContext.request.contextPath}/images/edit.gif" 	border="0" alt="<spring:message code="Report.edit"/>" /></a>
					</td>					
					<td valign="middle" align="left">
					
						<c:if test="${report.reportDesign != null }">
							<a href="downloadReportDesign.form?id=${report.reportDesign.id}">Download Report Design by ID</a>
							<a href="downloadReportDesign.form?uuid=${report.reportDesign.uuid}">Download Report Design by UUID</a>						
						</c:if>
					</td>

					<td valign="middle" align="left">${report.reportDefinition.name}</td>

				</tr>
			</c:forEach>			
		</table>
		<c:if test="${fn:length(reportList) == 0}">
			<i> &nbsp; <spring:message code="birt.noReports"/></i><br/>
		</c:if>		
	</div>
</div>		




<%@ include file="/WEB-INF/template/footer.jsp" %>
