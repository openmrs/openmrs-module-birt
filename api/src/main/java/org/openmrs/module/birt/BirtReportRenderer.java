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
package org.openmrs.module.birt;

import liquibase.csv.opencsv.CSVWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.core.data.DataType;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.model.api.DataSourceHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.datatools.connectivity.oda.flatfile.CommonConstants;
import org.hibernate.cfg.Environment;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetMetaData;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingException;
import org.openmrs.module.reporting.report.renderer.ReportDesignRenderer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Renders a Report using the Birt Runtime Engine and a Birt report design configuration
 * http://www.eclipse.org/birt/phoenix/deploy/reportEngineAPI.php for more information
 *
 * TODO: Consider whether or not to expose options automatically for pdf, doc, html, xls
 */
public class BirtReportRenderer extends ReportDesignRenderer {

	private Log log = LogFactory.getLog(this.getClass());

	public static final String REPORT_DATA_DATASOURCE_NAME = "reportData";
	public static final String OPENMRS_DATABASE_DATASOURCE_NAME = "openmrs";
	public static final String RPT_DESIGN_EXTENSION = ".rptdesign";
	public static final String PROPERTY_OUTPUT_FORMAT = "outputFormat";
	public static final String OUTPUT_FORMAT_ZIP = "zip"; // This is used to output raw data as a zip of CSV data sets;
	public static final String OUTPUT_FORMAT_HTML = "html";
	public static final String OUTPUT_FORMAT_PDF = "pdf";
	public static final String OUTPUT_FORMAT_DOC = "doc";
	public static final String OUTPUT_FORMAT_XLS = "xls";

	public BirtReportRenderer() {
		super();
	}

	public String getOutputFormat(String argument) {
		ReportDesign design = getDesign(argument);
		return getOutputFormat(design);
	}

	public String getOutputFormat(ReportDesign design) {
		return design.getPropertyValue(PROPERTY_OUTPUT_FORMAT, OUTPUT_FORMAT_HTML);
	}

	@Override
	public String getRenderedContentType(ReportDefinition definition, String argument) {
		String format = getOutputFormat(argument);
		if (OUTPUT_FORMAT_DOC.equals(format)) {
			return "application/msword";
		}
		else if (OUTPUT_FORMAT_XLS.equals(format)) {
			return "application/vnd.ms-excel";
		}
		else if (OUTPUT_FORMAT_PDF.equals(format)) {
			return "application/pdf";
		}
		else if (OUTPUT_FORMAT_ZIP.equals(format)) {
			return "application/zip";
		}
		else {
			return "text/html";
		}
	}

	@Override
	public String getFilename(ReportDefinition definition, String argument) {
		ReportDesign design = getDesign(argument);
		return design.getReportDefinition().getName() + "." + getOutputFormat(argument);
	}

	/**
	 * Get Birt design file
	 */
	public ReportDesignResource getBirtDesignResource(ReportDesign design) {
		for (ReportDesignResource resource : design.getResources()) {
			if (resource.getResourceFilename().endsWith(RPT_DESIGN_EXTENSION)) {
				return resource;
			}
		}
		if (design.getResources().size() == 1) {
			return design.getResources().iterator().next();
		}
		return null;
	}

	/**
	 * @see org.openmrs.module.reporting.report.renderer.ReportRenderer#render(org.openmrs.module.reporting.report.ReportData, String, java.io.OutputStream)
	 */
	public void render(ReportData reportData, String argument, OutputStream out) throws IOException, RenderingException {
		log.debug("Rendering report with " + getClass().getSimpleName());
		try {
			ReportDesign design = getDesign(argument);
			String outputFormat = getOutputFormat(design);
			ReportDesignResource rptDesign = getBirtDesignResource(design);
			render(reportData, outputFormat, rptDesign.getContents(), out);
		}
		catch (Exception e) {
			throw new RenderingException("Unable to render results due to: " + e, e);
		}
	}

