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
package org.eclipse.nebula.widgets.nattable.reorder;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.reorder.event.ColumnReorderEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseDataLayerFixture;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Test;

public class ColumnReorderLayerVisibleChangeTest {

    @Test
    /**
     * 	Index		10	11	12	13 ... 20
     *          --------------------
     *  Position 	0 	1	2	3 ... 20
     */
    public void returnsCorrectPositionRectangleForMultiColumnReorderLeftCase() {
        ColumnReorderLayer reorderLayer = new ColumnReorderLayer(new BaseDataLayerFixture(20, 20));

        // Build expected cell positions to redraw
        HashSet<Rectangle> expectedPositions = new HashSet<Rectangle>();
        expectedPositions.add(new Rectangle(0, 0, 20, 20));

        reorderLayer.addLayerListener(new ILayerListener() {
            @Override
            public void handleLayerEvent(ILayerEvent event) {
                ColumnReorderEvent multiReorder = (ColumnReorderEvent) event;
                assertTrue(multiReorder.getChangedPositionRectangles().containsAll(expectedPositions));
            }
        });

        // Reorder to beginning of grid
        reorderLayer.reorderMultipleColumnPositions(new int[] { 10, 11, 12, 13 }, 0);

        // Reorder to middle of grid
        expectedPositions.clear();
        expectedPositions.add(new Rectangle(10, 0, 10, 20));
        reorderLayer.reorderMultipleColumnPositions(new int[] { 19, 18, 17, 16 }, 10);

        // Reorder to end of grid
        expectedPositions.clear();
        expectedPositions.add(new Rectangle(5, 0, 15, 20));
        reorderLayer.reorderMultipleColumnPositions(new int[] { 5, 6, 7, 8 }, 10);
    }

    @Test
    /**
     * 	Index		2	3	0	1 ... 20
     *          --------------------
     *  Position 	0 	1	2	3 ... 20
     */
    public void returnsCorrectPositionRectangleForMultiColumnReorderRightCase() {
        ColumnReorderLayer reorderLayer = new ColumnReorderLayer(new BaseDataLayerFixture(20, 20));

        // Build expected cell positions to redraw
        HashSet<Rectangle> expectedPositions = new HashSet<Rectangle>();
        expectedPositions.add(new Rectangle(0, 0, 20, 20));

        reorderLayer.addLayerListener(new ILayerListener() {
            @Override
            public void handleLayerEvent(ILayerEvent event) {
                ColumnReorderEvent multiReorder = (ColumnReorderEvent) event;
                assertTrue(multiReorder.getChangedPositionRectangles().containsAll(expectedPositions));
            }
        });

        // Reorder from beginning of grid
        reorderLayer.reorderMultipleColumnPositions(new int[] { 0, 1 }, 2);

        // Reorder to middle of grid
        expectedPositions.clear();
        expectedPositions.add(new Rectangle(5, 0, 15, 20));
        reorderLayer.reorderMultipleColumnPositions(new int[] { 5, 6, 7, 8 }, 10);

        // Reorder to end of grid
        expectedPositions.clear();
        expectedPositions.add(new Rectangle(10, 0, 10, 20));
        reorderLayer.reorderMultipleColumnPositions(new int[] { 10, 11, 12, 13 }, 19);
    }

}
