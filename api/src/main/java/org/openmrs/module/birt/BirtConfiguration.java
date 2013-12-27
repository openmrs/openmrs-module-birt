package org.openmrs.module.birt;

/* OpenMRS */
import org.openmrs.api.context.Context;

/* BIRT module classes */

/* BIRT Core classs */
		import org.eclipse.birt.core.framework.Platform;

/* BIRT Engine classes */
//import org.eclipse.birt.chart.engine.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.eclipse.birt.report.engine.api.HTMLRenderContext;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.EngineConfig;
//import org.eclipse.birt.report.engine.api.HTMLEmitterConfig;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;

/* BIRT Design classes */
import org.eclipse.birt.report.model.api.DesignConfig;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.IDesignEngineFactory;

import java.io.File;
import java.util.logging.Level;


/**
 * Contains configuration information for the BIRT runtime engine.
 * 
 * @author Justin Miranda
 * @version 1.0
 */
public class BirtConfiguration {

	/* Required BIRT module properties.
	 * Names correspond to global properties in the database */
	public static final String PROPERTY_LOGGING_DIR 			= 	"birt.loggingDir";
	/* BIRT Logging properties */
	public static String LOGGING_PATH = getGlobalProperty(PROPERTY_LOGGING_DIR);
	public static final String PROPERTY_LOGGING_LEVEL		 	=   "birt.loggingLevel";
	public static Level LOGGING_LEVEL = Level.parse(getGlobalProperty(PROPERTY_LOGGING_LEVEL));
	public static final String PROPERTY_BIRT_HOME 				= 	"birt.birtHome";
	public static final String PROPERTY_REPORT_DIR				= 	"birt.reportDir";
	public static String REPORT_DIR = getGlobalProperty(PROPERTY_REPORT_DIR);
	public static final String PROPERTY_OUTPUT_DIR 				= 	"birt.outputDir";
	public static String OUTPUT_DIR = getGlobalProperty(PROPERTY_OUTPUT_DIR);
	public static final String PROPERTY_DATASET_DIR				= 	"birt.datasetDir";
	/* OpenMRS specific properties */
	public static String DATASET_DIR = getGlobalProperty(PROPERTY_DATASET_DIR);
	public static final String PROPERTY_REPORT_OUTPUT_FORMAT 	= 	"birt.reportOutputFormat";
	public static String REPORT_OUTPUT_FORMAT = getGlobalProperty(PROPERTY_REPORT_OUTPUT_FORMAT);
	public static final String PROPERTY_REPORT_OUTPUT_FILE 		= 	"birt.reportOutputFile";
	public static String REPORT_OUTPUT_FILE = getGlobalProperty(PROPERTY_REPORT_OUTPUT_FILE);
	public static final String PROPERTY_REPORT_PREVIEW_FILE 	= 	"birt.reportPreviewFile";
	public static String REPORT_PREVIEW_FILE = getGlobalProperty(PROPERTY_REPORT_PREVIEW_FILE);
	/**
	 * New
	 */
	public static final String PROPERTY_ALWAYS_USE_OPENMRS_JDBC_PROPERTIES
																= 	"birt.alwaysUseOpenmrsJdbcProperties";
	public static Boolean ALWAYS_USE_OPENMRS_JDBC_PROPERTIES =
		getBooleanGlobalProperty(PROPERTY_ALWAYS_USE_OPENMRS_JDBC_PROPERTIES, "false");
	/* OpenMRS properties not used yet */
	public static final String PROPERTY_DEFAULT_REPORT_DESIGN_FILE	= 	"birt.defaultReportDesignFile";
	public static String DEFAULT_REPORT_DESIGN_FILE = getGlobalProperty(PROPERTY_DEFAULT_REPORT_DESIGN_FILE);
	/* Optional BIRT module properties. */
	public static final String PROPERTY_BASE_URL 				= 	"birt.baseUrl";
	public static final String PROPERTY_BASE_IMAGE_URL 			= 	"birt.baseImageUrl";
	public static String BASE_IMAGE_URL = getGlobalProperty(PROPERTY_BASE_IMAGE_URL);
	public static final String PROPERTY_IMAGE_DIR	 			= 	"birt.imageDir";
	public static String IMAGE_DIR = getGlobalProperty(PROPERTY_IMAGE_DIR);
	public static final String PROPERTY_SUPPORTED_IMAGE_FORMATS = 	"birt.supportImageFormats";
	public static String SUPPORTED_IMAGE_FORMATS = getGlobalProperty(PROPERTY_SUPPORTED_IMAGE_FORMATS);
	/* Optional BIRT module properties. */
	public static final String PROPERTY_ODA_USER 				= 	"birt.odaUser";
	public static final String PROPERTY_ODA_PASSWORD 			= 	"birt.odaPassword";
	public static final String PROPERTY_ODA_URL	 				= 	"birt.odaURL";
	/* BIRT image properties */
	/* TODO These values do not seem to be working for generated images (graphs/charts) */
	public static final String PROPERTY_BASE_URL_DEFAULT = "http://localhost";
	/* BIRT Image properties */
	public static String BASE_URL = getGlobalProperty(PROPERTY_BASE_URL_DEFAULT);
	public static final String PROPERTY_BASE_IMAGE_URL_DEFAULT = "http://localhost/openmrs/images";
	public static final String PROPERTY_SUPPORTED_IMAGE_FORMATS_DEFAULT = "JPG;PNG;BMP;SVG";
	/* Default properties values - currently not used */
	public static final String DEFAULT_BIRT_HOME 				= 	"c:/java/birt-runtime-2.2.2";
	public static final String DEFAULT_BASE_DIR 				= 	System.getProperty("user.home");
	public static final String DEFAULT_REPORT_DIR				= 	DEFAULT_BASE_DIR + File.separator + "reports";
	public static final String DEFAULT_DATASET_DIR				= 	DEFAULT_REPORT_DIR + File.separator + "datasets";
	public static final String DEFAULT_OUTPUT_DIR 				= 	DEFAULT_REPORT_DIR + File.separator + "output";
	public static final String DEFAULT_REPORT_PREVIEW_FILE 		= 	DEFAULT_OUTPUT_DIR + File.separator + "ReportPreview.pdf";
	public static final String DEFAULT_REPORT_OUTPUT_FILE 		= 	DEFAULT_OUTPUT_DIR + File.separator + "ReportOutput.pdf";
	public static final String DEFAULT_LOGGING_DIR 				= 	DEFAULT_REPORT_DIR + File.separator + "logs";
	/* Used for BIRT report parameters */
	public static final String DEFAULT_DATETIME_FORMAT 			= "yyyy-MM-dd hh:ss:mm a";
	public static final String DEFAULT_DATE_FORMAT 				= "yyyy-MM-dd";
	public static final String DEFAULT_TIME_FORMAT 				= "hh:ss:mm a";
	public static final String DEFAULT_REPORT_OUTPUT_FORMAT 	= 	"pdf";
	public static final String DEFAULT_BASE_URL 				= 	"http://localhost";
	public static final String DEFAULT_BASE_IMAGE_URL 			= 	"http://localhost/images";
	public static final String DEFAULT_IMAGE_DIR 				= 	"images";
	public static final String DEFAULT_SUPPORTED_IMAGE_FORMATS 	= 	"JPG;PNG;BMP;SVG";
	/* BIRT Engine properties */
	public static String BIRT_HOME = ".";
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
			log.debug("Creating BIRT engine config with BIRT_HOME = " + BIRT_HOME + ", the Current Directory");
			engineConfig = new EngineConfig();
			engineConfig.setEngineHome(BIRT_HOME);
			engineConfig.setLogConfig(LOGGING_PATH, LOGGING_LEVEL);
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
			log.debug("Creating BIRT design config with BIRT_HOME = " + BIRT_HOME);
			designConfig = new DesignConfig( );
			designConfig.setProperty(Platform.PROPERTY_BIRT_HOME, BIRT_HOME);
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
				
