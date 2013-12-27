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
package org.openmrs.module.birt.scheduler.tasks;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BirtConfiguration;
import org.openmrs.module.birt.BirtConstants;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportException;
import org.openmrs.module.birt.BirtReportService;
import org.openmrs.module.birt.BirtReportUtil;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 *  Implementation of a task that generates and sends an email 
 *  with a PDF/HTML report attachment.
 *
 */
public class GenerateAndEmailReportTask extends AbstractTask { 
	
	// Logger 
	private Log log = LogFactory.getLog(GenerateAndEmailReportTask.class);
	
	/** 
	 * Generates the given report and sends it over email to the appropriate people.
	 */
	public void execute() {		
		log.debug("Executing send report email task ...");
		if (!Context.isAuthenticated()) 
			authenticate();
					
		try { 

			// Get access to the birt report service
			BirtReportService service = 
				(BirtReportService) Context.getService(BirtReportService.class);
			
			// Get the parameters need to identify the report 
			Integer reportId = Integer.parseInt(taskDefinition.getProperty(BirtConstants.REPORT_ID));			
					
			// Get report and populate parameters
			BirtReport report = service.getReport(reportId);	
			report.addParameters(getReportParameters());
			report.setOutputFormat(BirtConfiguration.DEFAULT_REPORT_OUTPUT_FORMAT);
			report.setEmailProperties(getEmailProperties());
	
			// Add default start date parameter
			Integer daysFromStartDate = Integer.parseInt(
					taskDefinition.getProperty(BirtConstants.REPORT_PERIOD_DAYS_FROM_START_DATE));
			report.addParameter("startDate", BirtReportUtil.addDays(new Date(), daysFromStartDate));
			
			// Add default end date parameter
			Integer daysFromEndDate = Integer.parseInt( 
					taskDefinition.getProperty(BirtConstants.REPORT_PERIOD_DAYS_FROM_END_DATE));			
			report.addParameter("endDate", BirtReportUtil.addDays(new Date(), daysFromEndDate));
			
			try { 
				Integer cohortId = Integer.parseInt(taskDefinition.getProperty(BirtConstants.COHORT_ID));		
				report.setCohort(new Cohort(cohortId));
			} 
			catch (NumberFormatException e) { 
				log.warn("Unable to parse the cohort task property " + e.getMessage());
			}
			
			// Override the output format if the task defines one 
			if (taskDefinition.getProperty(BirtConstants.REPORT_FORMAT)!=null) { 
				report.setOutputFormat(taskDefinition.getProperty(BirtConstants.REPORT_FORMAT));
			}
			
		
			service.generateAndEmailReport(report);		
		
		}			
		catch (BirtReportException e) { 
			log.error("Unable to send email due to BIRT API exception: " + e.getMessage(), e);			
		}			
		catch (Exception e) { 
			log.warn("Unable to generate report " + e.getMessage());			
		}
	}
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#shutdown()
	 */
    public void shutdown() {
    	log.debug("Shutting down send report email task ...");
    }


    
    /**
     * Get report parameters from the task definition properties.
     * @return	a map of report parameters
     */
    private Map<String,Object> getReportParameters() { 
    	Map<String,Object> parameters = new HashMap<String,Object>();
    	
    	// If the task property is a report parameter, then we add it to the 
    	// report parameter map 
    	for (String key : taskDefinition.getProperties().keySet()) { 
    		if (key.startsWith(BirtConstants.REPORT_PARAM_PREFIX)) {    			
    			// We need to remove the parameter prefix ("report.param.")
    			String paramName = key.substring(BirtConstants.REPORT_PARAM_PREFIX.length());
    			String paramValue = taskDefinition.getProperty(key);
    			Object paramObject = null;
				try { 
					if (paramValue.startsWith("dateTime:")) { 
    					paramObject = 
    						Context.getDateFormat().parse(paramValue.substring("dateTime:".length()));    				
	    			} else if (paramValue.startsWith("date:")) { 
	    				paramObject = 
	    					java.sql.Date.valueOf(paramValue.substring("date:".length()));
	    			} else if (paramValue.startsWith("integer:")) { 
	    				paramObject = Integer.parseInt(paramValue.substring("integer:".length())); 
	    			} else { 
	    				paramObject = paramValue;
	    			}					
				} catch (ParseException e) { 
					throw new BirtReportException(e); 
				}    					
    			
    			parameters.put(paramName, paramObject);
    		}
    	}    	
    	return parameters;	
    }
    

    /**
     * Get properties needed to send the email to the appropriate people.
     * @return
     */
    private Map<String,String> getEmailProperties() { 
    	Map<String,String> properties = new HashMap<String,String>();
    	
    	// If the task property is a report parameter, then we add it to the 
    	// report parameter map 
    	for (String key : taskDefinition.getProperties().keySet()) { 
    		if (key.startsWith(BirtConstants.REPORT_EMAIL_PREFIX)) {    			
    			properties.put(key, taskDefinition.getProperty(key));
    		}
    	}    	
    	return properties;
    }	
    	
    	
    
}
