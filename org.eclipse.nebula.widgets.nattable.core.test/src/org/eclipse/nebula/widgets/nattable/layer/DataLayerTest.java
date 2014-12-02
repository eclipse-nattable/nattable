/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer;

import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DataLayerTest {

    private DataLayer dataLayer;

    @Before
    public void setup() {
        this.dataLayer = new DataLayerFixture();
    }

    // Horizontal features

    // Columns

    @Test
    public void testColumnCount() {
        Assert.assertEquals(5, this.dataLayer.getColumnCount());
    }

    @Test
    public void testColumnIndexByPosition() {
        Assert.assertEquals(-1, this.dataLayer.getColumnIndexByPosition(-1));

        Assert.assertEquals(0, this.dataLayer.getColumnIndexByPosition(0));
        Assert.assertEquals(1, this.dataLayer.getColumnIndexByPosition(1));
        Assert.assertEquals(2, this.dataLayer.getColumnIndexByPosition(2));
        Assert.assertEquals(3, this.dataLayer.getColumnIndexByPosition(3));
        Assert.assertEquals(4, this.dataLayer.getColumnIndexByPosition(4));

        Assert.assertEquals(-1, this.dataLayer.getColumnIndexByPosition(5));
    }

    @Test
    public void testColumnPositionByIndex() {
        Assert.assertEquals(-1, this.dataLayer.getColumnPositionByIndex(-1));

        Assert.assertEquals(0, this.dataLayer.getColumnPositionByIndex(0));
        Assert.assertEquals(1, this.dataLayer.getColumnPositionByIndex(1));
        Assert.assertEquals(2, this.dataLayer.getColumnPositionByIndex(2));
        Assert.assertEquals(3, this.dataLayer.getColumnPositionByIndex(3));
        Assert.assertEquals(4, this.dataLayer.getColumnPositionByIndex(4));

        Assert.assertEquals(-1, this.dataLayer.getColumnPositionByIndex(5));
    }

    // Width

    @Test
    public void testWidth() {
        Assert.assertEquals(465, this.dataLayer.getWidth());
    }

    @Test
    public void testWidthAfterModify() {
        testWidth();

        this.dataLayer.setColumnWidthByPosition(0, 120);
        this.dataLayer.setColumnWidthByPosition(2, 40);

        Assert.assertEquals(440, this.dataLayer.getWidth());
    }

    @Test
    public void testColumnWidthByPosition() {
        Assert.assertEquals(150, this.dataLayer.getColumnWidthByPosition(0));
        Assert.assertEquals(100, this.dataLayer.getColumnWidthByPosition(1));
        Assert.assertEquals(35, this.dataLayer.getColumnWidthByPosition(2));
        Assert.assertEquals(100, this.dataLayer.getColumnWidthByPosition(3));
        Assert.assertEquals(80, this.dataLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void testColumnWidthByPositionAfterModify() {
        testColumnWidthByPosition();

        this.dataLayer.setColumnWidthByPosition(0, 120);
        this.dataLayer.setColumnWidthByPosition(2, 40);

        Assert.assertEquals(120, this.dataLayer.getColumnWidthByPosition(0));
        Assert.assertEquals(100, this.dataLayer.getColumnWidthByPosition(1));
        Assert.assertEquals(40, this.dataLayer.getColumnWidthByPosition(2));
        Assert.assertEquals(100, this.dataLayer.getColumnWidthByPosition(3));
        Assert.assertEquals(80, this.dataLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void testPreferredColumnWidth() {
        testColumnWidthByPosition();

        this.dataLayer.setDefaultColumnWidth(200);

        Assert.assertEquals(150, this.dataLayer.getColumnWidthByPosition(0));
        Assert.assertEquals(200, this.dataLayer.getColumnWidthByPosition(1));
        Assert.assertEquals(35, this.dataLayer.getColumnWidthByPosition(2));
        Assert.assertEquals(200, this.dataLayer.getColumnWidthByPosition(3));
        Assert.assertEquals(80, this.dataLayer.getColumnWidthByPosition(4));
        Assert.assertEquals(665, this.dataLayer.getWidth());
    }

    @Test
    public void testPreferredColumnWidthByPosition() {
        testColumnWidthByPosition();

        this.dataLayer.setDefaultColumnWidthByPosition(1, 75);
        this.dataLayer.setDefaultColumnWidthByPosition(3, 45);

        Assert.assertEquals(150, this.dataLayer.getColumnWidthByPosition(0));
        Assert.assertEquals(75, this.dataLayer.getColumnWidthByPosition(1));
        Assert.assertEquals(35, this.dataLayer.getColumnWidthByPosition(2));
        Assert.assertEquals(45, this.dataLayer.getColumnWidthByPosition(3));
        Assert.assertEquals(80, this.dataLayer.getColumnWidthByPosition(4));

        this.dataLayer.setColumnWidthByPosition(1, 30);

        Assert.assertEquals(150, this.dataLayer.getColumnWidthByPosition(0));
        Assert.assertEquals(30, this.dataLayer.getColumnWidthByPosition(1));
        Assert.assertEquals(35, this.dataLayer.getColumnWidthByPosition(2));
        Assert.assertEquals(45, this.dataLayer.getColumnWidthByPosition(3));
        Assert.assertEquals(80, this.dataLayer.getColumnWidthByPosition(4));
    }

    // Column resize

    @Test
    public void testColumnsResizableByDefault() {
        testColumnWidthByPosition();

        this.dataLayer.setColumnWidthByPosition(0, 35);
        Assert.assertEquals(35, this.dataLayer.getColumnWidthByPosition(0));

        this.dataLayer.setColumnsResizableByDefault(false);
        this.dataLayer.setColumnWidthByPosition(0, 85);
        Assert.assertEquals(35, this.dataLayer.getColumnWidthByPosition(0));

        this.dataLayer.setColumnsResizableByDefault(true);
        this.dataLayer.setColumnWidthByPosition(0, 65);
        Assert.assertEquals(65, this.dataLayer.getColumnWidthByPosition(0));
    }

    @Test
    public void testColumnPositionResizable() {
        testColumnWidthByPosition();

        Assert.assertTrue(this.dataLayer.isColumnPositionResizable(0));
        this.dataLayer.setColumnWidthByPosition(0, 35);
        Assert.assertEquals(35, this.dataLayer.getColumnWidthByPosition(0));

        this.dataLayer.setColumnPositionResizable(0, false);
        Assert.assertFalse(this.dataLayer.isColumnPositionResizable(0));
        this.dataLayer.setColumnWidthByPosition(0, 85);
        Assert.assertEquals(35, this.dataLayer.getColumnWidthByPosition(0));

        this.dataLayer.setColumnsResizableByDefault(false);
        this.dataLayer.setColumnPositionResizable(0, true);
        Assert.assertTrue(this.dataLayer.isColumnPositionResizable(0));
        this.dataLayer.setColumnWidthByPosition(0, 65);
        Assert.assertEquals(65, this.dataLayer.getColumnWidthByPosition(0));
    }

    // X

    @Test
    public void testColumnPositionByX() {
        Assert.assertEquals(-1, this.dataLayer.getColumnPositionByX(-1));

        Assert.assertEquals(0, this.dataLayer.getColumnPositionByX(0));
        Assert.assertEquals(0, this.dataLayer.getColumnPositionByX(149));
        Assert.assertEquals(1, this.dataLayer.getColumnPositionByX(150));
        Assert.assertEquals(1, this.dataLayer.getColumnPositionByX(170));
        Assert.assertEquals(2, this.dataLayer.getColumnPositionByX(250));
        Assert.assertEquals(2, this.dataLayer.getColumnPositionByX(284));
        Assert.assertEquals(3, this.dataLayer.getColumnPositionByX(285));
        Assert.assertEquals(3, this.dataLayer.getColumnPositionByX(384));
        Assert.assertEquals(4, this.dataLayer.getColumnPositionByX(385));
        Assert.assertEquals(4, this.dataLayer.getColumnPositionByX(464));
    }

    @Test
    public void testColumnPositionByXAfterModify() {
        testColumnPositionByX();

        this.dataLayer.setColumnWidthByPosition(1, 50);

        Assert.assertEquals(-1, this.dataLayer.getColumnPositionByX(-1));

        Assert.assertEquals(0, this.dataLayer.getColumnPositionByX(0));
        Assert.assertEquals(0, this.dataLayer.getColumnPositionByX(149));
        Assert.assertEquals(1, this.dataLayer.getColumnPositionByX(150));
        Assert.assertEquals(1, this.dataLayer.getColumnPositionByX(170));
        Assert.assertEquals(2, this.dataLayer.getColumnPositionByX(200));
        Assert.assertEquals(2, this.dataLayer.getColumnPositionByX(234));
        Assert.assertEquals(3, this.dataLayer.getColumnPositionByX(235));
        Assert.assertEquals(3, this.dataLayer.getColumnPositionByX(334));
        Assert.assertEquals(4, this.dataLayer.getColumnPositionByX(335));
        Assert.assertEquals(4, this.dataLayer.getColumnPositionByX(414));
    }

    @Test
    public void testStartXOfColumnPosition() {
        Assert.assertEquals(0, this.dataLayer.getStartXOfColumnPosition(0));
        Assert.assertEquals(150, this.dataLayer.getStartXOfColumnPosition(1));
        Assert.assertEquals(250, this.dataLayer.getStartXOfColumnPosition(2));
        Assert.assertEquals(285, this.dataLayer.getStartXOfColumnPosition(3));
        Assert.assertEquals(385, this.dataLayer.getStartXOfColumnPosition(4));
    }

    @Test
    public void testStartXOfColumnPositionAfterModify() {
        testStartXOfColumnPosition();

        this.dataLayer.setColumnWidthByPosition(1, 50);

        Assert.assertEquals(0, this.dataLayer.getStartXOfColumnPosition(0));
        Assert.assertEquals(150, this.dataLayer.getStartXOfColumnPosition(1));
        Assert.assertEquals(200, this.dataLayer.getStartXOfColumnPosition(2));
        Assert.assertEquals(235, this.dataLayer.getStartXOfColumnPosition(3));
        Assert.assertEquals(335, this.dataLayer.getStartXOfColumnPosition(4));
    }

    // Vertical features

    // Rows

    @Test
    public void testRowCount() {
        Assert.assertEquals(7, this.dataLayer.getRowCount());
    }

    @Test
    public void testRowIndexByPosition() {
        Assert.assertEquals(-1, this.dataLayer.getRowIndexByPosition(-1));

        Assert.assertEquals(0, this.dataLayer.getRowIndexByPosition(0));
        Assert.assertEquals(1, this.dataLayer.getRowIndexByPosition(1));
        Assert.assertEquals(2, this.dataLayer.getRowIndexByPosition(2));
        Assert.assertEquals(3, this.dataLayer.getRowIndexByPosition(3));
        Assert.assertEquals(4, this.dataLayer.getRowIndexByPosition(4));
        Assert.assertEquals(5, this.dataLayer.getRowIndexByPosition(5));
        Assert.assertEquals(6, this.dataLayer.getRowIndexByPosition(6));

        Assert.assertEquals(-1, this.dataLayer.getRowIndexByPosition(7));
    }

    @Test
    public void testRowPositionByIndex() {
        Assert.assertEquals(-1, this.dataLayer.getRowPositionByIndex(-1));

        Assert.assertEquals(0, this.dataLayer.getRowPositionByIndex(0));
        Assert.assertEquals(1, this.dataLayer.getRowPositionByIndex(1));
        Assert.assertEquals(2, this.dataLayer.getRowPositionByIndex(2));
        Assert.assertEquals(3, this.dataLayer.getRowPositionByIndex(3));
        Assert.assertEquals(4, this.dataLayer.getRowPositionByIndex(4));
        Assert.assertEquals(5, this.dataLayer.getRowPositionByIndex(5));
        Assert.assertEquals(6, this.dataLayer.getRowPositionByIndex(6));

        Assert.assertEquals(-1, this.dataLayer.getRowPositionByIndex(7));
    }

    // Height

    @Test
    public void testHeight() {
        Assert.assertEquals(365, this.dataLayer.getHeight());
    }

    @Test
    public void testHeightAfterModify() {
        testHeight();

        this.dataLayer.setRowHeightByPosition(0, 20);
        this.dataLayer.setRowHeightByPosition(2, 30);

        Assert.assertEquals(350, this.dataLayer.getHeight());
    }

    @Test
    public void testRowHeightByPosition() {
        Assert.assertEquals(40, this.dataLayer.getRowHeightByPosition(0));
        Assert.assertEquals(70, this.dataLayer.getRowHeightByPosition(1));
        Assert.assertEquals(25, this.dataLayer.getRowHeightByPosition(2));
        Assert.assertEquals(40, this.dataLayer.getRowHeightByPosition(3));
        Assert.assertEquals(50, this.dataLayer.getRowHeightByPosition(4));
        Assert.assertEquals(40, this.dataLayer.getRowHeightByPosition(5));
        Assert.assertEquals(100, this.dataLayer.getRowHeightByPosition(6));

        Assert.assertEquals(365, this.dataLayer.getHeight());
    }

    @Test
    public void testRowHeightByPositionAfterModify() {
        testRowHeightByPosition();

        this.dataLayer.setRowHeightByPosition(0, 20);
        this.dataLayer.setRowHeightByPosition(3, 30);

        Assert.assertEquals(20, this.dataLayer.getRowHeightByPosition(0));
        Assert.assertEquals(70, this.dataLayer.getRowHeightByPosition(1));
        Assert.assertEquals(25, this.dataLayer.getRowHeightByPosition(2));
        Assert.assertEquals(30, this.dataLayer.getRowHeightByPosition(3));
        Assert.assertEquals(50, this.dataLayer.getRowHeightByPosition(4));
        Assert.assertEquals(40, this.dataLayer.getRowHeightByPosition(5));
        Assert.assertEquals(100, this.dataLayer.getRowHeightByPosition(6));

        Assert.assertEquals(335, this.dataLayer.getHeight());
    }

    @Test
    public void testPreferredRowHeight() {
        testRowHeightByPosition();

        this.dataLayer.setDefaultRowHeight(50);

        Assert.assertEquals(50, this.dataLayer.getRowHeightByPosition(0));
        Assert.assertEquals(70, this.dataLayer.getRowHeightByPosition(1));
        Assert.assertEquals(25, this.dataLayer.getRowHeightByPosition(2));
        Assert.assertEquals(50, this.dataLayer.getRowHeightByPosition(3));
        Assert.assertEquals(50, this.dataLayer.getRowHeightByPosition(4));
        Assert.assertEquals(50, this.dataLayer.getRowHeightByPosition(5));
        Assert.assertEquals(100, this.dataLayer.getRowHeightByPosition(6));

        Assert.assertEquals(395, this.dataLayer.getHeight());
    }

    @Test
    public void testPreferredRowHeightByPosition() {
        testRowHeightByPosition();

        this.dataLayer.setDefaultRowHeightByPosition(1, 75);
        this.dataLayer.setDefaultRowHeightByPosition(3, 45);

        Assert.assertEquals(40, this.dataLayer.getRowHeightByPosition(0));
        Assert.assertEquals(70, this.dataLayer.getRowHeightByPosition(1));
        Assert.assertEquals(25, this.dataLayer.getRowHeightByPosition(2));
        Assert.assertEquals(45, this.dataLayer.getRowHeightByPosition(3));
        Assert.assertEquals(50, this.dataLayer.getRowHeightByPosition(4));
        Assert.assertEquals(40, this.dataLayer.getRowHeightByPosition(5));
        Assert.assertEquals(100, this.dataLayer.getRowHeightByPosition(6));

        this.dataLayer.setRowHeightByPosition(1, 30);

        Assert.assertEquals(40, this.dataLayer.getRowHeightByPosition(0));
        Assert.assertEquals(30, this.dataLayer.getRowHeightByPosition(1));
        Assert.assertEquals(25, this.dataLayer.getRowHeightByPosition(2));
        Assert.assertEquals(45, this.dataLayer.getRowHeightByPosition(3));
        Assert.assertEquals(50, this.dataLayer.getRowHeightByPosition(4));
        Assert.assertEquals(40, this.dataLayer.getRowHeightByPosition(5));
        Assert.assertEquals(100, this.dataLayer.getRowHeightByPosition(6));
    }

    // Row resize

    @Test
    public void testRowsResizableByDefault() {
        testRowHeightByPosition();

        this.dataLayer.setRowHeightByPosition(0, 35);
        Assert.assertEquals(35, this.dataLayer.getRowHeightByPosition(0));

        this.dataLayer.setRowsResizableByDefault(false);
        this.dataLayer.setRowHeightByPosition(0, 85);
        Assert.assertEquals(35, this.dataLayer.getRowHeightByPosition(0));

        this.dataLayer.setRowsResizableByDefault(true);
        this.dataLayer.setRowHeightByPosition(0, 65);
        Assert.assertEquals(65, this.dataLayer.getRowHeightByPosition(0));
    }

    @Test
    public void testRowPositionResizable() {
        testRowHeightByPosition();

        Assert.assertTrue(this.dataLayer.isRowPositionResizable(0));
        this.dataLayer.setRowHeightByPosition(0, 35);
        Assert.assertEquals(35, this.dataLayer.getRowHeightByPosition(0));

        this.dataLayer.setRowPositionResizable(0, false);
        Assert.assertFalse(this.dataLayer.isRowPositionResizable(0));
        this.dataLayer.setRowHeightByPosition(0, 85);
        Assert.assertEquals(35, this.dataLayer.getRowHeightByPosition(0));

        this.dataLayer.setRowsResizableByDefault(false);
        this.dataLayer.setRowPositionResizable(0, true);
        Assert.assertTrue(this.dataLayer.isRowPositionResizable(0));
        this.dataLayer.setRowHeightByPosition(0, 65);
        Assert.assertEquals(65, this.dataLayer.getRowHeightByPosition(0));
    }

    // Y

    @Test
    public void testRowPositionByY() {
        Assert.assertEquals(-1, this.dataLayer.getRowPositionByY(-1));

        Assert.assertEquals(0, this.dataLayer.getRowPositionByY(0));
        Assert.assertEquals(0, this.dataLayer.getRowPositionByY(39));
        Assert.assertEquals(1, this.dataLayer.getRowPositionByY(40));
        Assert.assertEquals(1, this.dataLayer.getRowPositionByY(109));
        Assert.assertEquals(2, this.dataLayer.getRowPositionByY(110));
        Assert.assertEquals(2, this.dataLayer.getRowPositionByY(134));
        Assert.assertEquals(3, this.dataLayer.getRowPositionByY(135));
        Assert.assertEquals(3, this.dataLayer.getRowPositionByY(174));
        Assert.assertEquals(4, this.dataLayer.getRowPositionByY(175));
        Assert.assertEquals(4, this.dataLayer.getRowPositionByY(224));
        Assert.assertEquals(5, this.dataLayer.getRowPositionByY(225));
        Assert.assertEquals(5, this.dataLayer.getRowPositionByY(264));
        Assert.assertEquals(6, this.dataLayer.getRowPositionByY(265));
        Assert.assertEquals(6, this.dataLayer.getRowPositionByY(364));
    }

    @Test
    public void testRowPositionByYAfterModify() {
        testRowPositionByY();

        this.dataLayer.setRowHeightByPosition(2, 100);

        Assert.assertEquals(-1, this.dataLayer.getRowPositionByY(-1));

        Assert.assertEquals(0, this.dataLayer.getRowPositionByY(0));
        Assert.assertEquals(0, this.dataLayer.getRowPositionByY(39));
        Assert.assertEquals(1, this.dataLayer.getRowPositionByY(40));
        Assert.assertEquals(1, this.dataLayer.getRowPositionByY(109));
        Assert.assertEquals(2, this.dataLayer.getRowPositionByY(110));
        Assert.assertEquals(2, this.dataLayer.getRowPositionByY(134));
        Assert.assertEquals(2, this.dataLayer.getRowPositionByY(135));
        Assert.assertEquals(2, this.dataLayer.getRowPositionByY(209));
        Assert.assertEquals(3, this.dataLayer.getRowPositionByY(210));
        Assert.assertEquals(3, this.dataLayer.getRowPositionByY(249));
        Assert.assertEquals(4, this.dataLayer.getRowPositionByY(250));
        Assert.assertEquals(4, this.dataLayer.getRowPositionByY(299));
        Assert.assertEquals(5, this.dataLayer.getRowPositionByY(300));
        Assert.assertEquals(5, this.dataLayer.getRowPositionByY(339));
        Assert.assertEquals(6, this.dataLayer.getRowPositionByY(340));
        Assert.assertEquals(6, this.dataLayer.getRowPositionByY(439));
    }

    @Test
    public void testStartYOfRowPosition() {
        Assert.assertEquals(0, this.dataLayer.getStartYOfRowPosition(0));
        Assert.assertEquals(40, this.dataLayer.getStartYOfRowPosition(1));
        Assert.assertEquals(110, this.dataLayer.getStartYOfRowPosition(2));
        Assert.assertEquals(135, this.dataLayer.getStartYOfRowPosition(3));
        Assert.assertEquals(175, this.dataLayer.getStartYOfRowPosition(4));
        Assert.assertEquals(225, this.dataLayer.getStartYOfRowPosition(5));
        Assert.assertEquals(265, this.dataLayer.getStartYOfRowPosition(6));
    }

    @Test
    public void testStartYOfRowPositionAfterModify() {
        testStartYOfRowPosition();

        this.dataLayer.setRowHeightByPosition(2, 100);

        Assert.assertEquals(0, this.dataLayer.getStartYOfRowPosition(0));
        Assert.assertEquals(40, this.dataLayer.getStartYOfRowPosition(1));
        Assert.assertEquals(110, this.dataLayer.getStartYOfRowPosition(2));
        Assert.assertEquals(210, this.dataLayer.getStartYOfRowPosition(3));
        Assert.assertEquals(250, this.dataLayer.getStartYOfRowPosition(4));
        Assert.assertEquals(300, this.dataLayer.getStartYOfRowPosition(5));
        Assert.assertEquals(340, this.dataLayer.getStartYOfRowPosition(6));
    }

}
