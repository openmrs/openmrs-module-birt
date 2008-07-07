package org.openmrs.module.birt;

import java.util.logging.Level;
/* BIRT Core classs */
import org.eclipse.birt.core.framework.Platform;

/* BIRT Engine classes */
//import org.eclipse.birt.chart.engine.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.RenderOption;

/* BIRT Design classes */
import org.eclipse.birt.report.model.api.DesignConfig;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.IDesignEngineFactory;


/**
 * 
 * @author Justin Miranda
 *
 */
public class TestBirtConfiguration {
	
	// Logger
	private static Log log = LogFactory.getLog(TestBirtConfiguration.class);
	
	
	// BIRT logging configuration properties
	private static Level loggingLevel = Level.INFO;
	private static String loggingPath = BirtReportConstants.PROPERTY_LOGGING_DIR_DEFAULT;

	// BIRT engine configuration properties
	private static String engineHome = BirtReportConstants.PROPERTY_BIRT_HOME_DEFAULT;
	//private static String outputDir = BirtReportConstants.PROPERTY_OUTPUT_DIR_DEFAULT;
	//private static String reportDir = BirtReportConstants.PROPERTY_REPORT_DIR_DEFAULT;
	//private static String baseUrl = BirtReportConstants.PROPERTY_BASE_URL_DEFAULT;
	//private static String baseImageUrl = BirtReportConstants.PROPERTY_BASE_IMAGE_URL_DEFAULT;
	//private static String supportedImageFormats = BirtReportConstants.PROPERTY_SUPPORTED_IMAGE_FORMATS_DEFAULT;
		
	// BIRT configuration
	private static EngineConfig engineConfig;
	private static DesignConfig designConfig;
	
	// BIRT engines
	private static IDesignEngine designEngine;
	private static IReportEngine reportEngine;
	
	/** 
	 * Get the configuration object for the BIRT report engine.
	 * @return
	 */
	public synchronized static EngineConfig getEngineConfig() {		
		if (engineConfig == null) {
			engineConfig = new EngineConfig();
			engineConfig.setEngineHome(engineHome);
			engineConfig.setLogConfig(loggingPath, loggingLevel);		
	    }
	    return engineConfig;
	}

	/**
	 * Gets the configuration for the BIRT design engine.
	 * @return
	 */
	public synchronized static DesignConfig getDesignConfig() { 
		if (designConfig == null) { 
			designConfig = new DesignConfig( );
			designConfig.setProperty(Platform.PROPERTY_BIRT_HOME, engineHome);
		}
		return designConfig;
	}

	/**
	 * Gets the report engine - configures/creates the report engine if it doesn't already exist.
	 * @return	report engine 
	 */	
	public synchronized static IReportEngine getReportEngine() { 
		log.info("BIRT home " + BirtReportConstants.PROPERTY_BIRT_HOME_DEFAULT);
		log.info("Engine home " + TestBirtConfiguration.engineHome);
		if (reportEngine == null) {
			IReportEngineFactory factory = (IReportEngineFactory) 
				Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
			
			reportEngine = factory.createReportEngine( getEngineConfig() );
		}
		return reportEngine;
	}
	
	
	/**
	 * Get the BIRT design configuration object.
	 * @return
	 */
	public synchronized static IDesignEngine getDesignEngine() { 
		
		if (designEngine == null) { 
			//Configure the Engine and start the Platform
			try{
				Platform.startup( designConfig );
				IDesignEngineFactory factory = (IDesignEngineFactory) Platform
					.createFactoryObject( IDesignEngineFactory.EXTENSION_DESIGN_ENGINE_FACTORY );
				designEngine = factory.createDesignEngine(designConfig);
			} catch( Exception e){
				log.error("Error creating design engine: " + e.getMessage(), e);
			}		
		}
		return designEngine;					
	}

	/**
     * Set the rendering options - such as file or stream output, output format, whether it is embeddable, etc
	 * 
	 * @return	an object that specifes the output options
	 */
	public static IRenderOption getRenderOption(String format) { 
		IRenderOption options = null;
		if ("html".equals(format)) { 
			log.info("writing to html");
			options = new HTMLRenderOption();
			options.setOutputFormat(HTMLRenderOption.OUTPUT_FORMAT_HTML);		
			options.setOutputFileName(BirtReportConstants.PROPERTY_REPORT_OUTPUT_DEFAULT + "." + format);
		} 
		else if ("xls".equals("format")) {
			log.info("writing to xls");
			options = new RenderOption();
			options.setOutputFormat("xls");
			options.setOutputFileName(BirtReportConstants.PROPERTY_REPORT_OUTPUT_DEFAULT + "." + format);
		}
		else { 
			log.info("writing to pdf");
			options = new HTMLRenderOption();
			options.setOutputFormat(HTMLRenderOption.OUTPUT_FORMAT_PDF);
			options.setOutputFileName(BirtReportConstants.PROPERTY_REPORT_OUTPUT_DEFAULT + "." + format);
		}
		return options;
	}
		
	
	
	
	
	/**
	 * Get render context parameters (i.e. where images are located and how they're retrieved, formats supported).
	 * 
	 * @return	a map of render context parameters
	public static Map getRenderContext() { 
		HashMap<String, HTMLRenderContext> contextMap = new HashMap<String, HTMLRenderContext>();
		HTMLRenderContext renderContext = new HTMLRenderContext();
		renderContext.setBaseURL(baseUrl);
		renderContext.setBaseImageURL(baseImageUrl);
		renderContext.setImageDirectory(reportDir + "images");
		renderContext.setSupportedImageFormats(supportedImageFormats);
		contextMap.put( EngineConstants.APPCONTEXT_HTML_RENDER_CONTEXT, renderContext );
		return contextMap;
	}	
	 */

}
