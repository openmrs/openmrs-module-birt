package org.openmrs.module.birt.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportService;
import org.openmrs.reporting.export.DataExportReportObject;
import org.openmrs.propertyeditor.CohortEditor;
import org.openmrs.propertyeditor.DataExportReportObjectEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;


public class DownloadReportController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Allows for Integers to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
        binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
        // TO DO Mike
		//binder.registerCustomEditor(DataExportReportObject.class, new DataExportReportObjectEditor());
		binder.registerCustomEditor(Cohort.class, new CohortEditor());
	}

	/** 
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(
				HttpServletRequest request, 
				HttpServletResponse response, 
				Object obj, 
				BindException errors) throws Exception {

		BirtReport report = (BirtReport)obj;
		
		// Get the report file
		String reportDesignPath = report.getReportDesignPath();
		File reportDesignFile = new File(reportDesignPath);

		// Write report design file to response
		InputStream fileInputStream = new FileInputStream(reportDesignFile);
		response.setContentType("text/xml; charset=utf-8");
		// TO DO
		response.setHeader("Content-Disposition", "attachment; filename=" + report.getReportDefinition().getId() + ".rptdesign");
		//response.setHeader("Content-Disposition", "attachment; filename=" + report.getReportDefinition().getReportObjectId() + ".rptdesign");
		FileCopyUtils.copy(fileInputStream, response.getOutputStream());
		
		return null;
	}

	
	/**
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

		BirtReport report = null;
		
    	String reportId = request.getParameter("reportId");
    	if (reportId != null) { 	
    		BirtReportService reportService = (BirtReportService)Context.getService(BirtReportService.class);
    		report = reportService.getReport(Integer.valueOf(reportId));
    	}
		
		if (report == null)
			report = new BirtReport();
    	
        return report;
    }
    
}