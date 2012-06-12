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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.model.api.OdaDataSetHandle;
import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ReportService;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportException;
import org.openmrs.module.birt.BirtReportService;
import org.openmrs.module.birt.impl.BirtConfiguration;
import org.openmrs.module.birt.impl.BirtReportServiceImpl;
import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingException;
import org.openmrs.module.reporting.report.renderer.ReportRenderer;
import org.openmrs.module.reporting.report.renderer.ReportTemplateRenderer;
import org.springframework.util.FileCopyUtils;
import org.openmrs.module.birt.BirtReportUtil;

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
		
		FileOutputStream fos = null;
		try {
			log.debug("Attempting to render report with BirtTemplateRenderer");

			ReportDesign design = getDesign(argument);
			ReportDesignResource r = getTemplate(design);			

			String pathDir = BirtReportUtil.createTempDirectory("reports").getAbsolutePath() + File.separator;
			
			BirtReport report = new BirtReport();
			report.setOutputFormat("pdf");
			report.setOutputFilename(pathDir + r.getName() + "." + report.getOutputFormat());	

			String pathName = pathDir + r.getResourceFilename();
			fos = new FileOutputStream(pathName);
			fos.write(r.getContents());

			//need to get the right prefix for the path to append to fileName
			report.setReportDesignPath(pathName);
			BirtReportService reportService = (BirtReportService) Context.getService(BirtReportService.class);
			
			BirtReportServiceImpl brt = new BirtReportServiceImpl();
			
			ReportDesignHandle dezign = brt.openReportDesign(report.getReportDesignPath());
			
			// set csv
			//csvOperation(dezign);
			
			// set xml
			xmlOperation(dezign);			

			reportService.generateReport(report);

			// Get a reference to the report output file to be copied to the response
			InputStream fileInputStream = new FileInputStream(report.getOutputFile());

			FileCopyUtils.copy(fileInputStream, out);

		} catch (Exception e) {
			throw new RenderingException("Unable to render results due to: " + e, e);
		}

	}
	
	public void csvOperation(ReportDesignHandle dezign) {

		try {
			OdaDataSourceHandle  dataSource = (OdaDataSourceHandle) dezign.getAllDataSources().get(0);
			dataSource.setProperty("URI", "C:\\Projects\\OpenMRS\\BIRT\\dsnimi\\concept_name.csv");	
			

			OdaDataSetHandle  dataSet = (OdaDataSetHandle) dezign.getAllDataSets().get(0);
			dataSet.setProperty("queryText", "select 'name', 'date_created' from 'file:/C:/Projects/OpenMRS/BIRT/project-documents/concept_name.csv' : {'name',\"name\",STRING;\"date_created\",\"date_created\",STRING}");
		} catch (SemanticException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void xmlOperation(ReportDesignHandle dezign) {
		try {
			OdaDataSourceHandle  dataSource = (OdaDataSourceHandle) dezign.getAllDataSources().get(0);
			dataSource.setProperty("FILELIST", "http://www.nasa.gov/rss/breaking_news.rss");	

			OdaDataSetHandle  dataSet = (OdaDataSetHandle) dezign.getAllDataSets().get(0);
			dataSet.setProperty("queryText", "[CDATA[table0#-TNAME-#table0#:#[/rss/channel/title]#:#{Title;STRING;../title},{Description;STRING;../description}]]");
		} catch (SemanticException e) {
			e.printStackTrace();
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
