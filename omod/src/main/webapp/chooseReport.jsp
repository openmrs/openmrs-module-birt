<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/module/birt/report.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="birt.runner.wizard.title"/></h2>	

<br />

<b class="boxHeader"><spring:message code="birt.runner.wizard.chooseReport.title"/></b>
<form id="" action="" method="post" class="box">

	<input type="hidden" name="_page0" value="true"/>
	<input type="hidden" name="_target1" value="true"/>

	<div>
		<h3><spring:message code="birt.runner.wizard.chooseReport.title"/></h3>		
		<spring:nestedPath path="report">	
			<spring:bind path="reportDefinition">
				<select id="${status.expression}" name="${status.expression}">
					<c:forEach var="report" items="${reports}">
						<option value="${report.reportDefinition.reportObjectId}">${report.reportDefinition.name} (${report.reportDefinition.class.name})</option>	
					</c:forEach>			
				</select>		
			</spring:bind>
		</spring:nestedPath>
			
	    <input type="submit" class="button" name="save" value="Continue"/>
	    <input type="submit" class="button" name="cancel" value="Cancel"/>	

	</div>
	
</form>