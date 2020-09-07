/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.NullComparator;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.NoScalingDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.sort.config.DefaultSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.swt.SWT;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

public class SortIntegrationTest {

    private static final String TEST_LABEL = "testLabel";
    private NatTableFixture nattable;
    private GlazedListsGridLayer<RowDataFixture> gridLayerStack;

    @Before
    public void setup() {
        EventList<RowDataFixture> eventList = GlazedLists
                .eventList(RowDataListFixture.getList().subList(0, 4));
        ConfigRegistry configRegistry = new ConfigRegistry();

        this.gridLayerStack = new GlazedListsGridLayer<>(
                GlazedLists.threadSafeList(eventList),
                RowDataListFixture.getPropertyNames(),
                RowDataListFixture.getPropertyToLabelMap(), configRegistry);

        this.nattable = new NatTableFixture(this.gridLayerStack, false);
        this.nattable.setConfigRegistry(configRegistry);
        this.nattable.addConfiguration(new DefaultNatTableStyleConfiguration());
        this.nattable.addConfiguration(new DefaultSortConfiguration());
        this.nattable.configure();

        this.nattable.doCommand(new ConfigureScalingCommand(new NoScalingDpiConverter()));
    }

    @After
    public void tearDown() {
        this.nattable.doCommand(new DisposeResourcesCommand());
    }

    @Test
    public void shouldSortByClickingOnTheHeader() {
        assertColumn2BeforeSort();

        // Ascending
        altClickColumn2Header();

        assertColumn2AscendingSort();

        // Descending
        altClickColumn2Header();

        // Assert presence of sort icon
        List<String> labels = this.nattable.getConfigLabelsByPosition(2, 0);
        assertEquals(4, labels.size());
        assertTrue(labels.contains("SORT"));
        assertTrue(labels.contains("SORT_DOWN"));
        assertTrue(labels.contains("SORT_SEQ_0"));
        assertTrue(labels.contains("COLUMN_HEADER"));

        assertColumn2DescendingSort();

        // Clear sort
        altClickColumn2Header();
        assertColumn2BeforeSort();
    }

    @Test
    public void shouldNotSortUnsortableColumns() {
        assertColumn2BeforeSort();

        // Register custom label on column 2 (index 1)
        DataLayer colHeaderDataLayer = this.gridLayerStack.getColumnHeaderDataLayer();
        this.nattable.registerLabelOnColumnHeader(colHeaderDataLayer, 1, TEST_LABEL);

        // Disable sorting on column 2 - null comparator
        this.nattable.getConfigRegistry().registerConfigAttribute(
                SortConfigAttributes.SORT_COMPARATOR, new NullComparator(),
                DisplayMode.NORMAL, TEST_LABEL);

        altClickColumn2Header();

        // Assert no sort icon is displayed
        List<String> labels = this.nattable.getConfigLabelsByPosition(2, 0);
        assertEquals(2, labels.size());
        assertTrue(labels.contains("COLUMN_HEADER"));
        assertTrue(labels.contains(TEST_LABEL));

        assertColumn2BeforeSort();
    }

    @Test
    public void shouldUseCustomCompratorsIfSpecified() {
        assertColumn2BeforeSort();

        // Register custom label on column 2 (index 1)
        DataLayer colHeaderDataLayer = this.gridLayerStack.getColumnHeaderDataLayer();
        this.nattable.registerLabelOnColumnHeader(colHeaderDataLayer, 1, TEST_LABEL);

        // Custom comparator on column 2
        this.nattable.getConfigRegistry().registerConfigAttribute(
                SortConfigAttributes.SORT_COMPARATOR, getGEAtTopComparator(),
                DisplayMode.NORMAL, TEST_LABEL);

        altClickColumn2Header();

        // Assert sort order - GE at top
        assertEquals("C General Electric Co", this.nattable.getCellByPosition(2, 1).getDataValue());
    }

