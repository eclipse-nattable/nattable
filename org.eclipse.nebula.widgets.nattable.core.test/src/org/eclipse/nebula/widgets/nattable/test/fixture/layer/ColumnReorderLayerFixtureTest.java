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
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColumnReorderLayerFixtureTest {

    private ILayer columnReorderLayerFixture;

    @BeforeEach
    public void setup() {
        this.columnReorderLayerFixture = new ColumnReorderLayerFixture();
    }

    @Test
    public void testColumnIndexes() {
        assertEquals(4,
                this.columnReorderLayerFixture.getColumnIndexByPosition(0));
        assertEquals(1,
                this.columnReorderLayerFixture.getColumnIndexByPosition(1));
        assertEquals(0,
                this.columnReorderLayerFixture.getColumnIndexByPosition(2));
        assertEquals(2,
                this.columnReorderLayerFixture.getColumnIndexByPosition(3));
        assertEquals(3,
                this.columnReorderLayerFixture.getColumnIndexByPosition(4));
    }

    @Test
    public void testColumnWidths() {
        assertEquals(80,
                this.columnReorderLayerFixture.getColumnWidthByPosition(0));
        assertEquals(100,
                this.columnReorderLayerFixture.getColumnWidthByPosition(1));
        assertEquals(150,
                this.columnReorderLayerFixture.getColumnWidthByPosition(2));
        assertEquals(35,
                this.columnReorderLayerFixture.getColumnWidthByPosition(3));
        assertEquals(100,
                this.columnReorderLayerFixture.getColumnWidthByPosition(4));
    }

    @Test
    public void testRowIndexes() {
        assertEquals(0,
                this.columnReorderLayerFixture.getRowIndexByPosition(0));
        assertEquals(1,
                this.columnReorderLayerFixture.getRowIndexByPosition(1));
        assertEquals(2,
                this.columnReorderLayerFixture.getRowIndexByPosition(2));
        assertEquals(3,
                this.columnReorderLayerFixture.getRowIndexByPosition(3));
        assertEquals(4,
                this.columnReorderLayerFixture.getRowIndexByPosition(4));
        assertEquals(5,
                this.columnReorderLayerFixture.getRowIndexByPosition(5));
        assertEquals(6,
                this.columnReorderLayerFixture.getRowIndexByPosition(6));
    }

    @Test
    public void testRowHeights() {
        assertEquals(40,
                this.columnReorderLayerFixture.getRowHeightByPosition(0));
        assertEquals(70,
                this.columnReorderLayerFixture.getRowHeightByPosition(1));
        assertEquals(25,
                this.columnReorderLayerFixture.getRowHeightByPosition(2));
        assertEquals(40,
                this.columnReorderLayerFixture.getRowHeightByPosition(3));
        assertEquals(50,
                this.columnReorderLayerFixture.getRowHeightByPosition(4));
        assertEquals(40,
                this.columnReorderLayerFixture.getRowHeightByPosition(5));
        assertEquals(100,
                this.columnReorderLayerFixture.getRowHeightByPosition(6));
    }
}