<ul id="menu">	
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>	
	<openmrs:hasPrivilege privilege="Manage Reports">
		<li <c:if test='<%= request.getRequestURI().contains("listReports") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/birt/listReports.list"><spring:message code="birt.list.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>	
	<openmrs:hasPrivilege privilege="Manage Global Properties">
		<li <c:if test='<%= request.getRequestURI().contains("configureProperties") %>'>class="active"</c:if>>
			<a href="configureProperties.htm"><spring:message code="birt.configure.title"/></a>
		</li>
	</openmrs:hasPrivilege>
</ul>