/*******************************************************************************
 * Copyright (c) 2013, 2018 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.hideshow.indicator.HideIndicatorConstants;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.RowHideShowLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RowHideShowLayerTest {

    private RowHideShowLayer rowHideShowLayer;

    @Before
    public void setup() {
        this.rowHideShowLayer = new RowHideShowLayerFixture();
    }

    @Test
    public void getRowIndexByPosition() throws Exception {
        Assert.assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(0));
        Assert.assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        Assert.assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(2));
        Assert.assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(3));
        Assert.assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(4));
        Assert.assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(5));
    }

    @Test
    public void getRowIndexHideAdditionalColumn() throws Exception {
        getRowIndexByPosition();

        this.rowHideShowLayer.hideRowPositions(Arrays.asList(1));

        Assert.assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(0));
        Assert.assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(1));
        Assert.assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(2));
        Assert.assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(3));
        Assert.assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(4));
    }

    @Test
    public void getRowPositionForASingleHiddenRow() throws Exception {
        assertEquals(-1, this.rowHideShowLayer.getRowPositionByIndex(0));
        assertEquals(1, this.rowHideShowLayer.getRowPositionByIndex(1));
        assertEquals(2, this.rowHideShowLayer.getRowPositionByIndex(2));
        assertEquals(-1, this.rowHideShowLayer.getRowPositionByIndex(3));
        assertEquals(0, this.rowHideShowLayer.getRowPositionByIndex(4));
        assertEquals(3, this.rowHideShowLayer.getRowPositionByIndex(5));
        assertEquals(4, this.rowHideShowLayer.getRowPositionByIndex(6));
    }

    @Test
    public void hideAllRows() throws Exception {
        this.rowHideShowLayer.hideRowPositions(Arrays.asList(0, 1, 2, 3, 4));

        assertEquals(0, this.rowHideShowLayer.getRowCount());
    }

    @Test
    public void hideAllRows2() throws Exception {
        List<Integer> rowPositions = Arrays.asList(0);
        this.rowHideShowLayer.hideRowPositions(rowPositions);
        this.rowHideShowLayer.hideRowPositions(rowPositions);
        this.rowHideShowLayer.hideRowPositions(rowPositions);
        this.rowHideShowLayer.hideRowPositions(rowPositions);
        this.rowHideShowLayer.hideRowPositions(rowPositions);
        assertEquals(0, this.rowHideShowLayer.getRowCount());
    }

    @Test
    public void showARow() throws Exception {
        assertEquals(5, this.rowHideShowLayer.getRowCount());

        List<Integer> rowPositions = Arrays.asList(2);
        this.rowHideShowLayer.hideRowPositions(rowPositions); // index = 2
        rowPositions = Arrays.asList(0);
        this.rowHideShowLayer.hideRowPositions(rowPositions); // index = 4
        assertEquals(3, this.rowHideShowLayer.getRowCount());
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(3));

        this.rowHideShowLayer.showRowIndexes(Arrays.asList(0));
        assertEquals(4, this.rowHideShowLayer.getRowCount());
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(4));

        this.rowHideShowLayer.showRowIndexes(Arrays.asList(2));
        assertEquals(5, this.rowHideShowLayer.getRowCount());
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(5));
    }

    @Test
    public void showAllRows() throws Exception {
        assertEquals(5, this.rowHideShowLayer.getRowCount());

        this.rowHideShowLayer.hideRowPositions(Arrays.asList(0));
        assertEquals(4, this.rowHideShowLayer.getRowCount());
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(4));

        this.rowHideShowLayer.showAllRows();
        assertEquals(7, this.rowHideShowLayer.getRowCount());
        Assert.assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(0));
        Assert.assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        Assert.assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(2));
        Assert.assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(3));
        Assert.assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(4));
        Assert.assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(5));
        Assert.assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(6));
    }

    @Test
    public void showRowPositions() throws Exception {
        this.rowHideShowLayer = new RowHideShowLayerFixture(new DataLayerFixture(2,
                10, 100, 20));

        assertEquals(10, this.rowHideShowLayer.getRowCount());

        this.rowHideShowLayer.hideRowPositions(Arrays.asList(3, 4, 5));
        assertEquals(7, this.rowHideShowLayer.getRowCount());
        assertEquals(-1, this.rowHideShowLayer.getRowPositionByIndex(3));
        assertEquals(-1, this.rowHideShowLayer.getRowPositionByIndex(4));

        this.rowHideShowLayer.showRowIndexes(Arrays.asList(3, 4));
        assertEquals(9, this.rowHideShowLayer.getRowCount());
        assertEquals(3, this.rowHideShowLayer.getRowPositionByIndex(3));
        assertEquals(4, this.rowHideShowLayer.getRowPositionByIndex(4));

    }

    @Test
    public void shouldContainHideIndicatorLabels() {
        this.rowHideShowLayer = new RowHideShowLayer(new DataLayerFixture());
        assertEquals(7, this.rowHideShowLayer.getRowCount());

        this.rowHideShowLayer.hideRowPositions(Arrays.asList(0));

        LabelStack configLabels = this.rowHideShowLayer.getConfigLabelsByPosition(0, 0);
        assertTrue(configLabels.hasLabel(HideIndicatorConstants.ROW_TOP_HIDDEN));
        assertFalse(configLabels.hasLabel(HideIndicatorConstants.ROW_BOTTOM_HIDDEN));

        this.rowHideShowLayer.hideRowPositions(Arrays.asList(1));

        configLabels = this.rowHideShowLayer.getConfigLabelsByPosition(0, 0);
        assertTrue(configLabels.hasLabel(HideIndicatorConstants.ROW_TOP_HIDDEN));
        assertTrue(configLabels.hasLabel(HideIndicatorConstants.ROW_BOTTOM_HIDDEN));

        this.rowHideShowLayer.showRowIndexes(Arrays.asList(0));

        configLabels = this.rowHideShowLayer.getConfigLabelsByPosition(0, 1);
        assertFalse(configLabels.hasLabel(HideIndicatorConstants.ROW_TOP_HIDDEN));
        assertTrue(configLabels.hasLabel(HideIndicatorConstants.ROW_BOTTOM_HIDDEN));
    }
}
