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


<b class="boxHeader">
	<c:choose>
		<c:when test="${empty report.reportDefinition.id}">
			<spring:message code="birt.create.title"/>
		</c:when>
		<c:otherwise>
			<spring:message code="birt.update.title"/>
		</c:otherwise>
	</c:choose>
</b>
<div class="box">

	<div id="reportDetails">
		<table>
			<tr>
				<td align="center">					
					<table border="0" width="100%">					
						<c:if test="${!(empty report.reportDefinition.id)}">
							<tr>		
								<th class="headerCell" align="right"><spring:message code="general.id"/></th>
								<td class="inputCell" colspan="5">
									<spring:bind path="report.reportDefinition.id">
										${status.value}
									</spring:bind>
									
								</td>
							</tr>
		
							<tr>
								<th class="" align="right" valign="top"><spring:message code="birt.report.reportDesign"/></th>
								<td class="" valign="top" colspan="5">
									<form id="uploadReportForm" method="post" action="uploadReport.form" enctype="multipart/form-data">
										<input type="hidden" name="reportId" value="${report.reportDefinition.id}" />
										<input type="file" name="reportFile" size="40" />
										<input type="submit" class="smallButton" value='<spring:message code="birt.reportDesign.upload" />' />				
									</form>	
								</td>
							</tr>
					
							<tr>
								<th class="headerCell" align="right" valign="top"></th>
								<td class="inputCell" valign="top" colspan="5">								
									<form id="downloadReportForm" name="downloadReportForm" method="post">
										<spring:bind path="report.reportDefinition.id">
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
													<input type="button" class="smallButton" name="Properties" value="View" onClick="javascript:toggleProperties('reportDesign', this);"/>
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
						<c:if test="${!(report.reportDefinition.id == null)}" >
							<spring:bind path="report.reportDefinition.id">
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

						
						<c:if test="${!(report.reportDefinition.id == null)}" >

							
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
</div>

<br/>


<%@ include file="/WEB-INF/template/footer.jsp" %>							
							
							