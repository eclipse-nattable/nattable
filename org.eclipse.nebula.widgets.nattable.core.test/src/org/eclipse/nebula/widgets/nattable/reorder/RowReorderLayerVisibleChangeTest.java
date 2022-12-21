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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;

import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.reorder.event.RowReorderEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseDataLayerFixture;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.Test;

public class RowReorderLayerVisibleChangeTest {

    @Test
    /**
     * 	Index		10	11	12	13 ... 20
     *          --------------------
     *  Position 	0 	1	2	3 ... 20
     */
    public void returnsCorrectPositionRectangleForMultiColumnReorderLeftCase() {
        RowReorderLayer reorderLayer = new RowReorderLayer(new BaseDataLayerFixture(20, 20));

        // Build expected cell positions to redraw
        HashSet<Rectangle> expectedPositions = new HashSet<Rectangle>();
        expectedPositions.add(new Rectangle(0, 0, 20, 20));

        reorderLayer.addLayerListener(new ILayerListener() {
            @Override
            public void handleLayerEvent(ILayerEvent event) {
                RowReorderEvent multiReorder = (RowReorderEvent) event;
                assertTrue(multiReorder.getChangedPositionRectangles().containsAll(expectedPositions));
            }
        });

        // Reorder to beginning of grid
        reorderLayer.reorderMultipleRowPositions(new int[] { 10, 11, 12, 13 }, 0);

        // Reorder to middle of grid
        expectedPositions.clear();
        expectedPositions.add(new Rectangle(0, 10, 20, 10));
        reorderLayer.reorderMultipleRowPositions(new int[] { 19, 18, 17, 16 }, 10);

        // Reorder to end of grid
        expectedPositions.clear();
        expectedPositions.add(new Rectangle(0, 5, 20, 15));
        reorderLayer.reorderMultipleRowPositions(new int[] { 5, 6, 7, 8 }, 10);
    }

    @Test
    /**
     * 	Index		2	3	0	1 ... 20
     *          --------------------
     *  Position 	0 	1	2	3 ... 20
     */
    public void returnsCorrectPositionRectangleForMultiColumnReorderRightCase() {
        RowReorderLayer reorderLayer = new RowReorderLayer(new BaseDataLayerFixture(20, 20));

        // Build expected cell positions to redraw
        HashSet<Rectangle> expectedPositions = new HashSet<Rectangle>();
        expectedPositions.add(new Rectangle(0, 0, 20, 20));

        reorderLayer.addLayerListener(new ILayerListener() {
            @Override
            public void handleLayerEvent(ILayerEvent event) {
                RowReorderEvent multiReorder = (RowReorderEvent) event;
                assertTrue(multiReorder.getChangedPositionRectangles().containsAll(expectedPositions));
            }
        });

        // Reorder from beginning of grid
        reorderLayer.reorderMultipleRowPositions(new int[] { 0, 1 }, 2);

        // Reorder to middle of grid
        expectedPositions.clear();
        expectedPositions.add(new Rectangle(0, 5, 20, 15));
        reorderLayer.reorderMultipleRowPositions(new int[] { 5, 6, 7, 8 }, 10);

        // Reorder to end of grid
        expectedPositions.clear();
        expectedPositions.add(new Rectangle(0, 10, 20, 10));
        reorderLayer.reorderMultipleRowPositions(new int[] { 10, 11, 12, 13 }, 19);
    }

}
