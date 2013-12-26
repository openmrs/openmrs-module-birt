<%@ include file="/WEB-INF/template/include.jsp" %>


<openmrs:hasPrivilege privilege="Manage Reports">


	<openmrs:htmlInclude file="/dwr/engine.js" />
	<openmrs:htmlInclude file="/dwr/util.js" />
	<openmrs:htmlInclude file="/dwr/interface/DwrBirtReportService.js"/>

	<%--
		showUnpublishedReports == 'true' means allow users to enter forms that haven't been published yet
	--%>
	<c:set var="showUnpublishedReports" value="false" />
	<openmrs:hasPrivilege privilege="View Unpublished Reports"><c:set var="showUnpublishedReports" value="true" /></openmrs:hasPrivilege>
	
	<%--
		goBackOnGeneration == 'true' means have the browser go back to the find patient page after starting to enter a form
	--%>
	<c:set var="goBackOnGenerate" value="true" />
	<openmrs:globalProperty key="birt.patientReports.goBackOnGenerate" var="goBackOnGenerate" defaultValue="false"/>
	
	
						
	<div id="selectReportHeader" class="boxHeader${model.patientVariation}">
		<spring:message code="birt.patientDashboard.reports"/>
	</div>
	<div id="selectReport" class="box${model.patientVariation}">
		<table id="reportListTable" border="0" width="50%" cellspacing="1" cellpadding="5">
			<thead>
				<tr>
					<th>Report</th>
					<th>PDF</th>
					<th>HTML</th>
					<th>XLS</th>
					<th>DOC</th>
					<th>Show Form</th>
				</tr>
			</thead>
			<tbody id="reportListTbody">
				<!-- populate with report list using DWR -->				
			</tbody>
		</table>
	</div>
		


	
<script type="text/javascript">			
	addEvent(window, 'load', function() { try { fillReportTable(); } catch (err) { /* ignore */ }});
</script>
	
<script type="text/javascript">

	function fillReportTable() {
		DwrBirtReportService.getReports(fillReportsCallback);
	}
	
	function fillReportsCallback(reports) { 
		var tableId = "reportListTable";
		
		if (reports.length > 0) { 							
			//DWRUtil.removeAllRows(tableId);
			DWRUtil.addRows(tableId, reports, [
				function(report) { return report.name; },
				function(report) { return '<a href="javascript:downloadReport(' + report.id + ', \'pdf\', \'' + report.description + '\', true);"><img src="moduleResources/birt/pdf.jpg" border=0" alt="PDF"/></a>';},
				function(report) { return '<a href="javascript:downloadReport(' + report.id + ', \'html\', \'' + report.description + '\', true);"><img src="moduleResources/birt/html.jpg" border=0" alt="HTML"/></a>';},
				function(report) { return '<a href="javascript:downloadReport(' + report.id + ', \'xsl\', \'' + report.description + '\', true);"><img src="moduleResources/birt/xls.jpg" border=0" alt="XLS"/></a>';},
				function(report) { return '<a href="javascript:downloadReport(' + report.id + ', \'doc\', \'' + report.description + '\', true);"><img src="moduleResources/birt/doc.jpg" border=0" alt="DOC"/></a>';},
				function(report) { return '<a href="javascript:downloadReport(' + report.id + ', \'\', \'' + report.description + '\', false);"><img src="moduleResources/birt/lookup.gif" border=0" alt="Show Form"/></a>';}
			], {'escapeHtml':false});
		}
	}
	
	function downloadReport(reportId, format, params, autoSubmit) { 
		var url = '${pageContext.request.contextPath}/module/birt/generateReport.form?showHeader=false&showFooter=false&reportId=' + reportId + '&outputFormat=' + format + 
			'&patientId=${patient.patientId}&' + params;
		
		if (autoSubmit) { url += '&autoSubmit=' + autoSubmit; }  
		
		window.open(url,'reportWindow','left=20,top=20,width=700,height=500,toolbar=0,scrollbars=1,resizable=1,location=1,status=1');
	}
	
	
	
	
</script>
	
</openmrs:hasPrivilege>