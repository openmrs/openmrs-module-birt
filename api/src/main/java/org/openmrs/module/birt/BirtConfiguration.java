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
}
