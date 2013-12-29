package org.openmrs.module.birt.web.controller.portlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.service.BirtReportService;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappedPropertyPortletController extends SimpleFormController {
	
	/**
	 * Logger for this class and subclasses
	 */
	protected static final Log log = LogFactory.getLog(MappedPropertyPortletController.class);
	
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		return null;
	}
	
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		BirtReportService reportService = Context.getService(BirtReportService.class);
    	List<DataSetDefinition> dataSetDefinitionList = new ArrayList<DataSetDefinition>();
    	dataSetDefinitionList.addAll(DefinitionContext.getDataSetDefinitionService().getAllDefinitions(true));
    	map.put("reports", reportService.getAllReports());
    	map.put("dataSetDefinitions", dataSetDefinitionList);
		return map;
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		BirtReport report = null;
    	String reportId = request.getParameter("reportId");
    	if (reportId != null) {
    		BirtReportService reportService = Context.getService(BirtReportService.class);
    		report = reportService.getReport(Integer.valueOf(reportId));
    	}
		if (report == null) {
			report = new BirtReport();
		}
        return report;
	}
}
