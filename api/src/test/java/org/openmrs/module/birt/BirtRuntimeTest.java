package org.openmrs.module.birt;

import org.eclipse.birt.report.engine.api.IReportEngine;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Test the Birt Runtime class
 */
public class BirtRuntimeTest extends BaseModuleContextSensitiveTest {

	@Test
	public void shouldStartupAndShutdownBirt() throws Exception {
		BirtConfiguration config = new BirtConfiguration();
		BirtRuntime.startup(config);
		IReportEngine reportEngine = BirtRuntime.getReportEngine();
		Assert.assertNotNull(reportEngine);
		BirtRuntime.shutdown();
	}
}