	/**
	 * Convenience method for rendering given a report design, rather than an argument that loads one from the database
	 */
	public void render(ReportData reportData, String outputFormat, byte[] rptDesign, OutputStream out) throws IOException, RenderingException {
		try {
			if (OUTPUT_FORMAT_ZIP.equals(outputFormat)) {
				renderOutputToZip(reportData, out);
				return;
			}

			IReportEngine birtEngine = BirtRuntime.getReportEngine();
			IReportRunnable reportRunnable = birtEngine.openReportDesign(new ByteArrayInputStream(rptDesign));

			String outputDir = BirtRuntime.getConfiguration().getOutputDirectory();
			File tempDir = new File(outputDir, UUID.randomUUID().toString());
			if (!tempDir.exists()) {
				tempDir.mkdirs();
			}

			ModuleHandle moduleHandle = reportRunnable.getDesignHandle().getModuleHandle();

			// TODO: We should look into replacing the need for the below with a custom OpenMRS ODA Driver

			// If the report design references a data source named reportData, export the reportData to the file system
			// and change references within the report design so that it points to the appropriate files
			// TODO: Document on the wiki that this data source name is a convention, as are the data set names

			DataSourceHandle reportDataSource = moduleHandle.findDataSource(REPORT_DATA_DATASOURCE_NAME);
			if (reportDataSource != null) {
				reportDataSource.setProperty(CommonConstants.CONN_HOME_DIR_PROP, tempDir.getAbsolutePath());
				for (String dataSetKey : reportData.getDataSets().keySet()) {
					FileWriter writer = null;
					try {
						writer = new FileWriter(new File(tempDir, dataSetKey));
						Object includeTypeProp = reportDataSource.getProperty(CommonConstants.CONN_INCLTYPELINE_PROP);
						boolean includeTypeRow = includeTypeProp != null && CommonConstants.INC_TYPE_LINE_YES.equalsIgnoreCase(includeTypeProp.toString());
						writeDataSetToCsv(reportData.getDataSets().get(dataSetKey), includeTypeRow, writer);
					}
					finally {
						IOUtils.closeQuietly(writer);
					}
				}
			}

			// If the report design references a data source named openmrs, set credentials to those in runtime properties
			// TODO: Document on the wiki that this data source name is a convention, as are the data set names

			DataSourceHandle openmrsDataSource = moduleHandle.findDataSource(OPENMRS_DATABASE_DATASOURCE_NAME);
			if (openmrsDataSource != null) {
				Properties runtimeProperties = Context.getRuntimeProperties();
				String driverClass = runtimeProperties.getProperty(Environment.DRIVER);
				if (StringUtils.isNotBlank(driverClass)) {
					openmrsDataSource.setProperty("odaDriverClass", driverClass);
				}
				openmrsDataSource.setProperty("odaURL", runtimeProperties.getProperty("connection.url"));
				openmrsDataSource.setProperty("odaUser", runtimeProperties.getProperty("connection.username"));
				openmrsDataSource.setProperty("odaPassword", runtimeProperties.getProperty("connection.password"));
				openmrsDataSource.setEncryption("odaPassword", null);
			}

			IRunAndRenderTask task = birtEngine.createRunAndRenderTask(reportRunnable);
			task.setParameterValues(reportData.getContext().getParameterValues());

			IRenderOption option = new RenderOption();
			option.setOutputFormat(outputFormat);
			option.setOutputStream(out);

			// TODO: Allow properties set in the reporting module report design to influence the render option
			// TODO: Investigate creating a servlet that will expose any image uploaded as a report design resource at a url
			// TODO: Investigate creating a custom image handler

			if (OUTPUT_FORMAT_HTML.equalsIgnoreCase(outputFormat)) {
				HTMLRenderOption htmlOptions = new HTMLRenderOption(option);
				htmlOptions.setImageHandler(new HTMLServerImageHandler());
			}
			else if(OUTPUT_FORMAT_PDF.equalsIgnoreCase(outputFormat)) {
				PDFRenderOption pdfOptions = new PDFRenderOption(option);
				pdfOptions.setOption(PDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.FIT_TO_PAGE_SIZE);
			}
			task.setRenderOption(option);

			task.run();
			task.close();

			FileUtils.deleteDirectory(tempDir);
		}
		catch (Exception e) {
			throw new RenderingException("Unable to render results due to: " + e, e);
		}
	}

	protected void renderOutputToZip(ReportData reportData, OutputStream out) throws Exception {
		ZipOutputStream zip = new ZipOutputStream(out);
		for (String dataSetKey : reportData.getDataSets().keySet()) {
			zip.putNextEntry(new ZipEntry(dataSetKey+".csv"));
			writeDataSetToCsv(reportData.getDataSets().get(dataSetKey), true, new OutputStreamWriter(zip));
			zip.closeEntry();
		}
		zip.finish();
	}

	/**
	 * @return the birt data type name for the given class
	 */
	public static String getBirtDataType(Class<?> type) {
		String ret = DataType.STRING_TYPE_NAME;
		if (Date.class.isAssignableFrom(type)) {
			ret = DataType.DATE_TYPE_NAME;
		}
		else if (Boolean.class.isAssignableFrom(type)) {
			ret = DataType.BOOLEAN_TYPE_NAME;
		}
		else if (Number.class.isAssignableFrom(type)) {
			if (Integer.class.isAssignableFrom(type)) {
				ret = DataType.INTEGER_TYPE_NAME;
			}
			else {
				ret = DataType.DOUBLE_TYPE_NAME;
			}
		}
		return ret;
	}

	/**
	 * @return the birt data type name for the given class
	 */
	public static String getBirtFormattedValue(Class<?> type, Object value) {
		String format = null;
		if (Date.class.isAssignableFrom(type)) {
			format = "yyyy-MM-dd HH:mm:ss.S";
		}
		return ObjectUtil.format(value, format);
	}

	public static void writeDataSetToCsv(DataSet dataSet, boolean includeTypeRow, Writer writer) throws IOException {
		CSVWriter csvWriter = new CSVWriter(writer, ',');
		DataSetMetaData metadata = dataSet.getMetaData();
		String[] columns = new String[metadata.getColumns().size()];
		for (int i=0; i<metadata.getColumns().size(); i++) {
			columns[i] = metadata.getColumns().get(i).getName();
		}
		csvWriter.writeNext(columns);
		if (includeTypeRow) {
			String[] types = new String[metadata.getColumns().size()];
			for (int i=0; i<metadata.getColumns().size(); i++) {
				types[i] = getBirtDataType(metadata.getColumns().get(i).getDataType());
			}
			csvWriter.writeNext(types);
		}
		for (DataSetRow dataSetRow : dataSet) {
			String[] row = new String[metadata.getColumns().size()];
			for (int i = 0; i < metadata.getColumns().size(); i++) {
				Class<?> type = metadata.getColumns().get(i).getDataType();
				Object columnValue = dataSetRow.getColumnValue(metadata.getColumns().get(i));
				row[i] = getBirtFormattedValue(type, columnValue);
			}
			csvWriter.writeNext(row);
		}
		csvWriter.close();
	}
}
