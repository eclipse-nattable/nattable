/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
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
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnInsertEvent;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for testing handling of IStructuralChangeEvents in
 * RowHideShowLayer
 *
 * @author Dirk Fauth
 *
 */
public class ColumnHideShowLayerStructuralChangeEventTest {

    List<List<String>> contents;
    private IUniqueIndexLayer underlyingLayer;
    private ColumnHideShowLayer columnHideShowLayer;

    @Before
    public void setUp() {
        this.contents = new ArrayList<List<String>>();
        this.contents.add(new ArrayList<String>(Arrays.asList("one", "two", "three",
                "four", "five")));
        this.underlyingLayer = new DataLayer(new ListDataProvider<List<String>>(
                this.contents, new IColumnAccessor<List<String>>() {

                    @Override
                    public Object getDataValue(List<String> rowObject,
                            int columnIndex) {
                        return rowObject.get(columnIndex);
                    }

                    @Override
                    public void setDataValue(List<String> rowObject,
                            int columnIndex, Object newValue) {
                        // ignore
                    }

                    @Override
                    public int getColumnCount() {
                        return ColumnHideShowLayerStructuralChangeEventTest.this.contents.get(0).size();
                    }
                }));
        this.columnHideShowLayer = new ColumnHideShowLayer(this.underlyingLayer);
    }

    @Test
    public void testHandleColumnDeleteEvent() {
        // test start order: 0 1 2 3 4
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(4));

        assertEquals("one", this.columnHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.columnHideShowLayer.getDataValueByPosition(1, 0));
        assertEquals("three", this.columnHideShowLayer.getDataValueByPosition(2, 0));
        assertEquals("four", this.columnHideShowLayer.getDataValueByPosition(3, 0));
        assertEquals("five", this.columnHideShowLayer.getDataValueByPosition(4, 0));

        // hide column at position 2: 0 1 3 4
        List<Integer> columnsToHide = new ArrayList<Integer>();
        columnsToHide.add(2);
        this.columnHideShowLayer.hideColumnPositions(columnsToHide);

