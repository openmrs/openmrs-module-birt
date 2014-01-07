package org.openmrs.module.birt.web.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportRenderer;
import org.openmrs.module.birt.service.BirtReportService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class BirtReportController {

    protected final Log log = LogFactory.getLog(getClass());

	@RequestMapping("/module/birt/listReports")
	public void listReports(@RequestParam(value="nameToMatch") String nameToMatch,
							ModelMap model) throws Exception {

		List<BirtReport> reports;
		if (StringUtils.isBlank(nameToMatch)) {
			reports = getBirtReportService().getAllReports();
		}
		else {
			reports = getBirtReportService().getReportsByName(nameToMatch);
		}
		model.addAttribute("reports", reports);
	}

	@RequestMapping("/module/birt/downloadReportDesignXml")
	public void downloadReportDesignXml(@RequestParam(value="id") Integer id,
							   HttpServletResponse response) throws Exception {

		BirtReport report = getBirtReportService().getReport(id);
		String filename = report.getDesignFile().getResourceFilename();
		response.setContentType("text/xml");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		response.getOutputStream().write(report.getDesignFile().getContents());
	}

	/**
	 * This downloads all data sets within a report as a zip of CSV files that are in a format that
	 * the Birt report designer can read, for use when creating a new report design file.
	 * TODO: Handle parameters
	 */
	@RequestMapping("/module/birt/downloadDatasetsAsCsv")
	public void downloadDatasetsAsCsv(@RequestParam(value="id") Integer id,
									  HttpServletResponse response) throws Exception {

		BirtReport report = getBirtReportService().getReport(id);
		ReportDefinition rd = report.getReportDefinition();
		ReportData data = getReportDefinitionService().evaluate(rd, new EvaluationContext());
		BirtReportRenderer renderer = new BirtReportRenderer();
		renderer.render(data, "zip", report.getDesignFile().getContents(), response.getOutputStream());
	}

	protected BirtReportService getBirtReportService() {
		return Context.getService(BirtReportService.class);
	}

	protected ReportDefinitionService getReportDefinitionService() {
		return Context.getService(ReportDefinitionService.class);
	}
}


