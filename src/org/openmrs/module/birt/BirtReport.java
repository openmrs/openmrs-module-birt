package org.openmrs.module.birt;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Cohort;
import org.openmrs.module.birt.model.ParameterDefinition;
//import org.openmrs.reporting.ReportObjectXMLEncoder;
//import org.openmrs.reporting.export.DataExportReportObject;
//import org.openmrs.reporting.report.ReportDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.openmrs.module.reporting.report.definition.ReportDefinition;

/**
 * Model class used to hold information for a BIRT report.  
 * 
 * TODO Inherit from a base Report object once we refactor the reporting code.
 * 
 * @author Justin Miranda
 */
public class BirtReport implements Serializable {
	
	/* Serial version UID */
	private static final long serialVersionUID = -3597505787229438074L;	

    /* Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
	/* Directory path to the report design (.rptdesign) */
	private String reportDesignPath;

	/* Report design object - transient because we can get it from the filesystem. */
	private transient ReportDesignHandle reportDesign;
	
	// Report definition object 
	private ReportDefinition reportDefinition = new ReportDefinition();
	
/*	 Desired Columns 
	private DataExportReportObject dataExport = new DataExportReportObject();*/

	// Desired Rows 
	private Cohort cohort = new Cohort();
	
	/* Output format (default = pdf) */
	private String format = "pdf";

	/* Report output */
	private File output = null;
	
	/* Report output filename */
	private String outputFilename = null;
	
	/* Report parameters */
	private List<ParameterDefinition> parameters = new ArrayList<ParameterDefinition>();
	 	
	/* Report errors */
	private List errors = null;

	/* Email properties */
	private Map<String,String> emailProperties = new HashMap<String,String>();
	
	
	/**
	 * Public default constructor.
	 *
	 */
	public BirtReport() { }

	/**
	 * Public default constructor.
	 * 
	 * @param reportDefinition
	 */
	public BirtReport(ReportDefinition reportDefinition) { 
		this.reportDefinition = reportDefinition;
	}

	/**
	 * 
	 * @return
	 */
	public Integer getReportId() {
		return this.reportDefinition.getId();	
		//return this.reportDefinition.getReportObjectId();	
	}
	
	public String getName() { 
		return null;
		//return this.reportDefinition.getName();
	}
	
	public String getVersion() { 
		return "0.0";
	}
	
	public boolean getPublished() { 
		return true; 
	}
	 
	/**
	 * Gets the directory path of the report.
	 * @return
	 */
	public String getReportDesignPath() { 
		return reportDesignPath;
	}
	
	/**
	 * Get report design handle.
	 * @return
	 */
	public ReportDesignHandle getReportDesign() { 
		return reportDesign;
	}
	
	/**
	 * Get report definition.
	 * @return	the report definition object
	 */
	public ReportDefinition getReportDefinition() { 
		return reportDefinition;
	}
	
	/**
	 * Returns whether the report has an associated data export object.
	 * @return	true if data export exists, false otherwise
	 */
/*	public boolean hasFlatfileDataSet() {
		return getReportDefinition().getDataExport() != null && getReportDefinition().getDataExport().getReportObjectId() != null;		
	}*/

	/**
	 * Gets the assigned cohort that is to populate the data set.
	 * @return	a cohort of patients
	 */
	public Cohort getCohort() { 
		return cohort;
	}
	
	/** 
	 * Gets the generated output file.
	 * @return
	 */
	public File getOutputFile() { 
		return output;
	}
	
	/**
	 * Gets the desired output format.
	 * @return
	 */
	public String getOutputFormat() { 
		return format;
	}

	/**
	 * Gets the output filename.
	 * @return
	 */
	public String getOutputFilename() { 
		return outputFilename;
	}
	
	/**
	 * Gets the report parameters.
	 * 
	 * @return
	 */
	public List<ParameterDefinition> getParameters() { 
		return parameters;
	}
	
	/**
	 * Gets the default values for all parameters.
	 * @return
	 */
	public Map<String, Object> getParameterValues() { 
		Map<String, Object> parameterValues = new HashMap<String,Object>();
		for (ParameterDefinition parameter : parameters) { 
			// TODO Need to add support for multiple values
			parameterValues.put(parameter.getName(), parameter.getValue());
		}
		return parameterValues;
	}
	
	
	/**
	 * Gets the properties used to send an email.
	 * @return
	 */
	public Map<String,String> getEmailProperties() { 
		return emailProperties;
	}
	
	
	
	
	/**
	 * Set the report identifier.
	 * @param id	the report identifier
	 */
	public void setReportId(Integer id) { 
		//this.reportDefinition.setReportObjectId(id);
	}	
	
