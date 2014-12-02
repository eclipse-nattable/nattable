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
package org.eclipse.nebula.widgets.nattable.freeze;

import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommand;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseDataLayerFixture;
import org.junit.Assert;
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
        Assert.assertEquals(3, this.freezeLayer.getColumnCount());
        Assert.assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        Assert.assertEquals(2, this.freezeLayer.getColumnIndexByPosition(1));
        Assert.assertEquals(3, this.freezeLayer.getColumnIndexByPosition(2));
    }

    @Test
    public void testSetupRows() {
        Assert.assertEquals(4, this.freezeLayer.getRowCount());
        Assert.assertEquals(0, this.freezeLayer.getRowIndexByPosition(0));
        Assert.assertEquals(1, this.freezeLayer.getRowIndexByPosition(1));
        Assert.assertEquals(2, this.freezeLayer.getRowIndexByPosition(2));
        Assert.assertEquals(3, this.freezeLayer.getRowIndexByPosition(3));
    }

    @Test
    public void testReorderInInteriorColumn() {
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 5, 2));

        Assert.assertEquals(4, this.freezeLayer.getColumnCount());
        Assert.assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        Assert.assertEquals(5, this.freezeLayer.getColumnIndexByPosition(1));
        Assert.assertEquals(2, this.freezeLayer.getColumnIndexByPosition(2));
        Assert.assertEquals(3, this.freezeLayer.getColumnIndexByPosition(3));
    }

    @Test
    public void testReorderingIntoTopLeftCoordinate() {
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 5, 1));

        Assert.assertEquals(4, this.freezeLayer.getColumnCount());
        Assert.assertEquals(5, this.freezeLayer.getColumnIndexByPosition(0));
        Assert.assertEquals(1, this.freezeLayer.getColumnIndexByPosition(1));
        Assert.assertEquals(2, this.freezeLayer.getColumnIndexByPosition(2));
        Assert.assertEquals(3, this.freezeLayer.getColumnIndexByPosition(3));
    }

    @Test
    public void testReorderOutInteriorColumn() {
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 2, 5));

        Assert.assertEquals(2, this.freezeLayer.getColumnCount());
        Assert.assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        Assert.assertEquals(3, this.freezeLayer.getColumnIndexByPosition(1));
    }

    @Test
    public void testReorderingRightBottomCornerOutOfFrozenArea() {
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 3, 5));

        Assert.assertEquals(2, this.freezeLayer.getColumnCount());
        Assert.assertEquals(2,
                this.freezeLayer.getBottomRightPosition().columnPosition);
        Assert.assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        Assert.assertEquals(2, this.freezeLayer.getColumnIndexByPosition(1));
    }

    @Test
    public void testHideShowInteriorColumn() {
        this.hideShowLayer.doCommand(new ColumnHideCommand(this.hideShowLayer, 2));

        Assert.assertEquals(2, this.freezeLayer.getColumnCount());
        Assert.assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        Assert.assertEquals(3, this.freezeLayer.getColumnIndexByPosition(1));

        this.hideShowLayer.doCommand(new ShowAllColumnsCommand());

        Assert.assertEquals(3, this.freezeLayer.getColumnCount());
        Assert.assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        Assert.assertEquals(2, this.freezeLayer.getColumnIndexByPosition(1));
        Assert.assertEquals(3, this.freezeLayer.getColumnIndexByPosition(2));
    }

    @Test
    public void testMovingAroundColumns() {
        // ---------------------- Move into middle of frozen area
        // Frozen Columns: 1 5 2 3
        // Frozen Rows: 0 3 3 3
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 5, 2));

        // Test positions
        Assert.assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        Assert.assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        Assert.assertEquals(4,
                this.freezeLayer.getBottomRightPosition().columnPosition);
        Assert.assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        // Test indexes
        Assert.assertEquals(4, this.freezeLayer.getColumnCount());
        Assert.assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        Assert.assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        Assert.assertEquals(4,
                this.freezeLayer.getBottomRightPosition().columnPosition);
        Assert.assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        Assert.assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        Assert.assertEquals(5, this.freezeLayer.getColumnIndexByPosition(1));
        Assert.assertEquals(2, this.freezeLayer.getColumnIndexByPosition(2));
        Assert.assertEquals(3, this.freezeLayer.getColumnIndexByPosition(3));

        // ---------------------- Move right edge out of frozen area
        // Frozen Columns: 1 5 2
        // Frozen Rows: 0 3 3
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 4, 6));

        // Test indexes
        Assert.assertEquals(3, this.freezeLayer.getColumnCount());
        Assert.assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        Assert.assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        Assert.assertEquals(3,
                this.freezeLayer.getBottomRightPosition().columnPosition);
        Assert.assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        // Test positions
        Assert.assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        Assert.assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        Assert.assertEquals(3,
                this.freezeLayer.getBottomRightPosition().columnPosition);
        Assert.assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        // ---------------------- Swap right edge with preceeding column
        // Frozen Columns: 1 2 5
        // Frozen Rows: 0 3 3
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 3, 2));

        // Test indexes
        Assert.assertEquals(3, this.freezeLayer.getColumnCount());
        Assert.assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        Assert.assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        Assert.assertEquals(3,
                this.freezeLayer.getBottomRightPosition().columnPosition);
        Assert.assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);
        Assert.assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        Assert.assertEquals(2, this.freezeLayer.getColumnIndexByPosition(1));
        Assert.assertEquals(5, this.freezeLayer.getColumnIndexByPosition(2));

        // Test positions
        Assert.assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        Assert.assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        Assert.assertEquals(3,
                this.freezeLayer.getBottomRightPosition().columnPosition);
        Assert.assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        // ---------------------- Move new right edge out
        // Frozen Columns: 1 2
        // Frozen Rows: 0 3
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 3, 5));

        // Test indexes
        Assert.assertEquals(2, this.freezeLayer.getColumnCount());
        Assert.assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        Assert.assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        Assert.assertEquals(2,
                this.freezeLayer.getBottomRightPosition().columnPosition);
        Assert.assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);
        Assert.assertEquals(1, this.freezeLayer.getColumnIndexByPosition(0));
        Assert.assertEquals(2, this.freezeLayer.getColumnIndexByPosition(1));

        // Test positions
        Assert.assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        Assert.assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        Assert.assertEquals(2,
                this.freezeLayer.getBottomRightPosition().columnPosition);
        Assert.assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        // ---------------------- Move column into frozen area replacing top
        // left index
        // Frozen Columns: 8 1 2
        // Frozen Rows: 1 3 3
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 8, 1));

        // Test indexes
        Assert.assertEquals(3, this.freezeLayer.getColumnCount());
        Assert.assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        Assert.assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        Assert.assertEquals(3,
                this.freezeLayer.getBottomRightPosition().columnPosition);
        Assert.assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);
        Assert.assertEquals(8, this.freezeLayer.getColumnIndexByPosition(0));
        Assert.assertEquals(1, this.freezeLayer.getColumnIndexByPosition(1));
        Assert.assertEquals(2, this.freezeLayer.getColumnIndexByPosition(2));

        // Test positions
        Assert.assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        Assert.assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        Assert.assertEquals(3,
                this.freezeLayer.getBottomRightPosition().columnPosition);
        Assert.assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);

        // ---------------------- Move right edge out
        // Frozen Columns: 8 1
        // Frozen Rows: 1 3
        this.hideShowLayer.doCommand(new ColumnReorderCommand(this.hideShowLayer, 3, 5));
        Assert.assertEquals(2, this.freezeLayer.getColumnCount());
        Assert.assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        Assert.assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        Assert.assertEquals(2,
                this.freezeLayer.getBottomRightPosition().columnPosition);
        Assert.assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);
        Assert.assertEquals(8, this.freezeLayer.getColumnIndexByPosition(0));
        Assert.assertEquals(1, this.freezeLayer.getColumnIndexByPosition(1));

        // Test positions
        Assert.assertEquals(1, this.freezeLayer.getTopLeftPosition().columnPosition);
        Assert.assertEquals(0, this.freezeLayer.getTopLeftPosition().rowPosition);
        Assert.assertEquals(2,
                this.freezeLayer.getBottomRightPosition().columnPosition);
        Assert.assertEquals(3, this.freezeLayer.getBottomRightPosition().rowPosition);
    }

}
