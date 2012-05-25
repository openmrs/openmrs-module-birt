package org.openmrs.module.birt.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.model.api.DesignFileException;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.OdaDataSetHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.SessionHandle;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.openmrs.Cohort;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortDefinitionItemHolder;
import org.openmrs.module.birt.BirtConstants;
import org.openmrs.module.birt.BirtDataSetQuery;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.BirtReportException;
import org.openmrs.module.birt.BirtReportService;
import org.openmrs.module.birt.BirtReportUtil;
import org.openmrs.module.birt.db.BirtReportDAO;
import org.openmrs.module.birt.model.ParameterDefinition;
import org.openmrs.notification.MessageException;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.ReportObjectService;
import org.openmrs.reporting.data.DatasetDefinition;
import org.openmrs.reporting.export.DataExportReportObject;
import org.openmrs.reporting.export.DataExportUtil;
import org.openmrs.reporting.export.ExportColumn;
import org.openmrs.reporting.report.ReportDefinition;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.FileCopyUtils;

import com.ibm.icu.util.ULocale;


/**
 * Implementation of the Birt Report service.  This was originally going to implement 
 * org.openmrs.api.reporting.ReportService but the ReportService interface is for the 
 * reporting object framework. 
 * 
 * @author Justin Miranda
 */
public class BirtReportServiceImpl implements BirtReportService {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/** Reporting data access object */
	private BirtReportDAO reportDao;

	/** Instance of the BIRT reporting engine */
	private IReportEngine reportEngine;

	/** Instance of the BIRT design engine */
	private IDesignEngine designEngine;

	/**
	 * Public default constructor
	 */
	public BirtReportServiceImpl() { 
		initialize();
	}

	/**
	 * Set report service data access object.
	 * 
	 * @param dao
	 * 		report data access object	
	 * 
	 */
	public void setBirtReportDAO(BirtReportDAO dao) { 
		this.reportDao = dao;
	}

	/**
	 * Get report service data access object.
	 * 
	 * @return	report data access object 
	 * 
	 */
	public BirtReportDAO getBirtReportDAO() { 
		return this.reportDao;
	}


	/**
	 * Initialize the BIRT Runtime engine.
	 */
	public void initialize() {     	
		try { 			
			log.debug("Initializing the BIRT report engine");
			Platform.startup( BirtConfiguration.getEngineConfig());
			reportEngine = BirtConfiguration.getReportEngine();
			designEngine = BirtConfiguration.getDesignEngine();		
		} catch (Exception e) { 
			log.error("An error occurred while instantiating the report service", e);
		}    	
	}

	/**
	 * Convenience method
	 * @return	report object service.
	 */
	public ReportObjectService getReportObjectService() { 
		return Context.getReportObjectService();
	}


	/**
	 * @see org.openmrs.module.birt.BirtReportService#getReports()
	 */
	public List<BirtReport> getReports() { 
		return filterReports(BirtConstants.ALL_REPORTS);
	}



	/**
	 * @see org.openmrs.module.birt.BirtReportService#findReports(String)
	 */
	public List<BirtReport> filterReports(String searchTerm) { 

		List<BirtReport> reports = new Vector<BirtReport>();		

		if(searchTerm == null) { 
			searchTerm = BirtConstants.ALL_REPORTS;
		}

		// Iterate through the report definitions and wrap each with a BIRT report
		List<ReportDefinition> reportObjs = getReportDefinitions();
		for (AbstractReportObject obj : reportObjs) { 
			ReportDefinition reportDefinition = (ReportDefinition) obj;
			if (BirtConstants.ALL_REPORTS.equalsIgnoreCase(searchTerm.toLowerCase()) || 
					reportDefinition.getName().toLowerCase().contains(searchTerm.toLowerCase())) { 
				reports.add(getReportWithoutParameters(reportDefinition));
			}
		}
		sortByName(reports);

		return reports;
	}


	/**
	 * 
	 * @param reports
	 */
	public void sortByName(List<BirtReport> reports) {
		Collections.sort(reports, new Comparator<BirtReport>() {
			@SuppressWarnings("unchecked")
			public int compare(BirtReport left, BirtReport right) {
				Comparable l = (Comparable) left.getName();
				Comparable r = (Comparable) right.getName();
				return OpenmrsUtil.compareWithNullAsLowest(l, r);
			}
		});
	}



