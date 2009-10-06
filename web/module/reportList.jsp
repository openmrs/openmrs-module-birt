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
		<table id="reportFilterTable" width="100%" cellpadding="5" cellspacing="5">		
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
	
	
		<table id="reportListTable" cellspacing="5" cellpadding="5" border="0">
			<c:forEach var="report" items="${reportList}" varStatus="varStatus">
				<c:if test="${varStatus.first}">
					<tr class="evenRow">		
						<th align="center">Run<!--<spring:message code="Report.generate"/>--></th>
						<th align="center">Edit<!-- <spring:message code="Report.edit"/> --></th>
						<th align="left"><spring:message code="general.name" /></th>
						<!-- <th align="center">Delete</th>-->
						<!--  <th align="left">Data Set</th> -->				
						<%--<th align="center">Report Design (.rptdesign)</th>--%>
					</tr>
				</c:if>
				
				<c:set var="rowClass" scope="page">
					<c:choose><c:when test="${varStatus.index % 2 == 0}">oddRow</c:when><c:otherwise>evenRow</c:otherwise></c:choose>
				</c:set>
				<tr class="${rowClass}">

					<td valign="middle" align="center" width="3%">					
						<a href="generateReport.form?reportId=${report.reportDefinition.reportObjectId}"><img src="${pageContext.request.contextPath}/images/play.gif" 
								border="0" 
								alt="<spring:message code="Report.generate"/>" /></a>						
					</td>
					<td valign="middle" align="center" width="3%">
						<a href="report.form?reportId=${report.reportDefinition.reportObjectId}"><img src="${pageContext.request.contextPath}/images/edit.gif" 
								border="0" 
								alt="<spring:message code="Report.edit"/>" /></a>
					</td>					

					<td valign="middle" align="left">
						<a href="report.form?reportId=${report.reportDefinition.reportObjectId}">
							${report.reportDefinition.name}
						</a>
					</td>
					<%-- 
					<td valign="middle" align="left">
						${report.reportDefinition.createdDate}
					</td>	
					--%>				
					<!-- 
					<td valign="middle" align="left" width="300">
						<a href="${pageContext.request.contextPath}/admin/reports/dataExport.form?dataExportId=${report.reportDefinition.dataExport.reportObjectId}">
							${report.reportDefinition.dataExport.name}
						</a>
					</td>
					 -->
						<%--
						<a href="report.form?delete=true&reportId=${report.reportDefinition.reportObjectId}">
							<img src="${pageContext.request.contextPath}/images/trash.gif" border="0" alt="<spring:message code="Report.delete"/>" />
						</a>
						&nbsp;
						<img src="${pageContext.request.contextPath}/images/edit.gif" border="0" alt="<spring:message code="Report.edit"/>" />
						<img src="${pageContext.request.contextPath}/images/copy.gif" border="0" alt="<spring:message code="Report.duplicate"/>" />
						<img src="${pageContext.request.contextPath}/images/open.gif" border="0" alt="<spring:message code="Report.design"/>" />
						<img src="${pageContext.request.contextPath}/images/play.gif" border="0" alt="<spring:message code="Report.generate"/>" />
						--%>
					<%--
					<td valign="top" align="center">
						<c:if test="${report.reportDesignExists}">
							<input class="smallButton" type="submit" value="<spring:message code="birt.reportDesign.download"/>"/>
						</c:if>	
					</td>
					--%>
				</tr>
			</c:forEach>			
		</table>
		<c:if test="${fn:length(reportList) == 0}">
			<i> &nbsp; <spring:message code="birt.noReports"/></i><br/>
		</c:if>		
	</div>
</div>		




<%@ include file="/WEB-INF/template/footer.jsp" %>
