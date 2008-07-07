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
	
	
	function addRow(name, count) { 
		alert("name " + name + ", count " + count);
		/*
		var tbody = document.getElementById('parametersTbody');
		var row = document.getElementById('parameterRow');
		var newRow = row.cloneNode(true);
		newRow.style.display = "";
		newRow.id = tbody.childNodes.length;
		tbody.appendChild(newrow);			
		*/
	}	
</script>

<script type="text/javascript">
	var timeOut = null;
	addEvent(window, 'load', initTabs);

	<openmrs:authentication>var userId = "${authenticatedUser.userId}";</openmrs:authentication>

	function initTabs() {
		var c = getTabCookie();
		if (c == null) {
			var tabs = document.getElementById("patientTabs").getElementsByTagName("a");
			if (tabs.length && tabs[0].id)
				c = tabs[0].id;
		}
		changeTab(c);
	}
	
	function setTabCookie(tabType) {
		document.cookie = "dashboardTab-" + userId + "="+escape(tabType);
	}
	
	function getTabCookie() {
		var cookies = document.cookie.match('dashboardTab-' + userId + '=(.*?)(;|$)');
		if (cookies) {
			return unescape(cookies[1]);
		}
		return null;
	}
	
	function changeTab(tabObj) {
		if (!document.getElementById || !document.createTextNode) {return;}
		if (typeof tabObj == "string")
			tabObj = document.getElementById(tabObj);
		
		if (tabObj) {
			var tabs = tabObj.parentNode.parentNode.getElementsByTagName('a');
			for (var i=0; i<tabs.length; i++) {
				if (tabs[i].className.indexOf('current') != -1) {
					manipulateClass('remove', tabs[i], 'current');
				}
				var divId = tabs[i].id.substring(0, tabs[i].id.lastIndexOf("Tab"));
				var divObj = document.getElementById(divId);
				if (divObj) {
					if (tabs[i].id == tabObj.id)
						divObj.style.display = "";
					else
						divObj.style.display = "none";
				}
			}
			addClass(tabObj, 'current');
			
			setTabCookie(tabObj.id);
		}
		return false;
    }
</script>

<style>

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
</style>

<h2><spring:message code="birt.update.title"/></h2>

<spring:hasBindErrors name="report">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>


<div id="patientTabs">
	<ul>
		<li><a id="reportDefinitionTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus">Definition</a></li>
		<li><a id="reportDesignTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus">Design</a></li>
		<li><a id="reportPreviewTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus">Preview</a></li>
		<li><a id="reportDataTab" href="#" onclick="return changeTab(this);" hidefocus="hidefocus">Data</a></li>	
	</ul>
</div>



<b class="boxHeader">
	<c:choose>
		<c:when test="${report.reportDefinition.reportObjectId}==null"><spring:message code="birt.create.title"/></c:when>
		<c:otherwise><spring:message code="birt.update.title"/></c:otherwise>
	</c:choose>
