package org.openmrs.module.birt.web.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.module.birt.BirtConfiguration;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.propertyeditor.CohortEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;
import org.springframework.web.servlet.mvc.AbstractWizardFormController;

/**
 * Report generation wizard that handles 
 * 
 * TODO Use spring web flow for more advanced wizard behavior
 * 
 * @author Justin Miranda
 */
public class GenerateReportWizardController extends AbstractWizardFormController {

	
	/* Logger */
	private static Log log = LogFactory.getLog(GenerateReportWizardController.class);
	
	/**
	 * 
	 */
	@Override
	protected ModelAndView processFinish(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		log.debug("Finishing wizard ...");
		
		return null;
	}


	/**
	 * 
	 *
	 */
    public GenerateReportWizardController() { 
    	log.debug("Setting up pages");
		setPages(new String[] {
			"/module/birt/chooseReport",
			"/module/birt/chooseCohort", 
			"/module/birt/previewData",
			"/module/birt/enterParameters",			
			"/module/birt/generateReport"
		});
    }

    /**
     * 
     */
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
    	log.debug("Initializing binders");
    	binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat(BirtConfiguration.DEFAULT_DATE_FORMAT), false));
/*		binder.registerCustomEditor(AbstractReportObject.class, new AbstractReportObjectEditor());*/
		binder.registerCustomEditor(Cohort.class, new CohortEditor());
    }

    
    /**
     * 
     */
    protected void validatePage(Object command, Errors errors, int page) {
    	log.debug("Validating page " + page);
            
        BirtReport report = (BirtReport) command;

        log.debug("Validating report object " + report);
        
    	//validator = (DefaultBeanValidator) getValidator();
        //   	validator = new CustomerSignupValidator();
        //    	errors.setNestedPath("report");
    	switch (page) { 
	    	case 0: 
				//log.debug("Validating cohort");
				// validate 
				//validator.validate(report.getSomething(), errors);
				break;
	    	case 1:     		
				//log.debug("Validating");
				//validator.validate(report.getSomething(), errors);
				break;
			case 2: 
				//log.debug("Validating");    		
				//validator.validate(report.getSomething(), errors);
				break;
			default: 
				break;
    	}
    	// errors.setNestedPath("");
    }
    
    /**
     * 
     */
	protected Object formBackingObject(HttpServletRequest request) throws ModelAndViewDefiningException {
		/*
		// Get report from session
		BirtReport report = (BirtReport) request.getSession().getAttribute("report");
		if (report == null) {
			log.debug("Could not find report in session, checking request parameter reportId");
			// Get report from database			
			String reportId = request.getParameter("reportId");
			if (reportId != null) { 
				try {
					report = reportService.getReport(Integer.valueOf(reportId));
				} catch (NumberFormatException e) { 
					log.debug("Invalid report ID : Could not find report with id " + reportId);
				}
			}				
		}			
		// Last resort, instantiate new report object for Create Report use case
		if ( report == null ) { 
			log.debug("Could not find report at all, instantiating new report bean");
			report = new BirtReport();
		}*/
		
		
		BirtReport report = new BirtReport();
		log.debug("Form backing object: " + report);
		
		return report;
	}
    

	/**
	 * 
	 */
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors, int page) {
    	log.debug("Getting reference data for page " + page);
		Map<Object, Object> data = new HashMap<Object, Object>();
/*		
		BirtReportService reportService = (BirtReportService)Context.getService(BirtReportService.class);
		//BirtReport report = (BirtReport) command;    	
		data.put("reports", reportService.getAllReports());
		data.put("cohorts", Context.getCohortService().getCohorts());
		// TO DO Mike
		//data.put("dataExports", reportService.getDataExports());    	
    	
		// Lookup cohort 
    	if ( request.getParameter("cohortId") != null ) { 
    	}
*/
    	// Lookup report
    	/*
    	if ( request.getParameter("reportId") != null ) { 
    		report = reportService.getReport(Integer.valueOf(request.getParameter("reportId")));    		
    	} else { 
    		report = new BirtReport();
    	}
    	*/
    	
    	
    	/*
    	switch (page) { 
	    	case 0: 
	        	log.debug("Getting cohort definitions");
	    		break;
	    	case 1:     		
	        	log.debug("Getting ...");
	    		break;
	    	case 2: 
	        	log.debug("Getting ...");
	    		break;
	    	default: 
	    		break;
    	}*/
    	
    	
		return data;    	
    }
        
    /**
     * Get the next form view in the wizard.
	protected int getTargetPage(HttpServletRequest request, Object command, Errors errors, int currentPage) {
    	log.debug("Getting next page...");
		return currentPage+1;
	}
     */    

}
