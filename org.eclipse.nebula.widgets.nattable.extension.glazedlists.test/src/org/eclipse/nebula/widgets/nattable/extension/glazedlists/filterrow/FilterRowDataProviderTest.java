/*******************************************************************************
 * Copyright (c) 2012, 2021 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDoubleDisplayConverter;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.LayerListenerFixture;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.eclipse.nebula.widgets.nattable.filterrow.config.DefaultFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.filterrow.event.FilterAppliedEvent;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;

public class FilterRowDataProviderTest {

    private FilterRowDataProvider<RowDataFixture> dataProvider;
    private DataLayerFixture columnHeaderLayer;
    private FilterList<RowDataFixture> filterList;
    private ConfigRegistry configRegistry;

    @BeforeEach
    public void setup() {
        this.columnHeaderLayer = new DataLayerFixture(10, 2, 100, 50);

        this.configRegistry = new ConfigRegistry();
        new DefaultNatTableStyleConfiguration().configureRegistry(this.configRegistry);
        new DefaultFilterRowConfiguration().configureRegistry(this.configRegistry);

        this.filterList = new FilterList<>(GlazedLists.eventList(RowDataListFixture.getList()));

        this.dataProvider = new FilterRowDataProvider<>(
                new DefaultGlazedListsFilterStrategy<>(
                        this.filterList,
                        new ReflectiveColumnPropertyAccessor<RowDataFixture>(RowDataListFixture.getPropertyNames()),
                        this.configRegistry),
                this.columnHeaderLayer,
                this.columnHeaderLayer.getDataProvider(),
                this.configRegistry);
    }

    @Test
    public void setDataValue() {
        assertNull(this.dataProvider.getDataValue(1, 1));

        this.dataProvider.setDataValue(1, 1, "testValue");
        assertEquals("testValue", this.dataProvider.getDataValue(1, 1));
    }

    @Test
    public void settingTextValueAppliesTextFilter() {
        // original size
        assertEquals(13, this.filterList.size());

        // Apply filter
        this.dataProvider.setDataValue(1, 1, "ford");

        // list filtered
        assertEquals(1, this.filterList.size());

        // remove filter
        this.dataProvider.setDataValue(1, 1, null);

        assertEquals(13, this.filterList.size());
    }

    @Test
    public void settingThresholdValuesUpdatedFilter() {
        // Since we are triggering object comparison, we must provide the right
        // type
        this.configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
                new DefaultDoubleDisplayConverter(),
                DisplayMode.NORMAL,
                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 5);
        // We also have to set the text matching mode
        this.configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                TextMatchingMode.REGULAR_EXPRESSION,
                DisplayMode.NORMAL,
                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 5);

        assertEquals(13, this.filterList.size());

        // Index 5, 'bid' column
        this.dataProvider.setDataValue(5, 1, ">20");

        assertEquals(6, this.filterList.size());
    }

    @Test
    public void settingAValueFiresUpdateEvent() {
        final LayerListenerFixture listener = new LayerListenerFixture();
        this.columnHeaderLayer.addLayerListener(listener);
        this.dataProvider.setDataValue(3, 1, "testValue");

        assertEquals(1, listener.getEventsCount());
        assertNotNull(listener.getReceivedEvent(FilterAppliedEvent.class));
    }

    @Test
    public void clearingFilterFiresUpdateEvent() {
        final LayerListenerFixture listener = new LayerListenerFixture();
        this.columnHeaderLayer.addLayerListener(listener);

        // original size
        assertEquals(13, this.filterList.size());

        // Apply filter
        this.dataProvider.setDataValue(1, 1, "ford");

        // list filtered
        assertEquals(1, this.filterList.size());

        // remove filter
        this.dataProvider.clearAllFilters();

        assertEquals(13, this.filterList.size());

        assertEquals(2, listener.getEventsCount());
        assertNotNull(listener.getReceivedEvent(FilterAppliedEvent.class));
    }

    @Test
    public void loadingStateFiresUpdateEvent() {
        final LayerListenerFixture listener = new LayerListenerFixture();
        this.columnHeaderLayer.addLayerListener(listener);

        Properties properties = new Properties();
        properties.put("prefix" + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS, "1:testValue|3:testValue|");

        this.dataProvider.loadState("prefix", properties);

        assertEquals(1, listener.getEventsCount());
        assertNotNull(listener.getReceivedEvent(FilterAppliedEvent.class));
    }

    @Test
    public void persistence() {
        this.dataProvider.setDataValue(1, 1, "testValue");
        this.dataProvider.setDataValue(2, 1, "testValue");
        this.dataProvider.setDataValue(3, 1, "testValue");
        this.dataProvider.setDataValue(2, 1, null); // clear filter

        Properties properties = new Properties();

        // save state
        this.dataProvider.saveState("prefix", properties);
        String persistedProperty = properties.getProperty("prefix" + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS);

        assertEquals("1:testValue|3:testValue|", persistedProperty);

        // reset state
        setup();

        // load state
        this.dataProvider.loadState("prefix", properties);

        assertEquals("testValue", this.dataProvider.getDataValue(1, 1));
        assertEquals(null, this.dataProvider.getDataValue(2, 1));
        assertEquals("testValue", this.dataProvider.getDataValue(3, 1));
    }

    @Test
    public void shouldRecoverFromCorruptPersistedState() {
        Properties properties = new Properties();

        properties.put("prefix.filterTokens", "XX");
        this.dataProvider.loadState("prefix", properties);

        assertEquals(null, this.dataProvider.getDataValue(1, 1));
        assertEquals(null, this.dataProvider.getDataValue(2, 1));
        assertEquals(null, this.dataProvider.getDataValue(3, 1));
    }

    @Test
    public void shouldRemoveNonFilteredColumnsWhenLoadingState() {
        this.dataProvider.setDataValue(1, 1, "testValue");
        this.dataProvider.setDataValue(2, 1, "testValue");

        Properties properties = new Properties();

        // save state
        this.dataProvider.saveState("prefix", properties);

        // load a different configuration
        Properties differentState = new Properties();
        differentState.put("prefix.filterTokens", "2:newTestValue|3:newTestValue");

        this.dataProvider.loadState("prefix", differentState);

        assertNull(this.dataProvider.getDataValue(1, 1), "Filter on column 1 has not been removed");
        assertEquals("newTestValue", this.dataProvider.getDataValue(2, 1));
        assertEquals("newTestValue", this.dataProvider.getDataValue(3, 1));
    }

    @Test
    public void testRegularExpressionWithPipes() {
        this.configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                TextMatchingMode.REGULAR_EXPRESSION,
                DisplayMode.NORMAL,
                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 1);

        assertEquals(13, this.filterList.size());

        this.dataProvider.setDataValue(1, 1, "(D|E|F){1}.*");

        assertEquals(3, this.filterList.size());
    }

    @Test
    public void testPersistenceRegularExpressionWithPipes() {
        this.configRegistry.registerConfigAttribute(
                FilterRowConfigAttributes.TEXT_MATCHING_MODE,
                TextMatchingMode.REGULAR_EXPRESSION,
                DisplayMode.NORMAL,
                FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 1);

        assertEquals(13, this.filterList.size());

        this.dataProvider.setDataValue(1, 1, "(D|E|F){1}.*");

        assertEquals(3, this.filterList.size());

        Properties properties = new Properties();

        // save state
        this.dataProvider.saveState("prefix", properties);
        String persistedProperty = properties.getProperty("prefix" + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS);

        // check that the pipe character in the regular expression was replaced
        // for persistence
        assertEquals("1:(D" + FilterRowDataProvider.PIPE_REPLACEMENT + "E" + FilterRowDataProvider.PIPE_REPLACEMENT + "F){1}.*|", persistedProperty);

        // reset state
        setup();
        assertEquals(13, this.filterList.size());

        // load state
        this.dataProvider.loadState("prefix", properties);

        // after loading the state, the pipes in the regular expression need to
        // be restored correctly
        assertEquals("(D|E|F){1}.*", this.dataProvider.getDataValue(1, 1));
    }

    @Test
    public void shouldRemoveStateAfterClear() {
        // first check that a sort state is persisted in the properties
        this.dataProvider.setDataValue(1, 1, "testValue");
        this.dataProvider.setDataValue(3, 1, "testValue");

        Properties properties = new Properties();

        // save state
        this.dataProvider.saveState("prefix", properties);
        String persistedProperty = properties.getProperty("prefix" + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS);

        assertEquals("1:testValue|3:testValue|", persistedProperty);

        this.dataProvider.clearAllFilters();

        // now check that the sort state is removed from the properties
        this.dataProvider.saveState("prefix", properties);
        assertNull(properties.getProperty("prefix" + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS));
    }
}