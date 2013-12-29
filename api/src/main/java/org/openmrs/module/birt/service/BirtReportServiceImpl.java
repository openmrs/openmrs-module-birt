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
package org.openmrs.module.birt.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.service.ReportService;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Birt Report service
 */
public class BirtReportServiceImpl implements BirtReportService {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Public default constructor
	 */
	public BirtReportServiceImpl() {}

	/**
	 * @see org.openmrs.module.birt.service.BirtReportService#getReport(Integer)
	 */
	@Override
	public BirtReport getReport(Integer reportId) {
		ReportDesign rd = getReportService().getReportDesign(reportId);
		return new BirtReport(rd);
	}

	/**
	 * @see org.openmrs.module.birt.service.BirtReportService#getAllReports()
	 */
	@Override
	public List<BirtReport> getAllReports() {
		return getReportsByName(null);
	}

	@Override
	public List<BirtReport> getReportsByName(String nameToMatch) {
		List<BirtReport> ret = new ArrayList<BirtReport>();
		for (ReportDesign rd : getReportService().getAllReportDesigns(false)) {
			boolean add = nameToMatch == null;
			if (!add) {
				String s = (ObjectUtil.nvlStr(rd.getName(), "") + ObjectUtil.nvlStr(rd.getReportDefinition().getName(), "")).toLowerCase();
				add = s.contains(nameToMatch.toLowerCase());
			}
			if (add) {
				ret.add(new BirtReport(rd));
			}
		}
		return ret;
	}

	@Override
	public void saveReport(BirtReport report) {

	}

	@Override
	public void deleteReport(BirtReport report) {

	}

	@Override
	public void previewReport(BirtReport report) {

	}

	@Override
	public void generateReport(BirtReport report) {

	}

	private ReportService getReportService() {
		return Context.getService(ReportService.class);
	}
}
