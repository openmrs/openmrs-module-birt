package org.openmrs.module.birt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IParameterSelectionChoice;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.impl.ParameterDefn;
import org.eclipse.birt.report.engine.api.script.IReportContext;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleException;
import org.openmrs.module.birt.impl.BirtConfiguration;
import org.openmrs.module.birt.model.ParameterDefinition;
import org.openmrs.util.OpenmrsUtil;

public class BirtReportUtil {

	private static Log log = LogFactory.getLog(BirtReportUtil.class);
		
	/**
	 * Gets the folder where reports are stored. 
	 * 
	 * @throws ReportException on errors
	 * @return folder containing modules
	 */
	public static File getReportRepository() {
		
		String reportDirectory = 
			Context.getAdministrationService().getGlobalProperty(BirtConstants.PROPERTY_REPORT_DIR);
		
		return createDirectory(reportDirectory);
	}
	
	
	/**
	 * 
	 * @param date
	 * @param daysToAdd
	 * @return
	 */
	public static Date addDays(Date date, int daysToAdd) { 
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);		
		return calendar.getTime();			
	}
	
	
	/**
	 * Convenience method used to return report design path.
	 * @param filename
	 * @return
	 */
	public static String getReportDesignPath(String filename) { 		
		return new StringBuffer().
			append(BirtReportUtil.getReportRepository().getAbsolutePath()).
			append(BirtReportUtil.getReportRepository().getAbsolutePath().endsWith(BirtConstants.FILE_SEPARATOR)?"":BirtConstants.FILE_SEPARATOR).
			append(filename).
			append(".").
			append(BirtConstants.REPORT_DESIGN_EXTENSION).
			toString();		
	}
	
	
	/**
	 * 
	 * @param filename
	 * @return
	 */
	public static String getDataExportPath(String filename) { 
    	// Copy the generated data export to the location expected by the report module 
    	String datasetDir = 
    		Context.getAdministrationService().getGlobalProperty(BirtConstants.PROPERTY_DATASET_DIR);
    	
    	// Create the filename for the final data export
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(datasetDir);
    	buffer.append(File.separatorChar);	// TODO check to make sure the separator is necessary
    	buffer.append(filename);
    	buffer.append("-");
    	buffer.append(BirtConstants.DATE_FORMATTER.format(new Date()));
    	buffer.append(".csv");
    	
    	return buffer.toString();
		
	}
	
	/**
	 * Convenience method used to create an appropriate filename for a rendered report.
	 * @param name		the name of the report 
	 * @param format	the format of the report
	 * @return	the full path to the report output file
	 */
	public static String getOutputFilename(String name, String format) { 
		
		log.info("Name: " + name + " " + format);
		
		String extension = (format != null) ? 
				format.toLowerCase() : BirtConstants.DEFAULT_REPORT_OUTPUT_FORMAT;
		
		StringBuffer buffer = new StringBuffer().
			append(getOutputDirectory()).append(File.separator).
			append(name.replace(" ", "_")).
			append("-").append(BirtConstants.DATE_FORMATTER.format(new Date())).
			append(".").append(extension);

		return buffer.toString();
		
	}
		
	
	/**
	 * Returns the report output directory.
	 * 
	 * @return
	 */
	public static String getOutputDirectory() { 
		return new StringBuffer().append(BirtConstants.OUTPUT_DIR).toString();		
	}
	
	
	/**
	 * Create a temporary directory with the given prefix and a random suffix
	 *
	 * TODO Move to OpenmrsUtil
	 * 
	 * @param prefix
	 * @return New temp directory pointer
	 * @throws IOException
	 */
	public static File createTempDirectory(String prefix) throws IOException {
		String dirName = System.getProperty("java.io.tmpdir");
		if (dirName == null)
			throw new IOException("Cannot determine system temporary directory");

		File dir = new File(dirName);
		if (!dir.exists())
			throw new IOException("System temporary directory " + dir.getName() + " does not exist.");
		
		if (!dir.isDirectory())
			throw new IOException("System temporary directory " + dir.getName() + " is not really a directory.");

		File tempDir;
		do {
			String filename = prefix + System.currentTimeMillis();
			tempDir = new File(dir, filename);
		} while (tempDir.exists());

		if (!tempDir.mkdirs())
			throw new IOException("Could not create temporary directory '" + tempDir.getAbsolutePath() + "'");

		if (log.isDebugEnabled())
			log.debug("Successfully created temporary directory: " + tempDir.getAbsolutePath());

		tempDir.deleteOnExit();
		return tempDir;
	}
	
	/**
	 * Creates a new directory at the specified directory path.
	 * 
	 * @param directory
	 * @return
	 */
	public static File createDirectory(String directory) { 
		
		if (directory == null) 
			throw new ModuleException("The specified directory cannot be null or empty");
		
		File folder = new File(directory);
		
		// Create folder structure
		if (!folder.exists()) {
			log.info("The specified directory doesn't exist, creating... " + folder.getAbsolutePath());
			folder.mkdirs();
			if (!folder.exists()) { 
				throw new ModuleException("The specified directory " + folder.getAbsolutePath() + " could not be created");
			}
		}		
		// If folder does exist
		else { 
			// Folder exists, but is not a directory
			if (!folder.isDirectory()) {
				throw new ModuleException("The specified directory exists, but is not a directory at: " + folder.getAbsolutePath());
			}
		}
		return folder;
	}	
	
	
	public static final Object parseParameterValue(String type, String value) throws ParseException { 

		if ( DesignChoiceConstants.PARAM_TYPE_DECIMAL.equals( type ) ) { 
			// java.lang.Number
			DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance();
			return formatter.parse(value);
		} 
		else if ( DesignChoiceConstants.PARAM_TYPE_INTEGER.equals( type ) ) { 
			return new Integer(NumberFormat.getInstance().parse(value).intValue());
			
		}
		else if ( DesignChoiceConstants.PARAM_TYPE_FLOAT.equals( type ) ) { 
			// java.lang.Number number = null;
			return NumberFormat.getInstance().parse(value);
		}
		else if ( DesignChoiceConstants.PARAM_TYPE_DATETIME.equals( type ) )  { 
			//java.util.Date
			log.debug("Parsing datetime value '" + value + "'");  
			return new SimpleDateFormat(BirtConstants.DEFAULT_DATETIME_FORMAT).parse(value);
		}
		else if ( DesignChoiceConstants.PARAM_TYPE_DATE.equals( type ) ) { 
			// java.sql.Date
			java.util.Date datetimeValue = new SimpleDateFormat(BirtConstants.DEFAULT_DATE_FORMAT).parse(value);
			return new java.sql.Date(datetimeValue.getTime());	
		}
		else if ( DesignChoiceConstants.PARAM_TYPE_TIME.equals( type ) ) { 
			// java.sql.Time
			java.util.Date datetimeValue = new SimpleDateFormat(BirtConstants.DEFAULT_TIME_FORMAT).parse(value);
			return new java.sql.Time(datetimeValue.getTime());
		}
		else if ( DesignChoiceConstants.PARAM_TYPE_STRING.equals( type ) ) { 
			// java.lang.String
			return value.toString( ).trim( );							
		}
		else if ( DesignChoiceConstants.PARAM_TYPE_BOOLEAN.equals( type ) ) { 
			// java.lang.Boolean
			String stringValue = value.toString( ).trim( );
			return Boolean.parseBoolean(stringValue);
		}	
		else {
			return value.toString().trim();
		}
		
	}
	
	
	public static final String getDataType(int dataType) { 
		log.info("Get data type: " + dataType);
		switch (dataType) {
		
			case IScalarParameterDefn.TYPE_STRING:  
				return ParameterDefinition.TYPE_STRING;
				
			case IScalarParameterDefn.TYPE_FLOAT:  
				return ParameterDefinition.TYPE_FLOAT;
				
			case IScalarParameterDefn.TYPE_DECIMAL:  
				return ParameterDefinition.TYPE_DECIMAL;
				
			case IScalarParameterDefn.TYPE_DATE:
				return ParameterDefinition.TYPE_DATE;
				
			case IScalarParameterDefn.TYPE_DATE_TIME:  
				return ParameterDefinition.TYPE_DATE_TIME;
				
			case IScalarParameterDefn.TYPE_BOOLEAN:  
				return ParameterDefinition.TYPE_BOOLEAN;
				
			default:  
				return ParameterDefinition.TYPE_STRING;
		}
	}
	
	public static final String getControlType(int controlType) { 
		log.info("Get control type: " + controlType);
		switch (controlType) {
			case IScalarParameterDefn.TEXT_BOX:  
				return ParameterDefinition.TEXT_BOX; 
				
			case IScalarParameterDefn.LIST_BOX:  
				return ParameterDefinition.LIST_BOX; 
				
			case IScalarParameterDefn.RADIO_BUTTON:  
				return ParameterDefinition.RADIO_BUTTON; 
				
			case IScalarParameterDefn.CHECK_BOX:  
				return ParameterDefinition.CHECK_BOX; 
				
			default: 
				return ParameterDefinition.TEXT_BOX; 
		}
	}

	
	/*
	public static Collection<IParameterSelectionChoice> getParameterValues(String parameterName, IReportContext reportContext) {
		IGetParameterDefinitionTask task = null;
		HashMap curParams = new HashMap();
		IReportRunnable runnable = reportContext.getReportRunnable();
		try {
			task = runnable.getReportEngine().createGetParameterDefinitionTask(runnable);
	
			// get the names of all the parameters
			Collection paramRefs = task.getParameterDefns(false);
	
			// for each parameter name, get the parameter value
			// add the name and value to a hashmap
			for (Iterator iterator = paramRefs.iterator(); iterator.hasNext();) {
	
			    ParameterDefn pDefn = (ParameterDefn) iterator.next();
			    String name = pDefn.getName();
			    Object curP = reportContext.getParameterValue(name);
	
			    curParams.put(name, curP);
			}
	
			// set the parameter values for this task from the hashmap.
			task.setParameterValues(curParams);
	
			// get the parameter that is tied to this table.
			IParameterDefnBase scalar = task.getParameterDefn(parameterName);
			if (scalar instanceof IScalarParameterDefn) {
	
			    // bind the parameters to the query text
			    task.evaluateQuery(scalar.getName());
	
			    // get the values for this parameter from its DataSet
			    Collection<IParameterSelectionChoice> paramChoices = 
			    	(Collection<IParameterSelectionChoice>) task
			            .getSelectionList(scalar.getName());
			    return paramChoices;
	
			}
		} catch (Exception e) {
			log.error("Failure to get parameters", e);
		} 
		finally {
			task.close();
		}
		return null;
	}
	*/
	
	
}
