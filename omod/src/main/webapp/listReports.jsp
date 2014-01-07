<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<c:forEach items="${reports}" var="report">
	${report}<br/> <!-- TODO: Implement this page -->
</c:forEach>

<%@ include file="/WEB-INF/template/footer.jsp" %>							
							
							