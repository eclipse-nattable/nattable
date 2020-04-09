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
package org.eclipse.nebula.widgets.nattable.resize.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ResizeEventDiffTest {

    private ColumnResizeEvent event;
    private ViewportLayer viewportLayer;

    @Before
    public void before() {
        DataLayerFixture dataLayer = new DataLayerFixture(20, 20, 100, 40);
        this.viewportLayer = new ViewportLayer(dataLayer);
        this.viewportLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 400, 400);
            }

        });
        this.viewportLayer.setOriginX(this.viewportLayer.getStartXOfColumnPosition(2));
        this.viewportLayer.setOriginY(this.viewportLayer.getStartYOfRowPosition(2));
        this.event = new ColumnResizeEvent(dataLayer, 2);
    }

    @After
    public void after() {
        assertTrue(this.event.isHorizontalStructureChanged());

        assertFalse(this.event.isVerticalStructureChanged());
        assertNull(this.event.getRowDiffs());
    }

    @Test
    public void testColumnDiffs() {
        Collection<StructuralDiff> columnDiffs = this.event.getColumnDiffs();
        assertNotNull(columnDiffs);
        assertEquals(1, columnDiffs.size());
        assertEquals(new StructuralDiff(DiffTypeEnum.CHANGE, new Range(
                2, 3), new Range(2, 3)), columnDiffs.iterator().next());
    }

    @Test
    public void testConvertToLocal() {
        this.event.convertToLocal(this.viewportLayer);

        Collection<StructuralDiff> columnDiffs = this.event.getColumnDiffs();
        assertNotNull(columnDiffs);
        assertEquals(1, columnDiffs.size());
        assertEquals(new StructuralDiff(DiffTypeEnum.CHANGE, new Range(
                0, 1), new Range(0, 1)), columnDiffs.iterator().next());
    }

}
