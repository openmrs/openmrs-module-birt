package org.openmrs.module.birt;

import java.io.FileOutputStream;

import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;

public class TestGeneratePCPReport
{
	final String INPUT_FILE_NAME = "C:/Documents and Settings/tdugan/Desktop/java_source/query_changes/pcp_report.rptdesign";

	final String OUTPUT_FILE_NAME = "C:/Documents and Settings/tdugan/Desktop/pcp_report.html";

	final String BIRT_HOME = "C:/Documents and Settings/tdugan/Desktop/birt-runtime-2_2_0/ReportEngine";

	public void generateReport()
	{
		try
		{
			FileOutputStream outputStream = new FileOutputStream(OUTPUT_FILE_NAME);

			// Create and configure a report engine
			IReportEngine engine = null;
			EngineConfig config = null;
			config = new EngineConfig();
			config.setBIRTHome(BIRT_HOME);
			Platform.startup(config);
			IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
			engine = factory.createReportEngine(config);

			// create and configure a run an render task
			IReportRunnable runnable = engine.openReportDesign(INPUT_FILE_NAME);
			IRunAndRenderTask task = engine.createRunAndRenderTask(runnable);

			// set render options
			IRenderOption options = new HTMLRenderOption();
			options.setOutputFileName(OUTPUT_FILE_NAME);
			options.setOutputStream(outputStream);
			options.setOutputFormat("html");
			task.setRenderOption(options);

			// run the report
			task.run();

			// clean up
			task.close();
			engine.destroy();
			outputStream.close();

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		TestGeneratePCPReport test = new TestGeneratePCPReport();
		test.generateReport();
	}

}
