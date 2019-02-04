/*******************************************************************************
 * Copyright (c) 2013, 2019 Dirk Fauth and others.
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
import org.junit.Before;
import org.junit.Test;

public class RowHideShowLayerTest {

    private RowHideShowLayer rowHideShowLayer;

    @Before
    public void setup() {
        this.rowHideShowLayer = new RowHideShowLayerFixture();
    }

    @Test
    public void getRowIndexByPosition() {
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(4));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(5));
    }

    @Test
    public void getRowIndexHideAdditionalColumn() {
        getRowIndexByPosition();

        this.rowHideShowLayer.hideRowPositions(Arrays.asList(1));

        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(4));
    }

    @Test
    public void getRowPositionForASingleHiddenRow() {
        assertEquals(-1, this.rowHideShowLayer.getRowPositionByIndex(0));
        assertEquals(1, this.rowHideShowLayer.getRowPositionByIndex(1));
        assertEquals(2, this.rowHideShowLayer.getRowPositionByIndex(2));
        assertEquals(-1, this.rowHideShowLayer.getRowPositionByIndex(3));
        assertEquals(0, this.rowHideShowLayer.getRowPositionByIndex(4));
        assertEquals(3, this.rowHideShowLayer.getRowPositionByIndex(5));
        assertEquals(4, this.rowHideShowLayer.getRowPositionByIndex(6));
    }

    @Test
    public void hideAllRows() {
        this.rowHideShowLayer.hideRowPositions(Arrays.asList(0, 1, 2, 3, 4));

        assertEquals(0, this.rowHideShowLayer.getRowCount());
    }

    @Test
    public void hideAllRows2() {
        List<Integer> rowPositions = Arrays.asList(0);
        this.rowHideShowLayer.hideRowPositions(rowPositions);
        this.rowHideShowLayer.hideRowPositions(rowPositions);
        this.rowHideShowLayer.hideRowPositions(rowPositions);
        this.rowHideShowLayer.hideRowPositions(rowPositions);
        this.rowHideShowLayer.hideRowPositions(rowPositions);
        assertEquals(0, this.rowHideShowLayer.getRowCount());
    }

    @Test
    public void showARowByIndex() {
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
    public void showAllRows() {
        assertEquals(5, this.rowHideShowLayer.getRowCount());

        this.rowHideShowLayer.hideRowPositions(Arrays.asList(0));
        assertEquals(4, this.rowHideShowLayer.getRowCount());
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(4));

        this.rowHideShowLayer.showAllRows();
        assertEquals(7, this.rowHideShowLayer.getRowCount());
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(4));
        assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(5));
        assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(6));
    }

    @Test
    public void showRowIndexes() {
        this.rowHideShowLayer = new RowHideShowLayerFixture(
                new DataLayerFixture(2, 10, 100, 20));

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
    public void showRowPositions() {
        // Row reorder fixture index positions: 4 1 0 2 3 5 6
        // Row positions hidden: 2 4 (index 0 3)

        this.rowHideShowLayer.showRowPosition(2, true, false);

        assertEquals(6, this.rowHideShowLayer.getRowCount());
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(4));
        assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(5));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(6));

        this.rowHideShowLayer.showRowPosition(3, false, false);

        assertEquals(7, this.rowHideShowLayer.getRowCount());
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(4));
        assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(5));
        assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(6));
    }

    @Test
    public void showAllRowPositionsOnTopSide() {
        // Row reorder fixture index positions: 4 1 0 2 3 5 6
        // Row positions hidden: 2 4 (index 0 3)

        // hide additional row
        this.rowHideShowLayer.hideRowPositions(Arrays.asList(1));

        this.rowHideShowLayer.showRowPosition(1, true, false);

        assertEquals(5, this.rowHideShowLayer.getRowCount());
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(4));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(5));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(6));

        // hide again
        this.rowHideShowLayer.hideRowPositions(Arrays.asList(1));

        // now show all rows to the top
        this.rowHideShowLayer.showRowPosition(1, true, true);

        assertEquals(6, this.rowHideShowLayer.getRowCount());
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(4));
        assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(5));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(6));
    }

    @Test
    public void showAllRowPositionsOnBottomSide() {
        // Row reorder fixture index positions: 4 1 0 2 3 5 6
        // Row positions hidden: 2 4 (index 0 3)

        // hide additional row
        this.rowHideShowLayer.hideRowPositions(Arrays.asList(1));

        this.rowHideShowLayer.showRowPosition(0, false, false);

        assertEquals(5, this.rowHideShowLayer.getRowCount());
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(4));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(5));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(6));

        // hide again
        this.rowHideShowLayer.hideRowPositions(Arrays.asList(1));

        // now show all columns to the left
        this.rowHideShowLayer.showRowPosition(0, false, true);

        assertEquals(6, this.rowHideShowLayer.getRowCount());
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(4));
        assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(5));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(6));
    }

    @Test
    public void doNotShowIfNoDirectRowHiddenToTop() {
        // Row reorder fixture index positions: 4 1 0 2 3 5 6
        // Row positions hidden: 2 4 (index 0 3)

        // hide additional row
        this.rowHideShowLayer.hideRowPositions(Arrays.asList(1));

        // Row reorder fixture index positions: 4 2 5 6

        this.rowHideShowLayer.showRowPosition(3, true, false);

        assertEquals(4, this.rowHideShowLayer.getRowCount());
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(4));
    }

    @Test
    public void doNotShowIfNoDirectRowHiddenToBottom() {
        // Row reorder fixture index positions: 4 1 0 2 3 5 6
        // Row positions hidden: 2 4 (index 0 3)

        // hide additional row
        this.rowHideShowLayer.hideRowPositions(Arrays.asList(2));

        this.rowHideShowLayer.showRowPosition(0, false, false);

        assertEquals(4, this.rowHideShowLayer.getRowCount());
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(6, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(4));
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

    @Test
    public void shouldCalculateHeight() {
        this.rowHideShowLayer = new RowHideShowLayer(new DataLayerFixture(5, 5, 100, 20));
        assertEquals(5, this.rowHideShowLayer.getRowCount());
        assertEquals(100, this.rowHideShowLayer.getHeight());

        this.rowHideShowLayer.hideRowPositions(Arrays.asList(0, 1, 2, 3));

        assertEquals(1, this.rowHideShowLayer.getRowCount());
        assertEquals(20, this.rowHideShowLayer.getHeight());

        this.rowHideShowLayer.hideRowPositions(Arrays.asList(0));

        assertEquals(0, this.rowHideShowLayer.getRowCount());
        assertEquals(0, this.rowHideShowLayer.getHeight());
    }

    @Test
    public void shouldCalculateHeightForEmptyDataset() {
        this.rowHideShowLayer = new RowHideShowLayer(new DataLayerFixture(0, 0, 100, 20));

        assertEquals(0, this.rowHideShowLayer.getRowCount());
        assertEquals(0, this.rowHideShowLayer.getHeight());
    }

}
