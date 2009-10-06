package org.openmrs.module.birt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.Activator;

public class BirtApiModuleActivator  {

	private Log log = LogFactory.getLog(BirtApiModuleActivator.class);

	/**
	 * @see org.openmrs.module.Activator#startup()
	 */
	public void startup() {
		log.debug("Starting BIRT API Reporting Module");

	}

	/**
	 *  @see org.openmrs.module.Activator#shutdown()
	 */
	public void shutdown() {
		log.debug("Shutting down BIRT API Reporting Module");	
	}
	
	



	
	
}