	/**
	 * Prepares the dataset for the report.
	 * 
	 * Assumption:  Only one data set allowed per report.  
	 * TODO 	Need to support multiple data sets per report
	 * 
	 * @param report	a BIRT report object
	 */
	public void prepareDataset(BirtReport report) { 
		try { 			
			// TODO export data to filesystem using information from handle
			// Create the latest dataset given the report's dataset definition and selected cohort
			// Change the dataset within the report
			ReportDesignHandle reportDesign = getReportDesign(report.getReportDesignPath());					
			if ( reportDesign != null ) { 

				for (Iterator iterator = reportDesign.getAllDataSets().iterator(); iterator.hasNext(); ) { 
					Object obj = iterator.next();


					if ( obj instanceof OdaDataSetHandle) { 
						OdaDataSetHandle datasetHandle = (OdaDataSetHandle) obj;					

						// Flat File data set (reset the HOME property) 
						if ("org.eclipse.datatools.connectivity.oda.flatfile.dataSet".equals(datasetHandle.getExtensionID())) { 
							log.debug("Setting the properties for the Flat File data set");

							if (!report.hasFlatfileDataSet()) {
								throw new BirtReportException("Report is missing the '" + datasetHandle.getName() + "' dataset.  Please update the report to include this dataset.");
							}

							log.debug("Export dataset for report " + datasetHandle.getName());							
							File dataset = exportFlatfileDataset(report);

							log.debug("Dataset " + datasetHandle.getExtensionID() + " = " + dataset.getParentFile().getAbsolutePath());

							// First we need to set the data source to the dataset's current directory
							datasetHandle.getDataSource().setProperty("HOME", dataset.getParentFile().getAbsolutePath());

							// TODO Refactor to use a better query parser ... 
							// this one does not handle more complex queries 
							log.debug("Data set query [BEFORE]:\n" + datasetHandle.getQueryText());

							// Create the query object and change the table name
							BirtDataSetQuery datasetQuery = new BirtDataSetQuery(datasetHandle.getQueryText());					
							datasetQuery.setTable(dataset.getName());

							datasetHandle.setQueryText(datasetQuery.getQueryText());
							log.debug("Data set query [AFTER]:\n" + datasetHandle.getQueryText());
						} 

						// JDBC data set (set username/password properties)
						else if ("org.eclipse.birt.report.data.oda.jdbc.JdbcSelectDataSet".equals(datasetHandle.getExtensionID())) { 							
							log.debug("Setting the JDBC properties for the jdbc data set");

							// #1976: By default, do not replace jdbc properties 
							// Admin needs to set global property 'birt.alwaysUseOpenmrsJdbcProperties'='true'
							// TODO We need to be able to support this at the individual report > data source level.  
							// In other words, a user should be able to specify this property for each JDBC data source in a report design.
							if (BirtConstants.ALWAYS_USE_OPENMRS_JDBC_PROPERTIES) { 

								// Get all of the required ODA connection properties
								String odaUser = Context.getRuntimeProperties().getProperty("connection.username");
								String odaPassword = Context.getRuntimeProperties().getProperty("connection.password");
								String odaURL = Context.getRuntimeProperties().getProperty("connection.url");
								
								// We should only override the ODA connection properties if all OpenMRS connection properties are non-null 
								if (odaUser != null && odaPassword != null && odaURL != null) {
									datasetHandle.getDataSource().setProperty("odaUser", odaUser);	
									datasetHandle.getDataSource().setProperty("odaPassword", odaPassword);								
									datasetHandle.getDataSource().setProperty("odaURL", odaURL);	
								}
							}
						}						
					}					
				}
				reportDesign.checkReport();
				reportDesign.save();			
			} else { 
				log.debug("Report design " + report.getReportDesignPath() + " does not exists");
			}			
		}
		catch (SemanticException e) { 		
			log.error("Error preparing data export ", e);
			throw new BirtReportException("Unable to prepare data export due to BIRT exception: " + e.getMessage(), e);
		}
		catch (IOException e) { 		
			log.error("Error preparing data export ", e);
			throw new BirtReportException("Unable to prepare data export due to file exception: " + e.getMessage(), e);
		}
	}

	/**
	 * Parses the given parameter value.
	 *  
	 * @param parameter
	 * 				
	 * @return
	public Object parseParameterValue(String paramType, String paramValue) throws BirtReportException {
		log.debug("Parsing parameter value " + paramValue + " as " + paramType);
		return BirtReportUtil.parseParameterValue(paramType, paramValue);

	}
	 */


