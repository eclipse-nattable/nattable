/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.cell;

import static org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator.PERSISTENCE_KEY;

import java.util.List;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ColumnOverrideLabelAccumulatorTest {

	private static final String TEST_PREFIX = "TestPrefix";
	private static final String TEST_LABEL1 = "TestLabel1";
	private static final String TEST_LABEL2 = "TestLabel2";
	
	private ColumnOverrideLabelAccumulator labelAccumulator;
	private Properties testProperties;

	@Before
	public void setUp() {
		labelAccumulator = new ColumnOverrideLabelAccumulator(new DataLayerFixture());
		testProperties = new Properties();
	}

	@Test
	public void testSaveStateToProperties() throws Exception {
		labelAccumulator.registerColumnOverrides(0, TEST_LABEL1, TEST_LABEL2);
		labelAccumulator.registerColumnOverrides(1, TEST_LABEL2);

		labelAccumulator.saveState(TEST_PREFIX, testProperties);

		String baseKey = TEST_PREFIX + PERSISTENCE_KEY;

		Assert.assertEquals(TEST_LABEL1 + "," + TEST_LABEL2, testProperties.getProperty(baseKey + ".0"));
		Assert.assertEquals(TEST_LABEL2, testProperties.getProperty(baseKey + ".1"));
	}
	
	@SuppressWarnings("boxing")
	@Test
	public void testLoadLablesFromProperties() throws Exception {
		testProperties.setProperty(TEST_PREFIX + PERSISTENCE_KEY + ".0", TEST_LABEL1);
		testProperties.setProperty(TEST_PREFIX + PERSISTENCE_KEY + ".5", TEST_LABEL1+","+TEST_LABEL2);

		labelAccumulator.loadState(TEST_PREFIX, testProperties);
		
		List<String> overrides = labelAccumulator.getOverrides(0);
		Assert.assertEquals(1, overrides.size());
		Assert.assertEquals(TEST_LABEL1, overrides.get(0));

		overrides = labelAccumulator.getOverrides(5);
		Assert.assertEquals(2, overrides.size());
		Assert.assertEquals(TEST_LABEL1, overrides.get(0));
		Assert.assertEquals(TEST_LABEL2, overrides.get(1));
	}
}
