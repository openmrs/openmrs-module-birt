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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.ReportTemplateRenderer;
//import org.openmrs.api.ReportService;

/**
 * Abstract super-class for all Report Renderer implementation that supports rendering to a BIRT template
 */
@Handler // problems addding...
@Localized("reporting.BirtTemplateRenderer")
public abstract class BirtTemplateRenderer extends ReportTemplateRenderer {	

	private Log log = LogFactory.getLog(this.getClass());

	public BirtTemplateRenderer() {
		super();
	}
	
	/**
	 * @see org.openmrs.report.ReportRenderer#getLinkUrl(org.openmrs.report.ReportDefinition)
	 */
	public String getLinkUrl(ReportDefinition schema) {
		return null;
	}	
	
}
