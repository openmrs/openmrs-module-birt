package org.openmrs.module.birt.web.controller;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.core.exception.BirtException;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortDefinition;
import org.openmrs.module.birt.BirtConstants;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportException;
import org.openmrs.module.birt.BirtReportService;
import org.openmrs.module.birt.BirtReportUtil;
import org.openmrs.module.birt.model.ParameterDefinition;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;


/**
 * 
 * @author Justin Miranda
 *
 */
public class GenerateReportController extends SimpleFormController {

	/* Logger */
	private static Log log = LogFactory.getLog(GenerateReportController.class);
	
	private static String BIRT_ERRORS = "birtErrors";
	private static String COHORTS_KEY = "cohorts";
	private static String COHORT_KEY = "cohortKey";	
	private static String REPORT_ID_KEY = "reportId";
	private static String AUTO_SUBMIT_KEY = "autoSubmit";
	
	/**
	 * Allows for Integers to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
    	log.debug("initBinder");
		try {
			log.debug("super.initBinder");
			super.initBinder(request, binder);

			log.debug("bind customer integer editor");
			binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));	        
	        
	        // Register default date parameter
			log.debug("bind customer date editor");
	        SimpleDateFormat format = new SimpleDateFormat(BirtConstants.DEFAULT_DATE_FORMAT);
	        format.setLenient(true);
	        binder.registerCustomEditor(Date.class, new CustomDateEditor(format, true));        
	        
	        
		} catch (Exception e) { 
			log.error("Error occurred during binder initialization", e);
			throw e;
		}
        
	}	
	
	/** 
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException errors) throws Exception {
    	log.debug("onSubmit");
		try {   
			
			// We only want to generate the report IFF there's the form has been submitted
			// which happens on a POST from the generateReport page or a GET from the 
			// patient dashboard
			Boolean autoSubmit = ServletRequestUtils.getBooleanParameter(request, AUTO_SUBMIT_KEY, false);
			
			// If the request asks to explicitly forwarded to the form
			if (!autoSubmit) { 
    			return showForm(request, response, errors);
			}
		
			// Get command object
			BirtReport report = (BirtReport) object;
			
			log.debug("Report = " + report + ", hashcode " + report.hashCode());
			
			// Get BIRT report service
			BirtReportService reportService = (BirtReportService)Context.getService(BirtReportService.class);
			
    		// Find the report associated with the given ID
    		Integer reportId = ServletRequestUtils.getIntParameter(request, REPORT_ID_KEY, 0);
    		report = reportService.getReport(reportId);

    		if (report == null ) {
    			errors.reject("The selected report does not exist.");
    			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Report.not.exists");    			
    		}
    		
    	
    		
    		// Make sure there's a cohort specified, otherwise we just use default cohort
    		String cohortKey = ServletRequestUtils.getStringParameter(request, COHORT_KEY, "0");

    		
    		// Evaluate the selected cohort or use the default cohort (all patients) 
    		Cohort cohort = new Cohort();    		
    		if (!BirtConstants.INVALID_COHORT_KEY.equals(cohortKey)) {     			
    			cohort = Context.getCohortService().evaluate(Context.getCohortService().getCohortDefinition(cohortKey), null);
    		} else {     		
    			cohort = Context.getPatientSetService().getAllPatients();    			
    		}
			report.setCohort(cohort);
    					
    		// Set the output format for the selected report
    		report.setOutputFormat(request.getParameter(BirtConstants.PARAM_OUTPUT_FORMAT));
    		
    		// Preparing report parameters
    		log.debug(" ***** Checking parameters: " + report.getParameters());

    		
    		// FIXME Need to move parameter handling into a utility class 
    		if (report.getParameters() != null ) { 
	    	
    			for (ParameterDefinition parameter : report.getParameters()) { 	    			
	    			
	    			// Get object from the request
    				// FIXME #1984: Need to be able to support multiple values 
    				// Error: The type of parameter 'paramName' is expected as Object [], not java.lang.String
    				// --> handled in BirtReportServiceImpl (decide if that's the proper place to handle this exception)
	    			//String value = request.getParameter(parameter.getName());
	    			String [] values = request.getParameterValues(parameter.getName());
	    			
	    			log.debug(" ***** Parameter " + parameter.getName() + " = " + values);
	    			parameter.setValues(values);
	    			
	    			// If the user provided values we try to parse them 
	    			if (values != null && values.length > 0) { 
	    				try { 
		    				// Convert value to appropriate 
		    				Object [] objectValues = BirtReportUtil.parseParameterValues(parameter.getDataType(), values);
		    				log.debug(" ***** Using user-specified value " + values);	
		    				log.debug(" ***** Setting user-specified value " + objectValues);	
		    				parameter.setValues(objectValues);
	    				} 
	    				catch (ParseException e) { 
	    					log.error(" ***** Parse exception: " + e.getMessage());
	    					errors.reject("Unable to parse parameter " + parameter.getName() + ": " + e.getMessage());
	    					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "birt.generateReport.error");
	    				}
	    			} 
	    			// If the user didn't provide a value, use the default
	    			else { 
	    				log.debug(" ***** Using default value " + parameter.getDefaultValue());
	    				parameter.setValue( parameter.getDefaultValue() );
	    			}	    			
	    		}
    		}
    	
    		// If we find validation errors, we need send error back to the browser
    		if (errors.hasErrors()) { 
    			log.error("The generate report form submission has errors " + errors.getAllErrors());
    			return showForm(request, response, errors); 
    		}
    		
    		// Generate report output
    		log.debug("Report = " + report + ", hashcode " + report.hashCode());
			reportService.generateReport(report);
			
			// Handle any errors that were encountered on the BIRT side
			if (report.hasErrors()) {  
				errors.reject("Unable to generate report due to the following error(s)");	
				Iterator iterator = report.getErrors().iterator();
				while (iterator.hasNext()) {
					Exception e = (Exception) iterator.next();					
					log.error("Error: " + e.getMessage());
					errors.reject(e.getMessage());						
				}
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.generateReport.error");

				// We actually want to continue through to display the report in some cases 
				// (i.e. when outputFormat=HTML) you will get more information about an error.				
				if (!BirtConstants.HTML_FORMAT.equalsIgnoreCase(report.getOutputFormat())) { 
					return showForm(request, response, errors);					
				}
			}			
			
			// Set headers and content type of report file
			response.setContentType(getServletContext().getMimeType(report.getOutputFile().getAbsolutePath()));
			response.setHeader("Content-Disposition", "attachment; filename=\"" + report.getOutputFile().getName() + "\"");
				
			// Get a reference to the report output file to be copied to the response
			InputStream fileInputStream = new FileInputStream(report.getOutputFile());
			
			// Copy report output to response
			FileCopyUtils.copy(fileInputStream, response.getOutputStream());
			
			// Removed meaningless message (doesn't show up on the returned page because we hijack the response
			// in order to write the report output.  Therefore, the message isn't displayed until you move away
			// from the current page.  
			// 
			// TODO If we could refresh the report generation form page then we would probably get the 
			// message to display correctly.  Perhaps an immediate redirect to the report listing page?
			//
			//request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.generateReport.success");
						
		} catch (Exception e) { 
			log.error("Unable to generate report due to the following error(s): " + e.getMessage(), e);
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "birt.generateReport.error");
			errors.reject(e.getMessage());
			return showForm(request, response, errors);
		}
		
		return new ModelAndView("report.list");
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
    	log.debug("referenceData");
		Map<Object, Object> data = new HashMap<Object, Object>();
/*		BirtReportService reportService = 
			(BirtReportService)Context.getService(BirtReportService.class);
		
<<<<<<< .mine
		// TO DO Mike
    	//data.put(COHORTS_KEY, reportService.getCohortDefinitions());
=======
    	data.put(COHORTS_KEY, reportService.getCohortDefinitions());*/
    	
    	return data;
    }
	
	/**
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
    	log.debug("formBackingObject");

		BirtReport report = null;
		
    	String reportId = request.getParameter(REPORT_ID_KEY);
    	if (reportId != null) { 	    		
    		
    		BirtReportService reportService = 
    			(BirtReportService)Context.getService(BirtReportService.class);
    		
    		report = reportService.getReport(Integer.valueOf(reportId));
    	}
		
		if (report == null)
			report = new BirtReport();
    	
        return report;
    }
	
    
    /*
     * 
     */
    @Override
    protected boolean isFormSubmission(HttpServletRequest request) { 
    	log.debug("isFormSubmission");
    	return true;
    }
}
