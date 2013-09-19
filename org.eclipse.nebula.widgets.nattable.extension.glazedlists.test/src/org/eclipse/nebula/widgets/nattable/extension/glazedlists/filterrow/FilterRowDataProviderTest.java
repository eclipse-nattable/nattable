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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDoubleDisplayConverter;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.eclipse.nebula.widgets.nattable.filterrow.config.DefaultFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.filterrow.event.FilterAppliedEvent;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;

public class FilterRowDataProviderTest {

	private FilterRowDataProvider<RowDataFixture> dataProvider;
	private DataLayerFixture columnHeaderLayer;
	private FilterList<RowDataFixture> filterList;
	private ConfigRegistry configRegistry;

	@Before
	public void setup() {
		columnHeaderLayer = new DataLayerFixture(10, 2, 100, 50);

		configRegistry = new ConfigRegistry();
		new DefaultNatTableStyleConfiguration().configureRegistry(configRegistry);
		new DefaultFilterRowConfiguration().configureRegistry(configRegistry);

		filterList = new FilterList<RowDataFixture>(GlazedLists.eventList(RowDataListFixture.getList()));

		dataProvider = new FilterRowDataProvider<RowDataFixture>(
				new DefaultGlazedListsFilterStrategy<RowDataFixture>(
						filterList,
					new ReflectiveColumnPropertyAccessor<RowDataFixture>(RowDataListFixture.getPropertyNames()),
					configRegistry
				),
				columnHeaderLayer, columnHeaderLayer.getDataProvider(), configRegistry);
	}

	@Test
	public void setDataValue() {
		assertNull(dataProvider.getDataValue(1, 1));

		dataProvider.setDataValue(1, 1, "testValue");
		assertEquals("testValue", dataProvider.getDataValue(1, 1));
	}

	@Test
	public void settingTextValueAppliesTextFilter() {
		// original size
		assertEquals(13, filterList.size());

		// Apply filter
		dataProvider.setDataValue(1, 1, "ford");

		// list filtered
		assertEquals(1, filterList.size());

		// remove filter
		dataProvider.setDataValue(1, 1, null);

		assertEquals(13, filterList.size());
	}

	@Test
	public void settingThresholdValuesUpdatedFilter() {
		// Since we are triggering object comparison, we must provide the right type
		configRegistry.registerConfigAttribute(
				FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
				new DefaultDoubleDisplayConverter(),
				DisplayMode.NORMAL,
				FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 5);
		// We also have to set the text matching mode
		configRegistry.registerConfigAttribute(
				FilterRowConfigAttributes.TEXT_MATCHING_MODE,
				TextMatchingMode.REGULAR_EXPRESSION,
				DisplayMode.NORMAL,
				FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 5);

		assertEquals(13, filterList.size());

		// Index 5, 'bid' column
		dataProvider.setDataValue(5, 1, ">20");

		assertEquals(6, filterList.size());
	}
	
	@Test
	public void settingAValueFiresUpdateEvent() {
		final LayerListenerFixture listener = new LayerListenerFixture();
		columnHeaderLayer.addLayerListener(listener);
		dataProvider.setDataValue(3, 1, "testValue");

		assertEquals(1, listener.getEventsCount());
		assertNotNull(listener.getReceivedEvent(FilterAppliedEvent.class));
	}

	@Test
	public void persistence() {
		dataProvider.setDataValue(1, 1, "testValue");
		dataProvider.setDataValue(2, 1, "testValue");
		dataProvider.setDataValue(3, 1, "testValue");
		dataProvider.setDataValue(2, 1, null); // clear filter

		Properties properties = new Properties();

		// save state
		dataProvider.saveState("prefix", properties);
		String persistedProperty = properties.getProperty("prefix" + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS);

		assertEquals("1:testValue|3:testValue|", persistedProperty);

		// reset state
		setup();

		// load state
		dataProvider.loadState("prefix", properties);

		assertEquals("testValue", dataProvider.getDataValue(1, 1));
		assertEquals(null, dataProvider.getDataValue(2, 1));
		assertEquals("testValue", dataProvider.getDataValue(3, 1));
	}

	@Test
	public void shouldRecoverFromCorruptPersistedState() {
		Properties properties = new Properties();

		properties.put("prefix.filterTokens", "XX");
		dataProvider.loadState("prefix", properties);

		assertEquals(null, dataProvider.getDataValue(1, 1));
		assertEquals(null, dataProvider.getDataValue(2, 1));
		assertEquals(null, dataProvider.getDataValue(3, 1));
	}
	
	@Test 
	public void shouldRemoveNonFilteredColumnsWhenLoadingState() {
		dataProvider.setDataValue(1, 1, "testValue");
		dataProvider.setDataValue(2, 1, "testValue");
		
		Properties properties = new Properties();
		
		// save state
		dataProvider.saveState("prefix", properties);
		
		// load a different configuration
		Properties differentState = new Properties();
		differentState.put("prefix.filterTokens", "2:newTestValue|3:newTestValue");
		
		dataProvider.loadState("prefix", differentState);
		
		assertNull("Filter on column 1 has not been removed", dataProvider.getDataValue(1, 1));
		assertEquals("newTestValue", dataProvider.getDataValue(2, 1));
		assertEquals("newTestValue", dataProvider.getDataValue(3, 1));
	}

	@Test
	public void testRegularExpressionWithPipes() {
		configRegistry.registerConfigAttribute(
				FilterRowConfigAttributes.TEXT_MATCHING_MODE,
				TextMatchingMode.REGULAR_EXPRESSION,
				DisplayMode.NORMAL,
				FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 1);

		assertEquals(13, filterList.size());

		dataProvider.setDataValue(1, 1, "(D|E|F){1}.*");

		assertEquals(3, filterList.size());
	}

	@Test
	public void testPersistenceRegularExpressionWithPipes() {
		configRegistry.registerConfigAttribute(
				FilterRowConfigAttributes.TEXT_MATCHING_MODE,
				TextMatchingMode.REGULAR_EXPRESSION,
				DisplayMode.NORMAL,
				FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 1);

		assertEquals(13, filterList.size());

		dataProvider.setDataValue(1, 1, "(D|E|F){1}.*");

		assertEquals(3, filterList.size());
		
		Properties properties = new Properties();

		// save state
		dataProvider.saveState("prefix", properties);
		String persistedProperty = properties.getProperty("prefix" + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS);

		//check that the pipe character in the regular expression was replaced for persistence
		assertEquals("1:(D" 
				+ FilterRowDataProvider.PIPE_REPLACEMENT + "E" 
				+ FilterRowDataProvider.PIPE_REPLACEMENT + "F){1}.*|", persistedProperty);

		// reset state
		setup();
		assertEquals(13, filterList.size());

		// load state
		dataProvider.loadState("prefix", properties);

		//after loading the state, the pipes in the regular expression need to be restored correctly
		assertEquals("(D|E|F){1}.*", dataProvider.getDataValue(1, 1));
	}
}
