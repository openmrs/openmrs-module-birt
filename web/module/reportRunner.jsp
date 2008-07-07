<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/module/birt/report.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Report.title"/></h2>

<b class="boxHeader"><spring:message code="birt.generate.title"/></b>
<div class="box">
	<form id="" method="post" action="">
		<div>
			Report: 
			<select name="reportId">
				<option value=""></option>
				<c:forEach var="report" items="${reports}">
					<option value="${report.reportDefinition.reportObjectId}">${report.reportDefinition.name}</option>
				</c:forEach>
			</select>
		</div>
		
		<div>
			Cohort: 
			<select name="cohortId">
				<option value=""></option>
				<c:forEach var="cohort" items="${cohorts}">
					<option value="${cohort.cohortId}">${cohort.name}</option>
				</c:forEach>
			</select>
			
			<%--
			<span>OR</span> 
			Data export: 
			<select name="dataExport">
				<option value=""></option>
				<c:forEach var="export" items="${dataExports}">
					<option value="${export}">${export}</option>
				</c:forEach>
			</select>
			--%>
			
		</div>
		<input type="submit" name="generate" value="Generate"/>
	</form>
</div>
	
<%@ include file="/WEB-INF/template/footer.jsp" %>