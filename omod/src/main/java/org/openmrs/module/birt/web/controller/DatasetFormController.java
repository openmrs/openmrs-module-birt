package org.openmrs.module.birt.web.controller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.service.BirtReportService;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.propertyeditor.CohortEditor;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

// Apache
// BIRT
// Openmrs Core
//import org.openmrs.api.ReportService;
//import org.openmrs.reporting.AbstractReportObject;
//import org.openmrs.reporting.Report;
//import org.openmrs.reporting.export.DataExportReportObject;
//import org.openmrs.propertyeditor.DataExportReportObjectEditor;
// Spring


/**
 * Simple form controller used to process most of the BIRT report module 
 * use cases. 
 * 
 * TODO Refactor the BIRT use cases into separate controllers.
 *
 * @author Justin Miranda
 *
 */
public class DatasetFormController extends SimpleFormController {
	
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
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
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
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		String view = null;
		boolean formRedirect = false;
		BirtReport report = (BirtReport)obj;
		
		BirtReportService reportService = (BirtReportService)Context.getService(BirtReportService.class);
		log.debug("Birt report object: " + report);
		try { 

			// Save the report definition to the database
			if (request.getParameter("save") != null) { 
				log.debug("Saving report " + report);

				// TODO Save parameters to report definition
				/*List<ParameterDefinition> params = report.getParameters();
				log.debug("Adding parameters: " + params);				
				if (!params.isEmpty()) { 					
					//report.getReportDefinition().getParameters().addAll(params);
				}*/
				Integer id = report.getReportDefinition().getId();				
				reportService.saveReport(report);
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.saveReport.success");				

				// TODO redirect create report requests to the report form 

			}
			// Delete the report definition from the database
			else if (request.getParameter("delete") != null) { 
				log.debug("Deleting report " + report);
				reportService.deleteReport(report);
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.deleteReport.success");

			}
			// Generate a quick preview of the report 
			else if (request.getParameter("preview") != null) {				
				// MS: Removed this.  TODO: figure out what to do here
			}
			else if (request.getParameter("download") != null) {
				ReportDesignResource designFile = report.getDesignFile();
				response.setContentType("text/xml; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=" + designFile.getResourceFilename());
				FileCopyUtils.copy(designFile.getContents(), response.getOutputStream());
			}			
			else { 
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.noAction");
			} 
			
			
			
		} catch (Exception e) { 
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.general.error");			
			log.error("An unexpected error occurred: ", e);
		}

		// For posts that should be redirected to the form
		if (formRedirect) { 
			return showForm(request, response, errors);
		}
		// By default, we'll just go back to the list page and display an error message
		else {
			view = getSuccessView();		
		}
		
		return new ModelAndView(new RedirectView(view));
	}

	/**
	 * Gets the reference data required for the reporting use cases implemented within this controller.
	 * 
	 * @param	request
	 * @param	command
	 * @param	errors
	 * @return	a map containing data used in the presentation layer
	 */
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) {
		Map<Object, Object> data = new HashMap<Object, Object>();
		BirtReportService reportService = (BirtReportService)Context.getService(BirtReportService.class);
		
		BirtReport report = (BirtReport) command; 
		data.put("reports", reportService.getAllReports());
    	// TO DO Mike
    	//data.put("cohorts", Context.getCohortService().getCohorts());
    	//data.put("dataExports", reportService.getDataExports());    	

    	//data.put("datasets", reportService.getDatasets());
    	return data;
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
    		
    		BirtReportService reportService = 
    			(BirtReportService)Context.getService(BirtReportService.class);
    		
    		report = reportService.getReport(Integer.valueOf(reportId));
    	}
		
		if (report == null)
			report = new BirtReport();
    	
        return report;
    }
    
}


