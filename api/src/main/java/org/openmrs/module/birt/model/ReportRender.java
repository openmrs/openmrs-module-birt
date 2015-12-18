package org.openmrs.module.birt.model;

import java.util.Date;
import java.util.List;

import org.openmrs.User;

public class ReportRender {

	private int id;

	// Output format (pdf, html, excel, doc)
	// TODO create mime type map for format key
	private String format;

	// Output filename
	private String filename;

	// Filled parameters
	private List<ParameterDefinition> parameters;

	// Report rendered
	private ReportDefinition report;

	// Report design file contents
	private byte[] reportOutput;

	// Metadata
	private User creator;
	private Date dateCreated;
	private User changedBy;
	private Date dateChanged;

	/**
	 * @return Returns the report identifier.
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the report identifier
	 */
	public void setId(int id) {
		this.id = id;
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
