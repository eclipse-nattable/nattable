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
package org.eclipse.nebula.widgets.nattable.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.sort.SortStatePersistor.SortState;
import org.eclipse.nebula.widgets.nattable.test.fixture.SortModelFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SortStatePersistorTest {

    private static final String TEST_PREFIX = "test_prefix";
    private static final String KEY = TEST_PREFIX + SortStatePersistor.PERSISTENCE_KEY_SORTING_STATE;
    private Properties properties;
    private SortModelFixture sortModel;
    private SortHeaderLayer<RowDataFixture> sortHeaderLayer;
    private SortStatePersistor<RowDataFixture> sortStatePersistor;

    @BeforeEach
    public void setup() {
        this.properties = new Properties();
        this.sortModel = new SortModelFixture();
        this.sortHeaderLayer = new SortHeaderLayer<>(new DataLayerFixture(10, 20, 100, 20), this.sortModel);
        this.sortStatePersistor = new SortStatePersistor<>(this.sortModel);
    }

    @Test
    public void shouldSaveSortedColumnsAndSortOrder() {
        this.sortStatePersistor.saveState(TEST_PREFIX, this.properties);
        String savedProperty = this.properties.getProperty(KEY);
        assertEquals("0:DESC:3|5:DESC:1|6:ASC:0|3:ASC:2|", savedProperty);
    }

    @Test
    public void shouldNotSaveIfNothingIsSorted() {
        this.sortModel = SortModelFixture.getEmptyModel();
        this.sortHeaderLayer = new SortHeaderLayer<>(new DataLayerFixture(10, 20, 100, 20), this.sortModel);
        this.sortStatePersistor = new SortStatePersistor<>(this.sortModel);

        this.sortStatePersistor.saveState(TEST_PREFIX, this.properties);
        assertNull(this.properties.getProperty(KEY));
    }

    /**
     * Save string format: column index : sort direction : sort order |
     */
    @Test
    public void loadStateFromProperties() {
        this.properties.put(KEY, "0:DESC:3|3:ASC:2|5:DESC:1|6:ASC:0|");
        this.sortModel = SortModelFixture.getEmptyModel();
        this.sortHeaderLayer = new SortHeaderLayer<>(new DataLayerFixture(10, 20, 100, 20), this.sortModel);
        this.sortStatePersistor = new SortStatePersistor<>(this.sortModel);
        this.sortStatePersistor.loadState(TEST_PREFIX, this.properties);
        ISortModel sortModel = this.sortHeaderLayer.getSortModel();

        // Sort direction
        assertEquals(SortDirectionEnum.DESC, sortModel.getSortDirection(0));
        assertEquals(SortDirectionEnum.ASC, sortModel.getSortDirection(3));
        assertEquals(SortDirectionEnum.DESC, sortModel.getSortDirection(5));
        assertEquals(SortDirectionEnum.ASC, sortModel.getSortDirection(6));

        // Sort order
        assertEquals(3, sortModel.getSortOrder(0));
        assertEquals(2, sortModel.getSortOrder(3));
        assertEquals(1, sortModel.getSortOrder(5));
        assertEquals(0, sortModel.getSortOrder(6));

        // No other columns should be flagged as sorted
        assertEquals(-1, sortModel.getSortOrder(4));
        assertFalse(sortModel.isColumnIndexSorted(4));
        assertEquals(SortDirectionEnum.NONE, sortModel.getSortDirection(4));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void shouldParseTheSavedStringCorrectly() throws Exception {
        SortState sortState = this.sortStatePersistor.getSortStateFromString("0:DESC:3");
        assertEquals(0, sortState.columnIndex);
        assertEquals(SortDirectionEnum.DESC, sortState.sortDirection);
        assertEquals(3, sortState.sortOrder);
    }

    @Test
    public void shouldRemoveStateAfterClear() {
        // first check that a sort state is persisted in the properties
        this.sortHeaderLayer.saveState(TEST_PREFIX, this.properties);
        String savedProperty = this.properties.getProperty(KEY);
        assertEquals("0:DESC:3|5:DESC:1|6:ASC:0|3:ASC:2|", savedProperty);

        this.sortHeaderLayer.getSortModel().clear();

        // now check that the sort state is removed from the properties
        this.sortHeaderLayer.saveState(TEST_PREFIX, this.properties);
        assertNull(this.properties.getProperty(KEY));
    }
}
