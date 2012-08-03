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
			title: '<spring:message code="Module.addOrUpgrade" javaScriptEscape="true"/>',
			width: '50%'
		});
		
		$j('#newDsdEditLink').click(function() {
			$j('#addUpgradePopup').dialog('open');
		});

	});
	
</script>

<c:forEach items="${report.reportDefinition.dataSetDefinitions}" var="dsd" varStatus="dsdStatus">
	<div style="display:none; width:100%" id="dsdView${dsdStatus.index}">
		<table style="font-size:smaller; color:grey; border:1px solid black;">
			<tr>
				<th colspan="7">
					${dsd.value.parameterizable.name}
					(<a href="../definition/editDefinition.form?type=${dsd.value.parameterizable['class'].name}&uuid=${dsd.value.parameterizable.uuid}">Edit this Definition</a>)
				</th>
			</tr>
		</table>
	</div>
</c:forEach>

<div class="box" style="vertical-align:top;">
	<a style="font-weight:bold;" href="#" id="newDsdEditLink">[+] New Dataset Definition</a>
	<div id="addUpgradePopup">
			
			<form method="post" action="" onSubmit="return validateForm()">				
				<table>
					<tr>
						<td>Key:</td>
						<td>
							<input type="text" name="addkey" id="newDsdKey" size="40"/>
						</td>
					</tr>
					<tr>
						<td>DataSetDefinition:</td>
						<td>					
							<select name="encounterType">								
								<c:forEach items="${definitions}" var="entry" varStatus="entryStatus">
									<c:forEach items="${entry.value}" var="definition" varStatus="definitionStatus">
										<option value="${definition.name}">${definition.name}</option>
									</c:forEach>
								</c:forEach>
							</select>						
						</td>
					</tr>
					<tr>
						<td></td>
						<td>
							<input type="submit" value="Submit"/><input type="reset" value="Cancel"/>
						</td>
					</tr>
				</table>
				<input type="hidden" name="personType" value=""/>
				<input type="hidden" name="viewType" value=""/>
			</form>											
		<br/>
	</div>
</div>	