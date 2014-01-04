package org.openmrs.module.birt;

import junit.framework.Assert;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.report.definition.ReportDefinition;


/**
 * Test the Birt Report Renderer
 * 	TODO: Test parameters of each type
 * 	TODO: Test including types in csv and not doing so
 * 	TODO: Test html, doc, pdf, excel output formats
 * 	TODO: Test filesystem is cleaned up appropriately
 */
public class BirtReportRendererTest extends BaseBirtTest {

	@Test
	public void shouldRenderBirtReportWithReportData() throws Exception {
		BirtRuntime.startup(new BirtConfiguration());
		BirtReport birtReport = createBirtReportWithReportData();
		String html = getRenderedReportAsString(birtReport);
		assertHtmlContainsContents(html, "PATIENT_ID", "GENDER", "BIRTHDATE");
		assertHtmlContainsContents(html, "2", "M", "1975-04-08 00:00:00.0");
		assertHtmlContainsContents(html, "ENCOUNTER_ID", "ENCOUNTER_DATETIME", "NAME");
		assertHtmlContainsContents(html, "3", "2008-08-01 00:00:00.0", "Emergency");
	}

	@Test
	public void shouldRenderBirtReportWithEmbeddedSql() throws Exception {
		BirtReport birtReport = createBirtReportWithEmbeddedSql();
		String html = getRenderedReportAsString(birtReport);
		assertHtmlContainsContents(html, "ID", "Name", "Description");
		assertHtmlContainsContents(html, "1", "Scheduled", "Scheduled Visit");
		assertHtmlContainsContents(html, "2", "Emergency", "Emergency visit");
		assertHtmlContainsContents(html, "6", "Laboratory", "Visit to the laboratory");
	}

	protected void assertHtmlContainsContents(String html, String...expectedElements) {
		Document doc = Jsoup.parse(html);
		for (String s : expectedElements) {
			boolean found = false;
			for (Element element : doc.getAllElements()) {
				found = found || s.equalsIgnoreCase(element.text().trim());
			}
			Assert.assertTrue("Document does not contain " + s, found);
		}
	}

	protected BirtReport createBirtReportWithReportData() throws Exception {
		ReportDefinition rd = new ReportDefinition();
		rd.setName("Test Birt Report");

		SqlDataSetDefinition patients = new SqlDataSetDefinition();
		patients.setSqlQuery("select p.patient_id, n.gender, n.birthdate from patient p, person n where p.patient_id = n.person_id and p.voided = 0 and n.voided = 0");
		rd.addDataSetDefinition("patients", patients, null);

		SqlDataSetDefinition encounters = new SqlDataSetDefinition();
		encounters.setSqlQuery("select e.encounter_id, e.patient_id, e.encounter_datetime, t.name from encounter e, encounter_type t where e.encounter_type = t.encounter_type_id and e.voided = 0");
		rd.addDataSetDefinition("encounters", encounters, null);

		getReportDefinitionService().saveDefinition(rd);

		return createBirtReport(rd, "TestReportData");
	}

	protected BirtReport createBirtReportWithEmbeddedSql() throws Exception {
		ReportDefinition rd = new ReportDefinition();
		rd.setName("Test Birt Report");
		getReportDefinitionService().saveDefinition(rd);
		return createBirtReport(rd, "TestEmbeddedSql");
	}
}