        assertEquals(4, this.columnHideShowLayer.getColumnCount());
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(4));

        assertEquals("one", this.columnHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.columnHideShowLayer.getDataValueByPosition(1, 0));
        assertEquals("four", this.columnHideShowLayer.getDataValueByPosition(2, 0));
        assertEquals("five", this.columnHideShowLayer.getDataValueByPosition(3, 0));

        // delete column index 1: value "two")
        this.contents.get(0).remove(1);
        this.underlyingLayer
                .fireLayerEvent(new ColumnDeleteEvent(this.underlyingLayer, 1));

        assertEquals(3, this.columnHideShowLayer.getColumnCount());
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(3));

        assertEquals("one", this.columnHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("four", this.columnHideShowLayer.getDataValueByPosition(1, 0));
        assertEquals("five", this.columnHideShowLayer.getDataValueByPosition(2, 0));
    }

    @Test
    public void testHandleHiddenColumnDeleteEvent() {
        // test start order: 0 1 2 3 4
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(4));

        assertEquals("one", this.columnHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.columnHideShowLayer.getDataValueByPosition(1, 0));
        assertEquals("three", this.columnHideShowLayer.getDataValueByPosition(2, 0));
        assertEquals("four", this.columnHideShowLayer.getDataValueByPosition(3, 0));
        assertEquals("five", this.columnHideShowLayer.getDataValueByPosition(4, 0));

        // hide column at position 2: 0 1 3 4
        List<Integer> columnsToHide = new ArrayList<Integer>();
        columnsToHide.add(2);
        this.columnHideShowLayer.hideColumnPositions(columnsToHide);

        assertEquals(4, this.columnHideShowLayer.getColumnCount());
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(4));

        assertEquals("one", this.columnHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.columnHideShowLayer.getDataValueByPosition(1, 0));
        assertEquals("four", this.columnHideShowLayer.getDataValueByPosition(2, 0));
        assertEquals("five", this.columnHideShowLayer.getDataValueByPosition(3, 0));

        // delete column index 2: value "three"
        this.contents.get(0).remove(2);
        this.underlyingLayer
                .fireLayerEvent(new ColumnDeleteEvent(this.underlyingLayer, 2));

        assertEquals(4, this.columnHideShowLayer.getColumnCount());
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(4));

        assertEquals("one", this.columnHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.columnHideShowLayer.getDataValueByPosition(1, 0));
        assertEquals("four", this.columnHideShowLayer.getDataValueByPosition(2, 0));
        assertEquals("five", this.columnHideShowLayer.getDataValueByPosition(3, 0));
    }

    @Test
    public void testHandleLastColumnDeleteEvent() {
        // test start order: 0 1 2 3 4
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(4));

        assertEquals("one", this.columnHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.columnHideShowLayer.getDataValueByPosition(1, 0));
        assertEquals("three", this.columnHideShowLayer.getDataValueByPosition(2, 0));
        assertEquals("four", this.columnHideShowLayer.getDataValueByPosition(3, 0));
        assertEquals("five", this.columnHideShowLayer.getDataValueByPosition(4, 0));

        // hide column at position 2: 0 1 3 4
        List<Integer> columnsToHide = new ArrayList<Integer>();
        columnsToHide.add(2);
        this.columnHideShowLayer.hideColumnPositions(columnsToHide);

        assertEquals(4, this.columnHideShowLayer.getColumnCount());
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(4));

        assertEquals("one", this.columnHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.columnHideShowLayer.getDataValueByPosition(1, 0));
        assertEquals("four", this.columnHideShowLayer.getDataValueByPosition(2, 0));
        assertEquals("five", this.columnHideShowLayer.getDataValueByPosition(3, 0));

        // delete last column in list
        int lastColumnIndex = this.contents.get(0).size() - 1;
        this.contents.get(0).remove(lastColumnIndex);
        this.underlyingLayer.fireLayerEvent(new ColumnDeleteEvent(this.underlyingLayer,
                lastColumnIndex));

        assertEquals(3, this.columnHideShowLayer.getColumnCount());
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(3));

        assertEquals("one", this.columnHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.columnHideShowLayer.getDataValueByPosition(1, 0));
        assertEquals("four", this.columnHideShowLayer.getDataValueByPosition(2, 0));
    }

    @Test
    public void testHandleMultipleColumnDeleteEvent() {
        // test start order: 0 1 2 3 4
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(4));

        assertEquals("one", this.columnHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.columnHideShowLayer.getDataValueByPosition(1, 0));
        assertEquals("three", this.columnHideShowLayer.getDataValueByPosition(2, 0));
        assertEquals("four", this.columnHideShowLayer.getDataValueByPosition(3, 0));
        assertEquals("five", this.columnHideShowLayer.getDataValueByPosition(4, 0));

        // hide column at position 2: 0 1 3 4
        List<Integer> columnsToHide = new ArrayList<Integer>();
        columnsToHide.add(2);
        this.columnHideShowLayer.hideColumnPositions(columnsToHide);

        assertEquals(4, this.columnHideShowLayer.getColumnCount());
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(4));

        assertEquals("one", this.columnHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.columnHideShowLayer.getDataValueByPosition(1, 0));
        assertEquals("four", this.columnHideShowLayer.getDataValueByPosition(2, 0));
        assertEquals("five", this.columnHideShowLayer.getDataValueByPosition(3, 0));

        // delete columns in the middle
        this.contents.get(0).remove(1);
        this.contents.get(0).remove(1);
        this.contents.get(0).remove(1);
        this.underlyingLayer.fireLayerEvent(new ColumnDeleteEvent(this.underlyingLayer,
                new Range(1, 4)));

        assertEquals(2, this.columnHideShowLayer.getColumnCount());
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(2));

        assertEquals("one", this.columnHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("five", this.columnHideShowLayer.getDataValueByPosition(1, 0));
    }

    @Test
    public void testHandleColumnAddEvent() {
        // test start order: 0 1 2 3 4
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(4));

        assertEquals("one", this.columnHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.columnHideShowLayer.getDataValueByPosition(1, 0));
        assertEquals("three", this.columnHideShowLayer.getDataValueByPosition(2, 0));
        assertEquals("four", this.columnHideShowLayer.getDataValueByPosition(3, 0));
        assertEquals("five", this.columnHideShowLayer.getDataValueByPosition(4, 0));

        // hide column at position 2: 0 1 3 4
        List<Integer> columnsToHide = new ArrayList<Integer>();
        columnsToHide.add(2);
        this.columnHideShowLayer.hideColumnPositions(columnsToHide);

        assertEquals(4, this.columnHideShowLayer.getColumnCount());
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(3, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(4));

        assertEquals("one", this.columnHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.columnHideShowLayer.getDataValueByPosition(1, 0));
        assertEquals("four", this.columnHideShowLayer.getDataValueByPosition(2, 0));
        assertEquals("five", this.columnHideShowLayer.getDataValueByPosition(3, 0));

        // add column add index 2
        this.contents.get(0).add(2, "test");
        this.underlyingLayer
                .fireLayerEvent(new ColumnInsertEvent(this.underlyingLayer, 2));

        assertEquals(5, this.columnHideShowLayer.getColumnCount());
        assertEquals(0, this.columnHideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnHideShowLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.columnHideShowLayer.getColumnIndexByPosition(2));
        assertEquals(4, this.columnHideShowLayer.getColumnIndexByPosition(3));
        assertEquals(5, this.columnHideShowLayer.getColumnIndexByPosition(4));
        assertEquals(-1, this.columnHideShowLayer.getColumnIndexByPosition(5));

        assertEquals("one", this.columnHideShowLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.columnHideShowLayer.getDataValueByPosition(1, 0));
        assertEquals("test", this.columnHideShowLayer.getDataValueByPosition(2, 0));
        assertEquals("four", this.columnHideShowLayer.getDataValueByPosition(3, 0));
        assertEquals("five", this.columnHideShowLayer.getDataValueByPosition(4, 0));
    }
}