	/**
	 * 
	 * 
	 * @return 
	 * @throws IOException
	 */
	public File getDefaultReportDesignFile() throws IOException {

		try { 

			// TODO needs to be refactored to allow for different webapp names
			String reportDesignPath 
			= "webapps/openmrs/WEB-INF/view/module/birt/resources/default.rptdesign";

			return new FileSystemResource(reportDesignPath).getFile();

		} 
		catch (Exception e) { 
			// An exception should be thrown, but should not 
			// cause the create report use case to fail
			log.error("Could not find default report design file");
			throw new BirtReportException("Could not locate default report design file");
		}
	}	



	/**
	 * Generate a report based on the attributes of the given report object.
	 * 
	 * @param	report	the report to generate
	 */
	public void generateReport(BirtReport report) {
		log.debug("Generating output for report " + report + ", hashcode " + report.hashCode());
		IRunAndRenderTask task = null;
		try {     		
			// Prepares the dataset for use within the BIRT engine 
			// (i.e. exports csv file, sets username/password for JDBC dataset)
			prepareDataset(report);

			// Get the report engine that will be used to render the report
			IReportEngine engine = BirtConfiguration.getReportEngine();	    		

			// Open the report design
			IReportRunnable reportRunnable =
				engine.openReportDesign(report.getReportDesignPath());

			// Create a report rendering task
			task = engine.createRunAndRenderTask(reportRunnable);
			task.setParameterValues(report.getParameterValues());
			task.setRenderOption(BirtConfiguration.getRenderOption(report));
			
			// Validate runtime parameters
			task.validateParameters();    
			
			// Render report design
			task.run();

			// Add errors to the report object
			if (task.getErrors() != null && !task.getErrors().isEmpty()) {
				report.setErrors(task.getErrors());
			}

			// Set the output file 
			report.setOutputFile(new File(report.getOutputFilename()));	    	

			log.debug("Output file: " + report.getOutputFile().getAbsolutePath());
		} 
		catch (EngineException e) { 
			log.error("Unable to generate report due to a BIRT Exception: " + e.getMessage(), e);
			throw new BirtReportException("Unable to generate report due to a BIRT Exception: " + e.getMessage(), e);
		}
		finally { 
			if (task != null) task.close();
		}
	}

	/**
	 * Preview the report.  This method is like generate report except we use default parameter 
	 * values and the default dataset.
	 * 
	 * @param	report	the report to preview
	 */
	public void previewReport(BirtReport report) {

		IRunAndRenderTask task = null;
		try { 
			// Fill the report parameters
			//fillReportParameters(report);			

			//Open the report design
			log.debug("Opening report design file to be generated: " + report.getReportDesignPath());
			IReportRunnable reportRunnable = reportEngine.openReportDesign(report.getReportDesignPath()); 

			//Create task to run and render the report
			task = reportEngine.createRunAndRenderTask(reportRunnable); 							
			//task.setAppContext( BirtConfiguration.getRenderContext() );
			//task.setParameterValues( report.getReportParameters() );
			task.validateParameters();
			task.setRenderOption( BirtConfiguration.getRenderOption(report) );
			task.run();			

			// TODO Need to pass the file name as input to set render option
			report.setOutputFile(new File(BirtConstants.REPORT_OUTPUT_FILE));	


		} 
		catch (Exception e) { 
			log.error("Unable to preview report due to a BIRT Exception: " + e.getMessage(), e);
			throw new BirtReportException("Unable to preview report due to a BIRT Exception: " + e.getMessage(), e);
		} 
		finally { 
			if (task != null) task.close();
		}
	}

