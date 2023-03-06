/*******************************************************************************
 * Copyright (c) 2012, 2023 Original authors and others.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.ContextualDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDoubleDisplayConverter;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.edit.EditConstants;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.LayerListenerFixture;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.filterrow.config.DefaultFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.filterrow.config.FilterRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.filterrow.event.FilterAppliedEvent;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.persistence.IPersistable;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;

public class FilterRowDataProviderTest {

    private FilterRowDataProvider<RowDataFixture> dataProvider;
    private DataLayerFixture columnHeaderLayer;
    private EventList<RowDataFixture> baseList;
    private FilterList<RowDataFixture> filterList;
    private ConfigRegistry configRegistry;

    private ReflectiveColumnPropertyAccessor<RowDataFixture> columnAccessor =
            new ReflectiveColumnPropertyAccessor<>(RowDataListFixture.getPropertyNames());

    @BeforeEach
    public void setup() {
        this.columnHeaderLayer = new DataLayerFixture(10, 2, 100, 50);

        this.configRegistry = new ConfigRegistry();
        new DefaultNatTableStyleConfiguration().configureRegistry(this.configRegistry);
        new DefaultFilterRowConfiguration().configureRegistry(this.configRegistry);

        this.baseList = GlazedLists.eventList(RowDataListFixture.getList());
        this.filterList = new FilterList<>(this.baseList);

        this.dataProvider = new FilterRowDataProvider<>(
                new DefaultGlazedListsFilterStrategy<>(
                        this.filterList,
                        this.columnAccessor,
                        this.configRegistry),
                this.columnHeaderLayer,
                this.columnHeaderLayer.getDataProvider(),
                this.configRegistry);
    }

    @Test
    public void shouldSetDataValue() {
        assertNull(this.dataProvider.getDataValue(1, 1));

        this.dataProvider.setDataValue(1, 1, "testValue");
        assertEquals("testValue", this.dataProvider.getDataValue(1, 1));
    }

    @Test
    public void shouldApplyTextFilterOnSettingTextValue() {
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
    public void shouldUpdateFilterOnSettingThresholdValues() {
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
    public void shouldFireUpdateEventOnSettingAValue() {
        final LayerListenerFixture listener = new LayerListenerFixture();
        this.columnHeaderLayer.addLayerListener(listener);
        this.dataProvider.setDataValue(3, 1, "testValue");

        assertEquals(1, listener.getEventsCount());
        assertNotNull(listener.getReceivedEvent(FilterAppliedEvent.class));
    }

    @Test
    public void shouldFireUpdateEventOnClearingFilter() {
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
    public void shouldFireUpdateEventOnLoadingState() {
        final LayerListenerFixture listener = new LayerListenerFixture();
        this.columnHeaderLayer.addLayerListener(listener);

        Properties properties = new Properties();
        properties.put("prefix" + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS, "1:testValue|3:testValue|");

        this.dataProvider.loadState("prefix", properties);

        assertEquals(1, listener.getEventsCount());
        assertNotNull(listener.getReceivedEvent(FilterAppliedEvent.class));
    }

    @Test
    public void shouldSaveState() {
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
    public void shouldHandleRegularExpressionWithPipes() {
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
    public void shouldPersistRegularExpressionWithPipes() {
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

    @Test
    public void shouldRetainColonInFilterPersistence() {
        this.dataProvider.setDataValue(1, 1, "test:Value");
        this.dataProvider.setDataValue(2, 1, ":testValue");
        this.dataProvider.setDataValue(3, 1, "testValue:");
        this.dataProvider.setDataValue(4, 1, ":testValue:");
        this.dataProvider.setDataValue(5, 1, ":test:Value:");

        Properties properties = new Properties();

        // save state
        this.dataProvider.saveState("prefix", properties);
        String persistedProperty = properties.getProperty("prefix" + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS);

        assertEquals("1:test:Value|2::testValue|3:testValue:|4::testValue:|5::test:Value:|", persistedProperty);

        // reset state
        setup();

        assertNull(this.dataProvider.getDataValue(1, 1));
        assertNull(this.dataProvider.getDataValue(2, 1));
        assertNull(this.dataProvider.getDataValue(3, 1));
        assertNull(this.dataProvider.getDataValue(4, 1));
        assertNull(this.dataProvider.getDataValue(5, 1));

        // load state
        this.dataProvider.loadState("prefix", properties);

        assertEquals("test:Value", this.dataProvider.getDataValue(1, 1));
        assertEquals(":testValue", this.dataProvider.getDataValue(2, 1));
        assertEquals("testValue:", this.dataProvider.getDataValue(3, 1));
        assertEquals(":testValue:", this.dataProvider.getDataValue(4, 1));
        assertEquals(":test:Value:", this.dataProvider.getDataValue(5, 1));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void shouldRetainCommaInFilterPersistence() {
        // first check that a sort state is persisted in the properties
        this.dataProvider.setDataValue(1, 1, "foo,bar");
        this.dataProvider.setDataValue(3, 1, new ArrayList<>(Arrays.asList("foo", "bar", "foo,bar")));

        Properties properties = new Properties();

        // save state
        this.dataProvider.saveState("prefix", properties);
        String persistedProperty = properties.getProperty("prefix" + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS);

        String expectedPersistedCollection = FilterRowDataProvider.FILTER_COLLECTION_PREFIX + ArrayList.class.getName() + ")["
                + "foo" + IPersistable.VALUE_SEPARATOR
                + "bar" + IPersistable.VALUE_SEPARATOR
                + "foo" + FilterRowDataProvider.COMMA_REPLACEMENT + "bar"
                + "]";

        assertEquals("1:foo,bar|3:" + expectedPersistedCollection + "|", persistedProperty);

        // reset state
        setup();

        assertNull(this.dataProvider.getDataValue(1, 1));
        assertNull(this.dataProvider.getDataValue(3, 1));

        // load state
        this.dataProvider.loadState("prefix", properties);

        assertEquals("foo,bar", this.dataProvider.getDataValue(1, 1));
        Collection data = (Collection) this.dataProvider.getDataValue(3, 1);
        assertEquals(3, data.size());
        assertEquals(new ArrayList<>(Arrays.asList("foo", "bar", "foo,bar")), data);
    }

    @Test
    public void shouldPersistWithContextualDisplayConverter() {
        this.configRegistry.registerConfigAttribute(
                CellConfigAttributes.DISPLAY_CONVERTER,
                new ContextualDisplayConverter() {

                    @Override
                    public Object canonicalToDisplayValue(ILayerCell cell, IConfigRegistry configRegistry, Object canonicalValue) {
                        return canonicalValue.toString() + "_" + cell.getColumnIndex();
                    }

                    @Override
                    public Object displayToCanonicalValue(ILayerCell cell, IConfigRegistry configRegistry, Object displayValue) {
                        return displayValue.toString().substring(0, displayValue.toString().length() - 2);
                    }

                });

        this.dataProvider.setDataValue(1, 1, "foo");
        this.dataProvider.setDataValue(2, 1, "testValue");

        Properties properties = new Properties();

        // save state
        this.dataProvider.saveState("prefix", properties);
        String persistedProperty = properties.getProperty("prefix" + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS);

        assertEquals("1:foo_1|2:testValue_2|", persistedProperty);

        // reset state
        this.dataProvider.clearAllFilters();

        assertNull(this.dataProvider.getDataValue(1, 1));

        // load state
        this.dataProvider.loadState("prefix", properties);

        assertEquals("foo", this.dataProvider.getDataValue(1, 1));
        assertEquals("testValue", this.dataProvider.getDataValue(2, 1));
    }

    @Test
    public void shouldInvertFilterCollectionPersistence() {
        // enable inverted combobox filter persistence
        FilterRowComboBoxDataProvider<RowDataFixture> cbdp =
                new FilterRowComboBoxDataProvider<>(
                        new DataLayer(new ListDataProvider<>(this.filterList, this.columnAccessor)),
                        this.baseList,
                        this.columnAccessor);
        this.dataProvider.setFilterRowComboBoxDataProvider(cbdp);

        // set filter to filter out AAA and aaa
        this.dataProvider.setDataValue(2, 1, new ArrayList<>(Arrays.asList("A-", "AA", "B", "B-", "BB", "C", "a", "aa")));

        // save state
        Properties properties = new Properties();
        this.dataProvider.saveState("prefix", properties);
        String persistedProperty = properties.getProperty("prefix" + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS);

        String expectedPersistedCollection = "2:" + FilterRowDataProvider.FILTER_COLLECTION_PREFIX + ArrayList.class.getName() + ")["
                + "AAA" + IPersistable.VALUE_SEPARATOR
                + "aaa"
                + "]";

        assertEquals(expectedPersistedCollection + "|", persistedProperty);

        // reset state
        this.dataProvider.clearAllFilters();

        assertNull(this.dataProvider.getDataValue(2, 1));

        // load state
        this.dataProvider.loadState("prefix", properties);

        assertEquals(new ArrayList<>(Arrays.asList("A-", "AA", "B", "B-", "BB", "C", "a", "aa")), this.dataProvider.getDataValue(2, 1));
    }

    @Test
    public void shouldInvertAllSelectedFilterCollectionPersistence() {
        // enable inverted combobox filter persistence
        FilterRowComboBoxDataProvider<RowDataFixture> cbdp =
                new FilterRowComboBoxDataProvider<>(
                        new DataLayer(new ListDataProvider<>(this.filterList, this.columnAccessor)),
                        this.baseList,
                        this.columnAccessor);
        this.dataProvider.setFilterRowComboBoxDataProvider(cbdp);

        // set filter to select all, which means nothing is filtered
        this.dataProvider.setDataValue(2, 1, new ArrayList<>(Arrays.asList("A-", "AA", "AAA", "B", "B-", "BB", "C", "a", "aa", "aaa")));

        // save state
        Properties properties = new Properties();
        this.dataProvider.saveState("prefix", properties);
        String persistedProperty = properties.getProperty("prefix" + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS);

        String expectedPersistedCollection = "2:" + FilterRowDataProvider.FILTER_COLLECTION_PREFIX + ArrayList.class.getName() + ")[]";

        assertEquals(expectedPersistedCollection + "|", persistedProperty);

        // reset state
        this.dataProvider.clearAllFilters();

        assertNull(this.dataProvider.getDataValue(2, 1));

        // load state
        this.dataProvider.loadState("prefix", properties);

        assertEquals(EditConstants.SELECT_ALL_ITEMS_VALUE, this.dataProvider.getDataValue(2, 1));
    }

    @Test
    public void shouldInvertFilterCollectionNullValuePersistence() {
        // enable inverted combobox filter persistence
        FilterRowComboBoxDataProvider<RowDataFixture> cbdp =
                new FilterRowComboBoxDataProvider<>(
                        new DataLayer(new ListDataProvider<>(this.filterList, this.columnAccessor)),
                        this.baseList,
                        this.columnAccessor);
        this.dataProvider.setFilterRowComboBoxDataProvider(cbdp);

        this.filterList.get(0).setRating(null);

        assertEquals(Arrays.asList(null, "A-", "AA", "AAA", "B", "B-", "BB", "C", "aa", "aaa"), cbdp.getAllValues(2));

        // set filter to filter out null values
        this.dataProvider.setDataValue(2, 1, new ArrayList<>(Arrays.asList("A-", "AA", "AAA", "B", "B-", "BB", "C", "aa", "aaa")));

        // save state
        Properties properties = new Properties();
        this.dataProvider.saveState("prefix", properties);
        String persistedProperty = properties.getProperty("prefix" + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS);

        String expectedPersistedCollection = "2:" + FilterRowDataProvider.FILTER_COLLECTION_PREFIX + ArrayList.class.getName() + ")["
                + FilterRowDataProvider.NULL_REPLACEMENT
                + "]";

        assertEquals(expectedPersistedCollection + "|", persistedProperty);

        // reset state
        this.dataProvider.clearAllFilters();

        assertNull(this.dataProvider.getDataValue(2, 1));

        // load state
        this.dataProvider.loadState("prefix", properties);

        assertEquals(new ArrayList<>(Arrays.asList("A-", "AA", "AAA", "B", "B-", "BB", "C", "aa", "aaa")), this.dataProvider.getDataValue(2, 1));
    }

    @Test
    public void shouldInvertFilterCollectionEmptyValuePersistence() {
        // enable inverted combobox filter persistence
        FilterRowComboBoxDataProvider<RowDataFixture> cbdp =
                new FilterRowComboBoxDataProvider<>(
                        new DataLayer(new ListDataProvider<>(this.filterList, this.columnAccessor)),
                        this.baseList,
                        this.columnAccessor);
        this.dataProvider.setFilterRowComboBoxDataProvider(cbdp);

        this.filterList.get(0).setRating("");

        assertEquals(Arrays.asList("", "A-", "AA", "AAA", "B", "B-", "BB", "C", "aa", "aaa"), cbdp.getAllValues(2));

        // set filter to filter out null values
        this.dataProvider.setDataValue(2, 1, new ArrayList<>(Arrays.asList("A-", "AA", "AAA", "B", "B-", "BB", "C", "aa", "aaa")));

        // save state
        Properties properties = new Properties();
        this.dataProvider.saveState("prefix", properties);
        String persistedProperty = properties.getProperty("prefix" + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS);

        String expectedPersistedCollection = "2:" + FilterRowDataProvider.FILTER_COLLECTION_PREFIX + ArrayList.class.getName() + ")["
                + FilterRowDataProvider.EMPTY_REPLACEMENT
                + "]";

        assertEquals(expectedPersistedCollection + "|", persistedProperty);

        // reset state
        this.dataProvider.clearAllFilters();

        assertNull(this.dataProvider.getDataValue(2, 1));

        // load state
        this.dataProvider.loadState("prefix", properties);

        assertEquals(new ArrayList<>(Arrays.asList("A-", "AA", "AAA", "B", "B-", "BB", "C", "aa", "aaa")), this.dataProvider.getDataValue(2, 1));
    }
}