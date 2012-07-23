<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/module/birt/report.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>


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
					<form id="reportForm" method="post">
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
						
							<div <c:if test="${model.size != null}">style="width:${model.size};"</c:if>>
								<b class="boxHeader" style="font-weight:bold; text-align:right;">
									<span style="float:left;">${model.label}</span>
									<a style="color:lightyellow;" href="#" id="${model.id}EditLink">Edit</a>
								</b>
								<div class="box">
									<div class="metadataForm">
										<div class="metadataField">
											<label class="inline">Name:</label>${model.obj.name}
										</div>
										<div class="metadataField">
											<label class="inline" for="type">Query Type:</label>
											<rpt:displayLabel type="${model.obj['class'].name}"/>			
										</div>				
										<div class="metadataField">
											<label class="inline">Description:</label>
											<c:choose>
												<c:when test="${!empty model.obj.description}">
													${model.obj.description}
												</c:when>
												<c:otherwise>
													<i><spring:message code="reporting.none"/></i>							
												</c:otherwise>
											</c:choose>
										</div>
									</div>
								</div>
							</div>
													
						
							<br/>
							<b class="boxHeader">Output Designs</b>
							<div class="box">
							<form id="uploadReportForm" method="post" action="uploadReport.form" enctype="multipart/form-data">
								<c:if test="${!empty designs}">
									<table width="100%" style="margin-bottom:5px;">
										<tr>
											<th style="text-align:left; border-bottom:1px solid black;">Name</th>
											<th style="text-align:left; border-bottom:1px solid black;">Type</th>
											<th style="text-align:left; border-bottom:1px solid black;">Download</th>
											<th style="border-bottom:1px solid black;">[X]</th>
										</tr>
										<c:forEach items="${designs}" var="design" varStatus="designStatus">
											<tr>
												<td nowrap><a href="#" id="${design.uuid}DesignEditLink">${design.name}</a></td>
												<td width="100%">${design.rendererType.simpleName}</td>
												<td nowrap><a href="downloadReport.form?reportDesignId=${report.reportDefinition.id}">RPTDESIGN</a></td>
												<td nowrap align="center"><a href="#" id="${design.uuid}DesignRemoveLink">[X]</a></td>
											</tr>
										</c:forEach>
									</table>
								</c:if>
								
										<table>
											<tr>
												<td><spring:message code="birt.report.reportDesign"/></td>
												<td>
													<input type="hidden" name="reportId" value="${report.reportDefinition.id}" />
													<input type="file" name="reportFile" size="40" />
													<input type="submit" class="smallButton" value='<spring:message code="birt.reportDesign.upload" />' />
												</td>
											</tr>
										</table>				
									</form>	
								
							</div>
							<br/>							
						
						</td>
						<td valign="top" style="width:65%;">



						</td>
					</tr>
				</table>		
			</c:otherwise>			
		</c:choose>	
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>							
							
							