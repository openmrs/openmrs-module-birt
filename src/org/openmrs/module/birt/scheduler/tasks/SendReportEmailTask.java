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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.report.engine.api.EngineException;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BirtConstants;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportException;
import org.openmrs.module.birt.BirtReportService;
import org.openmrs.notification.Message;
import org.openmrs.notification.MessageException;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

/**
 *  Implementation of the stateful task that sends an email.
 *
 */
public class SendReportEmailTask extends AbstractTask { 
	
	// Logger 
	private Log log = LogFactory.getLog( SendReportEmailTask.class );

	private final static String REPORT_ID = "reportId";	
	private final static String COHORT_ID = "cohortId";	
	private final static String TODAY = "today";
	private final static String OUTPUT_FORMAT = "outputFormat";

	/** 
	 *  Process the next form entry in the database and then remove the form entry from the database.
	 *
	 *
	 */
	public void execute() {		
		log.info("****************************** SEND REPORT EMAIL TASK:  Executing task ...");
		if (!Context.isAuthenticated()) { 
			authenticate();
		}
		Integer reportId = Integer.parseInt(taskDefinition.getProperty(REPORT_ID));
		
		
		BirtReportService service = (BirtReportService) Context.getService(BirtReportService.class);
	
		BirtReport report = service.getReport(reportId);	

		// Add task properties as parameters to report
		report.addParameters(taskDefinition.getProperties());
		
		// Add today's date as a parameter
		report.addParameter(TODAY, new Date());
		
		// Set the output format based on the given property 
		if (taskDefinition.getProperty(OUTPUT_FORMAT)!=null) { 
			report.setOutputFormat(taskDefinition.getProperty(OUTPUT_FORMAT));
		} else { 
			report.setOutputFormat(BirtConstants.DEFAULT_REPORT_OUTPUT_FORMAT);
		}

		try { 
			// Set the desired cohort [optional] 
			Integer cohortId = Integer.parseInt(taskDefinition.getProperty(COHORT_ID));		
			report.setCohort(new Cohort(cohortId));
		} 
		catch (NumberFormatException e) { 
			// Not important and we'll most likely see a lot of these 
			log.debug("Unable to parse the cohortId task property " + e.getMessage());
		}
		
		try { 
			service.generateReport(report);
		} 
		catch (BirtReportException e) { 
			log.error("Unable to send email due to BIRT API exception: " + e.getMessage(), e);			
		}
			
		try { 
				
			// TODO Need to add File attachment support
			Message message = Context.getMessageService().createMessage(
					taskDefinition.getProperty("recipients"), 
					taskDefinition.getProperty("sender"), 
					taskDefinition.getProperty("subject"), 
					taskDefinition.getProperty("message"), 
					taskDefinition.getProperty("attachment"), 
					taskDefinition.getProperty("attachmentContentType"), 
					taskDefinition.getProperty("attachmentFileName")
				);
			
			Context.getMessageService().sendMessage(message);
		} 
		catch (MessageException e) { 
			log.warn("Unable to send email due to mail API exception: " + e.getMessage());
		}
		
	}
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#shutdown()
	 */
    public void shutdown() {
    	log.info("****************************** SEND REPORT EMAIL TASK:  Shutting down task ...");
    }
	
	
}
