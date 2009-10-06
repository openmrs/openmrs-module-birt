package org.openmrs.module.birt;

import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.Activator;
import org.openmrs.module.ModuleException;
import org.openmrs.module.birt.impl.BirtConfiguration;
import org.openmrs.util.OpenmrsUtil;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;


public class BirtModuleActivator implements Activator {

	private Log log = LogFactory.getLog(BirtModuleActivator.class);

	/**
	 * @see org.openmrs.module.Activator#startup()
	 */
	public void startup() {
		log.debug("Starting BIRT Reporting Module ...");

		// Define global properties
		String [] globalProperties = { 
			BirtConstants.PROPERTY_BIRT_HOME, 
			BirtConstants.PROPERTY_DATASET_DIR, 
			BirtConstants.PROPERTY_LOGGING_DIR, 
			BirtConstants.PROPERTY_REPORT_DIR, 
			BirtConstants.PROPERTY_REPORT_OUTPUT_FORMAT, 
			BirtConstants.PROPERTY_OUTPUT_DIR,
		};
				
		String [] directoryProperties = { 
			BirtConstants.PROPERTY_DATASET_DIR, 
			BirtConstants.PROPERTY_LOGGING_DIR, 
			BirtConstants.PROPERTY_REPORT_DIR, 
			BirtConstants.PROPERTY_OUTPUT_DIR				
		};
		String [] deprecatedProperties = { 
			BirtConstants.PROPERTY_REPORT_OUTPUT_FILE, 
			BirtConstants.PROPERTY_REPORT_PREVIEW_FILE, 
			BirtConstants.PROPERTY_DEFAULT_REPORT_DESIGN_FILE
		};

		// Warn implementers about deprecated properties
		validateDeprecatedProperties(deprecatedProperties);
		validateGlobalProperties(globalProperties);
		createDirectories(directoryProperties);
		
		try {					
			log.debug("Starting BIRT Report Engine ... ");
			Platform.startup( BirtConfiguration.getEngineConfig());
		} 
		catch (BirtException e) {
			throw new ModuleException(
					"Failure starting BIRT platform with error '" + e.getMessage() + "'.  " + 
					"This is usually an indication that your 'birt.birtHome' is invalid.  Please update your implementation's 'birt.birtHome' global property." + 
					"\n\nMake yours look like mine ... " + 
					"\n\tYours: 'birt.birtHome' = '" + BirtConstants.BIRT_HOME + "'. " + 
					"\n\tMine: 'birt.birtHome' = '/path/to/birt-runtime-x_y_z/ReportEngine'\n\n", e);
		}
		
	}

	/**
	 *  @see org.openmrs.module.Activator#shutdown()
	 */
	public void shutdown() {
		log.debug("Shutting down BIRT Reporting Module ...");	
		Platform.shutdown();
	}
	
	

	/**
	 * Warns implementors whether any of the properties are deprecated.
	 * 
	 * @param deprecatedProperties
	 */
	public void validateDeprecatedProperties(String [] deprecatedProperties) { 
		Properties runtimeProperties = Context.getRuntimeProperties();
		String val = null;
		for (String property : deprecatedProperties) {
			val = runtimeProperties.getProperty(property, null);
			if (val != null) {
				log.warn("Deprecated runtime property: " + property + ".  This property is no longer read in at runtime and can be deleted.");
			}
		}
	}	
	
	
	/**
	 * Checks the global properties to make sure there are values for each required property.
	 * @param	globalProperties 	the global properties to validate
	 */
	public void validateGlobalProperties(String [] globalProperties) { 		
		List<String> errorMessages = new Vector<String>();
		for (String propertyName : globalProperties) {
			String propertyValue = Context.getAdministrationService().getGlobalProperty(propertyName, ""); 
			if ("".equals(propertyValue)) {
				errorMessages.add("Global property '" + propertyName + "' must be defined.");
			}	
		}
		if (errorMessages.size() > 0) {
			throw new ModuleException(OpenmrsUtil.join(errorMessages, " \n"));
		}		
	}

	/**
	 * Checks the global properties to make sure there are values for each required property.
	 * @param	directories		the directories to create
	 */
	public void createDirectories(String [] directoryProperties) { 
		for (String propertyName : directoryProperties) {
			String propertyValue = Context.getAdministrationService().getGlobalProperty(propertyName, ""); 

			log.debug("Attempting to create directory " + propertyValue);
			BirtReportUtil.createDirectory(propertyValue);
		}
	}

	
	
}
