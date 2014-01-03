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
package org.openmrs.module.birt.renderer;

import liquibase.csv.opencsv.CSVWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
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
import org.eclipse.birt.report.engine.api.script.element.IDataSource;
import org.eclipse.birt.report.model.api.OdaDataSourceHandle;
import org.eclipse.datatools.connectivity.oda.flatfile.CommonConstants;
import org.openmrs.module.birt.BirtConfiguration;
import org.openmrs.module.birt.BirtRuntime;
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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.UUID;

/**
 * Renders a Report using the Birt Runtime Engine and a Birt report design configuration
 */
public class BirtReportRenderer extends ReportDesignRenderer {

	private Log log = LogFactory.getLog(this.getClass());

	public static final String BIRT_DATA_SOURCE_NAME = "reportData";
	public static final String RPT_DESIGN_EXTENSION = ".rptdesign";
	public static final String PROPERTY_OUTPUT_FORMAT = "outputFormat";
	public static final String OUTPUT_FORMAT_HTML = "html";
	public static final String OUTPUT_FORMAT_PDF = "pdf";
	public static final String OUTPUT_FORMAT_DOC = "doc";
	public static final String OUTPUT_FORMAT_XLS = "xls";

	public BirtReportRenderer() {
		super();
	}

	public String getOutputFormat(String argument) {
		ReportDesign design = getDesign(argument);
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
			ReportDesignResource rptDesign = getBirtDesignResource(design);

			BirtRuntime.startup(new BirtConfiguration());
			IReportEngine birtEngine = BirtRuntime.getReportEngine();
			IReportRunnable reportRunnable = birtEngine.openReportDesign(new ByteArrayInputStream(rptDesign.getContents()));

			String outputDir = BirtRuntime.getConfiguration().getOutputDirectory();
			File tempDir = new File(outputDir, UUID.randomUUID().toString());
			if (!tempDir.exists()) {
				tempDir.mkdirs();
			}

			IDataSource dataSource = reportRunnable.getDesignInstance().getDataSource(BIRT_DATA_SOURCE_NAME);
			OdaDataSourceHandle handle = null;

			if (dataSource != null && dataSource instanceof OdaDataSourceHandle) {
				handle = (OdaDataSourceHandle)dataSource;
				handle.setProperty(CommonConstants.CONN_FILE_URI_PROP, "file:/" + tempDir.getAbsolutePath());
			}

			for (String dataSetKey : reportData.getDataSets().keySet()) {
				File outputFile = new File(tempDir, dataSetKey);
				boolean includeTypeRow = true;
				if (handle != null) {
					Object includeTypeProp = handle.getProperty(CommonConstants.CONN_INCLTYPELINE_PROP);
					includeTypeRow = includeTypeProp != null && "TRUE".equalsIgnoreCase(includeTypeProp.toString());
				}
				writeDataSetToCsv(reportData.getDataSets().get(dataSetKey), includeTypeRow, outputFile);
			}

			IRunAndRenderTask task = birtEngine.createRunAndRenderTask(reportRunnable);
			task.setParameterValues(reportData.getContext().getParameterValues());

			// TODO: Properly handle parameters, and test this

			String outputFormat = getOutputFormat(argument);
			String outputFilename = tempDir + SystemUtils.FILE_SEPARATOR + getFilename(design.getReportDefinition(), argument);

			task.setRenderOption(getRenderOption(outputFormat, outputFilename));

			task.run();
			task.close();

			FileInputStream fis = new FileInputStream(new File(outputFilename));
			IOUtils.copy(fis, out);
			fis.close();

			FileUtils.deleteDirectory(tempDir);
		}
		catch (Exception e) {
			throw new RenderingException("Unable to render results due to: " + e, e);
		}
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

	public static void writeDataSetToCsv(DataSet dataSet, boolean includeTypeRow, File outputFile) throws IOException {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(outputFile, false);
			CSVWriter csvWriter = new CSVWriter(fileWriter, ',');
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
		finally {
			IOUtils.closeQuietly(fileWriter);
		}
	}

	/**
	 * Creates a render option with the specified output file and format options.
	 */
	public static IRenderOption getRenderOption(String outputFormat, String outputFilename) {

		IRenderOption options = new RenderOption();
		options.setOutputFormat(outputFormat);
		options.setOutputFileName(outputFilename);

		if (OUTPUT_FORMAT_HTML.equalsIgnoreCase(outputFormat)) {
			HTMLRenderOption htmlOptions = new HTMLRenderOption(options);
			htmlOptions.setImageDirectory(BirtRuntime.getConfiguration().getOutputDirectory() + "/images/");
			htmlOptions.setHtmlPagination(false);
		}
		else if(OUTPUT_FORMAT_PDF.equalsIgnoreCase(outputFormat)) {
			PDFRenderOption pdfOptions = new PDFRenderOption( options );
			pdfOptions.setOption(PDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.FIT_TO_PAGE_SIZE);
			pdfOptions.setOption(IRenderOption.HTML_PAGINATION, Boolean.FALSE);
		}
		options.setImageHandler(new HTMLServerImageHandler());
		options.setBaseURL(BirtConfiguration.DEFAULT_BASE_URL);
		options.setSupportedImageFormats(BirtConfiguration.DEFAULT_SUPPORTED_IMAGE_FORMATS);

		// TODO: options.setImageHandler(new HTMLCompleteImageHandler())

		return options;
	}
}
