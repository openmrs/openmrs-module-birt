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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.model.api.OdaDataSetHandle;
import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ReportService;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BirtDataSetQuery;
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
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.Cohort;
import org.openmrs.module.reporting.indicator.IndicatorResult;
import org.eclipse.birt.report.model.api.DesignFileException;
import org.eclipse.birt.report.model.api.ElementFactory;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.SessionHandle;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.IDesignEngineFactory;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.model.api.DesignConfig;
import org.openmrs.module.birt.BirtConstants;

import com.ibm.icu.util.ULocale;

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
	

	String pathName = null;
	String pathDir = null;
	/**
	 * @see ReportRenderer#render(ReportData, String, OutputStream)
	 */
	public void render(ReportData reportData, String argument, OutputStream out) throws IOException, RenderingException {
		try {
			log.debug("Attempting to render report with BirtTemplateRenderer");
			
			ReportDesign design = getDesign(argument);
			ReportDesignResource r = getTemplate(design);
			
			pathDir = BirtReportUtil.createTempDirectory("reports").getAbsolutePath() + File.separator;
			
			pathName = pathDir + r.getName() + ".rptdesign";
			
			prepareDatasets(reportData.getDataSets(), r);	
			
			modifyReport(r);						
					
			executeReport();

			// Get a reference to the report output file to be copied to the response
			InputStream fileInputStream = new FileInputStream(new File(pathDir + "my_report.pdf"));

			FileCopyUtils.copy(fileInputStream, out);

		} catch (Exception e) {
			throw new RenderingException("Unable to render results due to: " + e, e);
		}

	}
	
	String[] qtext = null;
	public void prepareDatasets(Map<String, DataSet> datasets, ReportDesignResource designResource) {
		Iterator<Entry<String, DataSet>> iter = datasets.entrySet().iterator();
		
		while (iter.hasNext()) {
			Map.Entry mEntry = (Map.Entry) iter.next();
			mEntry.getKey();
			
			DataSet dataset = (DataSet) mEntry.getValue();
			DataSetRow datasetRow = dataset.iterator().next();
			
			List<DataSetColumn> columns = dataset.getMetaData().getColumns();	
			
			try {
				FileWriter w = new FileWriter(pathDir + designResource.getName() + ".csv");
				
				qtext = new String[3];
				int i = 0;
				// header row
				w.append(getBeforeRowDelimiter());
				for (DataSetColumn column : columns) {
					w.append(getBeforeColumnDelimiter());
					w.append(escape(column.getName()));
					w.append(getAfterColumnDelimiter());
					qtext[i] = column.getName();
					i++;					
				}
				w.append(getAfterRowDelimiter());
				
				// data rows
				for (DataSetRow row : dataset) {
					w.append(getBeforeRowDelimiter());
					for (DataSetColumn column : columns) {
						Object colValue = row.getColumnValue(column);
						w.append(getBeforeColumnDelimiter());
						if (colValue != null) {
							if (colValue instanceof Cohort) {
								w.append(escape(Integer.toString(((Cohort) colValue).size())));
							} else if (colValue instanceof IndicatorResult) {
								w.append(((IndicatorResult) colValue).getValue().toString());
							}
							else {
								// this check is because a logic EmptyResult .toString() -> null
								String temp = escape(colValue.toString());
								if (temp != null)
									w.append(temp);
							}
						}
						w.append(getAfterColumnDelimiter());
					}
					w.append(getAfterRowDelimiter());
				}
				
				w.close();
			} catch (IOException e) {
				throw new RenderingException("Error: " + e, e);
			}
		}
	}
	
	/**
	 * @see DelimitedTextReportRenderer#getBeforeColumnDelimiter()
	 */
	public String getBeforeColumnDelimiter() {
		return "\"";
	}
	
	/**
	 * @see DelimitedTextReportRenderer#getBeforeRowDelimiter()
	 */
	public String getBeforeRowDelimiter() {
		return "";
	}
	
	/**
	 * @see DelimitedTextReportRenderer#getAfterRowDelimiter()
	 */
	public String getAfterRowDelimiter() {
		return "\n";
	}
	
	/**
	 * Convenience method used to escape a string of text.
	 * 
	 * @param	text 	The text to escape.
	 * @return	The escaped text.
	 */
	public String escape(String text) {
		if (text == null) {
			return null;
		}
		else {
			return text.replaceAll("\"", "\\\"");
		}
	}	
	
	/**
	 * @see DelimitedTextReportRenderer#getAfterColumnDelimiter()
	 */
	public String getAfterColumnDelimiter() {
		return "\",";
	}
	
	/**
	 * Modify a report based on the attributes of the given report object.
	 * 
	 */	
	public void modifyReport(ReportDesignResource designResource) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(pathName);
			fos.write(designResource.getContents());
			fos.close();
			
			ReportDesignHandle reportDesign = getReportDesign(pathName);
			
			if ( reportDesign != null ) { 

				for (Iterator iterator = reportDesign.getAllDataSets().iterator(); iterator.hasNext(); ) { 
					Object obj = iterator.next();


					if ( obj instanceof OdaDataSetHandle) { 
						OdaDataSetHandle datasetHandle = (OdaDataSetHandle) obj;					

						// Flat File data set (reset the HOME property) 
						if ("org.eclipse.datatools.connectivity.oda.flatfile.dataSet".equals(datasetHandle.getExtensionID())) { 
							
							// First we need to set the data source to the dataset's current directory
							String uriPath = "file:/" + pathDir + designResource.getName() + ".csv";
							
							datasetHandle.getDataSource().setProperty("URI", uriPath.replace(File.separator, "/"));
							//datasetHandle.setProperty("queryText", "select 'field_type_id', 'name' from '" + pathDir + designResource.getName() + ".csv'" + " : {'field_type_id',\"field_type_id\",STRING;\"name\",\"name\",STRING}");
							
							// TODO Refactor to use a better query parser ... 
							// this one does not handle more complex queries 
							log.debug("Data set query [BEFORE]:\n" + datasetHandle.getQueryText());

							// Create the query object and change the table name
							BirtDataSetQuery datasetQuery = new BirtDataSetQuery(datasetHandle.getQueryText());					
							datasetQuery.setTable(designResource.getName() + ".csv");
							
							StringBuilder sb = new StringBuilder();
							sb.append("SELECT ");
							for (int i = 0; i < qtext.length - 1; i++){
								sb.append("\"");
								if(i == qtext.length-2)
									sb.append(qtext[i]).append("\"");
								else
									sb.append(qtext[i]).append("\"").append(",");								
							}
							sb.append(" FROM ");
							sb.append("\"");
							sb.append("file:/");
							sb.append(pathDir.replace(File.separator, "/"));
							sb.append(designResource.getName() + ".csv");
							sb.append("\"");
							sb.append(" : {");
							for (int i = 0; i < qtext.length-1; i++){
								if(i == qtext.length-2)
									sb.append("\"").append(qtext[i]).append("\"").append(",").append("\"").append(qtext[i]).append("\"").append(",").append("STRING");
								else
									sb.append("\"").append(qtext[i]).append("\"").append(",").append("\"").append(qtext[i]).append("\"").append(",").append("STRING").append(";");
							}
							sb.append("}");

							//datasetHandle.setQueryText(datasetQuery.getQueryText());
							datasetHandle.setQueryText(sb.toString());
							log.debug("Data set query [AFTER]:\n" + datasetHandle.getQueryText());
						} 
	
					}					
				}
				reportDesign.checkReport();
				reportDesign.save();		
				reportDesign.close();
			} else { 
				log.debug("Report design " + "report.getReportDesignPath()" + " does not exists");
			}
			
		} catch (Exception e) {
			
		}
	}
	
	
	/**
	 * Generate a report based on the attributes of the given report object.
	 * 
	 * @param	report	the report to generate
	 */
	public void executeReport() {
		//log.debug("Generating output for report " + designResource + ", hashcode " + designResource.hashCode());
		IRunAndRenderTask task = null;
		try {			
		
		// Get the report engine that will be used to render the report
			IReportEngine engine = BirtConfiguration.getReportEngine();	    		

			// Open the report design
			IReportRunnable reportRunnable = engine.openReportDesign(pathName);
				
			// Create a report rendering task
			task = engine.createRunAndRenderTask(reportRunnable);
			task.setParameterValues(null);
			// Validate runtime parameters
			task.validateParameters();
			
			//task.setRenderOption(BirtConfiguration.getRenderOption(report));
			PDFRenderOption options = new PDFRenderOption();
			options.setOutputFileName(pathDir + "my_report.pdf");
			options.setOutputFormat("pdf");
			task.setRenderOption(options);
			//task.setRenderOption(BirtConfiguration.getRenderOption(report));    
			
			// Render report design
			task.run();
			task.close();
			engine.destroy();

/*			// Add errors to the report object
			if (task.getErrors() != null && !task.getErrors().isEmpty()) {
				report.setErrors(task.getErrors());
			}
*/
			// Set the output file 
			new File(pathName);
			//report.setOutputFile(new File(PathName));	    	

			//log.debug("Output file: " + report.getOutputFile().getAbsolutePath());
		} 
		catch (EngineException e) { 
			log.error("Unable to generate report due to a BIRT Exception: " + e.getMessage(), e);
			throw new BirtReportException("Unable to generate report due to a BIRT Exception: " + e.getMessage(), e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally { 
			if (task != null) task.close();
		}
	}
	
	/**
	 * Gets a report design file by opening an existing file or creating a new design file).
	 * 
	 * @param reportPath
	 * @return
	 */
	public ReportDesignHandle getReportDesign(String reportPath) { 
		ReportDesignHandle handle = null;
		try { 

			handle = openReportDesign(reportPath);	    	

			// Removed this functionality because it was sometimes overwriting report design files
			// that EXIST but were not found.  Not sure why/how the report design was not found.

			/*if (handle==null) { 
	    		log.debug("Could not open report design " + reportPath);
	    		handle = createReportDesign(reportPath);
	    	}*/

		} catch (Exception e) { 
			// TODO for debugging, re-throw exception
			log.warn("Unable to open report design at location " + reportPath);	 
			/* ignore for now since all reports start with no report design */
		}
		return handle;
	}
	
	/**
	 * Open an existing report
	 * 
	 * @param reportPath	path to the report
	 * @return	a report design handle
	 */
	public ReportDesignHandle openReportDesign(String reportPath) {
		ReportDesignHandle handle = null;
		try {
			IDesignEngine designEngine = BirtConfiguration.getDesignEngine();	
			handle = designEngine.newSessionHandle(ULocale.ENGLISH).openDesign(reportPath);
		}
		catch (DesignFileException e) { 
			// Ignore for now
			log.debug("Could not open report design " + reportPath + ": " + e.getMessage());
			//throw new BirtReportException(e);
		}
		return handle;
	}

	
	
}
