<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/module/birt/report.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript" charset="utf-8">	
	
	function confirmDelete(name, id) {
		if (confirm("Are you sure you want to delete " + name + " ?")) {
			$j.ajax({
				type: "POST",
				url: "report.list",				  
				data: { uuid : id, removeReport : name },
				success: function() { window.location.reload(true); },  
			})			
		}
	}

</script>
 
<div class="spacer" style="height: 15px"><!--  --></div>
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
						<a href="report.list"><spring:message code="birt.showAllReports"/></a>
					</form>					
				</td>
			</tr>
		</table>	
	
		<table id="reportListTable" cellspacing="0" cellpadding="0" border="0">
			<c:forEach var="report" items="${reportList}" varStatus="varStatus">
				<c:if test="${varStatus.first}">
					<tr class="evenRow">
						<th align="center" width="3%">Actions</th>						
						<th align="left"><spring:message code="birt.reportName.header" /></th>
						<th align="left"><spring:message code="birt.reportDescription.header" /></th>
						<th align="left"><spring:message code="birt.reportDownload.header" /></th>					
					</tr>
				</c:if>				
				<c:set var="rowClass" scope="page">
					<c:choose><c:when test="${varStatus.index % 2 == 0}">oddRow</c:when><c:otherwise>evenRow</c:otherwise></c:choose>
				</c:set>
				<tr class="${rowClass}">
					<td width="3%" align="center" nowrap>
							&nbsp;
							<a href="report.form?reportId=${report.reportDefinition.id}&uuid=${report.reportDefinition.uuid}&type=${report.reportDefinition['class'].name}"><img src="${pageContext.request.contextPath}/images/edit.gif" 	border="0" alt="<spring:message code="Report.edit"/>" /></a>					
							&nbsp;
							<a href="javascript:confirmDelete('${report.reportDefinition.name}','${report.reportDefinition.uuid}');"><img src="<c:url value='/images/trash.gif'/>" border="0"/></a>			
							&nbsp;
							<a href="${pageContext.request.contextPath}/module/reporting/run/runReport.form?reportId=${report.reportDefinition.uuid}"><img src="${pageContext.request.contextPath}/images/play.gif" border="0" alt="<spring:message code="Report.generate"/>" /></a>
					</td>
					<td valign="middle" align="left">${report.reportDefinition.name}</td>
					<td valign="middle" align="left">${report.reportDefinition.description}</td>
					<td id="o-report-deliver" valign="middle" align="left">					
						<c:if test="${report.reportDesign != null }">							
							<a href="downloadReportDesign.form?uuid=${report.reportDesign.uuid}&id=${report.reportDesign.id}" title="Deliver report to file system">RPTDESIGN</a>						
						</c:if>
					</td>
				</tr>
			</c:forEach>			
		</table>
		<c:if test="${fn:length(reportList) == 0}">
			<i> &nbsp; <spring:message code="birt.noReports"/></i><br/>
		</c:if>		
	</div>
</div>		




<%@ include file="/WEB-INF/template/footer.jsp" %>
