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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataLayerTest {

    private DataLayer dataLayer;

    @BeforeEach
    public void setup() {
        this.dataLayer = new DataLayerFixture();
    }

    // Horizontal features

    // Columns

    @Test
    public void testColumnCount() {
        assertEquals(5, this.dataLayer.getColumnCount());
    }

    @Test
    public void testColumnIndexByPosition() {
        assertEquals(-1, this.dataLayer.getColumnIndexByPosition(-1));

        assertEquals(0, this.dataLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.dataLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.dataLayer.getColumnIndexByPosition(2));
        assertEquals(3, this.dataLayer.getColumnIndexByPosition(3));
        assertEquals(4, this.dataLayer.getColumnIndexByPosition(4));

        assertEquals(-1, this.dataLayer.getColumnIndexByPosition(5));
    }

    @Test
    public void testColumnPositionByIndex() {
        assertEquals(-1, this.dataLayer.getColumnPositionByIndex(-1));

        assertEquals(0, this.dataLayer.getColumnPositionByIndex(0));
        assertEquals(1, this.dataLayer.getColumnPositionByIndex(1));
        assertEquals(2, this.dataLayer.getColumnPositionByIndex(2));
        assertEquals(3, this.dataLayer.getColumnPositionByIndex(3));
        assertEquals(4, this.dataLayer.getColumnPositionByIndex(4));

        assertEquals(-1, this.dataLayer.getColumnPositionByIndex(5));
    }

    // Width

    @Test
    public void testWidth() {
        assertEquals(465, this.dataLayer.getWidth());
    }

    @Test
    public void testWidthAfterModify() {
        testWidth();

        this.dataLayer.setColumnWidthByPosition(0, 120);
        this.dataLayer.setColumnWidthByPosition(2, 40);

        assertEquals(440, this.dataLayer.getWidth());
    }

    @Test
    public void testColumnWidthByPosition() {
        assertEquals(150, this.dataLayer.getColumnWidthByPosition(0));
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(1));
        assertEquals(35, this.dataLayer.getColumnWidthByPosition(2));
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(3));
        assertEquals(80, this.dataLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void testColumnWidthByPositionAfterModify() {
        testColumnWidthByPosition();

        this.dataLayer.setColumnWidthByPosition(0, 120);
        this.dataLayer.setColumnWidthByPosition(2, 40);

        assertEquals(120, this.dataLayer.getColumnWidthByPosition(0));
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(1));
        assertEquals(40, this.dataLayer.getColumnWidthByPosition(2));
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(3));
        assertEquals(80, this.dataLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void testPreferredColumnWidth() {
        testColumnWidthByPosition();

        this.dataLayer.setDefaultColumnWidth(200);

        assertEquals(150, this.dataLayer.getColumnWidthByPosition(0));
        assertEquals(200, this.dataLayer.getColumnWidthByPosition(1));
        assertEquals(35, this.dataLayer.getColumnWidthByPosition(2));
        assertEquals(200, this.dataLayer.getColumnWidthByPosition(3));
        assertEquals(80, this.dataLayer.getColumnWidthByPosition(4));
        assertEquals(665, this.dataLayer.getWidth());
    }

    @Test
    public void testPreferredColumnWidthByPosition() {
        testColumnWidthByPosition();

        this.dataLayer.setDefaultColumnWidthByPosition(1, 75);
        this.dataLayer.setDefaultColumnWidthByPosition(3, 45);

        assertEquals(150, this.dataLayer.getColumnWidthByPosition(0));
        assertEquals(75, this.dataLayer.getColumnWidthByPosition(1));
        assertEquals(35, this.dataLayer.getColumnWidthByPosition(2));
        assertEquals(45, this.dataLayer.getColumnWidthByPosition(3));
        assertEquals(80, this.dataLayer.getColumnWidthByPosition(4));

        this.dataLayer.setColumnWidthByPosition(1, 30);

        assertEquals(150, this.dataLayer.getColumnWidthByPosition(0));
        assertEquals(30, this.dataLayer.getColumnWidthByPosition(1));
        assertEquals(35, this.dataLayer.getColumnWidthByPosition(2));
        assertEquals(45, this.dataLayer.getColumnWidthByPosition(3));
        assertEquals(80, this.dataLayer.getColumnWidthByPosition(4));
    }

    // Column resize

    @Test
    public void testColumnsResizableByDefault() {
        testColumnWidthByPosition();

        this.dataLayer.setColumnWidthByPosition(0, 35);
        assertEquals(35, this.dataLayer.getColumnWidthByPosition(0));

        this.dataLayer.setColumnsResizableByDefault(false);
        this.dataLayer.setColumnWidthByPosition(0, 85);
        assertEquals(35, this.dataLayer.getColumnWidthByPosition(0));

        this.dataLayer.setColumnsResizableByDefault(true);
        this.dataLayer.setColumnWidthByPosition(0, 65);
        assertEquals(65, this.dataLayer.getColumnWidthByPosition(0));
    }

    @Test
    public void testColumnPositionResizable() {
        testColumnWidthByPosition();

        assertTrue(this.dataLayer.isColumnPositionResizable(0));
        this.dataLayer.setColumnWidthByPosition(0, 35);
        assertEquals(35, this.dataLayer.getColumnWidthByPosition(0));

        this.dataLayer.setColumnPositionResizable(0, false);
        assertFalse(this.dataLayer.isColumnPositionResizable(0));
        this.dataLayer.setColumnWidthByPosition(0, 85);
        assertEquals(35, this.dataLayer.getColumnWidthByPosition(0));

        this.dataLayer.setColumnsResizableByDefault(false);
        this.dataLayer.setColumnPositionResizable(0, true);
        assertTrue(this.dataLayer.isColumnPositionResizable(0));
        this.dataLayer.setColumnWidthByPosition(0, 65);
        assertEquals(65, this.dataLayer.getColumnWidthByPosition(0));
    }

    // X

    @Test
    public void testColumnPositionByX() {
        assertEquals(-1, this.dataLayer.getColumnPositionByX(-1));

        assertEquals(0, this.dataLayer.getColumnPositionByX(0));
        assertEquals(0, this.dataLayer.getColumnPositionByX(149));
        assertEquals(1, this.dataLayer.getColumnPositionByX(150));
        assertEquals(1, this.dataLayer.getColumnPositionByX(170));
        assertEquals(2, this.dataLayer.getColumnPositionByX(250));
        assertEquals(2, this.dataLayer.getColumnPositionByX(284));
        assertEquals(3, this.dataLayer.getColumnPositionByX(285));
        assertEquals(3, this.dataLayer.getColumnPositionByX(384));
        assertEquals(4, this.dataLayer.getColumnPositionByX(385));
        assertEquals(4, this.dataLayer.getColumnPositionByX(464));
    }

    @Test
    public void testColumnPositionByXAfterModify() {
        testColumnPositionByX();

        this.dataLayer.setColumnWidthByPosition(1, 50);

        assertEquals(-1, this.dataLayer.getColumnPositionByX(-1));

        assertEquals(0, this.dataLayer.getColumnPositionByX(0));
        assertEquals(0, this.dataLayer.getColumnPositionByX(149));
        assertEquals(1, this.dataLayer.getColumnPositionByX(150));
        assertEquals(1, this.dataLayer.getColumnPositionByX(170));
        assertEquals(2, this.dataLayer.getColumnPositionByX(200));
        assertEquals(2, this.dataLayer.getColumnPositionByX(234));
        assertEquals(3, this.dataLayer.getColumnPositionByX(235));
        assertEquals(3, this.dataLayer.getColumnPositionByX(334));
        assertEquals(4, this.dataLayer.getColumnPositionByX(335));
        assertEquals(4, this.dataLayer.getColumnPositionByX(414));
    }

    @Test
    public void testStartXOfColumnPosition() {
        assertEquals(0, this.dataLayer.getStartXOfColumnPosition(0));
        assertEquals(150, this.dataLayer.getStartXOfColumnPosition(1));
        assertEquals(250, this.dataLayer.getStartXOfColumnPosition(2));
        assertEquals(285, this.dataLayer.getStartXOfColumnPosition(3));
        assertEquals(385, this.dataLayer.getStartXOfColumnPosition(4));
    }

    @Test
    public void testStartXOfColumnPositionAfterModify() {
        testStartXOfColumnPosition();

        this.dataLayer.setColumnWidthByPosition(1, 50);

        assertEquals(0, this.dataLayer.getStartXOfColumnPosition(0));
        assertEquals(150, this.dataLayer.getStartXOfColumnPosition(1));
        assertEquals(200, this.dataLayer.getStartXOfColumnPosition(2));
        assertEquals(235, this.dataLayer.getStartXOfColumnPosition(3));
        assertEquals(335, this.dataLayer.getStartXOfColumnPosition(4));
    }

    // Vertical features

    // Rows

    @Test
    public void testRowCount() {
        assertEquals(7, this.dataLayer.getRowCount());
    }

    @Test
    public void testRowIndexByPosition() {
        assertEquals(-1, this.dataLayer.getRowIndexByPosition(-1));

        assertEquals(0, this.dataLayer.getRowIndexByPosition(0));
        assertEquals(1, this.dataLayer.getRowIndexByPosition(1));
        assertEquals(2, this.dataLayer.getRowIndexByPosition(2));
        assertEquals(3, this.dataLayer.getRowIndexByPosition(3));
        assertEquals(4, this.dataLayer.getRowIndexByPosition(4));
        assertEquals(5, this.dataLayer.getRowIndexByPosition(5));
        assertEquals(6, this.dataLayer.getRowIndexByPosition(6));

        assertEquals(-1, this.dataLayer.getRowIndexByPosition(7));
    }

    @Test
    public void testRowPositionByIndex() {
        assertEquals(-1, this.dataLayer.getRowPositionByIndex(-1));

        assertEquals(0, this.dataLayer.getRowPositionByIndex(0));
        assertEquals(1, this.dataLayer.getRowPositionByIndex(1));
        assertEquals(2, this.dataLayer.getRowPositionByIndex(2));
        assertEquals(3, this.dataLayer.getRowPositionByIndex(3));
        assertEquals(4, this.dataLayer.getRowPositionByIndex(4));
        assertEquals(5, this.dataLayer.getRowPositionByIndex(5));
        assertEquals(6, this.dataLayer.getRowPositionByIndex(6));

        assertEquals(-1, this.dataLayer.getRowPositionByIndex(7));
    }

    // Height

    @Test
    public void testHeight() {
        assertEquals(365, this.dataLayer.getHeight());
    }

    @Test
    public void testHeightAfterModify() {
        testHeight();

        this.dataLayer.setRowHeightByPosition(0, 20);
        this.dataLayer.setRowHeightByPosition(2, 30);

        assertEquals(350, this.dataLayer.getHeight());
    }

    @Test
    public void testRowHeightByPosition() {
        assertEquals(40, this.dataLayer.getRowHeightByPosition(0));
        assertEquals(70, this.dataLayer.getRowHeightByPosition(1));
        assertEquals(25, this.dataLayer.getRowHeightByPosition(2));
        assertEquals(40, this.dataLayer.getRowHeightByPosition(3));
        assertEquals(50, this.dataLayer.getRowHeightByPosition(4));
        assertEquals(40, this.dataLayer.getRowHeightByPosition(5));
        assertEquals(100, this.dataLayer.getRowHeightByPosition(6));

        assertEquals(365, this.dataLayer.getHeight());
    }

    @Test
    public void testRowHeightByPositionAfterModify() {
        testRowHeightByPosition();

        this.dataLayer.setRowHeightByPosition(0, 20);
        this.dataLayer.setRowHeightByPosition(3, 30);

        assertEquals(20, this.dataLayer.getRowHeightByPosition(0));
        assertEquals(70, this.dataLayer.getRowHeightByPosition(1));
        assertEquals(25, this.dataLayer.getRowHeightByPosition(2));
        assertEquals(30, this.dataLayer.getRowHeightByPosition(3));
        assertEquals(50, this.dataLayer.getRowHeightByPosition(4));
        assertEquals(40, this.dataLayer.getRowHeightByPosition(5));
        assertEquals(100, this.dataLayer.getRowHeightByPosition(6));

        assertEquals(335, this.dataLayer.getHeight());
    }

    @Test
    public void testPreferredRowHeight() {
        testRowHeightByPosition();

        this.dataLayer.setDefaultRowHeight(50);

        assertEquals(50, this.dataLayer.getRowHeightByPosition(0));
        assertEquals(70, this.dataLayer.getRowHeightByPosition(1));
        assertEquals(25, this.dataLayer.getRowHeightByPosition(2));
        assertEquals(50, this.dataLayer.getRowHeightByPosition(3));
        assertEquals(50, this.dataLayer.getRowHeightByPosition(4));
        assertEquals(50, this.dataLayer.getRowHeightByPosition(5));
        assertEquals(100, this.dataLayer.getRowHeightByPosition(6));

        assertEquals(395, this.dataLayer.getHeight());
    }

    @Test
    public void testPreferredRowHeightByPosition() {
        testRowHeightByPosition();

        this.dataLayer.setDefaultRowHeightByPosition(1, 75);
        this.dataLayer.setDefaultRowHeightByPosition(3, 45);

        assertEquals(40, this.dataLayer.getRowHeightByPosition(0));
        assertEquals(70, this.dataLayer.getRowHeightByPosition(1));
        assertEquals(25, this.dataLayer.getRowHeightByPosition(2));
        assertEquals(45, this.dataLayer.getRowHeightByPosition(3));
        assertEquals(50, this.dataLayer.getRowHeightByPosition(4));
        assertEquals(40, this.dataLayer.getRowHeightByPosition(5));
        assertEquals(100, this.dataLayer.getRowHeightByPosition(6));

        this.dataLayer.setRowHeightByPosition(1, 30);

        assertEquals(40, this.dataLayer.getRowHeightByPosition(0));
        assertEquals(30, this.dataLayer.getRowHeightByPosition(1));
        assertEquals(25, this.dataLayer.getRowHeightByPosition(2));
        assertEquals(45, this.dataLayer.getRowHeightByPosition(3));
        assertEquals(50, this.dataLayer.getRowHeightByPosition(4));
        assertEquals(40, this.dataLayer.getRowHeightByPosition(5));
        assertEquals(100, this.dataLayer.getRowHeightByPosition(6));
    }

    // Row resize

    @Test
    public void testRowsResizableByDefault() {
        testRowHeightByPosition();

        this.dataLayer.setRowHeightByPosition(0, 35);
        assertEquals(35, this.dataLayer.getRowHeightByPosition(0));

        this.dataLayer.setRowsResizableByDefault(false);
        this.dataLayer.setRowHeightByPosition(0, 85);
        assertEquals(35, this.dataLayer.getRowHeightByPosition(0));

        this.dataLayer.setRowsResizableByDefault(true);
        this.dataLayer.setRowHeightByPosition(0, 65);
        assertEquals(65, this.dataLayer.getRowHeightByPosition(0));
    }

    @Test
    public void testRowPositionResizable() {
        testRowHeightByPosition();

        assertTrue(this.dataLayer.isRowPositionResizable(0));
        this.dataLayer.setRowHeightByPosition(0, 35);
        assertEquals(35, this.dataLayer.getRowHeightByPosition(0));

        this.dataLayer.setRowPositionResizable(0, false);
        assertFalse(this.dataLayer.isRowPositionResizable(0));
        this.dataLayer.setRowHeightByPosition(0, 85);
        assertEquals(35, this.dataLayer.getRowHeightByPosition(0));

        this.dataLayer.setRowsResizableByDefault(false);
        this.dataLayer.setRowPositionResizable(0, true);
        assertTrue(this.dataLayer.isRowPositionResizable(0));
        this.dataLayer.setRowHeightByPosition(0, 65);
        assertEquals(65, this.dataLayer.getRowHeightByPosition(0));
    }

    // Y

    @Test
    public void testRowPositionByY() {
        assertEquals(-1, this.dataLayer.getRowPositionByY(-1));

        assertEquals(0, this.dataLayer.getRowPositionByY(0));
        assertEquals(0, this.dataLayer.getRowPositionByY(39));
        assertEquals(1, this.dataLayer.getRowPositionByY(40));
        assertEquals(1, this.dataLayer.getRowPositionByY(109));
        assertEquals(2, this.dataLayer.getRowPositionByY(110));
        assertEquals(2, this.dataLayer.getRowPositionByY(134));
        assertEquals(3, this.dataLayer.getRowPositionByY(135));
        assertEquals(3, this.dataLayer.getRowPositionByY(174));
        assertEquals(4, this.dataLayer.getRowPositionByY(175));
        assertEquals(4, this.dataLayer.getRowPositionByY(224));
        assertEquals(5, this.dataLayer.getRowPositionByY(225));
        assertEquals(5, this.dataLayer.getRowPositionByY(264));
        assertEquals(6, this.dataLayer.getRowPositionByY(265));
        assertEquals(6, this.dataLayer.getRowPositionByY(364));
    }

    @Test
    public void testRowPositionByYAfterModify() {
        testRowPositionByY();

        this.dataLayer.setRowHeightByPosition(2, 100);

        assertEquals(-1, this.dataLayer.getRowPositionByY(-1));

        assertEquals(0, this.dataLayer.getRowPositionByY(0));
        assertEquals(0, this.dataLayer.getRowPositionByY(39));
        assertEquals(1, this.dataLayer.getRowPositionByY(40));
        assertEquals(1, this.dataLayer.getRowPositionByY(109));
        assertEquals(2, this.dataLayer.getRowPositionByY(110));
        assertEquals(2, this.dataLayer.getRowPositionByY(134));
        assertEquals(2, this.dataLayer.getRowPositionByY(135));
        assertEquals(2, this.dataLayer.getRowPositionByY(209));
        assertEquals(3, this.dataLayer.getRowPositionByY(210));
        assertEquals(3, this.dataLayer.getRowPositionByY(249));
        assertEquals(4, this.dataLayer.getRowPositionByY(250));
        assertEquals(4, this.dataLayer.getRowPositionByY(299));
        assertEquals(5, this.dataLayer.getRowPositionByY(300));
        assertEquals(5, this.dataLayer.getRowPositionByY(339));
        assertEquals(6, this.dataLayer.getRowPositionByY(340));
        assertEquals(6, this.dataLayer.getRowPositionByY(439));
    }

    @Test
    public void testStartYOfRowPosition() {
        assertEquals(0, this.dataLayer.getStartYOfRowPosition(0));
        assertEquals(40, this.dataLayer.getStartYOfRowPosition(1));
        assertEquals(110, this.dataLayer.getStartYOfRowPosition(2));
        assertEquals(135, this.dataLayer.getStartYOfRowPosition(3));
        assertEquals(175, this.dataLayer.getStartYOfRowPosition(4));
        assertEquals(225, this.dataLayer.getStartYOfRowPosition(5));
        assertEquals(265, this.dataLayer.getStartYOfRowPosition(6));
    }

    @Test
    public void testStartYOfRowPositionAfterModify() {
        testStartYOfRowPosition();

        this.dataLayer.setRowHeightByPosition(2, 100);

        assertEquals(0, this.dataLayer.getStartYOfRowPosition(0));
        assertEquals(40, this.dataLayer.getStartYOfRowPosition(1));
        assertEquals(110, this.dataLayer.getStartYOfRowPosition(2));
        assertEquals(210, this.dataLayer.getStartYOfRowPosition(3));
        assertEquals(250, this.dataLayer.getStartYOfRowPosition(4));
        assertEquals(300, this.dataLayer.getStartYOfRowPosition(5));
        assertEquals(340, this.dataLayer.getStartYOfRowPosition(6));
    }

    @Test
    public void testResetColumnSize() {
        this.dataLayer.setColumnWidthByPosition(0, 20);
        this.dataLayer.setColumnWidthByPosition(1, 30);
        this.dataLayer.setColumnPositionResizable(1, false);

        assertEquals(20, this.dataLayer.getColumnWidthByPosition(0));
        assertEquals(30, this.dataLayer.getColumnWidthByPosition(1));
        assertEquals(35, this.dataLayer.getColumnWidthByPosition(2));
        assertFalse(this.dataLayer.isColumnPositionResizable(1));

        this.dataLayer.resetColumnWidthConfiguration(false);

        assertEquals(100, this.dataLayer.getColumnWidthByPosition(0));
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(1));
        assertEquals(100, this.dataLayer.getColumnWidthByPosition(2));
        assertTrue(this.dataLayer.isColumnPositionResizable(1));
    }

    @Test
    public void testResetRowSize() {
        this.dataLayer.setRowHeightByPosition(0, 20);
        this.dataLayer.setRowHeightByPosition(1, 30);
        this.dataLayer.setRowPositionResizable(1, false);

        assertEquals(20, this.dataLayer.getRowHeightByPosition(0));
        assertEquals(30, this.dataLayer.getRowHeightByPosition(1));
        assertEquals(25, this.dataLayer.getRowHeightByPosition(2));
        assertFalse(this.dataLayer.isRowPositionResizable(1));

        this.dataLayer.resetRowHeightConfiguration(false);

        assertEquals(40, this.dataLayer.getRowHeightByPosition(0));
        assertEquals(40, this.dataLayer.getRowHeightByPosition(1));
        assertEquals(40, this.dataLayer.getRowHeightByPosition(2));
        assertTrue(this.dataLayer.isRowPositionResizable(1));
    }
}
