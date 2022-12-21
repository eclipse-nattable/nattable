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
package org.eclipse.nebula.widgets.nattable.hideshow.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ShowRowPositionsEventDiffTest {

    private ShowRowPositionsEvent event;
    private DataLayerFixture dataLayer;
    private RowHideShowLayer hideShowLayer;
    private ViewportLayer viewportLayer;

    @BeforeEach
    public void before() {
        this.dataLayer = new DataLayerFixture(20, 20, 100, 40);
        this.hideShowLayer = new RowHideShowLayer(this.dataLayer);
        this.viewportLayer = new ViewportLayer(this.hideShowLayer);
        this.viewportLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 800, 400);
            }

        });
        this.viewportLayer.setOriginX(this.viewportLayer.getStartXOfColumnPosition(2));
        this.viewportLayer.setOriginY(this.viewportLayer.getStartYOfRowPosition(2));

        this.event = new ShowRowPositionsEvent(this.dataLayer, 2, 4, 7, 8, 9);
    }

    @AfterEach
    public void after() {
        assertFalse(this.event.isHorizontalStructureChanged());

        assertTrue(this.event.isVerticalStructureChanged());
        assertNull(this.event.getColumnDiffs());
    }

    /**
     * + + + before: 0 1 3 5 6 8 9 10 11 12 after: 0 1 2 3 4 5 6 7 8 9 + + + + +
     */
    @Test
    public void testColumnDiffs() {
        Collection<StructuralDiff> rowDiffs = this.event.getRowDiffs();
        assertNotNull(rowDiffs);
        assertEquals(3, rowDiffs.size());
        Iterator<StructuralDiff> iterator = rowDiffs.iterator();
        assertEquals(new StructuralDiff(DiffTypeEnum.ADD,
                new Range(2, 2), new Range(2, 3)), iterator.next());
        assertEquals(new StructuralDiff(DiffTypeEnum.ADD,
                new Range(3, 3), new Range(4, 5)), iterator.next());
        assertEquals(new StructuralDiff(DiffTypeEnum.ADD,
                new Range(5, 5), new Range(7, 10)), iterator.next());
    }

    /**
     * + + + before: 0 1 3 5 6 8 9 10 11 12 after: 0 1 2 3 4 5 6 7 8 9 + + + + +
     */
    @Test
    public void testConvertToLocal() {
        this.event.convertToLocal(this.hideShowLayer);

        Collection<StructuralDiff> rowDiffs = this.event.getRowDiffs();
        assertNotNull(rowDiffs);
        assertEquals(3, rowDiffs.size());
        Iterator<StructuralDiff> iterator = rowDiffs.iterator();
        assertEquals(new StructuralDiff(DiffTypeEnum.ADD,
                new Range(2, 2), new Range(2, 3)), iterator.next());
        assertEquals(new StructuralDiff(DiffTypeEnum.ADD,
                new Range(3, 3), new Range(4, 5)), iterator.next());
        assertEquals(new StructuralDiff(DiffTypeEnum.ADD,
                new Range(5, 5), new Range(7, 10)), iterator.next());

        this.event.convertToLocal(this.viewportLayer);

        rowDiffs = this.event.getRowDiffs();
        assertNotNull(rowDiffs);
        assertEquals(3, rowDiffs.size());
        iterator = rowDiffs.iterator();
        assertEquals(new StructuralDiff(DiffTypeEnum.ADD,
                new Range(0, 0), new Range(0, 1)), iterator.next());
        assertEquals(new StructuralDiff(DiffTypeEnum.ADD,
                new Range(1, 1), new Range(2, 3)), iterator.next());
        assertEquals(new StructuralDiff(DiffTypeEnum.ADD,
                new Range(3, 3), new Range(5, 8)), iterator.next());
    }

}
