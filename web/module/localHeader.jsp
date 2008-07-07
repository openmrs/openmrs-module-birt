<ul id="menu">
	
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	
	<openmrs:hasPrivilege privilege="Manage Reports">

		<li <c:if test="<%= request.getRequestURI().contains("birt/report.list") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/birt/report.list">
				<spring:message code="birt.list.title"/>
			</a>
		</li>

		<li <c:if test="<%= request.getRequestURI().contains("birt/report.form") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/birt/report.form">
				<spring:message code="birt.create.title"/>
			</a>
		</li>

	<%--
		<!-- TODO Generate report wizard not working yet -->
		<li <c:if test="<%= request.getRequestURI().contains("birt/generateReportWizard.form") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/birt/generateReportWizard.form">
				<spring:message code="birt.generate.title"/>
			</a>
		</li>
	--%>

	
	</openmrs:hasPrivilege>	
</ul>