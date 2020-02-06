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
package org.eclipse.nebula.widgets.nattable.reorder.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;

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

public class MultiColumnReorderEventDiffTest {

    private ColumnReorderEvent event;
    private DataLayerFixture dataLayer;
    private ViewportLayer viewportLayer;

    @Before
    public void before() {
        this.dataLayer = new DataLayerFixture(20, 20, 100, 40);
        this.viewportLayer = new ViewportLayer(this.dataLayer);
        this.viewportLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 800, 400);
            }

        });
        this.viewportLayer.setOriginX(this.viewportLayer.getStartXOfColumnPosition(2));
        this.viewportLayer.setOriginY(this.viewportLayer.getStartYOfRowPosition(2));
    }

    @After
    public void after() {
        assertTrue(this.event.isHorizontalStructureChanged());

        assertFalse(this.event.isVerticalStructureChanged());
        assertNull(this.event.getRowDiffs());
    }

    /**
     * - - - + before: 0 1 2 3 4 5 6 7 8 after: 0 1 5 6 2 3 4 7 8 - + + +
     */
    @Test
    public void testReorderRightColumnDiffs() {
        this.event = new ColumnReorderEvent(
                this.dataLayer,
                new int[] { 2, 3, 4 },
                new int[] { 2, 3, 4 },
                7,
                7,
                true);

        Collection<StructuralDiff> columnDiffs = this.event.getColumnDiffs();
        assertNotNull(columnDiffs);
        assertEquals(2, columnDiffs.size());
        Iterator<StructuralDiff> iterator = columnDiffs.iterator();
        assertEquals(new StructuralDiff(DiffTypeEnum.DELETE, new Range(2, 5), new Range(2, 2)), iterator.next());
        assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(7, 7), new Range(4, 7)), iterator.next());
    }

    /**
     * - - - + before: 0 1 2 3 4 5 6 7 8 after: 0 1 5 6 2 3 4 7 8 - + + +
     */
    @Test
    public void testReorderRightConvertToLocal() {
        this.event = new ColumnReorderEvent(
                this.dataLayer,
                new int[] { 2, 3, 4 },
                new int[] { 2, 3, 4 },
                7,
                7,
                true);
        this.event.convertToLocal(this.viewportLayer);

        Collection<StructuralDiff> columnDiffs = this.event.getColumnDiffs();
        assertNotNull(columnDiffs);
        assertEquals(2, columnDiffs.size());
        Iterator<StructuralDiff> iterator = columnDiffs.iterator();
        assertEquals(new StructuralDiff(DiffTypeEnum.DELETE, new Range(0, 3), new Range(0, 0)), iterator.next());
        assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(5, 5), new Range(2, 5)), iterator.next());
    }

    /**
     * + - - - before: 0 1 2 3 4 5 6 7 8 9 10 after: 0 1 7 8 9 2 3 4 5 6 10 + +
     * + -
     */
    @Test
    public void testReorderLeftColumnDiffs() {
        this.event = new ColumnReorderEvent(
                this.dataLayer,
                new int[] { 7, 8, 9 },
                new int[] { 7, 8, 9 },
                2,
                2,
                true);

        Collection<StructuralDiff> columnDiffs = this.event.getColumnDiffs();
        assertNotNull(columnDiffs);
        assertEquals(2, columnDiffs.size());
        Iterator<StructuralDiff> iterator = columnDiffs.iterator();
        assertEquals(new StructuralDiff(DiffTypeEnum.DELETE, new Range(7, 10), new Range(10, 10)), iterator.next());
        assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(2, 2), new Range(2, 5)), iterator.next());
    }

    /**
     * + - - - before: 0 1 2 3 4 5 6 7 8 9 10 after: 0 1 7 8 9 2 3 4 5 6 10 + +
     * + -
     */
    @Test
    public void testReorderLeftConvertToLocal() {
        this.event = new ColumnReorderEvent(
                this.dataLayer,
                new int[] { 7, 8, 9 },
                new int[] { 7, 8, 9 },
                2,
                2,
                true);
        this.event.convertToLocal(this.viewportLayer);

        Collection<StructuralDiff> columnDiffs = this.event.getColumnDiffs();
        assertNotNull(columnDiffs);
        assertEquals(2, columnDiffs.size());
        Iterator<StructuralDiff> iterator = columnDiffs.iterator();
        assertEquals(new StructuralDiff(DiffTypeEnum.DELETE, new Range(5, 8), new Range(8, 8)), iterator.next());
        assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(0, 0), new Range(0, 3)), iterator.next());
    }

}
