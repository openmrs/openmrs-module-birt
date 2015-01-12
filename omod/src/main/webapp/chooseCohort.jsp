<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/module/birt/report.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="birt.runner.wizard.title"/></h2>	

<br />
	
<b class="boxHeader"><spring:message code="birt.runner.wizard.chooseCohort.title"/></b>
<form id="" action="" method="post" class="box">
	<input type="hidden" name="_page1" value="true"/>
	<input type="hidden" name="_target2" value="true"/>

	${report}	
	
	<h3><spring:message code="birt.runner.wizard.chooseCohort.title"/></h3>
		<spring:bind path="report.cohort">
			<select id="${status.expression}" name="${status.expression}">
				<c:forEach var="cohort" items="${cohorts}">
					<option value="${cohort.cohortId}">${cohort.name} (${cohort.class.name}) </option>	
				</c:forEach>
			</select>	
		</spring:bind>
	
	
    <input type="submit" class="button" name="save" value="Continue"/>
    <input type="submit" class="button" name="cancel" value="Cancel"/>	
	
</form>