	/**
	 * Get the parameters required for the report.
	 * 
	 * @param report
	 */
	public void fillReportParameters(BirtReport report) { 
		log.debug("Filling report parameters for " + report.getReportDefinition().getName());
		IGetParameterDefinitionTask task = null;

		try { 
		
			List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
			
			// Get the executable report  
			IReportRunnable reportRunnable = reportEngine.openReportDesign(report.getReportDesignPath()); 

			if (reportRunnable != null) {
				
				//Create task to get parameter definitions for report
				task = reportEngine.createGetParameterDefinitionTask(reportRunnable);

				Collection parameterDefns = task.getParameterDefns(true);   // IParameterDefnBase

				// Iterate through the parameters
				for(Object obj : parameterDefns) { 
					IParameterDefnBase param = (IParameterDefnBase) obj;

					log.debug("Found parameter " + param.getName() + " [" + param.getParameterType() + "] ");

					ParameterDefinition parameter = new ParameterDefinition();

					// Parameter Group
					if ( param instanceof IParameterGroupDefn ) {
						// Iterate over grouped parameters	   
						IParameterGroupDefn group = (IParameterGroupDefn) param;						
						Iterator iterator = group.getContents( ).iterator( );
						while ( iterator.hasNext( ) ) {
							IScalarParameterDefn scalar = (IScalarParameterDefn) iterator.next( );
							parameter = BirtReportUtil.getParameter(task, scalar, reportRunnable, group);							
							parameters.add(parameter);
						}
					}
					// Scalar Parameter
					else if (param instanceof IScalarParameterDefn) {
						IScalarParameterDefn scalar = (IScalarParameterDefn) param;
						parameter = BirtReportUtil.getParameter( task, scalar, reportRunnable, null);
						parameters.add(parameter);                   
					} 
					// Other types (not supported yet)
					else {						
						log.warn("Parameter type " + param.getClass().getName() + " not supported.");
						// Add a blank parameter ... 
						// TODO We should look into whether this actually happens
						parameters.add(parameter);
					}					
				}
			}			
			report.addParameters(parameters);

		} catch (EngineException e) { 			
			// Explicitly suppression exception because report designs do not always exist when this method
			// is called, thus BIRT might throw an exception here.
			log.warn("Unable to fill report with parameters due to a BIRT exception: " + e.getMessage());
			
		} finally { 
			if (task != null) task.close();
		}
	}




	/**
	 * Validates the report parameters.
	 * 
	 * @param report
	 */
	public void validateReportParameters(BirtReport report) { 
		// Add report parameter validation 
		return;
	}


	/**
	 * Gets the report with the given report identifier.
	 * 
	 * @param reportId
	 * @return
	 */
	public BirtReport getReport(Integer reportId) { 
		BirtReport report = null;
		try { 
			// Find the report object in the database
			ReportDefinition reportDefinition =  
				(ReportDefinition) getReportObjectService().getReportObject(reportId);

			report = getReport(reportDefinition);

		} catch (Exception e) { 
			throw new BirtReportException("Could not find report with id " + reportId, e);
		}
		return report;
	}    


	/**
	 * Get all report definitions from the database.
	 * @return
	 */
	public List<ReportDefinition> getReportDefinitions() { 
		List <ReportDefinition> reportDefinitions = new Vector<ReportDefinition>();
		List<AbstractReportObject> reportObjs = 
			getReportObjectService().getReportObjectsByType(ReportDefinition.TYPE_NAME);

		// Iterate through the report definitions and wrap each with a BIRT report
		for (AbstractReportObject obj : reportObjs) { 
			ReportDefinition reportDefinition = (ReportDefinition) obj;
			reportDefinitions.add(reportDefinition);    		
		}    	
		return reportDefinitions;
	}

	/**
	 * Get all report definitions from the database.
	 * 
	 * @return
	 */
	public List<ReportDefinition> getDatasetDefinitions() { 
		List <ReportDefinition> reportDefs = new Vector<ReportDefinition>();
		List<AbstractReportObject> reportObjs = 
			getReportObjectService().getReportObjectsByType(DatasetDefinition.TYPE_NAME);

		// Iterate through the report definitions and wrap each with a BIRT report
		for (AbstractReportObject obj : reportObjs) { 
			ReportDefinition reportDefinition = (ReportDefinition) obj;
			reportDefs.add(reportDefinition);    		
		}    	
		return reportDefs;
	}

	/**
	 * Downloads a report with the given identifier.
	 * 
	 * @param	reportId 		the report identifier
	 * @return	the report design file associated with the report
	public File downloadReport(Integer reportId) {
		String filename = getReport(reportId).getReportDesign().getFileName();		
		return new File(filename);
	}
	 */

	/**
	 * Gets the birt report associated with the given report definition, along 
	 * with the report design and report parameters.
	 * 
	 * @param reportDefinition	the report definition
	 * @return	a birt report object
	 */
	public BirtReport getReport(ReportDefinition reportDefinition) { 
		return getReport(reportDefinition, true);
	}
	
	/**
	 * Gets the birt report associated with the given report definition, minus
	 * the parameters for the report.
	 * 
	 * @param reportDefinition	the report definition
	 * @return	a birt report object
	 */
	public BirtReport getReportWithoutParameters(ReportDefinition reportDefinition) { 
		return getReport(reportDefinition, false);
	}

	
	
