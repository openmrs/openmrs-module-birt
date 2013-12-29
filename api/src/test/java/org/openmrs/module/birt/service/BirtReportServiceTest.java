package org.openmrs.module.birt.service;

import org.eclipse.birt.report.engine.api.IReportEngine;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.BaseBirtTest;
import org.openmrs.module.birt.BirtConfiguration;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtRuntime;
import org.openmrs.module.birt.renderer.BirtReportRenderer;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.io.File;

/**
 * Test the Birt Report Service class
 */
public class BirtReportServiceTest extends BaseBirtTest {

	@Test
	public void shouldGetBirtReportById() throws Exception {
		BirtReport birtReport = getBirtReportService().getReport(testBirtReportId);
		Assert.assertEquals(testBirtReportId, birtReport.getId());
		Assert.assertEquals(testBirtReportName, birtReport.getName());
		Assert.assertNotNull(birtReport.getDesignFile());

		/*
		File dir = new File("/home/mseaton/Desktop/birtDataSets");
		ReportData data = getReportDefinitionService().evaluate(birtReport.getReportDefinition(), new EvaluationContext());
		for (String dsName : data.getDataSets().keySet()) {
			File outputFile = new File(dir, dsName);
			BirtReportRenderer.writeDataSetToCsv(data.getDataSets().get(dsName), outputFile);
		}
		*/
	}
}
