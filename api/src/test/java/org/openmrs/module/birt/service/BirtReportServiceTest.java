package org.openmrs.module.birt.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.birt.BaseBirtTest;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.reporting.report.definition.ReportDefinition;

/**
 * Test the Birt Report Service class
 * TODO: Test all other service methods
 */
public class BirtReportServiceTest extends BaseBirtTest {

	protected Integer birtReportId;
	protected String birtReportName;

	@Before
	public void setup() throws Exception {
		ReportDefinition rd = new ReportDefinition();
		rd.setName("Test Birt Report");
		getReportDefinitionService().saveDefinition(rd);
		BirtReport br = createBirtReport(rd, "TestEmbeddedSql");
		birtReportId = br.getId();
		birtReportName = br.getName();
	}

	@Test
	public void shouldGetBirtReportById() throws Exception {
		BirtReport birtReport = getBirtReportService().getReport(birtReportId);
		Assert.assertEquals(birtReportId, birtReport.getId());
		Assert.assertEquals(birtReportName, birtReport.getName());
		Assert.assertNotNull(birtReport.getDesignFile());
	}
}