</b>
<div class="box">
	<table>
		<tr>
			<td align="center">
				
				<table border="0" width="100%">
				
				<c:if test="${!(report.reportDefinition.reportObjectId == null)}">
					<tr>		
						<th class="headerCell" align="right"><spring:message code="general.id"/></th>
						<td class="inputCell" colspan="5">
							<spring:bind path="report.reportDefinition.reportObjectId">
								${status.value}
							</spring:bind>
							
						</td>
					</tr>

					<tr>
						<th class="" align="right" valign="top"><spring:message code="birt.report.reportDesign"/></th>
						<td class="" valign="top" colspan="5">
							<form id="uploadReportForm" method="post" action="uploadReport.form" enctype="multipart/form-data">
								<input type="hidden" name="reportId" value="${report.reportDefinition.reportObjectId}" />
								<input type="file" name="reportFile" size="40" />
								<input type="submit" class="smallButton" value='<spring:message code="birt.reportDesign.upload" />' />				
							</form>	
						</td>
					</tr>
				
					<tr>
						<th class="headerCell" align="right" valign="top"></th>
						<td class="inputCell" valign="top" colspan="5">								
							<form id="downloadReportForm" name="downloadReportForm" method="post">
								<spring:bind path="report.reportDefinition.reportObjectId">
									<input type="hidden" name="${status.expression}" value="${status.value}">
								</spring:bind>			
								<c:choose>
									<c:when test="${(report.reportDesignExists)}" >
										<spring:bind path="report.reportDesignPath">
											<i>${status.value}</i>
											
											<%-- 
												<c:forTokens var="token" items="${status.value}" delims="\\">
													&nbsp;${token}&nbsp;
												</c:forTokens>
											 --%>
											<input type="button" class="smallButton" name="Properties" value="View Report" onClick="javascript:toggleProperties('reportDesign', this);"/>
											<input type="submit" class="smallButton" name="downloadReport" value="<spring:message code="birt.reportDesign.download"/>">
										</spring:bind>									
									</c:when>
									<c:otherwise> 
										(Upload a .rptdesign file using the Browse button below)
									</c:otherwise>
								</c:choose>		
							</form>		
						</td>
					</tr>
					
					
				<c:if test="${report.reportDesignExists}">
					<tr>
						<td></td>
						
						<td colspan="5">
					
							<div id="reportDesign" class="expandable">					
								<div class="boxHeader">									
									<strong>Report Design</strong>
								</div>
								<table width="100%" class="box">
							
								<%-- 
								<tr>
									<th class="headerCell" align="right" valign="top"><spring:message code="birt.reportDesign.lastUploaded"/></th>
									<td class="inputCell" valign="top" colspan="5">
										<%= new java.util.Date() %>									
									</td>
								</tr>				
								--%>
								<tr>
									<td colspan="5">									
										<strong>Body</strong>		
										<table width="100%">
											<c:if test="${empty report.reportDesign.body.contents}">
												<tr>
													<td colspan="2" align="left">
														There are no report body elements associated with this report.
													</td>
												</tr>
											</c:if>													
											<c:forEach var="handle" items="${report.reportDesign.body.contents}" varStatus="row">
												<tr style="background-color: <c:if test="${row.count %2 == 1}">#fff;</c:if>">
													<td valign="top">${handle.name} ${handle.class}</td>
													<td align="right">
														<input type="button" class="smallButton" name="Properties" value="Expand" onClick="javascript:toggleProperties('${handle}', this);"/>	
													</td>
												</tr>
												<tr>
													<td colspan="2">
														<div id="${handle}" class="properties">									
															<h4>${handle.class}</h4>
															<table width="100%" cellpadding="2">
																<tr>
																	<c:forEach var="property" items="${handle.propertyIterator}" varStatus="row">
																		<td valign="top" align="right"><strong>${property.propertyDefn.name}:</strong></td>
																		<td valign="top" align="left"><c:out value="${property.value}" escapeXml="true"/></td>
																		<c:if test="${row.index % 2 == 1}"></tr><tr></c:if>
																	</c:forEach>
																</tr>
															</table>				
														</div>
													</td>
												</tr>
		
		
											</c:forEach>
										</table>
									</td>
								</tr>
								
								<tr>
									<td colspan="5">
									
										<strong>Data Sources</strong>
									
										<table width="100%">
										
											<c:if test="${empty report.reportDesign.allDataSources}">
												<tr>
													<td colspan="2" align="left">
														There are no report data sources associated with this report.
													</td>
												</tr>
											</c:if>										
										
										
											<c:forEach var="datasource" items="${report.reportDesign.allDataSources}" varStatus="row">
												<tr style="background-color: <c:if test="${row.count %2 == 1}">#fff;</c:if>">
													<td valign="top">${datasource.name}</td>
													<td align="right">
														<input type="button" class="smallButton" name="Properties" value="Expand" onClick="toggleProperties('${datasource}', this);"/>
													</td>
												</tr>
												<tr>
													<td colspan="2">
														<div id="${datasource}" class="properties">									
															<h4>${datasource.class}</h4>
															<table width="100%" cellpadding="2">
																<tr>
																	<c:forEach var="property" items="${datasource.propertyIterator}" varStatus="row">
																		
																			<td valign="top" align="right"><strong>${property.propertyDefn.name}:</strong></td>
																			<td valign="top" align="left">
																				<c:choose>
																					<c:when test="${property.propertyDefn.name=='odaPassword'}">********</c:when>
																					<c:otherwise><c:out value="${property.value}" escapeXml="true"/></c:otherwise>
																				</c:choose>																			
																			</td>
																			<c:if test="${row.index % 2 == 1}"></tr><tr></c:if>
																	</c:forEach>
																</tr>
															</table>				
														</div>
													</td>
												</tr>
											</c:forEach>
										</table>
									</td>			
								</tr>
								<tr>
									<td class="inputCell" colspan="5">	
									
										<strong>Data Sets</strong>
										
										<table width="100%">
											
											<c:if test="${empty report.reportDesign.allDataSets}">
												<tr>
													<td colspan="2" align="left">
														There are no report data sets associated with this report.
													</td>
												</tr>
											</c:if>										
										
											<c:forEach var="dataset" items="${report.reportDesign.allDataSets}" varStatus="row">
												<tr style="background-color: <c:if test="${row.count %2 == 1}">#fff;</c:if>">
													<td valign="top">${dataset.name}</td>
													<td align="right">							
														<input type="button" class="smallButton" name="Properties" value="Expand" onClick="toggleProperties('${dataset}', this);"/>
													</td>
												</tr>
												<tr>
													<td colspan="2">
														<div id="${dataset}" class="properties">
															<h4>${dataset.class}</h4>
															<table width="100%" cellpadding="2">
																<tr>
																	<c:forEach var="property" items="${dataset.propertyIterator}" varStatus="row">
																		<td valign="top" align="right"><strong>${property.propertyDefn.name}:</strong></td>
																		<td valign="top" align="left"><c:out value="${property.value}" escapeXml="true"/></td>
																		<c:if test="${row.index % 2 == 1}"></tr><tr></c:if>
																	</c:forEach>
																</tr>
															</table>				
														</div>
													</td>
												</tr>
											</c:forEach>
										</table>
									</td>
								</tr>
								<tr>
									<td class="inputCell" colspan="5">		
									
										<strong>Parameters</strong>
									
										<table width="100%">
										
											<c:if test="${empty report.reportDesign.flattenParameters}">
												<tr>
													<td colspan="2" align="left">
														There are no report parameters associated with this report.
													</td>
												</tr>
											</c:if>										
										
											<c:forEach var="parameter" items="${report.reportDesign.flattenParameters}" varStatus="row">
												<tr style="background-color: <c:if test="${row.count %2 == 1}">#fff;</c:if>">
													<td valign="top">${parameter.name}</td>
													<td valign="top">${parameter.dataType}</td>
													<td valign="top">${parameter.defaultValue}</td>
													<td valign="top" align="right">
														<input type="button" class="smallButton" name="Properties" value="Expand" onClick="toggleProperties('${parameter}', this);"/>
													</td>
												</tr>
												<tr>
													<td colspan="4">
														<div id="${parameter}" class="properties">
															<h4>${parameter.class}</h4>
															<table width="100%" cellpadding="2">
																<tr>
																	<c:forEach var="property" items="${parameter.propertyIterator}" varStatus="row">
																		<td valign="top" align="right"><strong>${property.propertyDefn.name}:</strong></td>
																		<td valign="top" align="left"><c:out value="${property.value}" escapeXml="true"/></td>
																		<c:if test="${row.index % 2 == 1}"></tr><tr></c:if>
																	</c:forEach>
																</tr>
															</table>				
														</div>
													
													</td>
												</tr>
											</c:forEach>
										</table>
									</td>
								</tr>
								
								</table>
							</div>
						</td>
					</tr>	
					<tr>
						<th class="headerCell" align="right" valign="top">						
						<td class="inputCell" valign="top" colspan="5" style="border-bottom: 1px solid #ccc;">&nbsp;</td>
					</tr>
					<tr>
						<th class="headerCell" align="right" valign="top">						
						<td class="inputCell" valign="top" colspan="5">&nbsp;</td>
					</tr>
					
					
				</c:if><%-- report design exists --%>
			</c:if>	<%-- report definition exists --%>
			
			

			
				
				
			<form id="reportForm" method="post">			
					<c:if test="${!(report.reportDefinition.reportObjectId == null)}" >
						<spring:bind path="report.reportDefinition.reportObjectId">
							<input type="hidden" name="${status.expression}" value="${status.value}">
						</spring:bind>								
					</c:if>
					<tr>
						<th class="headerCell" align="right"><spring:message code="general.name"/></th>
						<td class="inputCell" colspan="5">
							<spring:bind path="report.reportDefinition.name">
								<input type="text" name="${status.expression}" value="${status.value}" size="52" />
								<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
							</spring:bind>
						</td>
					</tr>
					<tr>
						<th class="headerCell" align="right" valign="top"><spring:message code="general.description"/></th>
						<td class="inputCell" valign="top" colspan="5">
							<spring:bind path="report.reportDefinition.description">
								<textarea name="${status.expression}" rows="3" cols="50">${status.value}</textarea>
								<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
							</spring:bind>
						</td>
					</tr>
					
					
					<tr>		
						<th class="headerCell" align="right" valign="top">Dataset</th>
						<td class="inputCell" valign="top" colspan="5"> 
							<spring:bind path="report.reportDefinition.dataExport">
								<select id="${status.expression}" name="${status.expression}">
									<option value="">None</option>
									<c:forEach var="export" items="${dataExports}">
										<option value="${export.reportObjectId}" <c:if test="${export.reportObjectId==report.reportDefinition.dataExport.reportObjectId}">selected</c:if>>${export.name}</option>	
									</c:forEach>
								</select>
								<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
							</spring:bind>

							<c:if test="${!empty report.reportDefinition.dataExport.reportObjectId}">
								<input type="button" class="smallButton" value="View Dataset" 
									onClick="javascript:toggleProperties('dataset', this);"/>	
									
								<input type="submit" class="smallButton" name="downloadDataset" 
									value="<spring:message code="birt.reportDesign.download"/>">
							</c:if>
						</td>
					</tr>	
					
					
					<c:if test="${!empty report.reportDefinition.dataExport}">											
						<tr>
							<th></th>
							<td>
								<div id="dataset" class="data">
									<div class="boxHeader">				
										<spring:bind path="report.reportDefinition.dataExport.name">
											<strong>${status.value}</strong>
										</spring:bind>
									</div>
									<div align="center">
										<table class="nothing">
											<tr>
												<th>Name</th>
												<th>Type</th>
											</tr>
											<c:forEach var="column" items="${report.reportDefinition.dataExport.columns}">
												<tr>
													<td>${column.columnName}</td><td>${column.class.simpleName}</td>
												</tr>
											</c:forEach>
										</table>
													
										<input type="button" class="smallButton" value="Close" 
											onClick="javascript:toggleProperties('dataset', this);"/>	
									
									</div>									
								</div>
							</td>
						</tr>
					</c:if>
					
					<c:if test="${!(report.reportDefinition.reportObjectId == null)}" >
									
					
					<!--  
							<tr>		
								<td align="right" valign="top">Data Source:</td>
								<td valign="top" colspan="5"> 
										<input type="radio" name="dataSource" value="CSV"/> Flat File Data Source (CSV, TSV)<br/>
										<input type="radio" name="dataSource" value="SQL"/> SQL Data Source<br/>
										<input type="radio" name="dataSource" value="XML"/> XML	Data Source<br/>
										<input type="radio" name="dataSource" value="OLAP"/> OLAP Data Source<br/>
										<input type="radio" name="dataSource" value="WS"/> Web Service Data Source<br/>
										<input type="radio" name="dataSource" value="OPENMRS"/> OpenMRS Data Source<br/>
								</td>
							</tr>
					-->		
					<%-- 
							<tr>		
								<th class="headerCell" align="right" valign="top"><spring:message code="birt.report.datasets"/></th>
								<td class="inputCell" valign="top" colspan="5"> 
									<spring:bind path="report.reportDefinition.dataExport">
										${status.value}	
									</spring:bind>
								</td>
							</tr>
							<tr>
								<th></th>
								<td>
									<ul>
										<c:forEach var="column" items="${report.reportDefinition.dataExport.columns}">
											<li>${column.columnName}</li>
										</c:forEach>
									</ul>								
								</td>
							</tr>
							<tr>		
								<th class="headerCell" align="right" valign="top"></th>
								<td class="inputCell" valign="top" colspan="5"> 
									<spring:bind path="report.reportDefinition.dataExport">
										<select id="${status.expression}" name="${status.expression}">
											<option value=""></option>
											<c:forEach var="export" items="${dataExports}">
												<option value="${export.reportObjectId}">${export.name}</option>	
											</c:forEach>
										</select>
										<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
									</spring:bind>
									<input type="submit" class="smallButton" name="addParameter" value="Add" />
								</td>
							</tr>						
	
						--%>

						<%--
							<tr>
								<th class="headerCell" align="right" valign="top"><spring:message code="birt.report.parameters"/></th>
								<td class="inputCell" valign="top" colspan="5">
									<table class="parameters">										
										<tr class="header">
											<th>Name</th>
											<th>Data Type</th>
											<th>Default Value</th>
											<th></th>
										</tr>
										
										<tbody id="parametersTbody">
											<c:choose>
												<c:when test="${!empty report.reportDefinition.parameters}">
													<c:set var="parameterCount" scope="page" value="${fn:length(report.reportDefinition.parameters)}"/>
												</c:when>
												<c:otherwise>
													<c:set var="parameterCount" scope="page" value="0"/>										
												</c:otherwise>
											</c:choose>
											<c:if test="${!empty report.reportDefinition.parameters}">
												<tr>
													<td>											
														There are ${parameterCount} parameters in this report.
													</td>
												</tr>
												<input type="hidden" id="parameterIndex" name="parameterIndex" value=""/>
												<c:forEach var="parameter" items="${report.reportDefinition.parameters}" varStatus="varStatus">
													<c:set var="parameterCount" scope="page" value="${varStatus.index}"/>
													<c:if test="${report.reportDefinition.parameters[varStatus.index]!=null}">					
														<tr>
															<td>
																<spring:bind path="report.reportDefinition.parameters[${varStatus.index}].name">
																	<input type="text" name="${status.expression}" value="${status.value}"/>
																</spring:bind>
															</td>
															<td>
																<spring:bind path="report.reportDefinition.parameters[${varStatus.index}].dataType">
																	<spring:bind path="report.parameters[${parameterCount}].dataType">													
																		<select name="${status.expression}">
																			<option value=""></option>
																			<option value="Boolean">Boolean</option>
																			<option value="Date">Date</option>
																			<option value="DateTime">DateTime</option>
																			<option value="Decimal">Decimal</option>
																			<option value="Float">Float</option>
																			<option value="Integer">Integer</option>
																			<option value="String">String</option>								
																			<option value="Time">Time</option>								
																		</select>
																	</spring:bind>
																</spring:bind>
															</td>
															<td>
																<spring:bind path="report.reportDefinition.parameters[${varStatus.index}].defaultValue">
																	<input type="text" name="${status.expression}" value="${status.value}"/>
																</spring:bind>
															</td>
															<td>
																<input type="submit" class="smallButton" name="removeParameter" onClick="removeReportParameter(${varStatus.index});" value='Remove' />				
															</td>
														</tr>
													</c:if>
												</c:forEach>												
											</c:if>
											
										<tr id="parameterRow">
											<td>
												<spring:bind path="report.parameters[${parameterCount}].name">
													<input type="text" name="${status.expression}" value="${status.value}"/>
												</spring:bind>
											</td>
											<td valign="top">
												<spring:bind path="report.parameters[${parameterCount}].dataType">													
													<select name="${status.expression}">
														<option value=""></option>
														<option value="Boolean">Boolean</option>
														<option value="Date">Date</option>
														<option value="DateTime">DateTime</option>
														<option value="Decimal">Decimal</option>
														<option value="Float">Float</option>
														<option value="Integer">Integer</option>
														<option value="String">String</option>								
														<option value="Time">Time</option>								
													</select>
												</spring:bind>
											</td>													
											<td>
												<spring:bind path="report.parameters[${parameterCount}].defaultValue">
													<input type="text" name="${status.expression}" value="${status.value}"/>
												</spring:bind>
											</td>													
											<td>
												<input type="button" class="smallButton" value='Add' onClick="addRow('report.parameters', ${parameterCount}});" />
											</td>
										</tr>										
									</table>
										
										<!--  
										<c:if test="${!empty report.parameters}">
											<spring:bind path="report.reportDefinition.parameters">
												<c:forEach var="param" items="${report.parameters}" varStatus="varStatus">
													<spring:bind path="report.reportDefinition.parameters[${varStatus.index}].reportObjectId">							
														<input type="hidden" name="${status.expression}" id="${status.expression}" value="${status.value}" />
													</spring:bind>
													<spring:bind path="reportparameters[${varStatus.index}].label">
														<input type="text" name="${status.expression}" id="${status.expression}" value="${status.value}" />
													</spring:bind>
												</c:forEach>
												<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
											</spring:bind>
										</c:if>
										-->
								<!-- 							
								<table id="parameters" cellspacing="2">
									<tbody id="parametersTbody">
										<tr id="parameterRow" style="display: none;">				
											<td valign="top">
												<input type="text" size="30" name="name" onmouseup="parameterChanged(this)" />
											</td>
											<td valign="top">
												<select name="parameterType" onclick="parameterChanged(this)">
													<option value=""></option>
													<option value="Boolean">Boolean</option>
													<option value="Date">Date</option>
													<option value="DateTime">DateTime</option>
													<option value="Decimal">Decimal</option>
													<option value="Float">Float</option>
													<option value="Integer">Integer</option>
													<option value="String">String</option>								
													<option value="Time">Time</option>								
												</select>
											</td>
											<td valign="top">
												<input type="text" size="30" name="defaultValue" onmouseup="parameterChanged(this)" />
											</td>
											<td valign="middle" align="center">
												<input type="checkbox" name="required" value="" onclick="parameterChanged(this)" />
											</td>
											<td valign="middle" align="center">
												<input type="button" name="closeButton" onClick="return removeRow(this);" class="closeButton" value='<spring:message code="general.remove"/>'/>
											</td>
										</tr>
									</tbody>
								</table>
								-->
								
								
							</td>														
						</tr>
--%>						
						
					</c:if>					
					<tr height="50">
						<td></td>
						<td class="inputCell" colspan="1" align="left">				
							<input type="submit" class="smallButton" name="save" value="<spring:message code="general.save"/>">
							<input type="submit" class="smallButton" name="delete" value="Delete">
							<input type="submit" class="smallButton" name="cancel" value="Done">
						</td>
					</tr>

	</form>

</table>	
				

				

				
				
			</td>
		</tr>
	</table>		
</div>
<br/>



<%@ include file="/WEB-INF/template/footer.jsp" %>							
							
							