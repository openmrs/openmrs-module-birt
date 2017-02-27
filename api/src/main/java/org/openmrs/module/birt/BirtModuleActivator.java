package org.openmrs.module.birt;

import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleException;
import org.openmrs.util.OpenmrsUtil;


public class BirtModuleActivator extends BaseModuleActivator {

	private Log log = LogFactory.getLog(BirtModuleActivator.class);

	/**
	 * @see org.openmrs.module.ModuleActivator#started()
	 */
	@Override
	public void started() {
		log.debug("Starting BIRT Reporting Module ...");

		// Define global properties
		String [] globalProperties = {
			BirtConfiguration.PROPERTY_DATASET_DIR,
			BirtConfiguration.PROPERTY_LOGGING_DIR,
			BirtConfiguration.PROPERTY_LOGGING_LEVEL,
			BirtConfiguration.PROPERTY_REPORT_DIR,
			BirtConfiguration.PROPERTY_REPORT_OUTPUT_FORMAT,
			BirtConfiguration.PROPERTY_OUTPUT_DIR,
		};
				
		String [] directoryProperties = { 
			BirtConfiguration.PROPERTY_DATASET_DIR,
			BirtConfiguration.PROPERTY_LOGGING_DIR,
			BirtConfiguration.PROPERTY_REPORT_DIR,
			BirtConfiguration.PROPERTY_OUTPUT_DIR
		};
		String [] deprecatedProperties = { 
			BirtConfiguration.PROPERTY_REPORT_OUTPUT_FILE,
			BirtConfiguration.PROPERTY_REPORT_PREVIEW_FILE,
			BirtConfiguration.PROPERTY_DEFAULT_REPORT_DESIGN_FILE
		};

		// Warn implementers about deprecated properties
		validateDeprecatedProperties(deprecatedProperties);
		validateGlobalProperties(globalProperties);
		createDirectories(directoryProperties);
		
		try {					
			log.debug("Starting BIRT Report Engine ... ");
			Platform.startup(BirtConfiguration.getReportEngine().getConfig());
		} 
		catch (BirtException e) {
			throw new ModuleException("Error starting BIRT report engine", e);
		}
	}

	/**
	 *  @see org.openmrs.module.ModuleActivator#willStop()
	 */
	@Override
	public void willStop() {
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
	 */
	public void createDirectories(String [] directoryProperties) { 
		for (String propertyName : directoryProperties) {
			String propertyValue = Context.getAdministrationService().getGlobalProperty(propertyName, ""); 

			log.debug("Attempting to create directory " + propertyValue);
			BirtReportUtil.createDirectory(propertyValue);
		}
	}

	
	
}