	/**
	 * Gets the birt report associated with the given report definition.  This method
	 * will return a BIRT report object that contains the BIRT report design along 
	 * with the report parameters that were defined by the user within the report design
	 * if the given includeParameters parameter is specified as true.
	 * 
	 * @param reportDefinition	the report definition
	 * @param includeParameters	if true, inspect the report design to get parameters 
	 * @return	a birt report object
	 */
	public BirtReport getReport(ReportDefinition reportDefinition, boolean includeParameters) { 

		BirtReport report = new BirtReport();
		try { 
			// Set the report definition
			report.setReportDefinition(reportDefinition);

			// Set report design file information 
			String reportId = reportDefinition.getReportObjectId().toString();
			String reportDesignPath = BirtReportUtil.getReportDesignPath(reportId);
			report.setReportDesignPath(reportDesignPath);

			// Find report design handle 
			ReportDesignHandle reportDesign = getReportDesign(reportDesignPath);
			report.setReportDesign(reportDesign);

			// TODO This should probably only be called for Generate Report use case. 
			// Fill the report parameters to a report
			if (includeParameters) 
				fillReportParameters(report);


		} catch (Exception e) { 
			throw new BirtReportException("Could not find report with id " + reportDefinition, e);
		}
		return report;
	}    



	/**
	 * Saves the report to the database and filesystem.
	 * 
	 * @param definition
	 */
	public void saveReport(BirtReport report) { 
		log.debug("Saving report " + report);
		if (report == null || report.getReportDefinition() == null) 
			throw new BirtReportException("Cannot create empty report");

		ReportDefinition reportDefinition = report.getReportDefinition();
		getReportObjectService().updateReportObject(reportDefinition);
		//saveReportDesign(report.getReportPath());

	}


	/**
	 * Deletes the report from the database.
	 * 
	 * @param	report	
	 */
	public void deleteReport(BirtReport report) { 
		try { 
			log.debug("Deleting report " + report);
			ReportDefinition reportDefinition = report.getReportDefinition();
			if (reportDefinition != null) { 
				log.debug("Deleting report definition " + reportDefinition);

				getReportObjectService().deleteReport(reportDefinition);
				deleteReportDesign(report.getReportDesignPath());
			}
		} 
		catch (Exception e ) { 
			log.warn("An error occurred while deleting report " + report.getReportDefinition().getName(), e);
		}
	}


	/**
	 * Convenience method used to compare data sets.
	 * 
	 * TODO Need to implement this to check columns from 
	 * data export against columns in 
	 * 
	 * @param	report	
	 */
	public void compareDatasets(BirtReport report) { 
		try { 
			List list = report.getReportDesign().getAllDataSets();


			// Get the current data export columns that are available from the report 
			List<ExportColumn> exportColumns = report.getReportDefinition().getDataExport().getColumns();

			// Iterate over report design datasets 
			Iterator iter = list.iterator();
			while (iter.hasNext()) {

				Object object = iter.next();

				if (object instanceof OdaDataSetHandle) { 
					OdaDataSetHandle datasetHandle = (OdaDataSetHandle) object;


					// Only compare if the dataset is a flatfile dataset
					if ("org.eclipse.datatools.connectivity.oda.flatfile.dataSet".equals(datasetHandle.getExtensionID())) { 

						BirtDataSetQuery query = new BirtDataSetQuery(datasetHandle.getQueryText());
						String [] datasetColumns = query.getColumnArray();

						// TODO need to compare datasetColumns with exportColumns


					}
				}



			}

		} catch (Exception e) { 
			log.warn("An error occurred while comparing datasets for report " + report.getReportDefinition().getName(), e);
		}    	
	}



	/**
	 * Saves a report design (either creates or overwrites an existing file)
	 * 
	 * TODO Need to rewrite this to save an existing report design if it exists
	 * since this method basically does nothing.
	 * 
	 * @param reportPath	path to the report design file to be saved
	 */
	public void saveReportDesign(String reportPath) { 
		try { 

			getReportDesign(reportPath).save();

		} catch (Exception e) { 
			throw new BirtReportException("Could not save report with name " + reportPath, e);
		}

	}




	/**
	 * Saves a report design (either creates or overwrites an existing file)
	 * 
	 * @param reportPath	path to the report design file to be saved
	 */
	public void deleteReportDesign(String reportPath) { 
		try { 
			new File(reportPath).delete();    		
		} catch (Exception e) { 
			log.warn("Could not delete file at path " + reportPath + ": " + e.getMessage());
		}	
	}


