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
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class PcpReportTest extends BaseModuleContextSensitiveTest {

	// Replaced "c:/" with "c:/", since you only need to escape if it's a back slash (i.e. c:\\)
	final String INPUT_FILE_NAME = "C:/Documents and Settings/Justin Miranda/My Documents/My Reports/test.rptdesign";
    final String OUTPUT_FILE_NAME = "C:/Documents and Settings/Justin Miranda/My Documents/My Reports/test.html";
    final String BIRT_HOME = "C:/java/birt-runtime-2_2_0/ReportEngine";
    
    public void testGenerateReport() { 
	    try {	    	
	        FileOutputStream outputStream = new FileOutputStream(OUTPUT_FILE_NAME);
	
	        // Create and configure a report engine
	        IReportEngine engine = null;
	        EngineConfig config = null;
	        config = new EngineConfig();
	        config.setBIRTHome(BIRT_HOME);
	        
	        // Not necessary if you're creating one below.  This may have been causing the exception.
	        //RenderOption hc = new RenderOption();
	        //config.setEmitterConfiguration(RenderOption.OUTPUT_FORMAT_HTML, hc);
	        
	        Platform.startup(config);
	        IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
	        engine = factory.createReportEngine(config);
	
	        // create and configure a run an render task
	        IReportRunnable runnable = engine.openReportDesign(INPUT_FILE_NAME);
	        IRunAndRenderTask task = engine.createRunAndRenderTask(runnable);
	
	        // Create and tell the task to use the following rendering options
	        //RenderOption options = new RenderOption();
	        //options.setOutputStream(outputStream);
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
	        
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
    }
   

}
