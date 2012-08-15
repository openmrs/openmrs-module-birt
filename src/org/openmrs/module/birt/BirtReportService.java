package org.openmrs.module.birt;

import java.io.File;
import java.io.IOException;
import java.util.List;

//import org.openmrs.cohort.CohortDefinitionItemHolder;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
//import org.openmrs.cohort.CohortDefinitionItemHolder;
import org.openmrs.module.birt.db.BirtReportDAO;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.report.ReportDesign;
//import org.openmrs.reporting.AbstractReportObject;
//import org.openmrs.reporting.export.DataExportReportObject;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface BirtReportService {

	/**
	 * Sets the data access object for the report service.
	 * 
	 * @param 	dao		the report DAO
	 */
	public void setBirtReportDAO(BirtReportDAO dao);
	
	public void prepareDataset(BirtReport report);
	
	public void createReportDesign(BirtReport report);

	
	/**
	 * Gets the data access object used by the report service.
	 * 
	 * @return	the report DAO
	 */
	public BirtReportDAO getBirtReportDAO();
	
	public List<ReportDesign> filterReportDesigns(Integer reportId);
	
	public List<BirtReport> getAllBirtReports();
	
	public void getDatasets();
	
	public List<BirtReport> getAllDataSetDefinitions();
	
	//public BirtReport getBirtReport();
	
	/**
	 * Gets all reports in the system.
	 * @return	a list of reports in the database
	 */
	@Transactional(readOnly=true)
	public List<BirtReport> getReports();
	
	/**
	 * Gets all report designs in the system.
	 * @return	a list of report designs in the database
	 */
	@Transactional(readOnly=true)
	public List<ReportDesign> getReportDesigns();

    /**
     * Find reports whose name/title matches the given search term.
     * @param	searchTerm 	search term to match or "ALL" for all reports
     * @return	a list of birt report objects 
     */
	@Transactional(readOnly=true)
	public List<BirtReport> filterReports(String searchTerm);

	/**
	 * Gets a report
	 */
	@Transactional(readOnly=true)
	public BirtReport getReport(Integer id);
	
	/**
	 * Gets a report that will be used to render output.
	 */
	@Transactional(readOnly=true)
	public BirtReport getReportForRender(Integer id);
	
	/**
	 * Saves a report.
	 * 
	 * @param	report	the report to save
	 */
	public void saveReport(BirtReport report);

	/**
	 * Deletes a report.
	 * 
	 * @param	report	the report to delete
	 */
	public void deleteReport(BirtReport report);
	
	/**
	 * Uploads a report.
	 * 
	 * @param	report	the report to upload
	 */
	public void uploadReport(BirtReport report);
	
	
	/**
	 * Duplicates the given report design.
	 * 
	 * @param oldPath
	 * @param newPath
	 */
	public void duplicateReportDesign(String oldPath, String newPath) throws IOException;
	
	/**
	 * Generates a report. 
	 * 
	 * @param report	the report to generate
	 */
	public void generateReport(BirtReport report);
	
	/**
	 * Adds the report parameters to the report.
	 * 
	 * @param report
	 */
	public void fillReportParameters(BirtReport report);

	
	/**
	 * Validates the report parameters.
	 * 
	 * @param report
	 */
	public void validateReportParameters(BirtReport report);
	
	/**
	 * Parses and validates the given value.  
	 * 
	 * @param paramType
	 * @param paramValue
	 */
	//public Object parseParameterValue(String paramType, String paramValue) throws ParseException;
	
	/**
	 * Previews a report.
	 * 
	 * @param report	the report to preview
	 */
	public void previewReport(BirtReport report);
	
	/**
	 * Generates a CSV export for the given report.
	 * 
	 * @param 	report	the report that contains a definition of data to be exported
	 * @return	a file reference to the csv export file 
	 */
	public File exportFlatfileDataset(BirtReport report);
	
	/**
	 * Generates a CSV export for the given the export definition.
	 * 
	 * @param 	export	the export definition (columns)
	 * @param 	cohort	the cohort definition (rows)
	 * @return	a file reference to the csv export file 
	 */


/*	public File exportFlatfileDataset(DataExportReportObject export);*/

	
	/**
	 * Generates a CSV export for the given the export definition.
	 * 
	 * @param 	export	the export definition (columns)
	 * @param 	cohort	the cohort definition (rows)
	 * @return	a file reference to the csv export file 
	 */
	//public File exportFlatfileDataset(DataExportReportObject export, Cohort cohort);	
	
	/**
	 * Compares the dataset specified within the report design against the 
	 * dataset defined in the report object to determine whether the columns
	 * match. 
	 */
	public void compareDatasets(BirtReport report);	
		
	/**
	 * Downlaods a report from the filesystem.
	 * 
	 * This is equivalent to BirtReportService.getReport().getReportDesignFile().
	 * 
	 * @param 	id	the identifier of the report to download
	 * @return	the report design file (.rptdesign) associated with this report
	 */
	//public File downloadReport(Integer id);
	
	/**
	 * Get a list of all data exports in the database.
	 * 
	 * @return 	list of data export objects
	 */
/*	@Transactional(readOnly=true)
	public List<AbstractReportObject> getDataExports();*/
	
	/**
	 * Get a list of all cohorts in the database.
	 * 
	 * @return	cohorts
	 */

	/*	@Transactional(readOnly=true)
	public List<CohortDefinitionItemHolder> getCohortDefinitions();*/

	

	/**
	 * Generate and email report to users.
	 * @param report
	 */
	public void generateAndEmailReport(BirtReport report);

	/**
	 * Open an existing report
	 * 
	 * @param reportPath	path to the report
	 * @return	a report design handle
	 */
	public ReportDesignHandle openReportDesign(String reportPath);	
	/**
	 * Gets a report design file by opening an existing file or creating a new design file).
	 * 
	 * @param reportPath
	 * @return
	 */
	public ReportDesignHandle getReportDesign(String reportPath);
	
	

}