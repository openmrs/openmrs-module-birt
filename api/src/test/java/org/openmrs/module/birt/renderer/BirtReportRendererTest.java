package org.openmrs.module.birt.renderer;

import org.junit.Test;
import org.openmrs.module.birt.BaseBirtTest;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Test the Birt Report Service class
 */
public class BirtReportRendererTest extends BaseBirtTest {

	@Test
	public void shouldRenderBirtReport() throws Exception {
		BirtReport birtReport = getBirtReportService().getReport(testBirtReportId);
		ReportData data = getReportDefinitionService().evaluate(birtReport.getReportDefinition(), new EvaluationContext());
		ReportDesign design = birtReport.getReportDesign();
		BirtReportRenderer renderer = new BirtReportRenderer();
		FileOutputStream fos = new FileOutputStream(new File("/home/mseaton/Desktop", birtReport.getExportFilename()));
		renderer.render(data, design.getUuid(), fos);
		fos.close();
	}
}
