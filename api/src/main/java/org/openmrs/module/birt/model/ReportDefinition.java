package org.openmrs.module.birt.model;

import java.util.Date;
import java.util.List;

import org.openmrs.User;

public class ReportDefinition {
	
	private int id;
	private String name;
	private String type;
	private Boolean isPublic;
	private Integer reportObjectId;
	
	// Report design file contents
	private byte[] reportDesign;
	
	// 
	private List<DatasetDefinition> datasets;
	private List<ParameterDefinition> parameters;
	
	
	// Metadata
	private User creator;
	private Date dateCreated;
	private User changedBy;
	private Date dateChanged;
	private User uploadedBy;
	private Date dateUploaded;

	
	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            The reportDefId to set.
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
