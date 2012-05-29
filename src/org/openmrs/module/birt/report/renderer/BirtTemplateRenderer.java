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
package org.openmrs.module.birt.report.renderer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportException;
import org.openmrs.module.birt.BirtReportService;
import org.openmrs.module.birt.impl.BirtConfiguration;
import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingException;
import org.openmrs.module.reporting.report.renderer.ReportRenderer;
import org.openmrs.module.reporting.report.renderer.ReportTemplateRenderer;
import org.openmrs.module.reporting.report.service.ReportService;
import org.springframework.util.FileCopyUtils;

/**
 * Report Renderer implementation that supports rendering to a BIRT template
 */
@Handler // problems addding...
@Localized("reporting.BirtTemplateRenderer")
public class BirtTemplateRenderer extends ReportTemplateRenderer {

	private Log log = LogFactory.getLog(this.getClass());

	public BirtTemplateRenderer() {
		super();
	}

	/**
	 * @see org.openmrs.report.ReportRenderer#getRenderedContentType(org.openmrs.report.ReportDefinition, java.lang.String)
	 */
	public String getRenderedContentType(ReportDefinition schema, String argument) {
		return "application/pdf";
	}

	/**
	 * @see org.openmrs.report.ReportRenderer#getLinkUrl(org.openmrs.report.ReportDefinition)
	 */
	public String getLinkUrl(ReportDefinition schema) {
		return null;
	}

	/**
	 * @see org.openmrs.report.ReportRenderer#getFilename(org.openmrs.report.ReportDefinition)
	 */
	public String getFilename(ReportDefinition schema, String argument) {
		return schema.getName() + ".pdf";
	}

	/**
	 * @see ReportRenderer#render(ReportData, String, OutputStream)
	 */
	public void render(ReportData reportData, String argument, OutputStream out) throws IOException, RenderingException {
		
		//InputStream is = null;
		try {
			log.debug("Attempting to render report with BirtTemplateRenderer");
			
			ReportDesign design = getDesign(argument);
			ReportDesignResource r = getTemplate(design);
						
			ByteArrayInputStream inStream = new ByteArrayInputStream(r.getContents());
						
			BirtReport report = new BirtReport();
			BirtReportService reportService = (BirtReportService) Context.getService(BirtReportService.class);
						
			report.setOutputFormat("pdf");
			//reportService.generateReport(report);
						
			FileCopyUtils.copy(inStream, out);
			
		} catch (Exception e) {
			throw new RenderingException("Unable to render results due to: " + e, e);
		}
		   
	}
	
	/**
	 * Generate a report based on the attributes of the given report object.
	 * 
	 * @param	report	the report to generate
	 */
	public void generateReport(BirtReport report) {
		log.debug("Generating output for report " + report + ", hashcode " + report.hashCode());
		IRunAndRenderTask task = null;
		try {     		
			// Prepares the dataset for use within the BIRT engine 
			// (i.e. exports csv file, sets username/password for JDBC dataset)
			//prepareDataset(report);

			// Get the report engine that will be used to render the report
			IReportEngine engine = BirtConfiguration.getReportEngine();	    		

			// Open the report design
			IReportRunnable reportRunnable =
				engine.openReportDesign(report.getReportDesignPath());

			// Create a report rendering task
			task = engine.createRunAndRenderTask(reportRunnable);
			task.setParameterValues(report.getParameterValues());
			task.validateParameters();    		
			task.setRenderOption(BirtConfiguration.getRenderOption(report));
			task.run();

			// Add errors to the report object
			if (task.getErrors() != null && !task.getErrors().isEmpty()) {
				report.setErrors(task.getErrors());
			}

			// Set the output file 
			report.setOutputFile(new File(report.getOutputFilename()));	    	

			log.debug("Output file: " + report.getOutputFile().getAbsolutePath());
		} 
		catch (EngineException e) { 
			log.error("Unable to generate report due to a BIRT Exception: " + e.getMessage(), e);
			throw new BirtReportException("Unable to generate report due to a BIRT Exception: " + e.getMessage(), e);
		}
		finally { 
			if (task != null) task.close();
		}
	}

	
	

}
