package org.openmrs.module.birt.model;

import java.util.Date;
import java.util.List;

import org.openmrs.User;

public class DatasetDefinition {
	
	private int id;
	private String name;
	private List<ColumnDefinition> columns;
	private Integer reportObjectId;
	
	
	// Metadata
	private User creator;
	private Date dateCreated;

	/**
	 * @return 		the dataset identifier
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id	the dataset identifier
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return Returns the name of the report
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name of the report.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated
	 *            The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	
	
	
}
