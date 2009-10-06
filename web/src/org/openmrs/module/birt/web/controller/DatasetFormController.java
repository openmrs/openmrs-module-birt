package org.openmrs.module.birt.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// Apache 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// BIRT
import org.eclipse.birt.report.model.api.ScalarParameterHandle;


// Openmrs Core
import org.openmrs.Cohort;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.ReportService;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportService;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.Report;
import org.openmrs.reporting.export.DataExportReportObject;
import org.openmrs.web.WebConstants;
import org.openmrs.propertyeditor.CohortEditor;
import org.openmrs.propertyeditor.DataExportReportObjectEditor;

// Spring
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;


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
		binder.registerCustomEditor(DataExportReportObject.class, new DataExportReportObjectEditor());
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
				Integer id = report.getReportDefinition().getReportObjectId();				
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
				log.debug("Previewing report " + report);
				reportService.previewReport(report);
				
				File file = report.getOutputFile();			
				if ( file != null) {
					try { 
						InputStream fileInputStream = new FileInputStream(file);
						String mimeType = this.getServletContext().getMimeType(file.getAbsolutePath());
						log.debug("Report preview mime type: " + mimeType);
						response.setContentType(mimeType);
						String filename = 
							report.getReportDefinition().getReportObjectId() + ".pdf";
						response.setHeader("Content-Disposition", "attachment; filename=" + filename);
						FileCopyUtils.copy(fileInputStream, response.getOutputStream());
						return null;
					} catch (Exception e) { 
						request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.previewReport.error");						
					}
				}			
			}
			/*
			// Add a parameter to the report definition 
			// NOTE: Binding to BirtReport.parameters because we needed to use a LazyList
			// implementation to make the list function properly with no values in the list.
			else if (request.getParameter("addParameter")!=null) { 
				log.debug("Adding parameters to the report definition");
				report.getReportDefinition().getParameters().clear();
				
				for(ParameterDefinition parameter : report.getParameters()) { 
					log.debug("\n\n ****** Add Parameter = " + parameter);
					report.getReportDefinition().getParameters().add(parameter);
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.addParameter.success");
				}
				reportService.saveReport(report);
				formRedirect = true;
			} 
			// Removes a parameters from the report definition
			else if (request.getParameter("removeParameter")!=null) { 
				log.debug("\n\n ****** Remove parameter " + request.getParameter("parameterIndex"));
				if (request.getParameter("parameterIndex")!=null) { 
					int parameterIndex = Integer.valueOf(request.getParameter("parameterIndex"));
					report.getReportDefinition().getParameters().remove(parameterIndex);
					reportService.saveReport(report);
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.parameterRemoved.success");
				} 
				else { 
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.parameterRemove.failure");					
				}
				formRedirect = true;
			}			
			else if (request.getParameter("removeAllParameters")!=null) { 
				log.debug("\n\n ****** Remove parameter " + request.getParameter("parameterIndex"));
				report.getReportDefinition().getParameters().clear();
				reportService.saveReport(report);
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.parameterRemoved.success");
				formRedirect = true;
			}
			*/
			else if (request.getParameter("download") != null) { 
				// Get the report file
				String reportDesignPath = report.getReportDesignPath();
				File reportDesignFile = new File(reportDesignPath);
	
				// Write report design file to response
				InputStream fileInputStream = new FileInputStream(reportDesignFile);
				response.setContentType("text/xml; charset=utf-8");
				response.setHeader("Content-Disposition", "attachment; filename=" + 
						report.getReportDefinition().getReportObjectId() + ".rptdesign");
				FileCopyUtils.copy(fileInputStream, response.getOutputStream());
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
		BirtReportService reportService = 
			(BirtReportService)Context.getService(BirtReportService.class);
		
		//BirtReport report = (BirtReport) command; 
    	data.put("reports", reportService.getReports());
    	data.put("cohorts", Context.getCohortService().getCohorts());
    	data.put("dataExports", reportService.getDataExports());
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


