package org.openmrs.module.birt.web.extension;

import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.PatientDashboardTabExt;

public class BirtPatientDashboardTabExt extends PatientDashboardTabExt {

	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	@Override
	public String getPortletUrl() {
		return "patientReports";
	}

	@Override
	public String getRequiredPrivilege() {
		return "Patient Dashboard - View Reports Section";
	}

	@Override
	public String getTabId() {
		return "patientReports";
	}

	@Override
	public String getTabName() {
		return "birt.patientDashboard.reports";
	}
	
}
