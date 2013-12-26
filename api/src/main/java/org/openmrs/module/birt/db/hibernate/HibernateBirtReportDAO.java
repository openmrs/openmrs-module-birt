package org.openmrs.module.birt.db.hibernate;

import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.birt.db.BirtReportDAO;
import org.openmrs.module.birt.model.ReportDefinition;

public class HibernateBirtReportDAO implements BirtReportDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	/**
	 * Default public constructor
	 */
	public HibernateBirtReportDAO() { }
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
	}

	
	
	/**
	 * Creates a report definition.
	 * 
	 * @param	the report definition to be created.
	 */
	public void createReportDefinition(ReportDefinition report) throws DAOException {
		sessionFactory.getCurrentSession().save(report);
	}

	
	/**
	 * Gets a report definition based on the given identifier.
	 * 
	 * @param	id 	the identifier of the desired report
	 * @return	the report definition
	 */
	public ReportDefinition getReportDefinition(Integer id)
			throws DAOException {
		
		ReportDefinition report;
		report = (ReportDefinition) sessionFactory.getCurrentSession().get(ReportDefinition.class,
				id);

		return report;
	}

	

	/**
	 * Gets all report definitions.
	 * 
	 * @return	a list of report definition
	 */
	@SuppressWarnings("unchecked")
	public List<ReportDefinition> getReportDefinitions() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery(
				"from ReportDefinition order by name").list();
	}

	/**
	 * Updates the given report definition.
	 * 
	 * @param	the report definition to update
	 */
	public void updateReportDefinition(ReportDefinition reportDef)
			throws DAOException {

		if (reportDef.getId() == 0)
			createReportDefinition(reportDef);
		else {
			sessionFactory.getCurrentSession().saveOrUpdate(reportDef);
		}

	}

	
	/**
	 * Deletes the given report definition.
	 * 
	 * @param	the report definition to delete
	 */
	public void deleteReportDefinition(ReportDefinition report) throws DAOException {
		sessionFactory.getCurrentSession().delete(report);
	}

	
	/**
	 * Gets the report definition by user.
	 * @param	the user who created or updated the report
	 * @return	a list of reports created or updated by the given user
	 */
	public List<ReportDefinition> getReportDefinitions(User user)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	
	/**
	 * Get the report definitions based on the given search term.
	 * @param	the name of the report
	 * @return	a list of report definitions
	 */
	public List<ReportDefinition> getReportDefinitions(String name)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
