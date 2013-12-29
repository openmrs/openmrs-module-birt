package org.openmrs.module.birt;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.renderer.BirtReportRenderer;
import org.openmrs.module.birt.service.BirtReportService;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.util.OpenmrsClassLoader;

import java.io.InputStream;

/**
 * Base class for testing BIRT Report Module capabilities
 */
public abstract class BaseBirtTest extends BaseModuleContextSensitiveTest {

	protected static Log log = LogFactory.getLog(BaseBirtTest.class);
	protected Integer testBirtReportId;
	protected String testBirtReportName = "Test Birt Report";

	/**
	 * Set up some Birt Reports in the database
	 * @throws Exception
	 */
	@Before
	public void setup() throws Exception {

		ReportDefinition rd = new ReportDefinition();
		rd.setName(testBirtReportName);

		SqlDataSetDefinition patients = new SqlDataSetDefinition();
		patients.setSqlQuery("select p.patient_id, n.gender, n.birthdate from patient p, person n where p.patient_id = n.person_id and p.voided = 0 and n.voided = 0");
		rd.addDataSetDefinition("patients", patients, null);

		SqlDataSetDefinition encounters = new SqlDataSetDefinition();
		encounters.setSqlQuery("select e.encounter_id, e.patient_id, e.encounter_datetime, t.name from encounter e, encounter_type t where e.encounter_type = t.encounter_type_id and e.voided = 0");
		rd.addDataSetDefinition("encounters", encounters, null);

		getReportDefinitionService().saveDefinition(rd);

		ReportDesign design = new ReportDesign();
		design.setName(testBirtReportName);
		design.setRendererType(BirtReportRenderer.class);
		design.setReportDefinition(rd);

		ReportDesignResource resource = new ReportDesignResource();
		resource.setName("Design1.rptdesign");
		resource.setReportDesign(design);
		InputStream is = OpenmrsClassLoader.getInstance().getResourceAsStream("org/openmrs/module/birt/TestBirtReport.rptdesign");
		resource.setContents(IOUtils.toByteArray(is));
		IOUtils.closeQuietly(is);
		design.addResource(resource);

		design = getReportService().saveReportDesign(design);
		testBirtReportId = design.getId();
	}

	protected ReportDefinitionService getReportDefinitionService() {
		return Context.getService(ReportDefinitionService.class);
	}

	protected ReportService getReportService() {
		return Context.getService(ReportService.class);
	}

	protected BirtReportService getBirtReportService() {
		return Context.getService(BirtReportService.class);
	}
}
