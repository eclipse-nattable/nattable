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
package org.eclipse.nebula.widgets.nattable.resize.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResizeEventPropagationTest {
    private LayerListenerFixture layerListener;

    private DataLayerFixture dataLayer;

    @BeforeEach
    public void setUp() {
        // Total width should be 500 and total height should be 280
        this.dataLayer = new DataLayerFixture(100, 40);
    }

    @Test
    public void shouldFireResizeEventAfterColumnResizeCommand() {
        this.dataLayer.addLayerListener(new LayerListenerFixture());
        this.dataLayer.setColumnWidthByPosition(4, 100);
    }

    @Test
    public void shouldFireResizeEventAfterRowResizeCommand() {
        this.dataLayer.addLayerListener(new LayerListenerFixture());
        this.dataLayer.setRowHeightByPosition(2, 100);
    }

    @Test
    public void shouldReturnARectangleStartingFromResizedColumnToEndOfGrid() {
        // Mimics resizing the second column
        this.layerListener = new LayerListenerFixture();
        this.dataLayer.addLayerListener(this.layerListener);
        this.dataLayer.setColumnWidthByPosition(2, 200);

        // This is the propagated event
        ColumnResizeEvent columnResizeEvent =
                (ColumnResizeEvent) this.layerListener.getReceivedEvents().get(0);
        Collection<Rectangle> actualRectangles =
                columnResizeEvent.getChangedPositionRectangles();

        // The affected region should have the following size
        Rectangle expectedRectangle = new Rectangle(2, 0, 3, 7);
        assertEquals(expectedRectangle, actualRectangles.iterator().next());
    }

    @Test
    public void shouldReturnARectangleStartingFromResizedRowToEndOfGrid() {
        // Mimics resizing the third row
        this.layerListener = new LayerListenerFixture();
        this.dataLayer.addLayerListener(this.layerListener);
        this.dataLayer.setRowHeightByPosition(3, 100);

        // This is the propagated event
        RowResizeEvent rowResizeEvent =
                (RowResizeEvent) this.layerListener.getReceivedEvents().get(0);
        Collection<Rectangle> actualRectangles =
                rowResizeEvent.getChangedPositionRectangles();

        // The affected region should have the following size
        Rectangle expectedRectangle = new Rectangle(0, 3, 5, 4);
        assertEquals(expectedRectangle, actualRectangles.iterator().next());
    }
}
