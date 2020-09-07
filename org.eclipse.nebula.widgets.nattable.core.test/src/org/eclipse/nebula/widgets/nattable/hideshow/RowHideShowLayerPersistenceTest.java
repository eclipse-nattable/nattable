/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.junit.Before;
import org.junit.Test;

public class RowHideShowLayerPersistenceTest {

    private RowHideShowLayer layer;

    @Before
    public void setup() {
        this.layer = new RowHideShowLayer(new DataLayer(new DummyBodyDataProvider(
                10, 10)) {

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
        this.layer.hideRowPositions(Arrays.asList(new Integer[] { 3, 5, 6 }));

        Properties properties = new Properties();
        this.layer.saveState("prefix", properties);

        assertEquals(1, properties.size());
        assertEquals(
                "3,5,6",
                properties.getProperty("prefix"
                        + RowHideShowLayer.PERSISTENCE_KEY_HIDDEN_ROW_INDEXES));
    }

    @Test
    public void testLoadState() {
        Properties properties = new Properties();
        properties
                .setProperty("prefix"
                        + RowHideShowLayer.PERSISTENCE_KEY_HIDDEN_ROW_INDEXES,
                        "1,3,5,");

        this.layer.loadState("prefix", properties);

        assertEquals(7, this.layer.getRowCount());

        assertEquals(0, this.layer.getRowIndexByPosition(0));
        assertEquals(2, this.layer.getRowIndexByPosition(1));
        assertEquals(4, this.layer.getRowIndexByPosition(2));
        assertEquals(6, this.layer.getRowIndexByPosition(3));
        assertEquals(7, this.layer.getRowIndexByPosition(4));
        assertEquals(8, this.layer.getRowIndexByPosition(5));
        assertEquals(9, this.layer.getRowIndexByPosition(6));
    }
}
