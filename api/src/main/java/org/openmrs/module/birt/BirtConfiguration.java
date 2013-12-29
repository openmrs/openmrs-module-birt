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

import org.apache.commons.lang.SystemUtils;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.util.logging.Level;

/**
 * Encapsulates all of the configuration logic for the module
 */
public class BirtConfiguration {

	public static final String PROPERTY_OUTPUT_DIR = "birt.outputDir";
	public static final String PROPERTY_LOGGING_DIR = "birt.loggingDir";
	public static final String PROPERTY_LOGGING_LEVEL = "birt.loggingLevel";

	public String getOutputDirectory() {
		String defaultVal = OpenmrsUtil.getApplicationDataDirectory() + SystemUtils.FILE_SEPARATOR + "BIRT";
		return getConfigurationValue(PROPERTY_OUTPUT_DIR, defaultVal);
	}

	public EngineConfig getBirtEngineConfig() {
		EngineConfig config = new EngineConfig();
		String loggingDir = getConfigurationValue(PROPERTY_LOGGING_DIR, getOutputDirectory());
		Level loggingLevel = Level.parse(getConfigurationValue(PROPERTY_LOGGING_LEVEL, "INFO"));
		config.setLogConfig(loggingDir, loggingLevel);
		return config;
	}

	private String getConfigurationValue(String gpName, String defaultValue) {
		return Context.getAdministrationService().getGlobalProperty(gpName, defaultValue);
	}

	/* OpenMRS specific properties */

	public static final String PROPERTY_REPORT_DIR = "birt.reportDir";
	public static final String PROPERTY_DATASET_DIR = "birt.datasetDir";
	public static final String PROPERTY_REPORT_OUTPUT_FORMAT = "birt.reportOutputFormat";
	public static final String PROPERTY_REPORT_OUTPUT_FILE = "birt.reportOutputFile";
	public static final String PROPERTY_REPORT_PREVIEW_FILE = "birt.reportPreviewFile";
	public static final String PROPERTY_ALWAYS_USE_OPENMRS_JDBC_PROPERTIES = "birt.alwaysUseOpenmrsJdbcProperties";
	public static final String PROPERTY_DEFAULT_REPORT_DESIGN_FILE	= 	"birt.defaultReportDesignFile";
	public static final String PROPERTY_BASE_URL = "birt.baseUrl";
	public static final String PROPERTY_BASE_IMAGE_URL = "birt.baseImageUrl";
	public static final String PROPERTY_IMAGE_DIR = "birt.imageDir";
	public static final String PROPERTY_SUPPORTED_IMAGE_FORMATS = "birt.supportImageFormats";
	public static final String PROPERTY_ODA_USER =	"birt.odaUser";
	public static final String PROPERTY_ODA_PASSWORD = "birt.odaPassword";
	public static final String PROPERTY_ODA_URL = "birt.odaURL";
	public static final String PROPERTY_BASE_URL_DEFAULT = "http://localhost";
	public static final String PROPERTY_BASE_IMAGE_URL_DEFAULT = "http://localhost/openmrs/images";
	public static final String PROPERTY_SUPPORTED_IMAGE_FORMATS_DEFAULT = "JPG;PNG;BMP;SVG";

	public static final String DEFAULT_BASE_DIR 				= 	System.getProperty("user.home");
	public static final String DEFAULT_REPORT_DIR				= 	DEFAULT_BASE_DIR + File.separator + "reports";
	public static final String DEFAULT_DATASET_DIR				= 	DEFAULT_REPORT_DIR + File.separator + "datasets";
	public static final String DEFAULT_OUTPUT_DIR 				= 	DEFAULT_REPORT_DIR + File.separator + "output";
	public static final String DEFAULT_REPORT_PREVIEW_FILE 		= 	DEFAULT_OUTPUT_DIR + File.separator + "ReportPreview.pdf";
	public static final String DEFAULT_REPORT_OUTPUT_FILE 		= 	DEFAULT_OUTPUT_DIR + File.separator + "ReportOutput.pdf";
	public static final String DEFAULT_LOGGING_DIR 				= 	DEFAULT_REPORT_DIR + File.separator + "logs";

	public static final String DEFAULT_DATETIME_FORMAT 			= "yyyy-MM-dd hh:ss:mm a";
	public static final String DEFAULT_DATE_FORMAT 				= "yyyy-MM-dd";
	public static final String DEFAULT_TIME_FORMAT 				= "hh:ss:mm a";
	public static final String DEFAULT_REPORT_OUTPUT_FORMAT 	= 	"pdf";
	public static final String DEFAULT_BASE_URL 				= 	"http://localhost";
	public static final String DEFAULT_BASE_IMAGE_URL 			= 	"http://localhost/images";
	public static final String DEFAULT_IMAGE_DIR 				= 	"images";
	public static final String DEFAULT_SUPPORTED_IMAGE_FORMATS 	= 	"JPG;PNG;BMP;SVG";
}
