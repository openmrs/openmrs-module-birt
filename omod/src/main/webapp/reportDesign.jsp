<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/module/birt/report.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Report.title"/></h2>

<spring:hasBindErrors name="report">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>
<form method="post">
<h3><spring:message code="birt.reportMetadata"/></h3>
<table>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td colspan="5">
			<spring:bind path="report.name">
				${status.value}
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td colspan="5">
			<spring:bind path="report.reportDesign.displayNameKey">
				${status.value}
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td colspan="5">
			<spring:bind path="report.reportDesign.displayName">
				${status.value}
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td valign="top" colspan="5">
			<spring:bind path="report.description">
				<textarea name="description" rows="3" cols="40">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="general.name"/></td>
		<td colspan="5">
			<spring:bind path="report.reportDesign.description">
				<textarea name="description" rows="3" cols="40">${status.value}</textarea>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(report.reportId == null)}" >
		<tr>
			<td><spring:message code="general.creator"/></td>
			<td>
				<spring:bind path="report.creator">
					${report.creator.username}
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="general.dateCreated"/></td>
			<td>
				<spring:bind path="report.dateCreated">
					<openmrs:formatDate date="${report.dateCreated}" type="long"/>
				</spring:bind>
			</td>
		</tr>
		<input type="hidden" name="reportId:int" value="${report.reportId}">
	</c:if>
</table>
<br />

<h3><spring:message code="birt.report.reportDesign"/></h3>
<div>
	<h4><spring:message code="birt.report.body"/></h4>
	<ul>
		<c:forEach var="content" items="${report.reportDesign.body.contents}" varStatus="varStatus">
			<li>${content}</li>
		</c:forEach>
	</ul>

	<h4><spring:message code="birt.report.parameters"/></h4>
	<ul>
		<c:forEach var="parameter" items="${report.reportDesign.allParameters}" varStatus="varStatus">
			<li>(${parameter.dataType}) ${parameter.displayName} = [default ${parameter.defaultValue}]</li>
		</c:forEach>
	</ul>
	
	<h4><spring:message code="birt.report.datasources"/></h4>
	<ul>
		<c:forEach var="dataSource" items="${report.reportDesign.allDataSources}" varStatus="varStatus">
			<h5>${dataSource.name}</h5>
			Data Source: ${dataSource} <br/>
			Element: ${dataSource.element}<br/>
			Definition: ${dataSource.defn}<br/>
			Properties: <br/>	
		</c:forEach>
	</ul>
		
	<h4><spring:message code="birt.report.dataset"/></h4>
	<ul>
		<c:forEach var="dataSet" items="${report.reportDesign.allDataSets}" varStatus="varStatus">
			<div>
				<h5>${dataSet.name}</h5>
				Dataset: ${dataSet}<br>
				Result Sets: ${dataSet.resultSetName}<br/>
				Cached Metadata: ${dataSet.cachedMetaData}<br/>
				Cached Row Count: ${dataSet.cachedRowCount}<br/>
				Column Hints: ${dataSet.columnHints}<br/>
			</div>
		</c:forEach>
	</ul>
</div>

<input type="submit" value="<spring:message code="Report.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>