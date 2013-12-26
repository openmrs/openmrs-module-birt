package org.openmrs.module.birt.web.extension;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.AdministrationSectionExt;

public class BirtAdminListExt extends AdministrationSectionExt {

	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	public String getTitle() {
		return "birt.title";
	}
	
	public String getRequiredPrivilege() {
		return "Manage Reports";
	}
	
	public Map<String, String> getLinks() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("module/birt/configureProperties.htm", "birt.configure.title");
		map.put("module/birt/report.list", "birt.manage.title");
		return map;
	}
	
}
