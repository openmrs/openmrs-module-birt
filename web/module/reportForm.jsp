<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/module/birt/report.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<%@ taglib prefix="wgt" uri="/WEB-INF/view/module/htmlwidgets/resources/htmlwidgets.tld" %>

<script type="text/javascript" language="javascript">
	/**
	 *  Submits a post request to the server to remove the selected parameter.
	 */
	function removeReportParameter(index) { 

		var confirmRemove = confirm("Are you sure you want to delete this parameter?");
		if (confirmRemove) { 
			if (index >= 0) { 
				var parameterIndexField = document.getElementById("parameterIndex");
				parameterIndexField.value = index;
				document.getElementById("reportForm").submit();
			}
			else { 
				alert("The selected parameter cannot be removed.");
			}
		}
		return false;
	}

	/** 
	 *  Toggles the visiblity of the dom element identified by the given identifier.
	 */
	function toggleProperties(id, button) { 
		var div = document.getElementById(id);
		if (div.style.visibility=="visible") { 
			div.style.visibility="hidden";
			document.getElementById(id).style.display="none";			
			if (button.value == "Collapse") button.value = "Expand";
		} else { 
			document.getElementById(id).style.visibility="visible";
			document.getElementById(id).style.display="block";
			if (button.value == "Expand") button.value = "Collapse";
		}
	}	
	
	function deleteMapping(keyName) {
		if (confirm("Please confirm you wish to remove " + keyName)) {
		$j.ajax({
				  type: "POST",
				  url: "report.form",				  
				  data: { removeMappedProperty: "removeDatasetMapping", type : "${report.reportDefinition['class'].name}", uuid : "${report.reportDefinition.uuid}", property : "dataSetDefinitions", currentKey : keyName  },
				  success: function() { window.location.reload(true); },
				})			
		}
	}
	
	function removeReportDesign(reportUuid, name) {
		if (confirm("Please confirm you wish to remove " + name + " report?")) {
		$j.ajax({
				  type: "POST",
				  url: "report.form",				  
				  data: { uuid : reportUuid, removeReportDesign : "removeReportDesign" },
				  success: function() { window.location.reload(true); },
				})			
		}
	} 
	
	$j(document).ready(function() {
		

		$j('#editReportDesignPopup').dialog({
			autoOpen: false,
			modal: true,
			title: '<spring:message code="birt.Report.edit" javaScriptEscape="true"/>',
			width: '50%'
		});
		
		$j('#editReportDesignPopupLink').click(function() {
			$j('#editReportDesignPopup').dialog('open');
		});
		
		$j('#${design.uuid}DesignRemoveLink').click(function(event){					
			if (confirm('Please confirm you wish to permanantly delete <b>${design.name}</b>')) {
				document.location.href='${pageContext.request.contextPath}/module/reporting/reports/deleteReportDesign.form?uuid=${design.uuid}&returnUrl=${pageUrl}';
			}
		});
		
		$j('#${design.uuid}DesignEditLink').click(function(event){					
			$j('#editReportDesignPopup').dialog('open');
		});	
		
		$j('#newReportDesignPopupLink').click(function() {
			$j('#addReportDesignPopup').dialog('open');
		});
		
		$j('#newReportDesignCancelButton').click(function(event){			
			$j('#addReportDesignPopup').dialog('close');
		});
		
		$j('#addReportDesignPopup').dialog({
			autoOpen: false,
			modal: true,
			title: '<spring:message code="birt.Report.new" javaScriptEscape="true"/>',			
			width: '50%'
		});
		
		$j('#designSubmitButton').click(function(event){
			$j('#uploadReportForm').submit();			
		});
		
		$j('#designCancelButton').click(function(event){			
			$j('#editReportDesignPopup').dialog('close');
		});
		
		$j('#designCancelButton').click(function(event){			
			$j('#addReportDesignPopup').dialog('close');
		});
		
		$j('#editBasicDetailsPopup').dialog({
			autoOpen: false,
			modal: true,
			title: '<spring:message code="birt.editBasicDetails" javaScriptEscape="true"/>',			
			width: '50%'
		});
		
		
		$j('#reportEditLink').click(function() {
			$j('#editBasicDetailsPopup').dialog('open');
		});
		
		$j('#basicDetailsSubmitButton').click(function(event){
			$j('#editDetailsReportForm').submit();			
		});
		
		$j('#basicDetailsCancelButton').click(function(event){			
			$j('#editBasicDetailsPopup').dialog('close');
		});
		
	});
	
	