	/**
	 * Gets a report design file by opening an existing file or creating a new design file).
	 * 
	 * @param reportPath
	 * @return
	 */
	public ReportDesignHandle getReportDesign(String reportPath) { 
		ReportDesignHandle handle = null;
		try { 

			handle = openReportDesign(reportPath);	    	

			// Removed this functionality because it was sometimes overwriting report design files
			// that EXIST but were not found.  Not sure why/how the report design was not found.

			/*if (handle==null) { 
	    		log.debug("Could not open report design " + reportPath);
	    		handle = createReportDesign(reportPath);
	    	}*/

		} catch (Exception e) { 
			// TODO for debugging, re-throw exception
			log.warn("Unable to open report design at location " + reportPath);	 
			/* ignore for now since all reports start with no report design */
		}
		return handle;
	}

	/**
	 * Gets the actual report design file from the file system.
	 * 
	 * @param reportPath
	 * @return
	 */
	public File getReportDesignFile(String reportPath) {
		File file = null;
		try { 
			file = new File(reportPath);
		} 
		catch (Exception e) { 
			// Ignore
			log.warn("An error occurred while opening the file for report " + reportPath, e);
		}
		return file;		
	}

	/**
	 * Gets a report runnable instance that can be executed by the report engine.
	 * 
	 * @param 	reportPath	the path to the report
	 * @return	a runnable instance of a report 
	public IReportRunnable getReportRunnable(String reportPath) { 

		IReportRunnable reportRunnable = null;
		try { 

			reportRunnable = reportEngine.openReportDesign(reportPath); 

		} 
		catch (Exception e) { 
			log.debug("Could not locate report " + reportPath + ".  Please upload report design.");
		}

		return reportRunnable;
	}
	 */


	/**
	 * Create a new report design.
	 * 
	 * @return	a report design handle
	 */
	public ReportDesignHandle createReportDesign() { 
		ReportDesignHandle handle = null;
		try { 
			SessionHandle session = designEngine.newSessionHandle(ULocale.ENGLISH);

			handle = session.createDesign();
			//handle.saveAs();
		} 
		catch (Exception e) { 
			log.warn("Could not create report design: " + e.getMessage(), e);
		}
		return handle;
	}

	/**
	 * Creates a new named report.
	 * 
	 * @param reportPath
	 * 			path to the report design 
	 * @return	
	 * 			handle to the report design 
	 * 				
	 */
	public ReportDesignHandle createReportDesign(String reportPath) { 
		log.debug("Creating new rptdesign file " + reportPath );
		ReportDesignHandle handle = null;
		try { 
			handle = designEngine.newSessionHandle(ULocale.ENGLISH).createDesign(reportPath);
		} 
		catch (Exception e) { 
			log.warn("Could not create report design " + reportPath + ": " + e.getMessage());
		}
		return handle;
	}


	/**
	 * Copies a report design from an existing path to the new path.  Used for duplicating
	 * existing reports as well as creating a copy of the default report design.
	 * 
	 * @param oldPath
	 * @param newPath
    public void copyReportDesign(String oldPath, String newPath) { 
    	ReportDesignHandle handle = null;
    	try { 
	    	handle = openReportDesign(oldPath);
	    	handle.saveAs(newPath);
    	} 
    	catch (IOException e) { 
    		log.error("Could not copy report design " + oldPath + " to " + newPath);
    		throw new BirtReportException(e);    		
    	}
    }
	 */

	/**
	 * Open an existing report
	 * 
	 * @param reportPath	path to the report
	 * @return	a report design handle
	 */
	public ReportDesignHandle openReportDesign(String reportPath) {
		ReportDesignHandle handle = null;
		try {     		
			handle = designEngine.newSessionHandle(ULocale.ENGLISH).openDesign(reportPath);
		}
		catch (DesignFileException e) { 
			// Ignore for now
			log.debug("Could not open report design " + reportPath + ": " + e.getMessage());
			//throw new BirtReportException(e);
		}
		return handle;
	}

	/**
	 * Exports the dataset for the associated report to the file system.
	 * 
	 * @param report
	 * @return	a 
	 * @throws BirtReportException
	 */
	public File exportFlatfileDataset(BirtReport report) throws BirtReportException { 
		// TODO need to fix the birt report object to contain a single reference to cohort and data export objects
		DataExportReportObject export = report.getReportDefinition().getDataExport();
		Cohort cohort = report.getCohort();

		// TODO workaround until we fix the way we pass a set of entities to the data exporter
		if(export!=null) {
			export.setCohortId(cohort.getCohortId());
		}


		return exportFlatfileDataset(export);
	}