	/**
	 * Sets the directory path of the report.
	 * @param path
	 */
	public void setReportDesignPath(String reportDesignPath) { 
		this.reportDesignPath = reportDesignPath;
	}
	 
	/**
	 * Set report design handle.
	 * 
	 * @param reportDesign
	 */
	public void setReportDesign(ReportDesignHandle reportDesign) { 
		this.reportDesign = reportDesign;
	}	
	
	/** 
	 * Set report definition.
	 * 
	 * @param reportDefinition
	 */
	public void setReportDefinition(ReportDefinition reportDefinition) {
		this.reportDefinition = reportDefinition;
	}

	/**
	 * Sets the default data export object.
	 * @param dataExport
	 */
/*	public void setDataExport(DataExportReportObject dataExport) { 
		this.dataExport = dataExport;	
	}*/
	
	/** 
	 * 
	 * @param cohort
	 */
	public void setCohort(Cohort cohort) { 
		this.cohort = cohort;
	}

	/** 
	 * Sets the generated output file.
	 * 
	 * @return
	 */
	public void setOutputFile(File output) { 		
		this.output = output;
	}
			
	/**
	 * Sets the desired output format.
	 * 
	 * @return
	 */
	public void setOutputFormat(String format) { 		
		this.format = format;
	}
	
	
	/**
	 * Adds the report parameters.
	 * @param parameters
	 */
	public void addParameters(List<ParameterDefinition> parameters) { 
		this.parameters.addAll(parameters);
	}
		
	/**
	 * Adds the given parameters to the report.
	 * @param parameters
	 */
	public void addParameters(Map<String, Object> parameters) { 
		for (String name : parameters.keySet()) {
			Object value = parameters.get(name);			
			addParameter(name, value);			
		}
	}

	/**
	 * Adds a parameter to the report.
	 * @param name
	 * @param value
	 */
	public void addParameter(String name, Object value) { 
		this.parameters.add(new ParameterDefinition(name, value));
	}
	
	/**
	 * Sets the properties for sending a report via email.
	 * 
	 * @param emailProperties
	 */
	public void setEmailProperties(Map<String,String> emailProperties) { 
		this.emailProperties = emailProperties;
	}
	
	
	
	/**
	 * Convenience method to check if the file exists.
	 * 
	 * @return
	 */
	public boolean getReportDesignExists() { 
		try { 
			return new File(reportDesignPath).exists();
		} catch (Exception e) { 
			return false;
		}
	}

	
	/**
	 * Returns an encoded XML string that represents the report definition object.
	 * 
	 * @return
	 */
	public String getReportXml() {
		return null;
		//return new ReportObjectXMLEncoder(this.getReportDefinition()).toXmlString();
	}
	
	/**
	 * Retuns an encoded XML string that represents the dataset definition object.
	 * 
	 * @return
	 */
	public String getDatasetXml() { 
		return null;
		//return new ReportObjectXMLEncoder(this.getReportDefinition().getDataExport()).toXmlString();		
	}
	
	/**
	 * Returns a list of errors that occur during the report generation phase.
	 * 
	 * @return	list of errors
	 */
	public List getErrors() { 
		return errors;
	}

	/**
	 * Sets errors that occur during the report generation phase.
	 * 
	 * @param errors	list of errors
	 */
	public void setErrors(List errors) { 
		this.errors = errors;		
	}
		
	/**
	 * Returns whether the report has errors.
	 * 
	 * @return
	 */
	public boolean hasErrors() { 
		return errors != null && !errors.isEmpty();
	}
	
	
	/**
	 * Sets the output filename.
	 * @return
	 */
	public void setOutputFilename(String filename) { 
		this.outputFilename = filename;
	}
	
	/**
	 * Convert birt report to 
	 */
	public String toString() { 
		return null;
/*		return new StringBuffer().
			append("[").
			append("id=").append(this.getReportDefinition().getReportObjectId()).
			append(", name=").append(this.getReportDefinition().getName()).
			append(", description=").append(this.getReportDefinition().getDescription()).
			append(", format=").append(this.getOutputFormat()).
			append(", parameters=").append(this.getParameters()).
			//append(", cohort=").append(this.getCohort()).
			//append(", dataExport=").append(this.getDataExport().getColumns()).
			append("]").
			toString();*/
	}

	
	
	
}
