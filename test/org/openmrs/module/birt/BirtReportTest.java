package org.openmrs.module.birt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IParameterSelectionChoice;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.model.api.CascadingParameterGroupHandle;
import org.eclipse.birt.report.model.api.DesignFileException;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.ScalarParameterHandle;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;

import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortDefinition;
import org.openmrs.cohort.CohortDefinitionItemHolder;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportService;
import org.openmrs.module.birt.BirtReportUtil;
import org.openmrs.module.birt.impl.BirtReportServiceImpl;
import org.openmrs.module.birt.impl.BirtConfiguration;
import org.openmrs.module.birt.model.ParameterDefinition;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.report.ReportDefinition;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.ibm.icu.util.ULocale;

/**
 * Test BIRT Reporting capabilities.  
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

	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		super.onSetUpBeforeTransaction();
		authenticate();
	}	
	
	/* (non-Javadoc)
	 * @see org.springframework.test.AbstractTransactionalSpringContextTests#onTearDown()
	 */
	@Override
	public void onSetUp() throws Exception { 
		super.onSetUp();
		startupReportEngine();
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.test.AbstractTransactionalSpringContextTests#onTearDown()
	 */
	@Override
	protected void onTearDown() throws Exception {
		// TODO Auto-generated method stub
		super.onTearDown();
		shutdownReportEngine();
	}

	@Override
	public Boolean useInMemoryDatabase() {
		return false;
	}


	public BirtReportTest() { 
		setDependencyCheck(false);
	}

	/**
	 * Initializes all BIRT resources.
	 */
	public void startupReportEngine() {
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
	public void shutdownReportEngine() {
		logger.info("Shutting down BIRT report engine");	
		Platform.shutdown();
	}	
	
	
	
	
	
	
	
	public void testGenerateImageTestReport() throws Exception { 
		
		String reportDesign = 
			"c:/Documents and Settings/Justin Miranda/My Documents/My Workspace/BirtReportModule/test/" + 
			"org/openmrs/module/birt/include/ImageTest.rptdesign";
		
		File file = new File(reportDesign);

		
		log.info("File: " + file.getAbsolutePath());
		if (file.exists()) { 
			generateReport(file.getAbsolutePath(), "pdf");
		}
		else { 
			throw new FileNotFoundException("Unable to find '" + reportDesign + "'!");
		}
	}
	
	
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
	 * Gets all cohort definitions.
	 * @throws Exception
	 */
	public void testGetCohorts() throws Exception { 
		
		List<CohortDefinitionItemHolder> cohorts = 
			Context.getCohortService().getAllCohortDefinitions();
		
		for(CohortDefinitionItemHolder item : cohorts) { 
			try { 			
				CohortDefinition definition = 
					Context.getCohortService().getCohortDefinition(item.getKey());
				
				log.info("Cohort: " + item);
				assertNotNull(definition);
								
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
	public void testCreateAndDeleteReportDefinition() throws Exception { 
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("JUnit Test Report");
		Context.getReportObjectService().createReportObject(reportDefinition);
	
		log.info("Created Report: " + reportDefinition.getReportObjectId() + " " + reportDefinition.getName());
		assertNotNull(reportDefinition.getReportElements());
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
	public void testGetReportDesign() { 
		
		try { 

			BirtReportService service = new BirtReportServiceImpl();
			BirtReport report = service.getReport(75);
			service.compareDatasets(report);
			
		} catch (Exception e) { 
			e.printStackTrace(System.out);
		}
		
	}
	
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
	

