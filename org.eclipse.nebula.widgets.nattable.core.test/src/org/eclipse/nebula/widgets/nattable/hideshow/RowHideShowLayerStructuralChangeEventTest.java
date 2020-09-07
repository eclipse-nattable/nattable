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
package org.eclipse.nebula.widgets.nattable.hideshow;

import static org.junit.Assert.assertEquals;

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
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for testing handling of IStructuralChangeEvents in
 * RowHideShowLayer
 *
 * @author Dirk Fauth
 *
 */
public class RowHideShowLayerStructuralChangeEventTest {

    List<String> contents;
    private IUniqueIndexLayer underlyingLayer;
    private RowHideShowLayer rowHideShowLayer;

    @Before
    public void setUp() {
        this.contents = new ArrayList<String>(Arrays.asList("one", "two", "three",
                "four", "five"));
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
        this.rowHideShowLayer = new RowHideShowLayer(this.underlyingLayer);
    }

    @Test
    public void testHandleRowDeleteEvent() {
        // test start order: 0 1 2 3 4
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(4));

        assertEquals("one", this.rowHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowHideShowLayer.getDataValueByPosition(0, 1));
        assertEquals("three", this.rowHideShowLayer.getDataValueByPosition(0, 2));
        assertEquals("four", this.rowHideShowLayer.getDataValueByPosition(0, 3));
        assertEquals("five", this.rowHideShowLayer.getDataValueByPosition(0, 4));

        // hide row at position 2: 0 1 3 4
        List<Integer> rowsToHide = new ArrayList<Integer>();
        rowsToHide.add(2);
        this.rowHideShowLayer.hideRowPositions(rowsToHide);

