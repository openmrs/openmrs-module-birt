package org.openmrs.module.birt;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IParameterSelectionChoice;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.model.api.CascadingParameterGroupHandle;
import org.eclipse.birt.report.model.api.ModuleUtil;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.ScalarParameterHandle;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleException;
import org.openmrs.module.birt.model.ParameterDefinition;

public class BirtReportUtil {

	private static Log log = LogFactory.getLog(BirtReportUtil.class);

	/**
	 * Gets the folder where reports are stored.
	 */
	public static File getReportRepository() {

		String reportDirectory = Context.getAdministrationService().getGlobalProperty(BirtConfiguration.PROPERTY_REPORT_DIR);

		return createDirectory(reportDirectory);
	}

	/**
	 *
	 * @return	a string representing the version of the birt runtime engine
	 */
	public static String getBirtVersion() {
		return ModuleUtil.getReportVersion();
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
    		Context.getAdministrationService().getGlobalProperty(BirtConfiguration.PROPERTY_DATASET_DIR);

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

		log.debug("Name: " + name + " " + format);

		String extension = (format != null) ?
				format.toLowerCase() : BirtConfiguration.DEFAULT_REPORT_OUTPUT_FORMAT;

		log.info("Output directory " + getOutputDirectory());
		log.info("File separator " + File.separator);
		log.info("Name: " + name);
		log.info("Extension: " + extension);

		StringBuffer buffer = new StringBuffer();
		buffer.
			append(getOutputDirectory().replace("/", File.separator)).append(File.separator).
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
		return BirtConfiguration.OUTPUT_DIR;
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
			log.debug("The specified directory doesn't exist, creating... " + folder.getAbsolutePath());
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


	/**
	 * Return an array of objects of the given type parsed from an array of strings.
	 * @param type
	 * @param values
	 * @return
	 * @throws ParseException
	 */
	public static final Object [] parseParameterValues(String type, String [] values) throws ParseException {
		Object [] objects = null;
		if (values != null && values.length > 0) {
			objects = new Object[values.length];
			for (int i=0; i<values.length; i++) {
				objects[i] = parseParameterValue(type, values[i]);
			}
		}
		return objects;
	}



	/**
	 * Return an object of given type that has been parsed from a string.
	 * @param type
	 * @param value
	 * @return
	 * @throws ParseException
	 */
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
			return new SimpleDateFormat(BirtConfiguration.DEFAULT_DATETIME_FORMAT).parse(value);
		}
		else if ( DesignChoiceConstants.PARAM_TYPE_DATE.equals( type ) ) {
			// java.sql.Date
			java.util.Date datetimeValue = new SimpleDateFormat(BirtConfiguration.DEFAULT_DATE_FORMAT).parse(value);
			return new java.sql.Date(datetimeValue.getTime());
		}
		else if ( DesignChoiceConstants.PARAM_TYPE_TIME.equals( type ) ) {
			// java.sql.Time
			java.util.Date datetimeValue = new SimpleDateFormat(BirtConfiguration.DEFAULT_TIME_FORMAT).parse(value);
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
		log.debug("Get data type: " + dataType);
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
		log.debug("Get control type: " + controlType);
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

	public static final String getParameterType(int parameterType) {
		log.debug("Get parameter type: " + parameterType);
		switch (parameterType) {
			default:
				return ParameterDefinition.TEXT_BOX;
		}
	}


	/**
	 * Creates a parameter based on the BIRT report parameter.
	 */
	public static ParameterDefinition getParameter(
			IGetParameterDefinitionTask task,
			IScalarParameterDefn scalar,
			IReportRunnable reportRunnable,
			IParameterGroupDefn group) {

		// Initialize and populate the parameter
		ParameterDefinition parameter = new ParameterDefinition();
		parameter.setName(scalar.getName());
		parameter.setValue(scalar.getDefaultValue());
		parameter.setDefaultValue(scalar.getDefaultValue());
		parameter.setRequired(scalar.isRequired());
		parameter.setPromptText(scalar.getPromptText());
		parameter.setAllowNull(scalar.allowNull());
		parameter.setHelpText(scalar.getHelpText());
		parameter.setDisplayFormat(scalar.getDisplayFormat());
		parameter.setHidden(scalar.isHidden());
		parameter.setConceal(scalar.isValueConcealed());

		// Intrepret the display type (select, text, radio, etc)
		parameter.setControlType(BirtReportUtil.getControlType(scalar.getControlType()));

		// Interpret data type (integer, string, date, datetime)
		parameter.setDataType(BirtReportUtil.getDataType(scalar.getDataType()));

		// Interpret the parameter type (simple, multi-value, ?)
		// parameter.setParameterType(BirtReportUtil.getParameterType(scalar.getParameterType()));


		// Get report design and find default value, prompt text and data set expression using the DE API
		ReportDesignHandle reportHandle = (ReportDesignHandle) reportRunnable.getDesignHandle( );
		ScalarParameterHandle parameterHandle = (ScalarParameterHandle) reportHandle.findParameter(scalar.getName());
		parameter.setDefaultValue(parameterHandle.getDefaultValue());
		parameter.setPromptText(parameterHandle.getPromptText());

		// Sets whether we should allow multiple values or not (based on parameter type simple vs multi-value)
		parameter.setAllowMultiple("multi-value".equals(parameterHandle.getParamType())?true:false);

		// If the parameter's control type is not TEXT BOX, then it is some type of SELECT LIST
		if(scalar.getControlType() != IScalarParameterDefn.TEXT_BOX) {

			// Cascaded parameter
			if ( parameterHandle.getContainer() instanceof CascadingParameterGroupHandle ) {
				Collection sList = Collections.EMPTY_LIST;
				if ( parameterHandle.getContainer( ) instanceof CascadingParameterGroupHandle ) {
					int index = parameterHandle.getContainerSlotHandle().findPosn( parameterHandle );
					Object[] keyValue = new Object[index];
					for ( int i = 0; i < index; i++ ) {
						ScalarParameterHandle handle = (ScalarParameterHandle)
						( (CascadingParameterGroupHandle) parameterHandle.getContainer( ) ).getParameters( ).get( i );
						//Use parameter default values
						keyValue[i] = handle.getDefaultValue();
					}
					String groupName = parameterHandle.getContainer( ).getName( );
					task.evaluateQuery( groupName );

					sList = task.getSelectionListForCascadingGroup( groupName, keyValue );
					Map<Object, String> dynamicList = new HashMap<Object, String>();


					for ( Iterator sl = sList.iterator( ); sl.hasNext( ); ) {
						IParameterSelectionChoice sI = (IParameterSelectionChoice) sl.next( );
						Object value = sI.getValue( );
						Object label = sI.getLabel( );
						log.debug( label + "--" + value);
						dynamicList.put(value,(String) label);

					}
					parameter.setSelectionList(dynamicList);
				}
			}

			// Scalar parameter
			else {
				Collection selectionList = task.getSelectionList( scalar.getName() );

				if ( selectionList != null ) {
					Map<Object, String> dynamicList = new HashMap<Object, String>();

					for ( Iterator iter = selectionList.iterator(); iter.hasNext(); ) {

						IParameterSelectionChoice selectionItem = (IParameterSelectionChoice) iter.next();
						Object value = selectionItem.getValue( );
						String label = selectionItem.getLabel( );
						dynamicList.put(value,label);

					}
					parameter.setSelectionList(dynamicList);
				}
			}
		}

		log.debug("*** Parameter = " + parameter);


		return parameter;
	}

	
}
