package org.openmrs.module.birt.db;

import java.util.List;

import org.openmrs.User;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.birt.model.ReportDefinition;

/**
 * 
 * @author Justin Miranda
 *
 */
public interface BirtReportDAO {
	
	/**
	 * Create a report definition.
	 * @param report
	 * @throws DAOException
	 */
	public void createReportDefinition(ReportDefinition report) throws DAOException;
	
	/**
	 * Get all report definition.
	 * @return
	 * @throws DAOException
	 */
	public List<ReportDefinition> getReportDefinitions() throws DAOException;

	/**
	 * Gets report definitions created or modified by the given user.
	 * @param user
	 * @return
	 * @throws DAOException
	 */
	public List<ReportDefinition> getReportDefinitions(User user) throws DAOException;
	
	/**
	 * Gets a list of reports based on the given name.
	 * @param name
	 * @return
	 * @throws DAOException
	 */
	public List<ReportDefinition> getReportDefinitions(String name) throws DAOException;
	
	/**
	 * Get a report defintion based on the given identifier.
	 * @param id
	 * @return
	 * @throws DAOException
	 */
	public ReportDefinition getReportDefinition(Integer id) throws DAOException;
	
	/**
	 * Update the given report definition.
	 * @param report
	 * @throws DAOException
	 */
	public void updateReportDefinition(ReportDefinition report) throws DAOException;
	
	/**
	 * Deletes the given report definition.
	 * @param report
	 * @throws DAOException
	 */
	public void deleteReportDefinition(ReportDefinition report) throws DAOException;
	
	
}
