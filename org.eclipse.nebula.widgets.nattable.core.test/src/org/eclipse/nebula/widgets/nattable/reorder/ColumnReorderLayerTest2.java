/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.layer.event.ColumnStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.test.LayerAssert;
import org.eclipse.nebula.widgets.nattable.test.fixture.TestLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

public class ColumnReorderLayerTest2 {

    private ColumnReorderLayer reorderLayer;

    @Before
    public void setup() {
        TestLayer dataLayer = new TestLayer(4, 4,
                "0:0;100 | 1:1;100 | 2:2;100 | 3:3;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40", "A0 | B0 | C0 | D0 \n"
                        + "A1 | B1 | C1 | D1 \n" + "A2 | B2 | C2 | D2 \n"
                        + "A3 | B3 | C3 | D3 \n");

        this.reorderLayer = new ColumnReorderLayer(dataLayer);
    }

    @Test
    public void reorderColumns() {
        this.reorderLayer.reorderColumnPosition(2, 0);

        TestLayer expectedLayer = new TestLayer(4, 4,
                "2:2;100 | 0:0;100 | 1:1;100 | 3:3;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40", "C0 | A0 | B0 | D0 \n"
                        + "C1 | A1 | B1 | D1 \n" + "C2 | A2 | B2 | D2 \n"
                        + "C3 | A3 | B3 | D3 \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.reorderLayer);
    }

    @Test
    public void shouldLoadstateFromProperties() {
        LayerListenerFixture listener = new LayerListenerFixture();
        this.reorderLayer.addLayerListener(listener);

        Properties testProperties = new Properties();
        testProperties.put(
                ColumnReorderLayer.PERSISTENCE_KEY_COLUMN_INDEX_ORDER,
                "0,1,3,2,");

        this.reorderLayer.loadState("", testProperties);

        assertEquals(0, this.reorderLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.reorderLayer.getColumnIndexByPosition(1));
        assertEquals(3, this.reorderLayer.getColumnIndexByPosition(2));
        assertEquals(2, this.reorderLayer.getColumnIndexByPosition(3));

        assertTrue(listener.containsInstanceOf(ColumnStructuralRefreshEvent.class));
    }

    @Test
    public void skipLoadingStateIfPersistedStateDoesNotMatchDataSource() {
        Properties testProperties = new Properties();

        // Index 5 is valid
        testProperties.put(
                ColumnReorderLayer.PERSISTENCE_KEY_COLUMN_INDEX_ORDER,
                "0,1,5,2,");
        this.reorderLayer.loadState("", testProperties);

        // Ordering unchanged
        assertEquals(0, this.reorderLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.reorderLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.reorderLayer.getColumnIndexByPosition(2));
        assertEquals(3, this.reorderLayer.getColumnIndexByPosition(3));

        // Number of columns is different
        testProperties.put(ColumnReorderLayer.PERSISTENCE_KEY_COLUMN_INDEX_ORDER, "2,1,0,");
        this.reorderLayer.loadState("", testProperties);

        // Ordering unchanged
        assertEquals(0, this.reorderLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.reorderLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.reorderLayer.getColumnIndexByPosition(2));
        assertEquals(3, this.reorderLayer.getColumnIndexByPosition(3));
    }

    @Test
    public void shouldSaveState() {
        this.reorderLayer.reorderColumnPosition(0, 4);

        assertEquals(1, this.reorderLayer.getColumnIndexByPosition(0));
        assertEquals(2, this.reorderLayer.getColumnIndexByPosition(1));
        assertEquals(3, this.reorderLayer.getColumnIndexByPosition(2));
        assertEquals(0, this.reorderLayer.getColumnIndexByPosition(3));

        Properties properties = new Properties();
        this.reorderLayer.saveState("", properties);

        assertTrue(properties.containsKey(ColumnReorderLayer.PERSISTENCE_KEY_COLUMN_INDEX_ORDER));

        String order = properties.get(ColumnReorderLayer.PERSISTENCE_KEY_COLUMN_INDEX_ORDER).toString();
        assertEquals("1,2,3,0", order);
    }
}