	/**
	 * Exports the data for a given dataset and cohort.
	 * 
	 * @param dataSetId
	 * @param patientSetId
	 * @return
	 * @throws BirtReportException
    public File exportFlatfileDataset(DataExportReportObject dataExport, Cohort cohort) throws BirtReportException { 

    	// TODO workaround until we fix the way we pass a set of entities to the data exporter
    	if(dataExport!=null) {
    		dataExport.setCohortId(cohort.getCohortId());
    	}

    	return exportFlatfileDataset(dataExport);
    }
	 */


	/**
	 * Exports a dataset based on the given export and patient set identifiers.
	 * 
	 * TODO Move this method to the OpenMRS API (i.e. DataExportService) so that other components can use it more easily.
	 * TODO Add support for more separators
	 * TODO Need to remove the copy of the export data to the report dataset directory by specifying output file to data export API.
	 * TODO Allow separator to be specified in the data export definition 
	 * 
	 * @param dataSetId
	 * @param patientSetId
	 * @return the absolute path to the file 
	 */
	public File exportFlatfileDataset(DataExportReportObject dataExport) throws BirtReportException { 
		File exportFile = null;

		if (dataExport != null) { 
			try { 	    		

				Cohort cohort = dataExport.generatePatientSet(null);	
				log.debug("Cohort '" + cohort.getName() + "' has " + (cohort != null ? cohort.getSize() : 0) + " patients");

				DataExportUtil.generateExport(dataExport, cohort, BirtConstants.COMMA_SEPARATOR, null);
				File tempExport = DataExportUtil.getGeneratedFile(dataExport);
				log.debug("Data export " + tempExport.getName() + " written to : " + tempExport.getAbsolutePath());

				exportFile = new File(BirtReportUtil.getDataExportPath(tempExport.getName()));
				log.debug("Data export to be used by report: " + exportFile.getAbsolutePath());

				// Copy temporary data export to final version of the data export
				FileCopyUtils.copy(new FileInputStream(tempExport), new FileOutputStream(exportFile));

			}
			catch (FileNotFoundException e) {
				log.warn("Error generating data export for data set" + dataExport, e);
				throw new BirtReportException("Data export file could not be found: " + e.getMessage(), e);			
			}
			catch (IOException e) {
				log.warn("Error generating data export for data set" + dataExport, e);
				throw new BirtReportException("Unable to generate export due to IO exception: " + e.getMessage(), e);
			}
			catch (Exception e) { 
				log.warn("Error generating data export for data set" + dataExport, e);
				throw new BirtReportException("Unable to generate export due to the following exception: " + e.getMessage(), e);				
			}
		}
		return exportFile;
	}

	/**
	 * Get a report with the given identifier.
	 * 
	 * @param reportId
	 */
	public BirtReport getReportForRender(Integer reportId) { 
		BirtReport report = null;
		try { 
			report = getReport(reportId);
			//fillReportParameters(report);
		} 
		catch (Exception e) { 
			throw new BirtReportException("Error getting report and parameters for " + reportId, e);
		}
		return report;
	}

	/**
	 * Get all cohorts from the database.
	 * 
	 * @return	a list of cohorts
	 */
	public List<CohortDefinitionItemHolder> getCohortDefinitions() {
		return Context.getCohortService().getAllCohortDefinitions();
	}

	/**
	 * Get data exports.
	 * 
	 * @return	a list of all data exports in the database
	 */
	public List<AbstractReportObject> getDataExports() {
		return Context.getReportObjectService().getReportObjectsByType(DataExportReportObject.TYPE_NAME);	    
	}

	/**
	 * Duplicate the 
	 * 
	 * TODO Needs to be tested 
	 * 
	 * @param oldReport
	 * @param newReport
	 * @throws IOException
	 * @throws DesignFileException
	 */
	public void duplicateReportDesign(String oldReport, String newReport) throws IOException { 
		log.debug("Duplicating report " + oldReport + " into " + newReport);
		ReportDesignHandle reportDesign = getReportDesign(oldReport);
		reportDesign.saveAs(newReport);		
	}

