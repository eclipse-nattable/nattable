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
package org.eclipse.nebula.widgets.nattable.reorder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.TestLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

public class RowReorderLayerTest2 {

    private RowReorderLayer reorderLayer;

    @Before
    public void setup() {
        TestLayer dataLayer = new TestLayer(4, 4,
                "0:0;100 | 1:1;100 | 2:2;100 | 3:3;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40", "A0 | B0 | C0 | D0 \n"
                        + "A1 | B1 | C1 | D1 \n" + "A2 | B2 | C2 | D2 \n"
                        + "A3 | B3 | C3 | D3 \n");

        this.reorderLayer = new RowReorderLayer(dataLayer);
    }

    @Test
    public void shouldLoadstateFromProperties() {
        LayerListenerFixture listener = new LayerListenerFixture();
        this.reorderLayer.addLayerListener(listener);

        Properties testProperties = new Properties();
        testProperties.put(RowReorderLayer.PERSISTENCE_KEY_ROW_INDEX_ORDER, "0,1,3,2,");

        this.reorderLayer.loadState("", testProperties);

        assertEquals(0, this.reorderLayer.getRowIndexByPosition(0));
        assertEquals(1, this.reorderLayer.getRowIndexByPosition(1));
        assertEquals(3, this.reorderLayer.getRowIndexByPosition(2));
        assertEquals(2, this.reorderLayer.getRowIndexByPosition(3));

        assertTrue(listener.containsInstanceOf(RowStructuralRefreshEvent.class));
    }

    @Test
    public void skipLoadingStateIfPersistedStateDoesNotMatchDataSource() {
        Properties testProperties = new Properties();

        // Index 5 is valid
        testProperties.put(RowReorderLayer.PERSISTENCE_KEY_ROW_INDEX_ORDER, "0,1,5,2,");
        this.reorderLayer.loadState("", testProperties);

        // Ordering unchanged
        assertEquals(0, this.reorderLayer.getRowIndexByPosition(0));
        assertEquals(1, this.reorderLayer.getRowIndexByPosition(1));
        assertEquals(2, this.reorderLayer.getRowIndexByPosition(2));
        assertEquals(3, this.reorderLayer.getRowIndexByPosition(3));

        // Number of columns is different
        testProperties.put(RowReorderLayer.PERSISTENCE_KEY_ROW_INDEX_ORDER, "2,1,0,");
        this.reorderLayer.loadState("", testProperties);

        // Ordering unchanged
        assertEquals(0, this.reorderLayer.getRowIndexByPosition(0));
        assertEquals(1, this.reorderLayer.getRowIndexByPosition(1));
        assertEquals(2, this.reorderLayer.getRowIndexByPosition(2));
        assertEquals(3, this.reorderLayer.getRowIndexByPosition(3));
    }

    @Test
    public void shouldSaveState() {
        this.reorderLayer.reorderRowPosition(0, 4);

        assertEquals(1, this.reorderLayer.getRowIndexByPosition(0));
        assertEquals(2, this.reorderLayer.getRowIndexByPosition(1));
        assertEquals(3, this.reorderLayer.getRowIndexByPosition(2));
        assertEquals(0, this.reorderLayer.getRowIndexByPosition(3));

        Properties properties = new Properties();
        this.reorderLayer.saveState("", properties);

        assertTrue(properties.containsKey(RowReorderLayer.PERSISTENCE_KEY_ROW_INDEX_ORDER));

        String order = properties.get(RowReorderLayer.PERSISTENCE_KEY_ROW_INDEX_ORDER).toString();
        assertEquals("1,2,3,0", order);
    }
}
