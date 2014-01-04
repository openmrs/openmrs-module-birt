package org.openmrs.module.birt;

import org.eclipse.birt.report.engine.api.IReportEngine;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Test the Birt Runtime class
 * TODO: Test that BirtConfiguration properties have an impact
 * TODO: Test that BirtRuntime can be started, stopped, and restarted again
 */
public class BirtRuntimeTest extends BaseModuleContextSensitiveTest {

	@Test
	public void shouldStartupAndShutdownBirt() throws Exception {
		BirtConfiguration config = new BirtConfiguration();
		BirtRuntime.startup(config);
		Assert.assertTrue(BirtRuntime.isStarted());
		IReportEngine reportEngine = BirtRuntime.getReportEngine();
		Assert.assertNotNull(reportEngine);
		BirtRuntime.shutdown();
		Assert.assertFalse(BirtRuntime.isStarted());
	}
}
