package org.openmrs.module.birt;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;

import org.openmrs.module.birt.impl.BirtConfiguration;

public class BirtConstants { 
	
	/* */
	public static final String INVALID_COHORT_KEY = "0";
	public static final String ALL_REPORTS = "all";
	public static final String PATIENT_REPORTS = "patient";
	public static final String COMMA_SEPARATOR = ",";	
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final String REPORT_DESIGN_EXTENSION = "rptdesign";
	
	
	public static final String PARAM_OUTPUT_FORMAT = "outputFormat";
	public static final String HTML_FORMAT = "html";
	public static final String PDF_FORMAT = "pdf";
	public static final String DOC_FORMAT = "doc";
	public static final String XLS_FORMAT = "xls";
	
	/* Required BIRT module properties.  Names correspond to global properties in the database */
	public static final String PROPERTY_LOGGING_DIR 			= 	"birt.loggingDir";
	public static final String PROPERTY_BIRT_HOME 				= 	"birt.birtHome";
	public static final String PROPERTY_REPORT_DIR				= 	"birt.reportDir";
	public static final String PROPERTY_OUTPUT_DIR 				= 	"birt.outputDir";
	public static final String PROPERTY_DATASET_DIR				= 	"birt.datasetDir";
	public static final String PROPERTY_REPORT_OUTPUT_FORMAT 	= 	"birt.reportOutputFormat";
	public static final String PROPERTY_REPORT_OUTPUT_FILE 		= 	"birt.reportOutputFile";
	public static final String PROPERTY_REPORT_PREVIEW_FILE 	= 	"birt.reportPreviewFile";

	/* OpenMRS properties not used yet */
	public static final String PROPERTY_DEFAULT_REPORT_DESIGN_FILE	= 	"birt.defaultReportDesignFile";

	/* Optional BIRT module properties. */
	public static final String PROPERTY_BASE_URL 				= 	"birt.baseUrl";
	public static final String PROPERTY_BASE_IMAGE_URL 			= 	"birt.baseImageUrl";
	public static final String PROPERTY_IMAGE_DIR	 			= 	"birt.imageDir";
	public static final String PROPERTY_SUPPORTED_IMAGE_FORMATS = 	"birt.supportImageFormats";

	/* Optional BIRT module properties. */
	public static final String PROPERTY_ODA_USER 				= 	"birt.odaUser";
	public static final String PROPERTY_ODA_PASSWORD 			= 	"birt.odaPassword";
	public static final String PROPERTY_ODA_URL	 				= 	"birt.odaURL";
	
	
	/* BIRT image properties */	
	/* TODO These values do not seem to be working for generated images (graphs/charts) */
	public static final String PROPERTY_BASE_URL_DEFAULT = "http://localhost";
	public static final String PROPERTY_BASE_IMAGE_URL_DEFAULT = "http://localhost/openmrs/images";
	public static final String PROPERTY_SUPPORTED_IMAGE_FORMATS_DEFAULT = "JPG;PNG;BMP;SVG";
	
	/* Default properties values - currently not used */
	public static final String DEFAULT_BIRT_HOME 				= 	"c:/java/birt-runtime-2.2.2";
	public static final String DEFAULT_BASE_DIR 				= 	System.getProperty("user.home");
	public static final String DEFAULT_REPORT_DIR				= 	DEFAULT_BASE_DIR + File.separator + "reports";
	public static final String DEFAULT_LOGGING_DIR 				= 	DEFAULT_REPORT_DIR + File.separator + "logs";

	/* Used for BIRT report parameters */
	public static final String DEFAULT_DATETIME_FORMAT 			= "yyyy-MM-dd hh:ss:mm a";
	public static final String DEFAULT_DATE_FORMAT 				= "yyyy-MM-dd";
	public static final String DEFAULT_TIME_FORMAT 				= "hh:ss:mm a";
	
	public static final String DEFAULT_REPORT_OUTPUT_FORMAT 	= 	"pdf";
	public static final String DEFAULT_OUTPUT_DIR 				= 	DEFAULT_REPORT_DIR + File.separator + "output";
	public static final String DEFAULT_DATASET_DIR				= 	DEFAULT_REPORT_DIR + File.separator + "datasets";
	public static final String DEFAULT_REPORT_OUTPUT_FILE 		= 	DEFAULT_OUTPUT_DIR + File.separator + "ReportOutput.pdf";
	public static final String DEFAULT_REPORT_PREVIEW_FILE 		= 	DEFAULT_OUTPUT_DIR + File.separator + "ReportPreview.pdf";

	public static final String DEFAULT_BASE_URL 				= 	"http://localhost";
	public static final String DEFAULT_BASE_IMAGE_URL 			= 	"http://localhost/images";
	public static final String DEFAULT_IMAGE_DIR 				= 	"images";	
	public static final String DEFAULT_SUPPORTED_IMAGE_FORMATS 	= 	"JPG;PNG;BMP;SVG";

	/* Used for report output name */
	public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MMM-yyyy-HHmmss");
	
	/* BIRT Logging properties */
	public static String LOGGING_PATH = BirtConfiguration.getGlobalProperty(BirtConstants.PROPERTY_LOGGING_DIR);
	
	public static Level LOGGING_LEVEL = Level.INFO;

	/* BIRT Engine properties */
	public static String BIRT_HOME = BirtConfiguration.getGlobalProperty(BirtConstants.PROPERTY_BIRT_HOME);	
	
	public static String OUTPUT_DIR = BirtConfiguration.getGlobalProperty(BirtConstants.PROPERTY_OUTPUT_DIR);
	
	public static String REPORT_DIR = BirtConfiguration.getGlobalProperty(BirtConstants.PROPERTY_REPORT_DIR);
	
	/* OpenMRS specific properties */
	public static String DATASET_DIR = BirtConfiguration.getGlobalProperty(BirtConstants.PROPERTY_DATASET_DIR);
	
	public static String DEFAULT_REPORT_DESIGN_FILE = BirtConfiguration.getGlobalProperty(BirtConstants.PROPERTY_DEFAULT_REPORT_DESIGN_FILE);
	
	public static String REPORT_OUTPUT_FORMAT = BirtConfiguration.getGlobalProperty(BirtConstants.PROPERTY_REPORT_OUTPUT_FORMAT);
	
	public static String REPORT_OUTPUT_FILE = BirtConfiguration.getGlobalProperty(BirtConstants.PROPERTY_REPORT_OUTPUT_FILE);
	
	public static String REPORT_PREVIEW_FILE = BirtConfiguration.getGlobalProperty(BirtConstants.PROPERTY_REPORT_PREVIEW_FILE);	
	
	/* BIRT Image properties */
	public static String BASE_URL = BirtConfiguration.getGlobalProperty(BirtConstants.PROPERTY_BASE_URL_DEFAULT);
	
	public static String BASE_IMAGE_URL = BirtConfiguration.getGlobalProperty(BirtConstants.PROPERTY_BASE_IMAGE_URL);
	
	public static String IMAGE_DIR = BirtConfiguration.getGlobalProperty(BirtConstants.PROPERTY_IMAGE_DIR);
	
	public static String SUPPORTED_IMAGE_FORMATS = BirtConfiguration.getGlobalProperty(BirtConstants.PROPERTY_SUPPORTED_IMAGE_FORMATS);
	

}