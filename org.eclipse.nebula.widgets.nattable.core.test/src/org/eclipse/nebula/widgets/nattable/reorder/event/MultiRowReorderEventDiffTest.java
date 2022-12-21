/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MultiRowReorderEventDiffTest {

    private RowReorderEvent event;
    private DataLayerFixture dataLayer;
    private ViewportLayer viewportLayer;

    @BeforeEach
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

    @AfterEach
    public void after() {
        assertFalse(this.event.isHorizontalStructureChanged());

        assertTrue(this.event.isVerticalStructureChanged());
        assertNull(this.event.getColumnDiffs());
    }

    /**
     * - - - + before: 0 1 2 3 4 5 6 7 8 after: 0 1 5 6 2 3 4 7 8 - + + +
     */
    @Test
    public void testReorderRightRowDiffs() {
        this.event = new RowReorderEvent(
                this.dataLayer,
                new int[] { 2, 3, 4 },
                new int[] { 2, 3, 4 },
                7,
                7,
                true);

        Collection<StructuralDiff> rowDiffs = this.event.getRowDiffs();
        assertNotNull(rowDiffs);
        assertEquals(2, rowDiffs.size());
        Iterator<StructuralDiff> iterator = rowDiffs.iterator();
        assertEquals(new StructuralDiff(DiffTypeEnum.DELETE, new Range(2, 5), new Range(2, 2)), iterator.next());
        assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(7, 7), new Range(4, 7)), iterator.next());
    }

    /**
     * - - - + before: 0 1 2 3 4 5 6 7 8 after: 0 1 5 6 2 3 4 7 8 - + + +
     */
    @Test
    public void testReorderRightConvertToLocal() {
        this.event = new RowReorderEvent(
                this.dataLayer,
                new int[] { 2, 3, 4 },
                new int[] { 2, 3, 4 },
                7,
                7,
                true);
        this.event.convertToLocal(this.viewportLayer);

        Collection<StructuralDiff> rowDiffs = this.event.getRowDiffs();
        assertNotNull(rowDiffs);
        assertEquals(2, rowDiffs.size());
        Iterator<StructuralDiff> iterator = rowDiffs.iterator();
        assertEquals(new StructuralDiff(DiffTypeEnum.DELETE, new Range(0, 3), new Range(0, 0)), iterator.next());
        assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(5, 5), new Range(2, 5)), iterator.next());
    }

    /**
     * + - - - before: 0 1 2 3 4 5 6 7 8 9 10 after: 0 1 7 8 9 2 3 4 5 6 10 + +
     * + -
     */
    @Test
    public void testReorderLeftRowDiffs() {
        this.event = new RowReorderEvent(
                this.dataLayer,
                new int[] { 7, 8, 9 },
                new int[] { 7, 8, 9 },
                2,
                2,
                true);

        Collection<StructuralDiff> rowDiffs = this.event.getRowDiffs();
        assertNotNull(rowDiffs);
        assertEquals(2, rowDiffs.size());
        Iterator<StructuralDiff> iterator = rowDiffs.iterator();
        assertEquals(new StructuralDiff(DiffTypeEnum.DELETE, new Range(7, 10), new Range(10, 10)), iterator.next());
        assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(2, 2), new Range(2, 5)), iterator.next());
    }

    /**
     * + - - - before: 0 1 2 3 4 5 6 7 8 9 10 after: 0 1 7 8 9 2 3 4 5 6 10 + +
     * + -
     */
    @Test
    public void testReorderLeftConvertToLocal() {
        this.event = new RowReorderEvent(
                this.dataLayer,
                new int[] { 7, 8, 9 },
                new int[] { 7, 8, 9 },
                2,
                2,
                true);
        this.event.convertToLocal(this.viewportLayer);

        Collection<StructuralDiff> rowDiffs = this.event.getRowDiffs();
        assertNotNull(rowDiffs);
        assertEquals(2, rowDiffs.size());
        Iterator<StructuralDiff> iterator = rowDiffs.iterator();
        assertEquals(new StructuralDiff(DiffTypeEnum.DELETE, new Range(5, 8), new Range(8, 8)), iterator.next());
        assertEquals(new StructuralDiff(DiffTypeEnum.ADD, new Range(0, 0), new Range(0, 3)), iterator.next());
    }

}