				log.debug("Creating instance of the BIRT Report Engine Factory " 
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
	 * @return
	 */
	public static IRenderOption getRenderOption(BirtReport report) { 
		
		// Populating the render options
		IRenderOption options = new RenderOption();
		if(BirtConstants.HTML_FORMAT.equalsIgnoreCase(report.getOutputFormat())){
			HTMLRenderOption htmlOptions = new HTMLRenderOption( options);
			htmlOptions.setImageDirectory(OUTPUT_DIR + "/images/");
			htmlOptions.setHtmlPagination(false);

			//set this if you want your image source url to be altered
			//If using the setBaseImageURL, make sure to set image handler to HTMLServerImageHandler
			//htmlOptions.setBaseImageURL("http://myhost/prependme?image=");
					
			//htmlOptions.setHtmlRtLFlag(false);
			//htmlOptions.setEmbeddable(false);
		}
		else if(BirtConstants.PDF_FORMAT.equalsIgnoreCase(report.getOutputFormat())) { 
			PDFRenderOption pdfOptions = new PDFRenderOption( options );
			pdfOptions.setOption(PDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.FIT_TO_PAGE_SIZE);
			pdfOptions.setOption(IPDFRenderOption.FIT_TO_PAGE, new Boolean(true));
			pdfOptions.setOption( IRenderOption.HTML_PAGINATION, Boolean.FALSE );
			pdfOptions.setOption(IPDFRenderOption.PAGEBREAK_PAGINATION_ONLY, new Boolean(true));
			pdfOptions.setSupportedImageFormats("PNG;GIF;JPG;BMP;SWF;SVG");
			pdfOptions.setOutputFormat("pdf");

		}
		//file based images
		//options.setImageHandler(new HTMLCompleteImageHandler())

		//Web based images.  Allows setBaseImageURL to prepend to img src tag
		options.setImageHandler(new HTMLServerImageHandler());
		options.setBaseURL(BASE_URL);
		options.setSupportedImageFormats(SUPPORTED_IMAGE_FORMATS);
		
		
		// Setting the report output file name
		if (report.getOutputFilename() == null) { 
			String name = report.getReportDefinition().getName();
			String filename = BirtReportUtil.getOutputFilename(name, report.getOutputFormat());
			report.setOutputFilename(filename);
		}

		log.debug("Setting report output filename " + report.getOutputFilename());

		
		
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
	
/*	public static Map getRenderContext() { 
		
		HashMap<String, RenderContext> contextMap = new HashMap<String, RenderContext>();
		HTMLRenderContext renderContext = new HTMLRenderContext();
		
		renderContext.setBaseURL(BASE_URL);
		renderContext.setBaseImageURL(BASE_IMAGE_URL);
		renderContext.setImageDirectory(IMAGE_DIR);
		renderContext.setSupportedImageFormats(SUPPORTED_IMAGE_FORMATS);				
		contextMap.put( EngineConstants.APPCONTEXT_HTML_RENDER_CONTEXT, renderContext );
		return contextMap;
	}*/
	
	
	/**
	 * Convenience method for getting global properties.
	 * 
	 * @param property	name of the global property
	 * @return	value of the global property
	 */
	public static String getGlobalProperty(String property) { 
		return Context.getAdministrationService().getGlobalProperty(property);
	}
	
	
	/**
	 * Convenience method to retrieve boolean value for the specified global property.
	 * 
	 * @param property		the property name 
	 * @param defaultValue	the default value to be used
	 * @return	value of global property or default value
	 */
	public static Boolean getBooleanGlobalProperty(String property, String defaultValue) { 
		return new Boolean(Context.getAdministrationService().getGlobalProperty(property, defaultValue));
	}
	

}
