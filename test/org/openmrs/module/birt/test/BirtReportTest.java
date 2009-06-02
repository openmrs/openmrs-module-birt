package org.openmrs.module.birt.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.model.api.DesignFileException;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortDefinition;
import org.openmrs.cohort.CohortDefinitionItemHolder;
import org.openmrs.module.birt.BirtConstants;
import org.openmrs.module.birt.BirtDataSetQuery;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportService;
import org.openmrs.module.birt.BirtReportUtil;
import org.openmrs.module.birt.impl.BirtConfiguration;
import org.openmrs.module.birt.impl.BirtReportServiceImpl;
import org.openmrs.module.birt.model.ParameterDefinition;
import org.openmrs.module.birt.util.BirtQueryUtil;
import org.openmrs.reporting.report.ReportDefinition;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.ibm.icu.util.ULocale;

/**
 * Test cases for BIRT Report Module capabilities.  
 * 
 * @author Justin Miranda
 */
public class BirtReportTest extends BaseModuleContextSensitiveTest {
	
	// Logger
	private static Log log = LogFactory.getLog(BirtReportTest.class);
	private static Logger logger;

	// BIRT report engine
	private static IReportEngine reportEngine;
	
	// BIRT design engine
	private static IDesignEngine designEngine;	

	/**
	 * Public constructor
	 */
	public BirtReportTest() { }
	
