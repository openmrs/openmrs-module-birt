<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/module/birt/report.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>


<script type="text/javascript" language="javascript">
	/**
	 *  Submits a post request to the server to remove the selected parameter.
	 */
	function removeReportParameter(index) { 

		var confirmRemove = confirm('<spring:message code="birt.removeParameter"/>');
		if (confirmRemove) { 
			if (index >= 0) { 
				var parameterIndexField = document.getElementById("parameterIndex");
				parameterIndexField.value = index;
				document.getElementById("reportForm").submit();
			}
			else { 
				alert('<spring:message code="birt.removeSelectedParameter.error"/>');
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
			<form id="reportForm" method="post" enctype="multipart/form-data">
				<table>
					<tr>
						<td><spring:message code="general.name"/>*</td>
						<td>
							<spring:bind path="report.reportDefinition.name">
								<input type="text" name="${status.expression}" value="${status.value}" size="52" />
								<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>			
							</spring:bind>
						</td>
					</tr>
					<tr>
						<td valign="top"><spring:message code="general.description"/></td>
						<td>
							<spring:bind path="report.reportDefinition.description">
								<textarea name="${status.expression}" rows="3" cols="50">${status.value}</textarea>
								<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
							</spring:bind>
						</td>
					</tr>
					<tr>
						<td>'<spring:message code="birt.reportName"/>'</td>
						<td>
        					<form method="post" action="upload.form" enctype="multipart/form-data">
            					<input type="file" name="file"/>
            					<input type="submit"/>
        					</form>
						</td>
					</tr>
					<tr>
						<td></td>
						<td><input type="submit" class="smallButton" name="save" value="<spring:message code="general.save"/>"><input type="submit" value="Preview"/><input type="submit" value="Cancel"/></td>
					</tr>
				</table>
			</form>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>							
							