        assertEquals(4, this.rowHideShowLayer.getRowCount());
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(4));

        assertEquals("one", this.rowHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowHideShowLayer.getDataValueByPosition(0, 1));
        assertEquals("four", this.rowHideShowLayer.getDataValueByPosition(0, 2));
        assertEquals("five", this.rowHideShowLayer.getDataValueByPosition(0, 3));

        // delete row index 1: value "two")
        this.contents.remove(1);
        this.underlyingLayer.fireLayerEvent(new RowDeleteEvent(this.underlyingLayer, 1));

        assertEquals(3, this.rowHideShowLayer.getRowCount());
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(3));

        assertEquals("one", this.rowHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("four", this.rowHideShowLayer.getDataValueByPosition(0, 1));
        assertEquals("five", this.rowHideShowLayer.getDataValueByPosition(0, 2));
    }

    @Test
    public void testHandleHiddenRowDeleteEvent() {
        // test start order: 0 1 2 3 4
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(4));

        assertEquals("one", this.rowHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowHideShowLayer.getDataValueByPosition(0, 1));
        assertEquals("three", this.rowHideShowLayer.getDataValueByPosition(0, 2));
        assertEquals("four", this.rowHideShowLayer.getDataValueByPosition(0, 3));
        assertEquals("five", this.rowHideShowLayer.getDataValueByPosition(0, 4));

        // hide row at position 2: 0 1 3 4
        List<Integer> rowsToHide = new ArrayList<Integer>();
        rowsToHide.add(2);
        this.rowHideShowLayer.hideRowPositions(rowsToHide);

        assertEquals(4, this.rowHideShowLayer.getRowCount());
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(4));

        assertEquals("one", this.rowHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowHideShowLayer.getDataValueByPosition(0, 1));
        assertEquals("four", this.rowHideShowLayer.getDataValueByPosition(0, 2));
        assertEquals("five", this.rowHideShowLayer.getDataValueByPosition(0, 3));

        // delete row index 2: value "three"
        this.contents.remove(2);
        this.underlyingLayer.fireLayerEvent(new RowDeleteEvent(this.underlyingLayer, 2));

        assertEquals(4, this.rowHideShowLayer.getRowCount());
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(4));

        assertEquals("one", this.rowHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowHideShowLayer.getDataValueByPosition(0, 1));
        assertEquals("four", this.rowHideShowLayer.getDataValueByPosition(0, 2));
        assertEquals("five", this.rowHideShowLayer.getDataValueByPosition(0, 3));
    }

    @Test
    public void testHandleLastRowDeleteEvent() {
        // test start order: 0 1 2 3 4
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(4));

        assertEquals("one", this.rowHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowHideShowLayer.getDataValueByPosition(0, 1));
        assertEquals("three", this.rowHideShowLayer.getDataValueByPosition(0, 2));
        assertEquals("four", this.rowHideShowLayer.getDataValueByPosition(0, 3));
        assertEquals("five", this.rowHideShowLayer.getDataValueByPosition(0, 4));

        // hide row at position 2: 0 1 3 4
        List<Integer> rowsToHide = new ArrayList<Integer>();
        rowsToHide.add(2);
        this.rowHideShowLayer.hideRowPositions(rowsToHide);

        assertEquals(4, this.rowHideShowLayer.getRowCount());
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(4));

        assertEquals("one", this.rowHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowHideShowLayer.getDataValueByPosition(0, 1));
        assertEquals("four", this.rowHideShowLayer.getDataValueByPosition(0, 2));
        assertEquals("five", this.rowHideShowLayer.getDataValueByPosition(0, 3));

        // delete last row in list
        int lastRowIndex = this.contents.size() - 1;
        this.contents.remove(lastRowIndex);
        this.underlyingLayer.fireLayerEvent(new RowDeleteEvent(this.underlyingLayer,
                lastRowIndex));

        assertEquals(3, this.rowHideShowLayer.getRowCount());
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(3));

        assertEquals("one", this.rowHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowHideShowLayer.getDataValueByPosition(0, 1));
        assertEquals("four", this.rowHideShowLayer.getDataValueByPosition(0, 2));
    }

    @Test
    public void testHandleMultipleRowDeleteEvent() {
        // test start order: 0 1 2 3 4
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(4));

        assertEquals("one", this.rowHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowHideShowLayer.getDataValueByPosition(0, 1));
        assertEquals("three", this.rowHideShowLayer.getDataValueByPosition(0, 2));
        assertEquals("four", this.rowHideShowLayer.getDataValueByPosition(0, 3));
        assertEquals("five", this.rowHideShowLayer.getDataValueByPosition(0, 4));

        // hide row at position 2: 0 1 3 4
        List<Integer> rowsToHide = new ArrayList<Integer>();
        rowsToHide.add(2);
        this.rowHideShowLayer.hideRowPositions(rowsToHide);

        assertEquals(4, this.rowHideShowLayer.getRowCount());
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(4));

        assertEquals("one", this.rowHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowHideShowLayer.getDataValueByPosition(0, 1));
        assertEquals("four", this.rowHideShowLayer.getDataValueByPosition(0, 2));
        assertEquals("five", this.rowHideShowLayer.getDataValueByPosition(0, 3));

        // delete rows in the middle
        this.contents.remove(1);
        this.contents.remove(1);
        this.contents.remove(1);
        this.underlyingLayer.fireLayerEvent(new RowDeleteEvent(this.underlyingLayer,
                new Range(1, 4)));

        assertEquals(2, this.rowHideShowLayer.getRowCount());
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(2));

        assertEquals("one", this.rowHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("five", this.rowHideShowLayer.getDataValueByPosition(0, 1));
    }

    @Test
    public void testHandleRowAddEvent() {
        // test start order: 0 1 2 3 4
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(4));

        assertEquals("one", this.rowHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowHideShowLayer.getDataValueByPosition(0, 1));
        assertEquals("three", this.rowHideShowLayer.getDataValueByPosition(0, 2));
        assertEquals("four", this.rowHideShowLayer.getDataValueByPosition(0, 3));
        assertEquals("five", this.rowHideShowLayer.getDataValueByPosition(0, 4));

        // hide row at position 2: 0 1 3 4
        List<Integer> rowsToHide = new ArrayList<Integer>();
        rowsToHide.add(2);
        this.rowHideShowLayer.hideRowPositions(rowsToHide);

        assertEquals(4, this.rowHideShowLayer.getRowCount());
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(3, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(4));

        assertEquals("one", this.rowHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowHideShowLayer.getDataValueByPosition(0, 1));
        assertEquals("four", this.rowHideShowLayer.getDataValueByPosition(0, 2));
        assertEquals("five", this.rowHideShowLayer.getDataValueByPosition(0, 3));

        // add row add index 2
        this.contents.add(2, "test");
        this.underlyingLayer.fireLayerEvent(new RowInsertEvent(this.underlyingLayer, 2));

        assertEquals(5, this.rowHideShowLayer.getRowCount());
        assertEquals(0, this.rowHideShowLayer.getRowIndexByPosition(0));
        assertEquals(1, this.rowHideShowLayer.getRowIndexByPosition(1));
        assertEquals(2, this.rowHideShowLayer.getRowIndexByPosition(2));
        assertEquals(4, this.rowHideShowLayer.getRowIndexByPosition(3));
        assertEquals(5, this.rowHideShowLayer.getRowIndexByPosition(4));
        assertEquals(-1, this.rowHideShowLayer.getRowIndexByPosition(5));

        assertEquals("one", this.rowHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.rowHideShowLayer.getDataValueByPosition(0, 1));
        assertEquals("test", this.rowHideShowLayer.getDataValueByPosition(0, 2));
        assertEquals("four", this.rowHideShowLayer.getDataValueByPosition(0, 3));
        assertEquals("five", this.rowHideShowLayer.getDataValueByPosition(0, 4));
    }
}
