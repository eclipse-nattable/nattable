/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.LayerListenerFixture;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowDataLayer;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearAllFiltersCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ClearFilterCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.command.ToggleFilterRowCommand;
import org.eclipse.nebula.widgets.nattable.filterrow.config.DefaultFilterRowConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;

public class FilterRowDataLayerTest {

    private DataLayerFixture columnHeaderLayer;
    private IConfigRegistry configRegistry;
    private FilterList<RowDataFixture> filterList;
    private FilterRowDataLayer<RowDataFixture> layerUnderTest;
    private LayerListenerFixture listener;

    @BeforeEach
    public void setup() {
        this.columnHeaderLayer = new DataLayerFixture(10, 2, 100, 50);

        this.configRegistry = new ConfigRegistry();
        new DefaultNatTableStyleConfiguration()
                .configureRegistry(this.configRegistry);
        new DefaultFilterRowConfiguration().configureRegistry(this.configRegistry);

        this.filterList = new FilterList<RowDataFixture>(
                GlazedLists.eventList(RowDataListFixture.getList()));

        this.layerUnderTest = new FilterRowDataLayer<RowDataFixture>(
                new DefaultGlazedListsFilterStrategy<RowDataFixture>(
                        this.filterList,
                        new ReflectiveColumnPropertyAccessor<RowDataFixture>(
                                RowDataListFixture.getPropertyNames()),
                        this.configRegistry),
                this.columnHeaderLayer,
                this.columnHeaderLayer.getDataProvider(), this.configRegistry);
        this.listener = new LayerListenerFixture();
        this.layerUnderTest.addLayerListener(this.listener);
    }

    @Test
    public void shouldHandleClearFilterCommand() throws Exception {
        assertEquals(13, this.filterList.size());

        this.layerUnderTest.doCommand(new UpdateDataCommand(this.layerUnderTest, 1, 0,
                "ford"));
        assertEquals(1, this.filterList.size());

        this.layerUnderTest.doCommand(new ClearFilterCommand(this.layerUnderTest, 1));
        assertEquals(13, this.filterList.size());

        this.listener.containsInstanceOf(RowStructuralRefreshEvent.class);
    }

    @Test
    public void shouldHandleTheClearAllFiltersCommand() throws Exception {
        assertEquals(13, this.filterList.size());

        this.layerUnderTest.doCommand(new UpdateDataCommand(this.layerUnderTest, 1, 0,
                "ford"));
        assertEquals(1, this.filterList.size());

        this.layerUnderTest.doCommand(new UpdateDataCommand(this.layerUnderTest, 0, 0,
                "XXX"));
        assertEquals(0, this.filterList.size());

        this.layerUnderTest.doCommand(new ClearAllFiltersCommand());
        assertEquals(13, this.filterList.size());

        this.listener.containsInstanceOf(RowStructuralRefreshEvent.class);
    }

    @Test
    public void shouldHandleTheToggeleFilterRowCommand() throws Exception {
        assertEquals(1, this.layerUnderTest.getRowCount());
        this.layerUnderTest.doCommand(new ToggleFilterRowCommand());
        // as the command is handled by the FilterRowHeaderComposite now, it
        // should
        // have no effect to do the command on the FilterRowDataLayer
        assertEquals(1, this.layerUnderTest.getRowCount());
    }

    @Test
    public void saveState() throws Exception {
        this.layerUnderTest.setDataValue(1, 1, "testValue");
        this.layerUnderTest.setDataValue(2, 1, "testValue");
        this.layerUnderTest.setDataValue(3, 1, "testValue");
        this.layerUnderTest.setDataValue(2, 1, null); // clear filter

        Properties properties = new Properties();

        // save state
        this.layerUnderTest.saveState("prefix", properties);
        String persistedProperty = properties.getProperty("prefix"
                + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS);

        assertEquals("1:testValue|3:testValue|", persistedProperty);
    }

    @Test
    public void loadState() throws Exception {
        Properties properties = new Properties();
        properties.put("prefix"
                + FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS,
                "1:testValue|3:testValue|");

        // load state
        this.layerUnderTest.loadState("prefix", properties);

        assertEquals("testValue", this.layerUnderTest.getDataValue(1, 1));
        assertNull(this.layerUnderTest.getDataValue(2, 1));
        assertEquals("testValue", this.layerUnderTest.getDataValue(3, 1));
    }

    @Test
    public void testUnregisterPersistable() {
        this.layerUnderTest.unregisterPersistable(this.layerUnderTest
                .getFilterRowDataProvider());

        this.layerUnderTest.setDataValue(1, 1, "testValue");
        this.layerUnderTest.setDataValue(2, 1, "testValue");
        this.layerUnderTest.setDataValue(3, 1, "testValue");
        this.layerUnderTest.setDataValue(2, 1, null); // clear filter

        Properties properties = new Properties();

        // save state
        this.layerUnderTest.saveState("prefix", properties);
        for (Object key : properties.keySet()) {
            if (key.toString().contains(
                    FilterRowDataLayer.PERSISTENCE_KEY_FILTER_ROW_TOKENS)) {
                fail("Filter state saved");
            }
        }
    }
}
