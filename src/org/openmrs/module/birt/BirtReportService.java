package org.openmrs.module.birt;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

//import org.openmrs.api.APIException;
import org.openmrs.Cohort;
import org.openmrs.cohort.CohortDefinitionItemHolder;
import org.openmrs.module.birt.model.ParameterDefinition;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.module.birt.BirtReport;
import org.openmrs.module.birt.db.BirtReportDAO;
import org.openmrs.reporting.export.DataExportReportObject;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface BirtReportService {

	/**
	 * Sets the data access object for the report service.
	 * 
	 * @param 	dao		the report DAO
	 */
	public void setBirtReportDAO(BirtReportDAO dao);

	
	/**
	 * Gets the data access object used by the report service.
	 * 
	 * @return	the report DAO
	 */
	public BirtReportDAO getBirtReportDAO();
	
	
	/**
	 * Gets all reports in the system.
	 * @return	a list of reports in the database
	 */
	@Transactional(readOnly=true)
	public List<BirtReport> getReports();

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
	public File exportFlatfileDataset(DataExportReportObject export);
	
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
	@Transactional(readOnly=true)
	public List<AbstractReportObject> getDataExports();
	
	/**
	 * Get a list of all cohorts in the database.
	 * 
	 * @return	cohorts
	 */
	@Transactional(readOnly=true)
	public List<CohortDefinitionItemHolder> getCohortDefinitions();
	

	/**
	 * Generate and email report to users.
	 * @param report
	 */
	public void generateAndEmailReport(BirtReport report);
	
	
	

}