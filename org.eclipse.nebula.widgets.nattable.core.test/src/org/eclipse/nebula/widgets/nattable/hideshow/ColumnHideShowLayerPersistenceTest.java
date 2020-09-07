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
package org.eclipse.nebula.widgets.nattable.hideshow;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.junit.Before;
import org.junit.Test;

public class ColumnHideShowLayerPersistenceTest {

    private ColumnHideShowLayer layer;

    @Before
    public void setup() {
        this.layer = new ColumnHideShowLayer(new DataLayer(
                new DummyBodyDataProvider(10, 10)) {

            @Override
            public void saveState(String prefix, Properties properties) {
                // Do nothing
            }

            @Override
            public void loadState(String prefix, Properties properties) {
                // Do nothing
            }

        });
    }

    @Test
    public void testSaveState() {
        this.layer.hideColumnPositions(Arrays.asList(new Integer[] { 3, 5, 6 }));

        Properties properties = new Properties();
        this.layer.saveState("prefix", properties);

        assertEquals(1, properties.size());
        assertEquals("3,5,6", properties.getProperty("prefix" + ColumnHideShowLayer.PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES));
    }

    @Test
    public void testLoadState() {
        Properties properties = new Properties();
        properties.setProperty("prefix" + ColumnHideShowLayer.PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES, "1,3,5,");

        this.layer.loadState("prefix", properties);

        assertEquals(7, this.layer.getColumnCount());

        assertEquals(0, this.layer.getColumnIndexByPosition(0));
        assertEquals(2, this.layer.getColumnIndexByPosition(1));
        assertEquals(4, this.layer.getColumnIndexByPosition(2));
        assertEquals(6, this.layer.getColumnIndexByPosition(3));
        assertEquals(7, this.layer.getColumnIndexByPosition(4));
        assertEquals(8, this.layer.getColumnIndexByPosition(5));
        assertEquals(9, this.layer.getColumnIndexByPosition(6));
    }

    @Test
    public void testSaveResetSaveLoad() {
        // first hide a column and save
        this.layer.hideColumnPositions(Arrays.asList(new Integer[] { 1 }));

        assertEquals(1, this.layer.getHiddenColumnIndexes().size());

        Properties properties = new Properties();
        this.layer.saveState("prefix", properties);

        assertEquals(1, properties.size());
        assertEquals("1", properties.getProperty("prefix" + ColumnHideShowLayer.PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES));

        this.layer.showColumnIndexes(Arrays.asList(new Integer[] { 1 }));

        assertEquals(0, this.layer.getHiddenColumnIndexes().size());
        this.layer.saveState("prefix", properties);

        assertEquals(0, properties.size());

        this.layer.loadState("prefix", properties);

        assertEquals(0, this.layer.getHiddenColumnIndexes().size());
    }
}
