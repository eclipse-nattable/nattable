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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowInsertEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for testing handling of IStructuralChangeEvents in RowReorderLayer
 *
 * @author Dirk Fauth
 *
 */
public class RowReorderLayerStructuralChangeEventTest {

    List<String> contents;
    private IUniqueIndexLayer underlyingLayer;
    private RowReorderLayer rowReorderLayer;

    @BeforeEach
    public void setUp() {
        this.contents = new ArrayList<String>(Arrays.asList("one", "two", "three",
                "four"));
        this.underlyingLayer = new DataLayer(new ListDataProvider<String>(this.contents,
                new IColumnAccessor<String>() {

                    @Override
                    public Object getDataValue(String rowObject, int columnIndex) {
                        return rowObject;
                    }

                    @Override
                    public void setDataValue(String rowObject, int columnIndex,
                            Object newValue) {
                        // ignore
                    }

                    @Override
                    public int getColumnCount() {
                        return 1;
                    }
                }));
        this.rowReorderLayer = new RowReorderLayer(this.underlyingLayer);
    }

    @Test
    public void testHandleRowDeleteEvent() {
        // test start order: 0 1 2 3
        assertEquals(0, this.rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(3, this.rowReorderLayer.getRowIndexByPosition(3));

        assertEquals("one", this.rowReorderLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowReorderLayer.getDataValueByPosition(0, 1));
        assertEquals("three", this.rowReorderLayer.getDataValueByPosition(0, 2));
        assertEquals("four", this.rowReorderLayer.getDataValueByPosition(0, 3));

        // reorder to inverse order: 3 2 1 0
        this.rowReorderLayer.reorderRowPosition(3, 0);
        this.rowReorderLayer.reorderRowPosition(3, 1);
        this.rowReorderLayer.reorderRowPosition(3, 2);
        assertEquals(3, this.rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(2, this.rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(1, this.rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(0, this.rowReorderLayer.getRowIndexByPosition(3));

        assertEquals("four", this.rowReorderLayer.getDataValueByPosition(0, 0));
        assertEquals("three", this.rowReorderLayer.getDataValueByPosition(0, 1));
        assertEquals("two", this.rowReorderLayer.getDataValueByPosition(0, 2));
        assertEquals("one", this.rowReorderLayer.getDataValueByPosition(0, 3));

        // delete row position 1 (index 2: value "three")
        this.contents.remove(2);
        this.underlyingLayer.fireLayerEvent(new RowDeleteEvent(this.underlyingLayer, 2));

        assertEquals(2, this.rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(0, this.rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(-1, this.rowReorderLayer.getRowIndexByPosition(3));

        assertEquals("four", this.rowReorderLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowReorderLayer.getDataValueByPosition(0, 1));
        assertEquals("one", this.rowReorderLayer.getDataValueByPosition(0, 2));
    }

    @Test
    public void testHandleLastRowDeleteEvent() {
        // test start order: 0 1 2 3
        assertEquals(0, this.rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(3, this.rowReorderLayer.getRowIndexByPosition(3));

        assertEquals("one", this.rowReorderLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowReorderLayer.getDataValueByPosition(0, 1));
        assertEquals("three", this.rowReorderLayer.getDataValueByPosition(0, 2));
        assertEquals("four", this.rowReorderLayer.getDataValueByPosition(0, 3));

        // reorder to inverse order: 3 2 1 0
        this.rowReorderLayer.reorderRowPosition(3, 0);
        this.rowReorderLayer.reorderRowPosition(3, 1);
        this.rowReorderLayer.reorderRowPosition(3, 2);
        assertEquals(3, this.rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(2, this.rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(1, this.rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(0, this.rowReorderLayer.getRowIndexByPosition(3));

        assertEquals("four", this.rowReorderLayer.getDataValueByPosition(0, 0));
        assertEquals("three", this.rowReorderLayer.getDataValueByPosition(0, 1));
        assertEquals("two", this.rowReorderLayer.getDataValueByPosition(0, 2));
        assertEquals("one", this.rowReorderLayer.getDataValueByPosition(0, 3));

        // delete last row in list
        int lastRowIndex = this.contents.size() - 1;
        this.contents.remove(lastRowIndex);
        this.underlyingLayer.fireLayerEvent(new RowDeleteEvent(this.underlyingLayer,
                lastRowIndex));

        assertEquals(2, this.rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(0, this.rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(-1, this.rowReorderLayer.getRowIndexByPosition(3));

        assertEquals("three", this.rowReorderLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowReorderLayer.getDataValueByPosition(0, 1));
        assertEquals("one", this.rowReorderLayer.getDataValueByPosition(0, 2));
    }

    @Test
    public void testHandleMultipleRowDeleteEvent() {
        // test start order: 0 1 2 3
        assertEquals(0, this.rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(3, this.rowReorderLayer.getRowIndexByPosition(3));

        assertEquals("one", this.rowReorderLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowReorderLayer.getDataValueByPosition(0, 1));
        assertEquals("three", this.rowReorderLayer.getDataValueByPosition(0, 2));
        assertEquals("four", this.rowReorderLayer.getDataValueByPosition(0, 3));

        // reorder to inverse order: 3 2 1 0
        this.rowReorderLayer.reorderRowPosition(3, 0);
        this.rowReorderLayer.reorderRowPosition(3, 1);
        this.rowReorderLayer.reorderRowPosition(3, 2);
        assertEquals(3, this.rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(2, this.rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(1, this.rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(0, this.rowReorderLayer.getRowIndexByPosition(3));

        assertEquals("four", this.rowReorderLayer.getDataValueByPosition(0, 0));
        assertEquals("three", this.rowReorderLayer.getDataValueByPosition(0, 1));
        assertEquals("two", this.rowReorderLayer.getDataValueByPosition(0, 2));
        assertEquals("one", this.rowReorderLayer.getDataValueByPosition(0, 3));

        // delete rows in the middle
        this.contents.remove(1);
        this.contents.remove(1);
        this.underlyingLayer.fireLayerEvent(new RowDeleteEvent(this.underlyingLayer,
                new Range(1, 3)));

        assertEquals(2, this.rowReorderLayer.getRowCount());
        assertEquals(1, this.rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(0, this.rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(-1, this.rowReorderLayer.getRowIndexByPosition(2));

        assertEquals("four", this.rowReorderLayer.getDataValueByPosition(0, 0));
        assertEquals("one", this.rowReorderLayer.getDataValueByPosition(0, 1));
    }

    @Test
    public void testHandleRowAddEvent() {
        // test start order: 0 1 2 3
        assertEquals(0, this.rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(3, this.rowReorderLayer.getRowIndexByPosition(3));

        assertEquals("one", this.rowReorderLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowReorderLayer.getDataValueByPosition(0, 1));
        assertEquals("three", this.rowReorderLayer.getDataValueByPosition(0, 2));
        assertEquals("four", this.rowReorderLayer.getDataValueByPosition(0, 3));

        // reorder to inverse order: 3 2 1 0
        this.rowReorderLayer.reorderRowPosition(3, 0);
        this.rowReorderLayer.reorderRowPosition(3, 1);
        this.rowReorderLayer.reorderRowPosition(3, 2);
        assertEquals(3, this.rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(2, this.rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(1, this.rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(0, this.rowReorderLayer.getRowIndexByPosition(3));

        assertEquals("four", this.rowReorderLayer.getDataValueByPosition(0, 0));
        assertEquals("three", this.rowReorderLayer.getDataValueByPosition(0, 1));
        assertEquals("two", this.rowReorderLayer.getDataValueByPosition(0, 2));
        assertEquals("one", this.rowReorderLayer.getDataValueByPosition(0, 3));

        // add row add index 2
        this.contents.add(2, "test");
        this.underlyingLayer.fireLayerEvent(new RowInsertEvent(this.underlyingLayer, 2));

        assertEquals(4, this.rowReorderLayer.getRowIndexByPosition(0));
        assertEquals(3, this.rowReorderLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowReorderLayer.getRowIndexByPosition(2));
        assertEquals(1, this.rowReorderLayer.getRowIndexByPosition(3));
        assertEquals(0, this.rowReorderLayer.getRowIndexByPosition(4));

        assertEquals("four", this.rowReorderLayer.getDataValueByPosition(0, 0));
        assertEquals("three", this.rowReorderLayer.getDataValueByPosition(0, 1));
        assertEquals("test", this.rowReorderLayer.getDataValueByPosition(0, 2));
        assertEquals("two", this.rowReorderLayer.getDataValueByPosition(0, 3));
        assertEquals("one", this.rowReorderLayer.getDataValueByPosition(0, 4));
    }
}
