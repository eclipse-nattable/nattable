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
package org.eclipse.nebula.widgets.nattable.layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

public class DataLayerPersistenceTest {

    private DataLayer dataLayer;

    @Before
    public void setup() {
        this.dataLayer = new DataLayer(new DummyBodyDataProvider(10, 10));
    }

    @Test
    public void testSaveState() {
        this.dataLayer.setColumnWidthByPosition(5, 10);
        this.dataLayer.setColumnPercentageSizing(true);

        Properties properties = new Properties();
        this.dataLayer.saveState("prefix", properties);

        assertEquals(11, properties.size());
        assertEquals("100", properties.getProperty("prefix.columnWidth.defaultSize"));
        assertEquals("5:10,", properties.getProperty("prefix.columnWidth.sizes"));
        assertTrue(Boolean.valueOf(properties.getProperty("prefix.columnWidth.resizableByDefault")));
        assertTrue(Boolean.valueOf(properties.getProperty("prefix.columnWidth.percentageSizing")));
        assertTrue(Boolean.valueOf(properties.getProperty("prefix.columnWidth.distributeRemainingSpace")));
        assertEquals("0", properties.getProperty("prefix.columnWidth.defaultMinSize"));
        assertEquals("20", properties.getProperty("prefix.rowHeight.defaultSize"));
        assertTrue(Boolean.valueOf(properties.getProperty("prefix.rowHeight.resizableByDefault")));
        assertFalse(Boolean.valueOf(properties.getProperty("prefix.rowHeight.percentageSizing")));
        assertTrue(Boolean.valueOf(properties.getProperty("prefix.rowHeight.distributeRemainingSpace")));
        assertEquals("0", properties.getProperty("prefix.rowHeight.defaultMinSize"));
    }

    @Test
    public void testLoadState() {
        LayerListenerFixture listener = new LayerListenerFixture();
        this.dataLayer.addLayerListener(listener);

        Properties properties = new Properties();
        properties.setProperty("prefix.columnWidth.defaultSize", "80");
        properties.setProperty("prefix.columnWidth.resizableByDefault", "false");
        properties.setProperty("prefix.columnWidth.percentageSizing", "true");
        properties.setProperty("prefix.rowHeight.defaultSize", "70");
        properties.setProperty("prefix.rowHeight.resizableByDefault", "true");

        this.dataLayer.loadState("prefix", properties);

        assertEquals(80, this.dataLayer.getColumnWidthByPosition(0));
        assertEquals(80, this.dataLayer.getColumnWidthByPosition(1));

        assertFalse(this.dataLayer.isColumnPositionResizable(0));
        assertFalse(this.dataLayer.isColumnPositionResizable(1));

        assertEquals(70, this.dataLayer.getRowHeightByPosition(0));
        assertEquals(70, this.dataLayer.getRowHeightByPosition(1));

        assertTrue(this.dataLayer.isRowPositionResizable(0));
        assertTrue(this.dataLayer.isRowPositionResizable(1));

        assertTrue(this.dataLayer.isColumnPercentageSizing());
        assertFalse(this.dataLayer.isRowPercentageSizing());
    }

}