    @Test
    public void multipleSort() {
        assertColumn2BeforeSort();

        altClickColumn2Header();
        altShiftClickColumn3Header();

        // Assert correct sort icons
        List<String> labels = this.nattable.getConfigLabelsByPosition(2, 0);
        assertEquals(4, labels.size());
        assertTrue(labels.contains("SORT"));
        assertTrue(labels.contains("SORT_UP"));
        assertTrue(labels.contains("SORT_SEQ_0"));
        assertTrue(labels.contains("COLUMN_HEADER"));

        labels = this.nattable.getConfigLabelsByPosition(3, 0);
        assertEquals(4, labels.size());
        assertTrue(labels.contains("SORT"));
        assertTrue(labels.contains("SORT_UP"));
        assertTrue(labels.contains("SORT_SEQ_1"));
        assertTrue(labels.contains("COLUMN_HEADER"));
    }

    @Test
    public void shouldPersistSortStateToProperties() throws Exception {
        altClickColumn2Header();
        altShiftClickColumn3Header();

        Properties properties = new Properties();
        this.nattable.saveState("test_prefix", properties);

        Object savedState = properties.get("test_prefix.COLUMN_HEADER.SortHeaderLayer.sortingState");
        assertNotNull(savedState);
        assertEquals("1:ASC:0|2:ASC:1|", savedState.toString());
    }

    @Test
    public void shouldRestoreSortStateFromProperties() throws Exception {
        // save
        altClickColumn2Header();
        altShiftClickColumn3Header();
        Properties properties = new Properties();
        this.nattable.saveState("test_prefix", properties);

        // clear
        tearDown();
        setup();
        assertColumn2BeforeSort();

        // load
        this.nattable.loadState("test_prefix", properties);
        assertColumn2AscendingSort();
    }

    // Convenience methods

    private void altShiftClickColumn3Header() {
        this.nattable.forceFocus();
        SWTUtils.leftClick(DataLayer.DEFAULT_COLUMN_WIDTH * 3, 15, SWT.ALT
                | SWT.SHIFT, this.nattable);
    }

    private void altClickColumn2Header() {
        this.nattable.forceFocus();
        SWTUtils.leftClick(DataLayer.DEFAULT_COLUMN_WIDTH * 2, 15, SWT.ALT,
                this.nattable);
    }

    private Comparator<?> getGEAtTopComparator() {
        return new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1.toString().contains("General Electric"))
                    return -1;
                else
                    return 0;
            }
        };
    }

    private void assertColumn2BeforeSort() {
        assertEquals("B Ford Motor", this.nattable.getCellByPosition(2, 1).getDataValue());
        assertEquals("A Alphabet Co.", this.nattable.getCellByPosition(2, 2).getDataValue());
        assertEquals("C General Electric Co", this.nattable.getCellByPosition(2, 3).getDataValue());
        assertEquals("E Nissan Motor Co., Ltd.", this.nattable.getCellByPosition(2, 4).getDataValue());
    }

    private void assertColumn2AscendingSort() {
        assertEquals("A Alphabet Co.", this.nattable.getCellByPosition(2, 1).getDataValue());
        assertEquals("B Ford Motor", this.nattable.getCellByPosition(2, 2).getDataValue());
        assertEquals("C General Electric Co", this.nattable.getCellByPosition(2, 3).getDataValue());
        assertEquals("E Nissan Motor Co., Ltd.", this.nattable.getCellByPosition(2, 4).getDataValue());
    }

    private void assertColumn2DescendingSort() {
        assertEquals("E Nissan Motor Co., Ltd.", this.nattable.getCellByPosition(2, 1).getDataValue());
        assertEquals("C General Electric Co", this.nattable.getCellByPosition(2, 2).getDataValue());
        assertEquals("B Ford Motor", this.nattable.getCellByPosition(2, 3).getDataValue());
        assertEquals("A Alphabet Co.", this.nattable.getCellByPosition(2, 4).getDataValue());
    }
}
