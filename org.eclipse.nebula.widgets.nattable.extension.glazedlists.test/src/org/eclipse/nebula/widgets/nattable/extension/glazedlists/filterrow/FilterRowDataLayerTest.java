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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearAllFiltersCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearFilterCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ToggleFilterRowCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.config.DefaultFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;

public class FilterRowDataLayerTest {

	private DataLayerFixture columnHeaderLayer;
	private IConfigRegistry configRegistry;
	private FilterList<RowDataFixture> filterList;
	private FilterRowDataLayer<RowDataFixture> layerUnderTest;
	private LayerListenerFixture listener;

	@Before
	public void setup() {
		columnHeaderLayer = new DataLayerFixture(10, 2, 100, 50);

		configRegistry = new ConfigRegistry();
		new DefaultNatTableStyleConfiguration().configureRegistry(configRegistry);
		new DefaultFilterRowConfiguration().configureRegistry(configRegistry);

		filterList = new FilterList<RowDataFixture>(GlazedLists.eventList(RowDataListFixture.getList()));
		
		layerUnderTest = new FilterRowDataLayer<RowDataFixture>(
				new DefaultGlazedListsFilterStrategy<RowDataFixture>(
						filterList,
					new ReflectiveColumnPropertyAccessor<RowDataFixture>(RowDataListFixture.getPropertyNames()),
					configRegistry
				),
				columnHeaderLayer, columnHeaderLayer.getDataProvider(), configRegistry);
		listener = new LayerListenerFixture();
		layerUnderTest.addLayerListener(listener);
	}

	@Test
	public void shouldHandleClearFilterCommand() throws Exception {
		assertEquals(13, filterList.size());

		layerUnderTest.doCommand(new UpdateDataCommand(layerUnderTest, 1, 0, "ford"));
		assertEquals(1, filterList.size());

		layerUnderTest.doCommand(new ClearFilterCommand(layerUnderTest, 1));
		assertEquals(13, filterList.size());

		listener.containsInstanceOf(RowStructuralRefreshEvent.class);
	}

	@Test
	public void shouldHandleTheClearAllFiltersCommand() throws Exception {
		assertEquals(13, filterList.size());

		layerUnderTest.doCommand(new UpdateDataCommand(layerUnderTest, 1, 0, "ford"));
		assertEquals(1, filterList.size());

		layerUnderTest.doCommand(new UpdateDataCommand(layerUnderTest, 0, 0, "XXX"));
		assertEquals(0, filterList.size());

		layerUnderTest.doCommand(new ClearAllFiltersCommand());
		assertEquals(13, filterList.size());

		listener.containsInstanceOf(RowStructuralRefreshEvent.class);
	}

	@Test
	public void shouldHandleTheToggeleFilterRowCommand() throws Exception {
		assertEquals(1, layerUnderTest.getRowCount());
		layerUnderTest.doCommand(new ToggleFilterRowCommand());
		//as the command is handled by the FilterRowHeaderComposite now, it should
		//have no effect to do the command on the FilterRowDataLayer
		assertEquals(1, layerUnderTest.getRowCount());
	}

	@Test
	public void saveState() throws Exception {
		layerUnderTest.setDataValue(1, 1, "testValue");
		layerUnderTest.setDataValue(2, 1, "testValue");
		layerUnderTest.setDataValue(3, 1, "testValue");
		layerUnderTest.setDataValue(2, 1, null); // clear filter

		Properties properties = new Properties();

		// save state
		layerUnderTest.saveState("prefix", properties);
		String persistedProperty = properties.getProperty("prefix" + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS);

		assertEquals("1:testValue|3:testValue|", persistedProperty);
	}

	@Test
	public void loadState() throws Exception {
		Properties properties = new Properties();
		properties.put("prefix" + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS, "1:testValue|3:testValue|");

		// load state
		layerUnderTest.loadState("prefix", properties);

		assertEquals("testValue", layerUnderTest.getDataValue(1, 1));
		assertNull(layerUnderTest.getDataValue(2, 1));
		assertEquals("testValue", layerUnderTest.getDataValue(3, 1));
	}

	@Test
	public void testUnregisterPersistable() {
		layerUnderTest.unregisterPersistable(layerUnderTest.getFilterRowDataProvider());

		layerUnderTest.setDataValue(1, 1, "testValue");
		layerUnderTest.setDataValue(2, 1, "testValue");
		layerUnderTest.setDataValue(3, 1, "testValue");
		layerUnderTest.setDataValue(2, 1, null); // clear filter

		Properties properties = new Properties();

		// save state
		layerUnderTest.saveState("prefix", properties);
		for (Object key : properties.keySet()) {
			if (key.toString().contains(FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS)) {
				fail("Filter state saved");
			}
		}
	}
}

