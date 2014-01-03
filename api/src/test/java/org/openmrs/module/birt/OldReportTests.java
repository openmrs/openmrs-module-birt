package org.openmrs.module.birt;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.birt.service.BirtReportService;
import org.openmrs.module.birt.service.BirtReportServiceImpl;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

/**
 * Test cases for BIRT Report Module capabilities
 */
@Ignore
public class OldReportTests extends BaseModuleContextSensitiveTest {

	private static Log log = LogFactory.getLog(OldReportTests.class);

	/**
	 * Public constructor
	 */
	public OldReportTests() {}

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
		assert(BirtDataSetQuery.getColumnsInfo(fullQueryText).equals(expectedColumnsInfo));
		assert(BirtDataSetQuery.getQuery(fullQueryText).equals(expectedQuery));
		assert(BirtDataSetQuery.getColumns(fullQueryText).equals(expectedColumns));
		assert(BirtDataSetQuery.getTable(fullQueryText).equals(expectedTable));

		// Test the short syntax
		assert(BirtDataSetQuery.getQuery(shortQueryText).equals(expectedQuery));
		assert(BirtDataSetQuery.getColumns(shortQueryText).equals(expectedColumns));
		assert(BirtDataSetQuery.getTable(shortQueryText).equals(expectedTable));

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

/*		IRunAndRenderTask task = null;
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
		*/
	}








	/**
	 * Retrieves and evaluates all cohort definitions.
	 * FIXME Needs to be rewritten as an actual test case.
	 * @throws Exception
	 */
	@Test
	public void shouldGetAllCohorts() throws Exception {

/*		List<CohortDefinitionItemHolder> cohorts =
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

		}*/
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
		/*
		TODO: MS commenting out for now
		String newPath = BirtConfiguration.REPORT_DIR + File.separator + "100.rptdesign";

		System.out.println("Copy old report design " + resource.getFilename());
		service.duplicateReportDesign(resource.getFilename(), newPath);
		*/

	}

	@Test
	public void testParseParameters() throws Exception {

		BirtReportService service = new BirtReportServiceImpl();
		Object value = null;

		/*
			// TODO: RE-add these tests for parameter checking using new code


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
		assert(value instanceof String);

		// Boolean test cases (everything except "true" should return false)
		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_BOOLEAN, "y");
		assert(value instanceof Boolean && value.equals(Boolean.TRUE));

		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_BOOLEAN, "n");
		assert(value instanceof Boolean && value.equals(Boolean.FALSE));

		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_BOOLEAN, "yes");
		assert(value instanceof Boolean && value.equals(Boolean.TRUE));

		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_BOOLEAN, "no");
		assert(value instanceof Boolean && value.equals(Boolean.FALSE));

		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_BOOLEAN, "true");
		assert(value instanceof Boolean && value.equals(Boolean.TRUE));

		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_BOOLEAN, "false");
		assert(value instanceof Boolean && value.equals(Boolean.FALSE));

		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_BOOLEAN, "t");
		assert(value instanceof Boolean && value.equals(Boolean.TRUE));

		value = BirtReportUtil.parseParameterValue(DesignChoiceConstants.PARAM_TYPE_BOOLEAN, "f");
		assert(value instanceof Boolean && value.equals(Boolean.FALSE));
		*/
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
}