	/**
	 * Initializes all BIRT resources.
	 */
	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		try {
			Platform.startup( BirtConfiguration.getEngineConfig());
			reportEngine = BirtConfiguration.getReportEngine();
			designEngine = BirtConfiguration.getDesignEngine();
			logger = reportEngine.getLogger();
			logger.info("Started BIRT report engine");
			
		} catch (BirtException be) {
			log.error("Error starting BIRT ", be);
			throw new IllegalArgumentException("Failure starting BIRT platform", be);
		}		

	}	

	/**
	 * Destroys the BIRT report engine used in the tests.
	 */
	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		Platform.shutdown();
	}

	/**
	 * Initialization method called before each test case.
	 * @throws Exception
	 */
	@Before
	public void onSetup() throws Exception { 
		authenticate();		
	}
	
	
	/**
	 * Indicates whether to use the in-memory database.
	 */
	@Override
	public Boolean useInMemoryDatabase() {
		return false;
	}

	
	/**
	 * Tests the default date formatter.
	 */
	@Test
	public void shouldParseDateCorrectly() { 
		try { 
			Context.getDateFormat().parse("01/01/2009 00:00:00");				
		} catch (ParseException e) {
			Assert.fail("Unable to parse date because string is not valid");
		}
	}

	/**
	 * Tests the generate-and-email-report functionality.
	 * @throws Exception
	 */
	@Test
	public void shouldGenerateAndEmailReport() throws Exception { 

		try { 
			BirtReportService service = 
				(BirtReportService) Context.getService(BirtReportService.class);
	
			
			Map<String,Object> reportParameters = new HashMap<String,Object>();
			reportParameters.put("DateParameter", java.sql.Date.valueOf("2008-10-01"));
			reportParameters.put("DatetimeParameter", new Date());

			Map<String,String> emailProperties = new HashMap<String,String>();
			emailProperties.put(BirtConstants.REPORT_EMAIL_FROM, "justin.miranda@gmail.com");
			emailProperties.put(BirtConstants.REPORT_EMAIL_TO, "justin.miranda@gmail.com");
			//emailProperties.put(BirtConstants.REPORT_EMAIL_CC, "");
			//emailProperties.put(BirtConstants.REPORT_EMAIL_BCC, "");
			emailProperties.put(BirtConstants.REPORT_EMAIL_SUBJECT, "Testing subject");
			emailProperties.put(BirtConstants.REPORT_EMAIL_BODY, "Testing body");
					
			BirtReport report = service.getReport(new Integer(285));		
			report.setOutputFormat(BirtConstants.DEFAULT_REPORT_OUTPUT_FORMAT);
			report.setCohort(new Cohort());
			report.setEmailProperties(emailProperties);
			report.addParameters(reportParameters);
			report.addParameter("startDate", new Date());
			report.addParameter("endDate", new Date());

			service.generateAndEmailReport(report);
			
			
		} catch (Exception e) { 
			Assert.fail("Unable to generate and email report");
		}
		
	}
	
	
	/**
	 * Tests the query parser.
	 */
	public void shouldBuildValidQuery() throws Exception { 

		// As of BIRT 2.2, the query text contains the SQL-like syntax with extra column information
		// "select columns from table : { extra column info }
		String fullQueryText = "select \"Patient Id\", \"ART Register Number\", \"Birthdate\", \"Age\", \"Health Center\", \"First Encounter Date\", \"last Encounter Date\", \"Current ARVs\", \"Earliest ARV start\", \"HIV Program enrollment date\", \"ANTIRETROVIRAL TREATMENT STATUS\", \"ANTIRETROVIRAL TREATMENT STATUS_obsDatetime\", \"BASELINE CD4\", \"BASELINE CD4_obsDatetime\", \"CD4 COUNT\", \"CD4 COUNT_obsDatetime\", \"CD4 COUNT_(1)\", \"CD4 COUNT_obsDatetime_(1)\", \"CD4 COUNT_(2)\", \"CD4 COUNT_obsDatetime_(2)\", \"WEIGHT (KG)\", \"PREGNANCY STATUS\", \"PREGNANCY STATUS_obsDatetime\", \"TRANSFER IN FROM\", \"TRANSFER IN FROM_obsDatetime\", \"MEDICATIONS DISPENSED\", \"MEDICATIONS DISPENSED_obsDatetime\", \"MEDICATIONS DISPENSED_(1)\", \"MEDICATIONS DISPENSED_obsDatetime_(1)\", \"MEDICATIONS DISPENSED_(2)\", \"MEDICATIONS DISPENSED_obsDatetime_(2)\", \"TREATMENT STATUS\", \"TREATMENT STATUS_obsDatetime\", \"Deceased\", \"HIV Program\", \"Gender\" from HAHPCO_Dataset_en_us-16-Jul-2008-145234.csv : {\"Patient Id\",\"Patient Id\",STRING;\"ART Register Number\",\"ART Register Number\",STRING;\"Birthdate\",\"Birthdate\",DATE;\"Age\",\"Age\",INT;\"Health Center\",\"Health Center\",STRING;\"First Encounter Date\",\"First Encounter Date\",DATE;\"last Encounter Date\",\"last Encounter Date\",DATE;\"Current ARVs\",\"Current ARVs\",STRING;\"Earliest ARV start\",\"Earliest ARV start\",DATE;\"HIV Program enrollment date\",\"HIV Program enrollment date\",DATE;\"ANTIRETROVIRAL TREATMENT STATUS\",\"ANTIRETROVIRAL TREATMENT STATUS\",STRING;\"ANTIRETROVIRAL TREATMENT STATUS_obsDatetime\",\"ANTIRETROVIRAL TREATMENT STATUS_obsDatetime\",DATE;\"BASELINE CD4\",\"BASELINE CD4\",STRING;\"BASELINE CD4_obsDatetime\",\"BASELINE CD4_obsDatetime\",DATE;\"CD4 COUNT\",\"CD4 COUNT\",STRING;\"CD4 COUNT_obsDatetime\",\"CD4 COUNT_obsDatetime\",DATE;\"CD4 COUNT_(1)\",\"CD4 COUNT_(1)\",STRING;\"CD4 COUNT_obsDatetime_(1)\",\"CD4 COUNT_obsDatetime_(1)\",DATE;\"CD4 COUNT_(2)\",\"CD4 COUNT_(2)\",STRING;\"CD4 COUNT_obsDatetime_(2)\",\"CD4 COUNT_obsDatetime_(2)\",DATE;\"WEIGHT (KG)\",\"WEIGHT (KG)\",STRING;\"PREGNANCY STATUS\",\"PREGNANCY STATUS\",STRING;\"PREGNANCY STATUS_obsDatetime\",\"PREGNANCY STATUS_obsDatetime\",DATE;\"TRANSFER IN FROM\",\"TRANSFER IN FROM\",STRING;\"TRANSFER IN FROM_obsDatetime\",\"TRANSFER IN FROM_obsDatetime\",DATE;\"MEDICATIONS DISPENSED\",\"MEDICATIONS DISPENSED\",STRING;\"MEDICATIONS DISPENSED_obsDatetime\",\"MEDICATIONS DISPENSED_obsDatetime\",DATE;\"MEDICATIONS DISPENSED_(1)\",\"MEDICATIONS DISPENSED_(1)\",STRING;\"MEDICATIONS DISPENSED_obsDatetime_(1)\",\"MEDICATIONS DISPENSED_obsDatetime_(1)\",DATE;\"MEDICATIONS DISPENSED_(2)\",\"MEDICATIONS DISPENSED_(2)\",STRING;\"MEDICATIONS DISPENSED_obsDatetime_(2)\",\"MEDICATIONS DISPENSED_obsDatetime_(2)\",DATE;\"TREATMENT STATUS\",\"TREATMENT STATUS\",STRING;\"TREATMENT STATUS_obsDatetime\",\"TREATMENT STATUS_obsDatetime\",DATE;\"Deceased\",\"Deceased\",BOOLEAN;\"HIV Program\",\"HIV Program\",STRING;\"Gender\",\"Gender\",STRING}";
		
		// Before BIRT 2.2, the query text was simple SQL 
		// i.e. "select columns from table"
		String shortQueryText = "select \"Patient Id\", \"ART Register Number\", \"Birthdate\", \"Age\", \"Health Center\", \"First Encounter Date\", \"last Encounter Date\", \"Current ARVs\", \"Earliest ARV start\", \"HIV Program enrollment date\", \"ANTIRETROVIRAL TREATMENT STATUS\", \"ANTIRETROVIRAL TREATMENT STATUS_obsDatetime\", \"BASELINE CD4\", \"BASELINE CD4_obsDatetime\", \"CD4 COUNT\", \"CD4 COUNT_obsDatetime\", \"CD4 COUNT_(1)\", \"CD4 COUNT_obsDatetime_(1)\", \"CD4 COUNT_(2)\", \"CD4 COUNT_obsDatetime_(2)\", \"WEIGHT (KG)\", \"PREGNANCY STATUS\", \"PREGNANCY STATUS_obsDatetime\", \"TRANSFER IN FROM\", \"TRANSFER IN FROM_obsDatetime\", \"MEDICATIONS DISPENSED\", \"MEDICATIONS DISPENSED_obsDatetime\", \"MEDICATIONS DISPENSED_(1)\", \"MEDICATIONS DISPENSED_obsDatetime_(1)\", \"MEDICATIONS DISPENSED_(2)\", \"MEDICATIONS DISPENSED_obsDatetime_(2)\", \"TREATMENT STATUS\", \"TREATMENT STATUS_obsDatetime\", \"Deceased\", \"HIV Program\", \"Gender\" from HAHPCO_Dataset_en_us-16-Jul-2008-145234.csv";

		
		String expectedColumnsInfo = "\"Patient Id\",\"Patient Id\",STRING;\"ART Register Number\",\"ART Register Number\",STRING;\"Birthdate\",\"Birthdate\",DATE;\"Age\",\"Age\",INT;\"Health Center\",\"Health Center\",STRING;\"First Encounter Date\",\"First Encounter Date\",DATE;\"last Encounter Date\",\"last Encounter Date\",DATE;\"Current ARVs\",\"Current ARVs\",STRING;\"Earliest ARV start\",\"Earliest ARV start\",DATE;\"HIV Program enrollment date\",\"HIV Program enrollment date\",DATE;\"ANTIRETROVIRAL TREATMENT STATUS\",\"ANTIRETROVIRAL TREATMENT STATUS\",STRING;\"ANTIRETROVIRAL TREATMENT STATUS_obsDatetime\",\"ANTIRETROVIRAL TREATMENT STATUS_obsDatetime\",DATE;\"BASELINE CD4\",\"BASELINE CD4\",STRING;\"BASELINE CD4_obsDatetime\",\"BASELINE CD4_obsDatetime\",DATE;\"CD4 COUNT\",\"CD4 COUNT\",STRING;\"CD4 COUNT_obsDatetime\",\"CD4 COUNT_obsDatetime\",DATE;\"CD4 COUNT_(1)\",\"CD4 COUNT_(1)\",STRING;\"CD4 COUNT_obsDatetime_(1)\",\"CD4 COUNT_obsDatetime_(1)\",DATE;\"CD4 COUNT_(2)\",\"CD4 COUNT_(2)\",STRING;\"CD4 COUNT_obsDatetime_(2)\",\"CD4 COUNT_obsDatetime_(2)\",DATE;\"WEIGHT (KG)\",\"WEIGHT (KG)\",STRING;\"PREGNANCY STATUS\",\"PREGNANCY STATUS\",STRING;\"PREGNANCY STATUS_obsDatetime\",\"PREGNANCY STATUS_obsDatetime\",DATE;\"TRANSFER IN FROM\",\"TRANSFER IN FROM\",STRING;\"TRANSFER IN FROM_obsDatetime\",\"TRANSFER IN FROM_obsDatetime\",DATE;\"MEDICATIONS DISPENSED\",\"MEDICATIONS DISPENSED\",STRING;\"MEDICATIONS DISPENSED_obsDatetime\",\"MEDICATIONS DISPENSED_obsDatetime\",DATE;\"MEDICATIONS DISPENSED_(1)\",\"MEDICATIONS DISPENSED_(1)\",STRING;\"MEDICATIONS DISPENSED_obsDatetime_(1)\",\"MEDICATIONS DISPENSED_obsDatetime_(1)\",DATE;\"MEDICATIONS DISPENSED_(2)\",\"MEDICATIONS DISPENSED_(2)\",STRING;\"MEDICATIONS DISPENSED_obsDatetime_(2)\",\"MEDICATIONS DISPENSED_obsDatetime_(2)\",DATE;\"TREATMENT STATUS\",\"TREATMENT STATUS\",STRING;\"TREATMENT STATUS_obsDatetime\",\"TREATMENT STATUS_obsDatetime\",DATE;\"Deceased\",\"Deceased\",BOOLEAN;\"HIV Program\",\"HIV Program\",STRING;\"Gender\",\"Gender\",STRING;";
		String expectedQuery = "select \"Patient Id\", \"ART Register Number\", \"Birthdate\", \"Age\", \"Health Center\", \"First Encounter Date\", \"last Encounter Date\", \"Current ARVs\", \"Earliest ARV start\", \"HIV Program enrollment date\", \"ANTIRETROVIRAL TREATMENT STATUS\", \"ANTIRETROVIRAL TREATMENT STATUS_obsDatetime\", \"BASELINE CD4\", \"BASELINE CD4_obsDatetime\", \"CD4 COUNT\", \"CD4 COUNT_obsDatetime\", \"CD4 COUNT_(1)\", \"CD4 COUNT_obsDatetime_(1)\", \"CD4 COUNT_(2)\", \"CD4 COUNT_obsDatetime_(2)\", \"WEIGHT (KG)\", \"PREGNANCY STATUS\", \"PREGNANCY STATUS_obsDatetime\", \"TRANSFER IN FROM\", \"TRANSFER IN FROM_obsDatetime\", \"MEDICATIONS DISPENSED\", \"MEDICATIONS DISPENSED_obsDatetime\", \"MEDICATIONS DISPENSED_(1)\", \"MEDICATIONS DISPENSED_obsDatetime_(1)\", \"MEDICATIONS DISPENSED_(2)\", \"MEDICATIONS DISPENSED_obsDatetime_(2)\", \"TREATMENT STATUS\", \"TREATMENT STATUS_obsDatetime\", \"Deceased\", \"HIV Program\", \"Gender\" from HAHPCO_Dataset_en_us-16-Jul-2008-145234.csv";
		String expectedColumns = "\"Patient Id\", \"ART Register Number\", \"Birthdate\", \"Age\", \"Health Center\", \"First Encounter Date\", \"last Encounter Date\", \"Current ARVs\", \"Earliest ARV start\", \"HIV Program enrollment date\", \"ANTIRETROVIRAL TREATMENT STATUS\", \"ANTIRETROVIRAL TREATMENT STATUS_obsDatetime\", \"BASELINE CD4\", \"BASELINE CD4_obsDatetime\", \"CD4 COUNT\", \"CD4 COUNT_obsDatetime\", \"CD4 COUNT_(1)\", \"CD4 COUNT_obsDatetime_(1)\", \"CD4 COUNT_(2)\", \"CD4 COUNT_obsDatetime_(2)\", \"WEIGHT (KG)\", \"PREGNANCY STATUS\", \"PREGNANCY STATUS_obsDatetime\", \"TRANSFER IN FROM\", \"TRANSFER IN FROM_obsDatetime\", \"MEDICATIONS DISPENSED\", \"MEDICATIONS DISPENSED_obsDatetime\", \"MEDICATIONS DISPENSED_(1)\", \"MEDICATIONS DISPENSED_obsDatetime_(1)\", \"MEDICATIONS DISPENSED_(2)\", \"MEDICATIONS DISPENSED_obsDatetime_(2)\", \"TREATMENT STATUS\", \"TREATMENT STATUS_obsDatetime\", \"Deceased\", \"HIV Program\", \"Gender\"";
		String expectedTable = "HAHPCO_Dataset_en_us-16-Jul-2008-145234.csv";
		
		// Test the full syntax
		assert(BirtQueryUtil.getColumnsInfo(fullQueryText).equals(expectedColumnsInfo));
		assert(BirtQueryUtil.getQuery(fullQueryText).equals(expectedQuery));
		assert(BirtQueryUtil.getColumns(fullQueryText).equals(expectedColumns));		
		assert(BirtQueryUtil.getTable(fullQueryText).equals(expectedTable));
		
		// Test the short syntax
		assert(BirtQueryUtil.getQuery(shortQueryText).equals(expectedQuery));
		assert(BirtQueryUtil.getColumns(shortQueryText).equals(expectedColumns));		
		assert(BirtQueryUtil.getTable(shortQueryText).equals(expectedTable));
		
		// Changing the query 
		BirtDataSetQuery datasetQuery = new BirtDataSetQuery(fullQueryText);					
		datasetQuery.setTable(expectedTable + "_changed");	
		
		log.info("New Query: " + datasetQuery.getQueryText());
		

	}
	
	
	
	/**
	 * Generate a report with an embedded image.
	 * FIXME This test case needs to be re-written.
	 */
	@Ignore
	public void shouldGenerateImageTestReport() throws Exception { 
		
		String reportDesign = 
			"c:/Documents and Settings/Justin Miranda/My Documents/My Workspace/BirtReportModule/test/" + 
			"org/openmrs/module/birt/include/ImageTest.rptdesign";
		
		File file = new File(reportDesign);		
		if (file.exists()) 
			generateReport(file.getAbsolutePath(), "pdf");
		
		else { 
			throw new FileNotFoundException("Unable to find '" + reportDesign + "'!");
		}
	}
	
	
	/**
	 * Convenience method to help find a report design on the file system.
	 * @param reportDesign
	 * @return
	 * @throws Exception
	 */
	@Ignore
	public File getReportDesign(String reportDesign) throws Exception { 
		File file = new File(reportDesign);
		
		log.info("File: " + file);
		
		if (!file.exists()) {
			
			URL url = getClass().getClassLoader().getResource(reportDesign);		
			
			FileUtils.copyURLToFile(url, file);
			
			log.info("File: " + file.getAbsolutePath());
						
			if (!file.exists())
				throw new FileNotFoundException("Unable to find '" + reportDesign + "' in the classpath");
		}
		
		return file;
	}
	
	
	/**
	 * Generates report output using the given report design path and an output format.
	 * 
	 * @param reportPath
	 * @param format
	 * @throws Exception
	 */
	public void generateReport(String reportPath, String format) throws Exception { 
		
		IRunAndRenderTask task = null;
		try { 			 			

			// Create a BIRT report object
			BirtReport report = new BirtReport();
			
			File file = new File(reportPath);
			ReportDefinition reportDefinition = new ReportDefinition();
			reportDefinition.setName("ImageTest");
			report.setReportDefinition(reportDefinition);
			report.setReportDesignPath(file.getAbsolutePath());
			report.setOutputFormat(format);
			report.setOutputFilename(file.getName() + "." + format);
						
			BirtReportService service = new BirtReportServiceImpl();
			service.generateReport(report);
			
			
			File outputFile = new File(report.getOutputFilename());
			
			log.info("Output File: " + outputFile.getAbsolutePath());
			
			if (task != null) { 
				for(Object error : task.getErrors()) { 
					Exception exception = (Exception) error;
					System.out.println("BIRT ERROR: " + error + "\nclass=" + error.getClass() + "\n");
					System.out.println("Exception: " + exception.getMessage() + "\nStacktrace=");				
					exception.printStackTrace(System.out);
		
				}
			}
			
		} catch (Exception e) { 
			if (task != null ) {
				System.out.println("Validation Errors: " + task.getErrors());
			}
			e.printStackTrace(System.out);
		}
		
	}
			
	
	
	
	
	
	
	 
	/**
	 * Retrieves and evaluates all cohort definitions.
	 * FIXME Needs to be rewritten as an actual test case. 
	 * @throws Exception
	 */
	@Test
	public void shouldGetAllCohorts() throws Exception { 
		
		List<CohortDefinitionItemHolder> cohorts = 
			Context.getCohortService().getAllCohortDefinitions();
		
		for(CohortDefinitionItemHolder item : cohorts) { 
			try { 			
				CohortDefinition definition = 
					Context.getCohortService().getCohortDefinition(item.getKey());
				
				log.info("Cohort: " + item);
				Assert.assertNotNull(definition);
								
				Cohort cohort = Context.getCohortService().evaluate(definition, null);
				log.info("Cohort " + definition.getClass() + " has " + cohort.getSize() + " patients ");
				
				
			} catch (Exception e) { 
				throw e;
			}
			
		}
	}
		
	/**
	 * Test the Duplicate Report use case.
	 * @throws Exception
	 */
	@Test
	public void testDuplicateReport() throws Exception { 
		
		String oldPath = 
			"webapps/openmrs/WEB-INF/view/module/birt/resources/default.rptdesign";
			
		Resource resource = new FileSystemResource(oldPath);

		
		BirtReportService service = (BirtReportService)
			Context.getService(BirtReportService.class);
		
		String newPath = BirtConstants.REPORT_DIR + File.separator + "100.rptdesign";
		
		System.out.println("Copy old report design " + resource.getFilename());
		service.duplicateReportDesign(resource.getFilename(), newPath);
		
		
	}
	
	/**
	 * Create and delete a report definition object. 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateAndDeleteReportDefinition() throws Exception { 
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("JUnit Test Report");
		Context.getReportObjectService().createReportObject(reportDefinition);
	
		log.info("Created Report: " + reportDefinition.getReportObjectId() + " " + reportDefinition.getName());
		Assert.assertNotNull(reportDefinition.getReportElements());
		Integer reportToDelete = reportDefinition.getReportObjectId();
		
		// Delete the report
		Context.getReportObjectService().deleteReport(reportDefinition);
		
		reportDefinition = (ReportDefinition) Context.getReportObjectService().getReportObject(reportToDelete);	
		log.info("Report: " + reportDefinition);
		
		// assert that the report does not exist
		// assertNull(reportDefinition);

		
	}
	
	
	
	
	/**
	 * 
	 */
	@Test
	public void testGetReportDesign() { 
		
		try { 

			BirtReportService service = new BirtReportServiceImpl();
			BirtReport report = service.getReport(75);
			service.compareDatasets(report);
			
		} catch (Exception e) { 
			e.printStackTrace(System.out);
		}
		
	}
	
	@Test
	public void testFillReportParameters() { 

		try { 

			BirtReportService service = new BirtReportServiceImpl();
			
			BirtReport report = service.getReport(65);
			
			
			log.info("Filling report parameters for " + report.getReportDefinition().getName());
			log.info("Report " + report);
			service.fillReportParameters(report);
			

		 	
			//Iterator iter = parameter.keySet().iterator();
		 	//System.out.println("===== Parameter '" + scalar.getName() + "' =====");
		 	//while (iter.hasNext()) {
		 	//	String name = (String) iter.next(); 
		 	//	if( name.equals("Selection List")){
		 	//		HashMap selList = (HashMap)parameter.get(name);
		 	//		Iterator selIter = selList.keySet().iterator();
		 	//		while (selIter.hasNext()) {
		 	//			Object lbl = selIter.next();
		 	//			System.out.println( "Selection List Entry ===== Key = " + lbl + " Value = " + selList.get(lbl));
		 	//		}
			//
		 	//	} else{
		 	//		System.out.println( name + " = " + parameter.get(name));     
		 	//	}
		 	//}
			
		} catch (Exception e) { 			
			log.warn("Could not fill report parameters due to the following error: " + e.getMessage());
		}
	}	
	
	
	@Test
	public void testParseParameters() throws Exception { 		

		BirtReportService service = new BirtReportServiceImpl();
		Object value = null;
		
		// Number test cases
		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_DECIMAL, "9.99999");
		assert(value instanceof Number && value.equals(new Float(9.99999)));
		
		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_FLOAT, "9.999999");
		assert(value instanceof Number && value.equals(new Float(9.99999)));

		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_INTEGER, "9.9");
		assert(value instanceof Number && value.equals(new Float(9.99999)));

		// SQL Date test case
		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_DATE, "09/08/2007");
		assert(value instanceof java.sql.Date);

		// Time test cases
		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_TIME, "6:45:32 AM");
		assert(value instanceof java.sql.Time);
		
		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_TIME, "6:45:32 PM");
		assert(value instanceof java.sql.Time);

		// Java Date test cases
		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_DATETIME, "09/09/2007 6:45:32 AM");
		assert(value instanceof java.util.Date);
		
		// String test cases
		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_STRING, "9.9");
		assert(value instanceof java.lang.String);

		// Boolean test cases (everything except "true" should return false)
		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_BOOLEAN, "y");
		assert(value instanceof java.lang.Boolean && value.equals(Boolean.TRUE));

		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_BOOLEAN, "n");
		assert(value instanceof java.lang.Boolean && value.equals(Boolean.FALSE));
		
		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_BOOLEAN, "yes");
		assert(value instanceof java.lang.Boolean && value.equals(Boolean.TRUE));

		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_BOOLEAN, "no");
		assert(value instanceof java.lang.Boolean && value.equals(Boolean.FALSE));

		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_BOOLEAN, "true");
		assert(value instanceof java.lang.Boolean && value.equals(Boolean.TRUE));

		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_BOOLEAN, "false");
		assert(value instanceof java.lang.Boolean && value.equals(Boolean.FALSE));

		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_BOOLEAN, "t");
		assert(value instanceof java.lang.Boolean && value.equals(Boolean.TRUE));

		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_BOOLEAN, "f");
		assert(value instanceof java.lang.Boolean && value.equals(Boolean.FALSE));
		
	}

	

	

	@Test
	public void testValidateParameters() throws Exception { 
		
		IRunAndRenderTask task = null;
		try { 
			
			 			
			BirtReportService service = new BirtReportServiceImpl();
			BirtReport report = service.getReport(56);
			
			
			System.out.println("Report " + report.getReportDefinition().getName());			
			Map<String,Object> parameterValues = report.getParameterValues();

			for (ParameterDefinition parameter : report.getParameters()) { 
				Object value = parameterValues.get(parameter.getName());
				BirtReportUtil.parseParameterValue(parameter.getDataType(), String.valueOf(value));
				log.info(parameter.getName());
			}
			
			System.out.println("Report parameters ... ");
			for (String name : parameterValues.keySet()) { 
				Date dateValue = new Date();
				parameterValues.put(name, dateValue);
			}	
			
			for (String name : parameterValues.keySet()) { 
				Object value = parameterValues.get(name);	
				System.out.println("Parameter: " + name + " [" + value.getClass() + "] = " + value);
				System.out.println("instanceof Date? " + (value instanceof Date));
				System.out.println("instanceof String? " + (value instanceof String));
				System.out.println("instanceof Integer? " + (value instanceof Integer));
				System.out.println("instanceof Long? " + (value instanceof Long));
			}	
			System.out.println("Generate report ... ");

			//Open the report design
			IReportRunnable runnable = reportEngine.openReportDesign(report.getReportDesignPath()); 
			
			//Create task to run and render the report,
			task = reportEngine.createRunAndRenderTask(runnable); 	

			task.setParameterValues(parameterValues);
			task.validateParameters();	 
			IRenderOption render = BirtConfiguration.getRenderOption(report);
			render.setOutputFileName("c:/temp/report.pdf");
			task.setRenderOption(render);
			task.run();
			task.close();
			
			for(Object error : task.getErrors()) { 
				Exception exception = (Exception) error;
				System.out.println("BIRT ERROR: " + error + "\nclass=" + error.getClass() + "\n");
				System.out.println("Exception: " + exception.getMessage() + "\nStacktrace=");				
				exception.printStackTrace(System.out);
			}
			
			
		} catch (Exception e) { 

			if (task != null ) System.out.println("Validation Errors: " + task.getErrors());
			e.printStackTrace(System.out);
		}
		
	}
	
	
	@Test
	public void testReportDesignPath() throws Exception { 
		
		String path = BirtReportUtil.getReportDesignPath("test");
		System.out.println("Path to test design file " + path);
	}
	
	
	/**
	 * Generates a report.
	 * @param report
	 * @throws EngineException
	public void testGeneratePdfReport() throws Exception {
		startup();

		
		BirtReportService service = new BirtReportServiceImpl();
		
		BirtReport report = service.getReport(56);
		
		System.out.println("Filling report parameters for " + report.getReportDefinition().getName());
		System.out.println("Report " + report);
		
		String format = "pdf";
		//String report = BirtReportConstants.PROPERTY_TEST_REPORT_DESIGN_PATH;
		try { 
			
			
			logger.info("Test generate report " + report);
			Map<String, Object> parameters = report.getParameterValues();
			
			for (String paramName : parameters.keySet()) { 
				Object paramValue = parameters.get(paramName);
				System.out.println("Parameter " + paramName + " " + paramValue);
			}
		
			//Open the report design
			IReportRunnable runnable = reportEngine.openReportDesign(report.getReportDesignPath()); 
			//IReportDocument document = reportEngine.openReportDocument(report);
			
			//Create task to run and render the report,
			IRunAndRenderTask task = reportEngine.createRunAndRenderTask(runnable); 	
			task.setParameterValues(parameters);
			task.validateParameters();	 
			task.setRenderOption(BirtConfiguration.getRenderOption(format));
			task.run();
			task.close();

			//shutdown();
			logger.info("Finished generating pdf report for " + runnable.getReportName() );	

		} catch (Exception e) { 
    		log.error("An error occurred while generating pdf report ", e);
    		fail();
    	}		
		//shutdown();	
	}
	 */

	/**
	 * Generate the given report.
	 * 
	 * @param	report	the report to generate
	public void testGenerateHtmlReport() {
		startup();
		
		String format = "html";
		String report = BirtReportConstants.PROPERTY_TEST_REPORT_DESIGN_PATH;
    	logger.info("Generating output for report " + report);
    	try {     		
			
    		// Set parameters
    		HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("StringParameter1", new String("new string"));
			parameters.put("DateTimeParameter1", new Date());		

			
			//Open the report design
			IReportRunnable design = reportEngine.openReportDesign(report); 
	
			//Create task to run and render the report,
			IRunAndRenderTask task = reportEngine.createRunAndRenderTask(design); 	
			task.setParameterValues(parameters);
			task.validateParameters();	 
			task.setRenderOption( BirtConfiguration.getRenderOption(format));
		 
			//run the report and destroy the engine
			task.run();
			task.close();

			logger.info("Finished generating html report for " + design.getReportName() );	
			//shutdown();    		
	    	
    	} 
    	catch (Exception e) { 
			logger.log(Level.SEVERE, "An error occurred while generating an html report ", e);
    		fail();
    	}		
		//shutdown();
	}
	 */
	
	/**
	 * Generates a report.
	 * @param report
	 * @throws EngineException
	public void testGenerateXlsReport() throws Exception {
		startup();

		String format = "xls";
		String report = BirtReportConstants.PROPERTY_TEST_REPORT_DESIGN_PATH;
		try { 
			logger.info("Test generate report " + report);
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("StringParameter1", new String("new string"));
			parameters.put("DateTimeParameter1",  new Date());
		
			//Open the report design
			IReportRunnable runnable = reportEngine.openReportDesign(report); 
			
			//Create task to run and render the report,
			IRunAndRenderTask task = reportEngine.createRunAndRenderTask(runnable); 	
			task.setParameterValues(parameters);
			task.validateParameters();	 
			task.setRenderOption(BirtConfiguration.getRenderOption(format));
			task.run();
			task.close();

			//shutdown();
			logger.info("Finished generating pdf report for " + runnable.getReportName() );	

		} catch (Exception e) { 
    		log.error("An error occurred while generating pdf report ", e);
    		fail();
    	}		
		//shutdown();	
	}
	 */
	
	
    /**
     * Display information about the given report.
     *
	public void testGetReportInfo() { 
		startup();
		
		String report = BirtReportConstants.PROPERTY_TEST_REPORT_DESIGN_PATH;
		
		try { 
			
			ReportDesignHandle handle = openReportDesign(report);	
			for(Object obj : handle.getAllDataSets()) { 
				logger.info("" + obj.getClass());
				OdaDataSetHandle dsHandle = (OdaDataSetHandle) obj;
				
				logger.info("Dataset Query: " + dsHandle.getQueryText());
						
				for (Iterator iter = dsHandle.getDataSource().getPropertyIterator(); iter.hasNext();) { 
					PropertyHandle obj2 = (PropertyHandle) iter.next();
					logger.info("Data source property: " + obj2 + ", " + obj2.getClass().getName());
					logger.info("Property definition: " + obj2.getDefn().getName() + " [" + obj2.getDefn().getClass() + "]");
					logger.info("Old value: " + obj2.getDisplayValue());
					//obj2.setStringValue("test");
					logger.info("New value: " + obj2.getDisplayValue());
					
				}
			
				logger.info("Data source home: " + dsHandle.getDataSource().getProperty("HOME"));
				
				BirtDataSetQuery query = new BirtDataSetQuery(dsHandle.getQueryText());
				query.setDataset("SomeOtherFile");
				query.getQueryText();
				
			}
			
		} catch(Exception e) { 
			logger.log(Level.SEVERE, "An error occurred while getting report info ", e);
    		logger.throwing(this.getClass().getName(), "testGetReportInfo", e);
			fail();
		}
		//shutdown();
	}
     */
	
	
	

	/**
     * Open an existing report
     * @param reportName
     * @return
     */
    public ReportDesignHandle openReportDesign(String reportPath) {
    	logger.log(Level.INFO, "Creating new rptdesign file " + reportPath);
    	ReportDesignHandle handle = null;
    	try {     		
    		handle = designEngine.newSessionHandle(ULocale.ENGLISH).openDesign(reportPath);
    	}
    	catch (DesignFileException e) { 
    		logger.throwing(this.getClass().getName(), "openReportDesign", e);
    	}
    	return handle;
    }	
				

}
	

