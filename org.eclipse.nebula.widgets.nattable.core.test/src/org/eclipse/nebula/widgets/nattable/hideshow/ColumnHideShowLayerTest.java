/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.indicator.HideIndicatorConstants;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ColumnHideShowLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

public class ColumnHideShowLayerTest {

    private ColumnHideShowLayer columnHideShowLayer;

    @Before
    public void setup() {
        this.columnHideShowLayer = new ColumnHideShowLayerFixture();
    }

    @Test
    public void getColumnIndexByPosition() {
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(2));

        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(3));
    }

    @Test
    public void getColumnIndexHideAdditionalColumn() {
        getColumnIndexByPosition();

        this.columnHideShowLayer.hideColumnPositions(Arrays.asList(1));

        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(1));

        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(2));
    }

    @Test
    public void getColumnPositionForASingleHiddenColumn() {
        assertEquals(-1, this.columnHideShowLayer.getColumnPositionByIndex(0));
        assertEquals(1, this.columnHideShowLayer.getColumnPositionByIndex(1));
        assertEquals(2, this.columnHideShowLayer.getColumnPositionByIndex(2));
        assertEquals(-1, this.columnHideShowLayer.getColumnPositionByIndex(3));
        assertEquals(0, this.columnHideShowLayer.getColumnPositionByIndex(4));
    }

    @Test
    public void hideAllColumns() {
        this.columnHideShowLayer.hideColumnPositions(Arrays.asList(0, 1, 2));

        assertEquals(0, this.columnHideShowLayer.getColumnCount());
    }

    @Test
    public void hideAllColumns2() {
        List<Integer> columnPositions = Arrays.asList(0);
        this.columnHideShowLayer.hideColumnPositions(columnPositions);
        this.columnHideShowLayer.hideColumnPositions(columnPositions);
        this.columnHideShowLayer.hideColumnPositions(columnPositions);
        assertEquals(0, this.columnHideShowLayer.getColumnCount());
    }

    @Test
    public void showAColumnByIndex() {
        assertEquals(3, this.columnHideShowLayer.getColumnCount());

        // index == 2
        List<Integer> columnPositions = Arrays.asList(2);
        this.columnHideShowLayer.hideColumnPositions(columnPositions);

        // index == 4
        columnPositions = Arrays.asList(0);
        this.columnHideShowLayer.hideColumnPositions(columnPositions);

        assertEquals(1, this.columnHideShowLayer.getColumnCount());
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(1));

        this.columnHideShowLayer.showColumnIndexes(Arrays.asList(0));
        assertEquals(2, this.columnHideShowLayer.getColumnCount());
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(2));

        this.columnHideShowLayer.showColumnIndexes(Arrays.asList(2));
        assertEquals(3, this.columnHideShowLayer.getColumnCount());
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(3));
    }

    @Test
    public void showAllColumns() {
        assertEquals(3, this.columnHideShowLayer.getColumnCount());

        this.columnHideShowLayer.hideColumnPositions(Arrays.asList(0));
        assertEquals(2, this.columnHideShowLayer.getColumnCount());
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(2));

        this.columnHideShowLayer.showAllColumns();
        assertEquals(5, this.columnHideShowLayer.getColumnCount());
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(4));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(5));
    }

    @Test
    public void showColumnIndexes() {
        this.columnHideShowLayer = new ColumnHideShowLayerFixture(
                new DataLayerFixture(10, 2, 100, 20));

        assertEquals(10, this.columnHideShowLayer.getColumnCount());

        this.columnHideShowLayer.hideColumnPositions(Arrays.asList(3, 4, 5));
        assertEquals(7, this.columnHideShowLayer.getColumnCount());
        assertEquals(-1, this.columnHideShowLayer.getColumnPositionByIndex(3));
        assertEquals(-1, this.columnHideShowLayer.getColumnPositionByIndex(4));

        this.columnHideShowLayer.showColumnIndexes(Arrays.asList(3, 4));
        assertEquals(9, this.columnHideShowLayer.getColumnCount());
        assertEquals(3, this.columnHideShowLayer.getColumnPositionByIndex(3));
        assertEquals(4, this.columnHideShowLayer.getColumnPositionByIndex(4));
    }

    @Test
    public void showColumnPositions() {
        // Column reorder fixture index positions: 4 1 0 2 3
        // Columns positions hidden: 2 4 (index 0 3)

        this.columnHideShowLayer.showColumnPosition(2, true, false);

        assertEquals(4, this.columnHideShowLayer.getColumnCount());
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(4));

        this.columnHideShowLayer.showColumnPosition(3, false, false);

        assertEquals(5, this.columnHideShowLayer.getColumnCount());
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(4));
    }

    @Test
    public void showAllColumnPositionsOnLeftSide() {
        // Column reorder fixture index positions: 4 1 0 2 3
        // Columns positions hidden: 2 4 (index 0 3)

        // hide additional column
        this.columnHideShowLayer.hideColumnPositions(1);

        this.columnHideShowLayer.showColumnPosition(1, true, false);

        assertEquals(3, this.columnHideShowLayer.getColumnCount());
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(3));

        // hide again
        this.columnHideShowLayer.hideColumnPositions(1);

        // now show all columns to the left
        this.columnHideShowLayer.showColumnPosition(1, true, true);

        assertEquals(4, this.columnHideShowLayer.getColumnCount());
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(4));
    }

    @Test
    public void showAllColumnPositionsOnRightSide() {
        // Column reorder fixture index positions: 4 1 0 2 3
        // Columns positions hidden: 2 4 (index 0 3)

        // hide additional column
        this.columnHideShowLayer.hideColumnPositions(1);

        this.columnHideShowLayer.showColumnPosition(0, false, false);

        assertEquals(3, this.columnHideShowLayer.getColumnCount());
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(3));

        // hide again
        this.columnHideShowLayer.hideColumnPositions(1);

        // now show all columns to the right
        this.columnHideShowLayer.showColumnPosition(0, false, true);

        assertEquals(4, this.columnHideShowLayer.getColumnCount());
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(4));
    }

    @Test
    public void doNotShowIfNoDirectColumnHiddenToLeft() {
        // Column reorder fixture index positions: 4 1 0 2 3
        // Columns positions hidden: 2 4 (index 0 3)

        // hide additional column
        this.columnHideShowLayer.hideColumnPositions(1);

        this.columnHideShowLayer.showColumnPosition(2, true, false);

        assertEquals(2, this.columnHideShowLayer.getColumnCount());
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(2));
    }

    @Test
    public void doNotShowIfNoDirectColumnHiddenToRight() {
        // Column reorder fixture index positions: 4 1 0 2 3
        // Columns positions hidden: 2 4 (index 0 3)

        // hide additional column
        this.columnHideShowLayer.hideColumnPositions(2);

        this.columnHideShowLayer.showColumnPosition(0, false, false);

        assertEquals(2, this.columnHideShowLayer.getColumnCount());
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(2));
    }

    @Test
    public void shouldContainHideIndicatorLabels() {
        this.columnHideShowLayer = new ColumnHideShowLayer(new DataLayerFixture());
        assertEquals(5, this.columnHideShowLayer.getColumnCount());

        this.columnHideShowLayer.hideColumnPositions(0);

        LabelStack configLabels = this.columnHideShowLayer.getConfigLabelsByPosition(0, 0);
        assertTrue(configLabels.hasLabel(HideIndicatorConstants.COLUMN_LEFT_HIDDEN));
        assertFalse(configLabels.hasLabel(HideIndicatorConstants.COLUMN_RIGHT_HIDDEN));

        this.columnHideShowLayer.hideColumnPositions(1);

        configLabels = this.columnHideShowLayer.getConfigLabelsByPosition(0, 0);
        assertTrue(configLabels.hasLabel(HideIndicatorConstants.COLUMN_LEFT_HIDDEN));
        assertTrue(configLabels.hasLabel(HideIndicatorConstants.COLUMN_RIGHT_HIDDEN));

        this.columnHideShowLayer.showColumnIndexes(0);

        configLabels = this.columnHideShowLayer.getConfigLabelsByPosition(1, 0);
        assertFalse(configLabels.hasLabel(HideIndicatorConstants.COLUMN_LEFT_HIDDEN));
        assertTrue(configLabels.hasLabel(HideIndicatorConstants.COLUMN_RIGHT_HIDDEN));
    }

    @Test
    public void shouldNotFireEventForNotProcessedColumn() {
        this.columnHideShowLayer = new ColumnHideShowLayer(new DataLayerFixture(10, 10, 100, 40));
        assertEquals(10, this.columnHideShowLayer.getColumnCount());

        this.columnHideShowLayer.hideColumnPositions(0, 2);

        assertEquals(8, this.columnHideShowLayer.getColumnCount());

        LayerListenerFixture listener = new LayerListenerFixture();
        this.columnHideShowLayer.addLayerListener(listener);

        this.columnHideShowLayer.showColumnIndexes(0, 1);

        assertEquals(9, this.columnHideShowLayer.getColumnCount());
        assertTrue(this.columnHideShowLayer.isColumnIndexHidden(2));

        assertEquals(1, listener.getEventsCount());
        assertTrue(listener.containsInstanceOf(ShowColumnPositionsEvent.class));

        ShowColumnPositionsEvent event = (ShowColumnPositionsEvent) listener.getReceivedEvents().get(0);
        Collection<Range> ranges = event.getColumnPositionRanges();
        assertEquals(1, ranges.size());
        assertEquals(new Range(0, 1), ranges.iterator().next());
    }
}
