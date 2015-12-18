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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportException;
import org.openmrs.module.birt.BirtReportService;
import org.openmrs.module.birt.BirtReportUtil;
import org.openmrs.module.birt.BirtConfiguration;
import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.indicator.IndicatorResult;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.DelimitedTextReportRenderer;
import org.openmrs.module.reporting.report.renderer.RenderingException;
import org.openmrs.module.reporting.report.renderer.ReportRenderer;
import org.springframework.util.FileCopyUtils;

/**
 * Report Renderer implementation that supports rendering to a Birt Doc template
 */
@Handler
@Localized("reporting.BirtDocTemplateRenderer")
public class BirtDocTemplateRenderer extends BirtTemplateRenderer {

	private Log log = LogFactory.getLog(this.getClass());

	public BirtDocTemplateRenderer() {
		super();
	}

	public String getRenderedContentType(ReportDefinition schema, String argument) {
		return "application/msword";
	}

	public String getFilename(ReportDefinition schema, String argument) {
		return schema.getName() + ".doc";
	}

	/**
	 * @see ReportRenderer#render(ReportData, String, OutputStream)
	 */
	public void render(ReportData reportData, String argument, OutputStream out)
			throws IOException, RenderingException {
		try {
			log.debug("Attempting to render report with BirtDocTemplateRenderer");

			ReportDesign design = getDesign(argument);

			Integer reportId = design.getReportDefinition().getId();

			ReportDesignResource r = getTemplate(design);

			String pathDir = BirtReportUtil.createTempDirectory("reports").getAbsolutePath() + File.separator;

			String pathName = pathDir + r.getName() + ".rptdesign";

			BirtReportService reportService = (BirtReportService) Context.getService(BirtReportService.class);
			BirtReport report = reportService.getReport(reportId);

			report.setReportDesignPath(pathName);
			report.setReportDesignResource(r);
			report.setOutputFormat("doc");
			report.setOutputDirectory(pathDir);

			// Setting the report output file name
			if (report.getOutputFilename() == null) {
				String name = report.getReportDefinition().getName();
				String filename = BirtReportUtil.getOutputFilename(name, report.getOutputFormat());
				report.setOutputFilename(filename);
			}

			if (report.getOutputFilename().isEmpty()) {
				report.setOutputFilename(pathDir + report.getReportDefinition().getName() + ".doc");
			}

			log.debug("Setting report output filename " + report.getOutputFilename());

			/* Generate a report design file */
			reportService.createReportDesign(report);

			prepareDatasets(reportData, report);

			reportService.prepareDataset(report);

			executeReport(report);

			// Get a reference to the report output file to be copied to the
			// response
			InputStream fileInputStream = new FileInputStream(new File(report.getOutputFilename()));

			FileCopyUtils.copy(fileInputStream, out);

		} catch (Exception e) {
			throw new RenderingException("Unable to render results due to: " + e, e);
		}
	}

	public void prepareDatasets(ReportData reportData, BirtReport report) {

		Iterator<Entry<String, DataSet>> iter = reportData.getDataSets().entrySet().iterator();

		List<String> fileNames = null;
		while (iter.hasNext()) {
			Map.Entry<String, DataSet> mEntry = (Map.Entry<String, DataSet>) iter.next();

			DataSet dataset = (DataSet) mEntry.getValue();

			List<DataSetColumn> columns = dataset.getMetaData().getColumns();

			try {
				String fileName = mEntry.getKey() + ".csv";
				FileWriter w = new FileWriter(report.getOutputDirectory() + fileName);
				fileNames = new ArrayList<String>();
				fileNames.add(fileName);

				// header row
				w.append(getBeforeRowDelimiter());
				for (DataSetColumn column : columns) {
					w.append(getBeforeColumnDelimiter());
					w.append(escape(column.getName()));
					w.append(getAfterColumnDelimiter());
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
							} else {
								// this check is because a logic EmptyResult
								// .toString() -> null
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

		report.setCsvFileNames(fileNames);
	}

	public String getBeforeColumnDelimiter() {
		return "\"";
	}

	public String getBeforeRowDelimiter() {
		return "";
	}

	public String getAfterRowDelimiter() {
		return "\n";
	}

	/**
	 * Convenience method used to escape a string of text.
	 * 
	 * @param text
	 *            The text to escape.
	 * @return The escaped text.
	 */
	public String escape(String text) {
		if (text == null) {
			return null;
		} else {
			return text.replaceAll("\"", "\\\"");
		}
	}

	public String getAfterColumnDelimiter() {
		return "\",";
	}

	/**
	 * Generate a report based on the attributes of the given report object.
	 * 
	 * @param report
	 *            the report to generate
	 */
	public void executeReport(BirtReport report) {
		// log.debug("Generating output for report " + designResource + ",
		// hashcode " + designResource.hashCode());
		IRunAndRenderTask task = null;
		try {

			// Get the report engine that will be used to render the report
			IReportEngine engine = BirtConfiguration.getReportEngine();

			// Open the report design
			IReportRunnable reportRunnable = engine.openReportDesign(report.getReportDesignPath());

			// Create a report rendering task
			task = engine.createRunAndRenderTask(reportRunnable);
			// task.setParameterValues(null);
			// Validate runtime parameters
			task.validateParameters();

			// task.setRenderOption(BirtConfiguration.getRenderOption(report));
			// Set the RenderOption to create a DOC file into the task.
			IRenderOption options = new RenderOption();
			options.setOutputFileName(report.getOutputFilename());
			options.setOutputFormat(report.getOutputFormat());
			task.setRenderOption(options);

			// task.setRenderOption(BirtConfiguration.getRenderOption(report));

			// Render report design
			task.run();

			// log.debug("Output file: " +
			// report.getOutputFile().getAbsolutePath());
		} catch (EngineException e) {
			log.error("Unable to generate report due to a BIRT Exception: " + e.getMessage(), e);
			throw new BirtReportException("Unable to generate report due to a BIRT Exception: " + e.getMessage(), e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (task != null)
				task.close();
		}
	}
}
