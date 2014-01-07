package org.openmrs.module.birt;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.service.BirtReportService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.util.OpenmrsClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Properties;

/**
 * Base class for testing BIRT Report Module capabilities
 */
public abstract class BaseBirtTest extends BaseModuleContextSensitiveTest {

	protected static Log log = LogFactory.getLog(BaseBirtTest.class);

	@Override
	public Properties getRuntimeProperties() {
		Properties p = super.getRuntimeProperties();
		p.setProperty("connection.url", "jdbc:hsqldb:mem:openmrs");
		return p;
	}

	protected BirtReport createBirtReport(ReportDefinition rd, String resourceName) throws Exception {
		ReportDesign design = new ReportDesign();
		design.setName(rd.getName());
		design.setRendererType(BirtReportRenderer.class);
		design.setReportDefinition(rd);

		ReportDesignResource resource = new ReportDesignResource();
		resource.setName(resourceName);
		resource.setReportDesign(design);
		InputStream is = OpenmrsClassLoader.getInstance().getResourceAsStream("org/openmrs/module/birt/"+resourceName+".rptdesign");
		resource.setContents(IOUtils.toByteArray(is));
		IOUtils.closeQuietly(is);
		design.addResource(resource);
		design = getReportService().saveReportDesign(design);
		return new BirtReport(design);
	}

	protected String getRenderedReportAsString(BirtReport birtReport) throws Exception {
		ReportData data = getReportDefinitionService().evaluate(birtReport.getReportDefinition(), new EvaluationContext());
		ReportDesign design = birtReport.getReportDesign();
		BirtReportRenderer renderer = new BirtReportRenderer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		renderer.render(data, design.getUuid(), os);
		String ret = os.toString("UTF-8");
		os.close();
		return ret;
	}

	protected void writeReportDataToDisk(BirtReport birtReport) throws Exception {
		File dir = new File(SystemUtils.getUserHome(), "birtDataSets");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		ReportData data = getReportDefinitionService().evaluate(birtReport.getReportDefinition(), new EvaluationContext());
		for (String dsName : data.getDataSets().keySet()) {
			FileWriter writer = new FileWriter(new File(dir, dsName));
			BirtReportRenderer.writeDataSetToCsv(data.getDataSets().get(dsName), true, writer);
			IOUtils.closeQuietly(writer);
		}
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