	/**
	 * Generates report output and sends it via email to users.
	 * @param report
	 * @throws MessageException
	 */
	public void generateAndEmailReport(BirtReport report) { 
		generateReport(report);
		emailReport(report);
	}	

	/**
	 * Emails a report 
	 * @param report
	 * @throws MessageException
	 */
	public void emailReport(BirtReport report) { 
		try { 
		
			if (report.getOutputFile() == null) 
				throw new BirtReportException("Unable to locate generated report");			
			
			String subject = report.getEmailProperties().get(BirtConstants.REPORT_EMAIL_SUBJECT);
			String body = report.getEmailProperties().get(BirtConstants.REPORT_EMAIL_BODY);	        
			String from = report.getEmailProperties().get(BirtConstants.REPORT_EMAIL_FROM);
			String to = report.getEmailProperties().get(BirtConstants.REPORT_EMAIL_TO); 

			
			// Set the report output filename and filepath
			String fileName = report.getOutputFile().getName();
			String filePath = report.getOutputFile().getAbsolutePath();

			// Define message
			Session session = getMailSession();
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));

			// Email to...
			String toEmails[] = to.split(",");
			for(int i=0; i<toEmails.length; i++) {
				message.addRecipient(RecipientType.TO, new InternetAddress(toEmails[i]));
			}
			// Email bcc to...
			//message.addRecipient(RecipientType.BCC, new InternetAddress(bcc));

			// Email subject
			message.setSubject(subject);

			// Create the multi-part
			Multipart multipart = new MimeMultipart();
			// Create text mimebody
			BodyPart messageBodyPart = new MimeBodyPart();
			// Fill the message
			messageBodyPart.setText(body);
			// Add the first part (text mimebody)
			multipart.addBodyPart(messageBodyPart);

			messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(filePath);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(fileName);

			// Create file attached mimebody
			//messageBodyPart = new MimeBodyPart(); 
			//URL url = new URL(filepath); //("http://localhost//test.doc");
			//DataSource source = new URLDataSource(url);
			//messageBodyPart.setDataHandler(new DataHandler(source));
			//messageBodyPart.setFileName(filename);

			// Add the second part (attached mimebody)
			multipart.addBodyPart(messageBodyPart);

			// Put parts in message
			message.setContent(multipart);

			Transport.send(message);

			log.debug("Sent message!");


		} catch (AddressException e) { 
			log.error(e);
		} catch (MessagingException e) {
			log.error(e);
		}

	}	


	/**
	 * Gets the mail session required by the mail message service. This function forces
	 * authentication via the getAdministrationService() method call
	 * 
	 * @return a java mail session
	 */
	private static javax.mail.Session getMailSession() {

		AdministrationService adminService = Context.getAdministrationService();

		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", adminService.getGlobalProperty("mail.transport_protocol"));
		props.setProperty("mail.smtp.host", adminService.getGlobalProperty("mail.smtp_host"));
		props.setProperty("mail.smtp.port", adminService.getGlobalProperty("mail.smtp_port"));
		props.setProperty("mail.from", adminService.getGlobalProperty("mail.from"));
		props.setProperty("mail.debug", adminService.getGlobalProperty("mail.debug"));

		// More advanced 
		Boolean smtpAuthEnabled = Boolean.valueOf(adminService.getGlobalProperty("mail.smtp_auth"));
		if (smtpAuthEnabled) { 
			props.setProperty("mail.smtp.auth", adminService.getGlobalProperty("mail.smtp_auth"));
			props.setProperty("mail.smtp.starttls.enable", adminService.getGlobalProperty("mail.smtp_starttls_enable"));  // true
			props.setProperty("mail.smtp.socketFactory.port", adminService.getGlobalProperty("mail.smtp_socketFactory_port")); // 465
			props.setProperty("mail.smtp.socketFactory.class", adminService.getGlobalProperty("mail.smtp_socketFactory_class")); // "javax.net.ssl.SSLSocketFactory"
			props.setProperty("mail.smtp.socketFactory.fallback", adminService.getGlobalProperty("mail.smtp_socketFactory_fallback")); // "false"
			props.setProperty("mail.smtp.quitwait", adminService.getGlobalProperty("mail.smtp_quitwait")); // "false"
		}

		Authenticator auth = new Authenticator() {			
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(
						Context.getAdministrationService().getGlobalProperty("mail.user"),
						Context.getAdministrationService().getGlobalProperty("mail.password"));
			}
		};

		return Session.getInstance(props, auth);
	}    








}
