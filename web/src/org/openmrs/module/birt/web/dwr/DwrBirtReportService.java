/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.birt.web.dwr;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BirtConstants;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportService;
import org.openmrs.module.birt.model.ParameterDefinition;
import org.openmrs.web.dwr.ListItem;

/**
 *
 */
public class DwrBirtReportService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Vector getReports() {
		
		Vector<Object> reportList = new Vector<Object>();

		BirtReportService reportService = 
			(BirtReportService) Context.getService(BirtReportService.class);
		
		
		List<BirtReport> reports = 
			reportService.filterReports(BirtConstants.PATIENT_REPORTS);
		
		if (reports.size() > 0) {
			reportList = new Vector<Object>(reports.size());
			for (BirtReport report : reports) {
				ListItem item = new ListItem();
				item.setId(report.getReportId());
				item.setName(report.getName());
				if (report.getParameters() != null ) { 
					StringBuffer paramBuffer = new StringBuffer();
					for (ParameterDefinition param : report.getParameters()) { 
						// We only care about non-patientId parameters and null default values
						if (!"patientId".equals(param.getName()) && param.getDefaultValue() != null) {
							paramBuffer.append(param.getName()).append("=").append(param.getDefaultValue()).append("&");
						}
					}
					item.setDescription(paramBuffer.toString());
				}
				reportList.add(item);
			}
		}		
		
		return reportList;
	}

}
