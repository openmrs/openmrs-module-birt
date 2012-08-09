<%@ include file="/WEB-INF/template/include.jsp" %>
<script type="text/javascript" language="javascript">
	function PopupCenter(pageURL, title,w,h) {
		var left = (screen.width/2)-(w/2);
		var top = (screen.height/2)-(h/2);
		var targetWin = window.open (pageURL, title, 'toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=no, resizable=no, copyhistory=no, width='+w+', height='+h+', top='+top+', left='+left);
	}
	

	$j(document).ready(function() {
		$j('#addUpgradePopup').dialog({
			autoOpen: false,
			modal: true,
			title: '<spring:message code="birt.newDataSetDefinition" javaScriptEscape="true"/>',
			width: '50%'
		});
		
		$j('#newDsdEditLink').click(function() {
			$j('#addUpgradePopup').dialog('open');
		});
		
		$j('#mapParametersFormCancelButton').click(function(event){			
			$j('#addUpgradePopup').dialog('close');
		});
		
		$j('#mapParametersFormSubmitButton').click(function(event){
			
			
			$j('#mapParametersForm').submit();
		});

	});
	
</script>

	<a style="font-weight:bold;" href="#" id="newDsdEditLink"><spring:message code="birt.newDataSetDefinition"/></a>
	<div id="addUpgradePopup">
			
			<form id="mapParametersForm" method="post" action="report.form">
			<input type="hidden" name="mapped" value="mappedForm"/>									
				<table>
					<tr>
						<td>Key:</td>
						<td><input type="text" name="newkey" id="newDsdKey" size="20" value="${param.newKey}"/></td>
					</tr>
					<tr>
						<td>DataSetDefinition:</td>
						<td>					
							<select name="definitionName">								
								<c:forEach items="${dataSetDefinitionNames}" var="dataSetDefinitionName" >
										<option value="${dataSetDefinitionName}">${dataSetDefinitionName}</option>									
								</c:forEach>
							</select>						
						</td>
					</tr>
				</table>				
				<hr style="color:blue;"/>
				<div style="width:100%; text-align:left;">
					<input type="button" name="send" id="mapParametersFormSubmitButton" class="ui-button ui-state-default ui-corner-all" value="Submit"/>
					<input type="button" id="mapParametersFormCancelButton" class="ui-button ui-state-default ui-corner-all" value="Cancel"/>
				</div>
			</form>											
		<br/>
	</div>
	