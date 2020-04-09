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
package org.eclipse.nebula.widgets.nattable.freeze;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommand;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseDataLayerFixture;
import org.junit.Before;
import org.junit.Test;

public class FreezeLayerTest {

    private FreezeLayer freezeLayer;
    private ColumnHideShowLayer hideShowLayer;
    private ColumnReorderLayer reorderLayer;

    @Before
    public void setup() {
        this.reorderLayer = new ColumnReorderLayer(new BaseDataLayerFixture(10, 10));
        this.hideShowLayer = new ColumnHideShowLayer(this.reorderLayer);
        this.freezeLayer = new FreezeLayer(this.hideShowLayer);
        this.freezeLayer.setTopLeftPosition(1, 0);
        this.freezeLayer.setBottomRightPosition(3, 3);
    }

    @Test
    public void testSetupColumns() {
        assertEquals(3, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        assertEquals(2, this.freezeLayer.getColumnIndexByPosition(1));
        assertEquals(3, this.freezeLayer.getColumnIndexByPosition(2));
    }

    @Test
    public void testSetupRows() {
        assertEquals(4, this.freezeLayer.getRowCount());
        assertEquals(0, this.freezeLayer.getRowIndexByPosition(0));
        assertEquals(1, this.freezeLayer.getRowIndexByPosition(1));
        assertEquals(2, this.freezeLayer.getRowIndexByPosition(2));
        assertEquals(3, this.freezeLayer.getRowIndexByPosition(3));
    }

    @Test
    public void testReorderInInteriorColumn() {
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 5, 2));

        assertEquals(4, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        assertEquals(5, this.freezeLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.freezeLayer.getColumnIndexByPosition(2));
        assertEquals(3, this.freezeLayer.getColumnIndexByPosition(3));
    }

    @Test
    public void testReorderingIntoTopLeftCoordinate() {
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 5, 1));

        assertEquals(4, this.freezeLayer.getColumnCount());
        assertEquals(5, this.freezeLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.freezeLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.freezeLayer.getColumnIndexByPosition(2));
        assertEquals(3, this.freezeLayer.getColumnIndexByPosition(3));
    }

    @Test
    public void testReorderOutInteriorColumn() {
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 2, 5));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        assertEquals(3, this.freezeLayer.getColumnIndexByPosition(1));
    }

    @Test
    public void testReorderingRightBottomCornerOutOfFrozenArea() {
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 3, 5));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(2, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        assertEquals(2, this.freezeLayer.getColumnIndexByPosition(1));
    }

    @Test
    public void testHideShowInteriorColumn() {
        this.hideShowLayer.doCommand(new ColumnHideCommand(this.hideShowLayer, 2));

        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        assertEquals(3, this.freezeLayer.getColumnIndexByPosition(1));

        this.hideShowLayer.doCommand(new ShowAllColumnsCommand());

        assertEquals(3, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        assertEquals(2, this.freezeLayer.getColumnIndexByPosition(1));
        assertEquals(3, this.freezeLayer.getColumnIndexByPosition(2));
    }

    @Test
    public void testMovingAroundColumns() {
        // ---------------------- Move into middle of frozen area
        // Frozen Columns: 1 5 2 3
        // Frozen Rows: 0 3 3 3
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 5, 2));

        // Test positions
        assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertEquals(4, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        // Test indexes
        assertEquals(4, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertEquals(4, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        assertEquals(5, this.freezeLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.freezeLayer.getColumnIndexByPosition(2));
        assertEquals(3, this.freezeLayer.getColumnIndexByPosition(3));

        // ---------------------- Move right edge out of frozen area
        // Frozen Columns: 1 5 2
        // Frozen Rows: 0 3 3
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 4, 6));

        // Test indexes
        assertEquals(3, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        // Test positions
        assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        // ---------------------- Swap right edge with preceeding column
        // Frozen Columns: 1 2 5
        // Frozen Rows: 0 3 3
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 3, 2));

        // Test indexes
        assertEquals(3, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);
        assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        assertEquals(2, this.freezeLayer.getColumnIndexByPosition(1));
        assertEquals(5, this.freezeLayer.getColumnIndexByPosition(2));

        // Test positions
        assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        // ---------------------- Move new right edge out
        // Frozen Columns: 1 2
        // Frozen Rows: 0 3
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 3, 5));

        // Test indexes
        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertEquals(2, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);
        assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        assertEquals(2, this.freezeLayer.getColumnIndexByPosition(1));

        // Test positions
        assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertEquals(2, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        // ---------------------- Move column into frozen area replacing top
        // left index
        // Frozen Columns: 8 1 2
        // Frozen Rows: 1 3 3
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 8, 1));

        // Test indexes
        assertEquals(3, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);
        assertEquals(8, this.freezeLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.freezeLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.freezeLayer.getColumnIndexByPosition(2));

        // Test positions
        assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        // ---------------------- Move right edge out
        // Frozen Columns: 8 1
        // Frozen Rows: 1 3
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 3, 5));
        assertEquals(2, this.freezeLayer.getColumnCount());
        assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertEquals(2, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);
        assertEquals(8, this.freezeLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.freezeLayer.getColumnIndexByPosition(1));

        // Test positions
        assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        assertEquals(2, this.freezeLayer.getBottomRightPosition().columnPosition);
        assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);
    }

}