</script>

<style>

#reportDetails { 
	font-size: 1.1em; 
}

th { text-align: left; } 
th.headerCell {
/*  border-top: 1px lightgray solid; border-right: 1px lightgray solid; */
}
td.inputCell {
/*	border-top: 1px lightgray solid; */
}
td.inputCell th {
		font-weight: normal;
}
.lastCell {
/*	border-bottom: 1px lightgray solid;	*/
}
.data { 
	display: none; 
	visibility: hidden; 
	positon: relative;
}
.data table { 
	border: 1px solid #CFC;
}
.data td { 
	font-family: verdana;
	font-size: 9px; 
	list-style: none;
}	
.properties { 
	display: none; 
	border: 3px solid black; 
	visibility: hidden; 
	positon: relative;
}
.properties table { 
	border: 1px solid #CFC;
}
.expandable { 
	display: none; 
}

div#dataset { 
	border: 1px dashed #ccc;
}

.properties td { 
	font-family: verdana;
	font-size: 9px; 
	list-style: none;
}
.header { 
	background-color: #CFC;
}

h4 { 
	
	width: 100%;
	text-align: center;
	letter-spacing: 1px;
	background-color: #CCC;
}
	div.metadataField { display:block; margin:0; padding:2px 2px 2px 2px; clear:both; color:#444; }
	div.metadataField label { line-height:100%; margin:0; padding:0 12px 3px 0; border:none; color:#222; font-weight:bold; }
	div.metadataField label.desc { display:block; }
	div.metadataField label.inline { display:inline; }
</style>
<div class="spacer" style="height: 15px"><!--  --></div>
<h2>
	<c:choose>
		<c:when test="${empty report.reportDefinition.id}">
			<spring:message code="birt.create.title"/>
		</c:when>
		<c:otherwise>
			<spring:message code="birt.update.title"/>
		</c:otherwise>
	</c:choose>
</h2>

<spring:hasBindErrors name="report">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>


<div class="page">
	<div id="container">
		<c:choose>		
			<c:when test="${report.reportDefinition.id == null}">
				<b class="boxHeader">
					<spring:message code="birt.create.title"/>
				</b>
				<div class="box">
					<form id="reportForm" method="post" action="report.form?uuid=${report.reportDefinition.uuid}&type=${report.reportDefinition['class'].name}">
						<div style="margin:0; padding:0; width:100%;">
								<c:if test="${!(report.reportDefinition.id == null)}" >
									<spring:bind path="report.reportDefinition.id">
										<input type="hidden" name="${status.expression}" value="${status.value}">
									</spring:bind>								
								</c:if>
								<div class="metadataField">
									<label class="desc" for="name"><spring:message code="general.name"/></label>
										<spring:bind path="report.reportDefinition.name">
											<input type="text" name="${status.expression}" tabindex="1" value="${status.value}" size="50" />
											<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>			
										</spring:bind>
								</div>
								<div class="metadataField">
									<label class="desc" for="description"><spring:message code="general.description"/></label>			
										<spring:bind path="report.reportDefinition.description">
											<textarea name="${status.expression}" tabindex="2" rows="10" cols="80">${status.value}</textarea>
											<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
										</spring:bind>
								</div>
							</div>
							<hr style="color:blue;"/>
							<div style="width:100%; text-align:left;">								
								<input tabindex="3" type="submit" id="submitButton" class="ui-button ui-state-default ui-corner-all" name="save" value="<spring:message code="general.save"/>">
								<input tabindex="4" type="submit" id="cancelButton" class="ui-button ui-state-default ui-corner-all" name="cancel" value="Cancel">
							</div>
					</form>
				</div>				
			</c:when>
			<c:otherwise>			

			
			
				<table style="font-size:small; width:100%;">
					<tr>
						<td valign="top" style="width:35%;">
						
							<div>
								<b class="boxHeader" style="font-weight:bold; text-align:right;">
									<span style="float:left;">Basic Details</span>
									<a style="color:lightyellow;" href="#" id="reportEditLink">Edit</a>
								</b>		
								
								<div class="box">
									<div class="metadataForm">
										<div class="metadataField">
											<label class="inline">Name:</label>${report.reportDefinition.name}
										</div>			
										<div class="metadataField">
											<label class="inline">Description:</label>
											<c:choose>
												<c:when test="${!empty report.reportDefinition.description}">
													${report.reportDefinition.description}
												</c:when>
												<c:otherwise>
													<i><spring:message code="reporting.none"/></i>							
												</c:otherwise>
											</c:choose>
										</div>
									</div>
									
									
				<div id="editBasicDetailsPopup">
					<form id="editDetailsReportForm" method="post" action="report.form?reportId=${report.reportDefinition.id}&uuid=${report.reportDefinition.uuid}&type=${report.reportDefinition['class'].name}">						
						<input type="hidden" name="editReportDetails" value="editreportdetails"/>
						
						<div style="margin:0; padding:0; width:100%;">
							<div class="metadataField">
								<label class="desc" for="name">Name</label>
								<input type="text" id="name" tabindex="1" name="name" value="${report.reportDefinition.name}" size="50"/>
							</div>
							<div class="metadataField">
								<label class="desc" for="description">Description</label>			
								<textarea id="description" cols="80" rows="10" tabindex="2" name="description">${report.reportDefinition.description}</textarea>
							</div>
						</div>
						<hr style="color:blue;"/>
						<div style="width:100%; text-align:left;">
							<input tabindex="3" type="button" id="basicDetailsSubmitButton" class="ui-button ui-state-default ui-corner-all" value="Submit"/>
							<input tabindex="4" type="button" id="basicDetailsCancelButton" class="ui-button ui-state-default ui-corner-all" value="Cancel"/>
						</div>
					</form>
				</div>										
									
									
								</div>
							</div>													
						
							<br/>
							<b class="boxHeader">Output Designs</b>
							<div class="box">
							
							<c:if test="${!empty design.id}">
									<table width="100%" style="margin-bottom:5px;">
										<tr>
											<th style="text-align:left; border-bottom:1px solid black;">Name</th>
											<th style="text-align:left; border-bottom:1px solid black;">Type</th>
											<th style="text-align:left; border-bottom:1px solid black;">Download</th>
											<th style="border-bottom:1px solid black;">[X]</th>
										</tr>
										
											<tr>
												<td nowrap><a href="#" id="${design.uuid}DesignEditLink">${design.name}</a></td>
												<td width="100%">${design.rendererType.simpleName}</td>
												<td nowrap><a href="downloadReportDesign.form?uuid=${design.uuid}&id=${design.id}">RPTDESIGN</a></td>
												<td nowrap align="center"><a href="javascript:removeReportDesign('${design.uuid}', '${design.name}');">[X]</a></td>												
											</tr>
										
									</table>
								</c:if>
								<br/>
						<c:choose>		
								<c:when test="${!empty design.id}">
									<a style="font-weight:bold;" href="#" id="editReportDesignPopupLink">[+] Edit</a>
									<div id="editReportDesignPopup">
								</c:when>
								<c:otherwise>
									<a style="font-weight:bold;" href="#" id="newReportDesignPopupLink">[+] Add</a>
									<div id="addReportDesignPopup">
								</c:otherwise>			
						</c:choose>	
							
							
							<form id="uploadReportForm" method="post" action="uploadReport.form" enctype="multipart/form-data">									
									
									<input type="hidden" name="resourceUuid" value="${resource.uuid}" />
									<input type="hidden" name="reportId" value="${report.reportDefinition.id}" />
									<input type="hidden" name="uuid" value="${report.reportDefinition.uuid}"/>
									<c:if test="${!empty design.id}">
										<input type="hidden" name="reportDesignUuid" value="${design.uuid}" />
									</c:if>								

								<div style="margin:0; padding:0; width:100%;">

									<div class="metadataField">
										<label class="desc" for="name">Name</label>								
										<input type="text" name="name" tabindex="1" value="${design.name}" size="50"/>											
									</div>									
									<div class="metadataField">
										<label class="desc" for="description">Description</label>
										<wgt:widget id="description" name="description" object="${design}" property="description" attributes="cols=38|rows=2"/>																			
									</div>									
									<div class="metadataField">
										<label class="desc" for="description">Report Definition</label>										
										<span style="color:navy;">${report.reportDefinition.name}</span>																				
									</div>									
									<div class="metadataField">
										<label class="desc" for="description">Renderer Type</label>																																	
										<wgt:widget id="rendererType" name="rendererType" object="${design}" property="rendererType" attributes="type=org.openmrs.module.reporting.report.renderer.ReportRenderer|simple=true"/>										
									</div>									
									<div class="metadataField">
										<label class="desc" for="description">Resource Files</label>
										<input type="hidden" name="reportId" value="${report.reportDefinition.id}" />
										<br/><c:if test="${!empty resource}"><a style="color:blue;" href="#">"${resource.name}.${resource.extension}"</a></c:if>
										<input type="file" name="reportFile" size="40" />																				
									</div>
									<br/><br/><br/>								
								</div>
								<hr style="color:blue;"/>
								<div style="width:100%; text-align:left;">
									<input type="button" id="designCancelButton" class="ui-button ui-state-default ui-corner-all" value="Cancel"/>
									<input type="button" id="designSubmitButton" class="ui-button ui-state-default ui-corner-all" value="Submit"/>
								</div>				
							</form>	
						
								
							</div>
							</div>
							<br/>							
						
						</td>
						<td valign="top" style="width:1%;"></td>
						<td valign="top" style="width:64%;">
						<b class="boxHeader">Dataset Definitions</b>						
						<div id="dataSetDefinitionBox" class="box" style="vertical-align:top;">
						<c:if test="${!empty reportt.dataSetDefinitions}">	
							<c:forEach items="${reportt.dataSetDefinitions}" var="dsd" varStatus="dsdStatus">
								
									<span>
										<span style="font-weight:bold;float:left;">${dsd.key}</span>&nbsp;&nbsp;&nbsp;
										<span>
											<a style="color:blue;" href="downloadDataSet.form?uuid=${dsd.value.parameterizable.uuid}">Download CSV</a>&nbsp;|&nbsp;
											<a style="color:blue;" href="javascript:deleteMapping('${dsd.key}');">Delete</a>											
										</span>
									</span>									
												
									<table style="font-size:smaller; color:grey; border:1px solid black;">
										<tr>
											<th colspan="6">
												${dsd.value.parameterizable.name}
												(<a href="../reporting/definition/editDefinition.form?type=${dsd.value.parameterizable['class'].name}&uuid=${dsd.value.parameterizable.uuid}">Edit this Definition</a>)
											</th>
										</tr>										
									</table>									
							
							<br/>
							
							</c:forEach>
							</c:if>
							<openmrs:portlet url="mappedPropertyForm" id="mappedPropertyForm" moduleId="birt"/>
							</div>
						</td>
					</tr>
				</table>		
			</c:otherwise>			
		</c:choose>	
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>							
							
							