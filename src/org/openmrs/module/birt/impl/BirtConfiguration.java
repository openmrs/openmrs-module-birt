package org.openmrs.module.birt.impl;

import java.awt.image.renderable.RenderContext;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/* OpenMRS */
import org.openmrs.api.context.Context;

/* BIRT module classes */
import org.openmrs.module.birt.BirtConstants;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportException;
import org.openmrs.module.birt.BirtReportUtil;

/* BIRT Core classs */
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;

/* BIRT Engine classes */
//import org.eclipse.birt.chart.engine.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderContext;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.HTMLActionHandler;
import org.eclipse.birt.report.engine.api.HTMLEmitterConfig;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;

/* BIRT Design classes */
import org.eclipse.birt.report.model.api.DesignConfig;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.IDesignEngineFactory;


/**
 * Contains configuration information for the BIRT runtime engine.
 * 
 * @author Justin Miranda
 * @version 1.0
 */
public class BirtConfiguration {
	
	// Logger
	private static Log log = LogFactory.getLog(BirtConfiguration.class);


			
	/* BIRT runtime and design configuration */  
	private static EngineConfig engineConfig;
	private static DesignConfig designConfig;
	
	/* BIRT runtime and design engines */
	private static IReportEngine reportEngine;
	private static IDesignEngine designEngine;
	
	/** 
	 * Get the configuration object for the BIRT report engine.
	 * 
	 * @return
	 */
	public synchronized static EngineConfig getEngineConfig() {
		if (engineConfig == null) {
			log.info("Creating BIRT engine config with BIRT_HOME = " + BirtConstants.BIRT_HOME);
			engineConfig = new EngineConfig();
			engineConfig.setEngineHome(BirtConstants.BIRT_HOME);
			engineConfig.setLogConfig(BirtConstants.LOGGING_PATH, BirtConstants.LOGGING_LEVEL);	
	    }
	    return engineConfig;
	}
	
	

	/**
	 * Gets the configuration for the BIRT design engine.
	 * 
	 * @return
	 */
	public synchronized static DesignConfig getDesignConfig() { 
		if (designConfig == null) { 
			log.info("Creating BIRT design config with BIRT_HOME = " + BirtConstants.BIRT_HOME);
			designConfig = new DesignConfig( );
			designConfig.setProperty(Platform.PROPERTY_BIRT_HOME, BirtConstants.BIRT_HOME);
		}
		
		return designConfig;
	}

	/**
	 * Configures a report engine if it doesn't already exist.
	 * 
	 * @return	report engine 
	 */	
	public synchronized static IReportEngine getReportEngine() throws BirtReportException { 
		if (reportEngine == null) {
			try { 
				
				log.info("Creating instance of the BIRT Report Engine Factory " 
						+ IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
				
				IReportEngineFactory factory = (IReportEngineFactory) 
					Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
								
				reportEngine = factory.createReportEngine( getEngineConfig() );
								
			} catch (Exception e) { 
				log.error("The BIRT Report Module failed to initialize the report engine: " + e.getMessage(), e);
				throw new BirtReportException(e);
			}
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
	 * Creates a render option with default output file and format options.
	 * 
	 * @return
	public static IRenderOption getRenderOption() { 
		return getRenderOption(REPORT_OUTPUT_FILE, REPORT_OUTPUT_FORMAT);
	}
	 */

	/**
	 * 
	 * @param outputFormat
	 * @return
	public static IRenderOption getRenderOption(String format) { 
		return getRenderOption(REPORT_OUTPUT_FILE, format);
	}
	 */

	/**
	 * Creates a render option with the specified output file and format options. 
	 * @param filename	the filename of the to-be-rendered output
	 * @param format	the format of the to-be-rendered output
	 * @return
	 */
	public static IRenderOption getRenderOption(BirtReport report) { 
		
		// Populating the render options
		IRenderOption options = new RenderOption();
		if(BirtConstants.HTML_FORMAT.equalsIgnoreCase(report.getOutputFormat())){
			HTMLRenderOption htmlOptions = new HTMLRenderOption( options);
			htmlOptions.setImageDirectory(BirtConstants.OUTPUT_DIR + "/images/");
			htmlOptions.setHtmlPagination(false);

			//set this if you want your image source url to be altered
			//If using the setBaseImageURL, make sure to set image handler to HTMLServerImageHandler
			//htmlOptions.setBaseImageURL("http://myhost/prependme?image=");
					
			//htmlOptions.setHtmlRtLFlag(false);
			//htmlOptions.setEmbeddable(false);
		}
		else if(BirtConstants.PDF_FORMAT.equalsIgnoreCase(report.getOutputFormat())) { 
			PDFRenderOption pdfOptions = new PDFRenderOption( options );
			pdfOptions.setOption(IPDFRenderOption.FIT_TO_PAGE, new Boolean(true));
			pdfOptions.setOption(IPDFRenderOption.PAGEBREAK_PAGINATION_ONLY, new Boolean(true));

		}
		//file based images
		//options.setImageHandler(new HTMLCompleteImageHandler())

		//Web based images.  Allows setBaseImageURL to prepend to img src tag
		options.setImageHandler(new HTMLServerImageHandler());
		options.setBaseURL(BirtConstants.BASE_URL);
		options.setSupportedImageFormats(BirtConstants.SUPPORTED_IMAGE_FORMATS);		
		
		
		// Setting the report output file name
		String name = report.getReportDefinition().getName();
		String filename = BirtReportUtil.getOutputFilename(name, report.getOutputFormat());
		report.setOutputFilename(filename);

		log.info("Setting report output filename " + filename);

		
		
		// Set output filename and format
		options.setOutputFormat(report.getOutputFormat());
		options.setOutputFileName(report.getOutputFilename());		
			
		
		return options;
	}
	
	/*
	 * TODO Add support for PDF/HTML
	public static IRenderOption getPdfRenderOption(String outputFile) { 
		// replace outputFile extension
		return getRenderOption(outputFile, "pdf");
	}
	
	/*
	 * TODO Add support for Html
	public static IRenderOption getHtmlRenderOption(String outputFile) { 
		// replace outFile extension
		return getRenderOption(outputFile, "html");
	*/
	
	
	/**
	 * Get render context parameters.
	 * 
	 * @return
	 */
	/*
	public static Map getRenderContext() { 
		
		HashMap<String, RenderContext> contextMap = new HashMap<String, RenderContext>();
		HTMLRenderContext renderContext = new HTMLRenderContext();
		
		renderContext.setBaseURL(BASE_URL);
		renderContext.setBaseImageURL(BASE_IMAGE_URL);
		renderContext.setImageDirectory(IMAGE_DIR);
		renderContext.setSupportedImageFormats(SUPPORTED_IMAGE_FORMATS);				
		contextMap.put( EngineConstants.APPCONTEXT_HTML_RENDER_CONTEXT, renderContext );
		return contextMap;
	}
	*/
	
	/**
	 * Convenience method for getting global properties.
	 * @param property	name of the global property
	 * @return	values of the global property
	 */
	public static String getGlobalProperty(String property) { 
		return Context.getAdministrationService().getGlobalProperty(property);
	}
	

}
