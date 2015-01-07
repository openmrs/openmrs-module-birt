<%@ page import="org.openmrs.module.birt.BirtConfiguration" %>
<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/module/birt/report.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<!--<h2><spring:message code="Report.title"/></h2>-->
<h2><spring:message code="birt.generate.title"/></h2>

<spring:hasBindErrors name="report">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${error} -->
		</c:forEach>
	</div>
</spring:hasBindErrors>

<form method="post">

	<input type="hidden" name="autoSubmit" value="true">

	<b class="boxHeader"><spring:message code="birt.generate.title"/></b>	
	<table class="box" cellpadding="0" cellspacing="0">
		<tr>
			<td align="left">
				<table border="0" cellspacing="5" cellpadding="5">
					<tr>
						<th class="headerCell" align="left" valign="top"><spring:message code="birt.generate.report"/></th>
						<td class="inputCell" valign="top" colspan="5">							
							<spring:bind path="report.reportDefinition.name">
								${status.value} &nbsp;
								<a href="report.form?reportId=${report.reportDefinition.reportObjectId}">edit report</a>
							</spring:bind>
						</td>
					</tr>
					<tr>
						<th class="headerCell" align="left" valign="top"><spring:message code="birt.generate.format"/></th>
						<td class="inputCell" valign="top" colspan="5">
							<spring:bind path="report.outputFormat">
								<select name="${status.expression}">
									<option value="pdf"><spring:message code="birt.acrobatReader"/></option>
									<option value="html"><spring:message code="birt.webPage"/></option>
									<option value="xls"><spring:message code="birt.microsoftExcel"/></option>
									<option value="doc"><spring:message code="birt.microsoftWord"/></option>
									<option value="html"><spring:message code="birt.show.errors"/></option>
								</select>
							</spring:bind>
						</td>
					</tr>
					<c:if test="${!empty report.reportDefinition.dataExport}">
						<tr>
							<th class="headerCell" align="left" valign="top"><spring:message code="birt.generate.filter"/></th>
							<td class="inputCell" valign="top" colspan="5"></td>
						</tr>
						<tr>
							<td class="inputCell" align="left" valign="top">
								<spring:bind path="report.reportDefinition.dataExport">${status.value.name}</spring:bind>
							</th>
							<td class="inputCell" valign="top" colspan="5">									
								<select name="cohortKey">
									<option value="0">All patients</option>
									<c:forEach var="cohort" items="${cohorts}">
										<option value="${cohort.key}">${cohort.name}</option>
									</c:forEach>					
								</select>
							</td>
						</tr>
					</c:if>			
					<c:if test="${!empty report.parameters}">	
						<tr>
							<th class="headerCell" align="left" valign="top"><spring:message code="birt.generate.parameters"/></th>
							<c:if test="${empty report.parameters}">
								<td class="inputCell" valign="top" colspan="5">None</td>
							</c:if>				
						</tr>
						<c:forEach var="parameter" items="${report.parameters}" varStatus="row">
							<tr>
								<td class="headerCell" align="right" valign="top">
									<c:choose>
										<c:when test="${!empty parameter.promptText}">${parameter.promptText}</c:when>
										<c:otherwise>${parameter.name}</c:otherwise>
									</c:choose>	
									
									<c:set var="parameterValue" value="${param[parameter.name]}"/>
									<c:if test="${empty parameterValue}">
										<c:set var="parameterValue" value="${parameter.defaultValue}"/>										
									</c:if>
								</td>
								<td class="inputCell" valign="top" align="left" colspan="5">								
									<c:choose>
										<%-- Date time parameter --%>
										<c:when test="${parameter.dataType=='dateTime'}">								
											<input type="text" name="${parameter.name}" size="30" 
											   value="${parameterValue}" />
											(Format: <%= BirtConfiguration.DEFAULT_DATETIME_FORMAT %>)
										</c:when>
										<%-- Date parameter --%>
										<c:when test="${parameter.dataType=='date'}">
											<input type="text" name="${parameter.name}" size="30" 
											   value="${parameterValue}" />
											(Format: <%= BirtConfiguration.DEFAULT_DATE_FORMAT %>)
										</c:when>
										<%-- All other parameters displayed as TEXT BOX or SELECT LIST --%>
										<c:otherwise>			
											<c:choose>
												<%-- SELECT LIST --%> 
												<%-- TODO Mark default value as 'selected' --%>
												<c:when test="${parameter.controlType=='select'}">											
													<select name="${parameter.name}" size="5" multiple="true">
														<c:forEach var="option" items="${parameter.selectionList}" varStatus="row">
															<c:set var="selectedOption">
																<c:if test="${option.key==parameterValue}">selected</c:if>
															</c:set>
															<option value="${option.key}" ${selectedOption}>${option.value}</option>															
														</c:forEach>
													</select>
												</c:when>
												<%-- TEXT BOX --%>
												<c:otherwise>												
													<input type="text" name="${parameter.name}" value="${parameterValue}" size="30"/>												
												</c:otherwise>
											</c:choose>
										</c:otherwise>
									</c:choose>	
								</td>
							</tr>
															
						</c:forEach>						
					</c:if>											
					<tr>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td align="right">
							<input type="submit" name="generate" value="Generate"/>
						</td>
					</tr>		
				</table>
			</td>
		</tr>						
	</table>
</form>
<br/>


<%@ include file="/WEB-INF/template/footer.jsp" %>
