package org.openmrs.module.birt;

import java.util.List;

/**
 * Represents exceptions that occur within the Birt Reporting module
 * 
 * @author Justin Miranda
 * @version 1.0
 */
public class BirtReportException extends RuntimeException {

	private List errors;

	/**
	 * 
	 */
	private static final long serialVersionUID = -6581096406721446434L;

	public BirtReportException(Throwable cause) {
		super(cause);
	}

	public BirtReportException(String message) {
		super(message);
	}

	public BirtReportException(List errors) {
		this.errors = errors;
	}

	public BirtReportException(String message, List errors) {
		super(message);
		this.errors = errors;
	}

	public BirtReportException(String message, Throwable cause) {
		super(message, cause);
	}

	public BirtReportException(String message, String reportName) {
		super(message + " Report: " + reportName);
	}

	public BirtReportException(String message, String reportName, Throwable cause) {
		super(message + " Report: " + reportName, cause);
	}

	public List getErrors() {
		return this.errors;
	}

	public void setErrors(List errors) {
		this.errors = errors;
	}

}
