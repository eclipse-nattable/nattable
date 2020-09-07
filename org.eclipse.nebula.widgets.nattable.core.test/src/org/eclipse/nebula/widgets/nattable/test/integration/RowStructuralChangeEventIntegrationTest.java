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
package org.eclipse.nebula.widgets.nattable.test.integration;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommand;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowInsertEvent;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.RowReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class RowStructuralChangeEventIntegrationTest {

    List<String> contents;
    private IUniqueIndexLayer underlyingLayer;
    private RowReorderLayer rowReorderLayer;
    private RowHideShowLayer rowHideShowLayer;
    private SelectionLayer selectionLayer;
    private ViewportLayer viewportLayer;

    private NatTable natTable;

    @Before
    public void setUp() {
        this.contents = new ArrayList<String>(Arrays.asList("one", "two", "three", "four", "five"));
        IDataProvider bodyDataProvider = new ListDataProvider<String>(this.contents,
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
                });
        this.underlyingLayer = new DataLayer(bodyDataProvider);
        this.rowReorderLayer = new RowReorderLayer(this.underlyingLayer);
        this.rowHideShowLayer = new RowHideShowLayer(this.rowReorderLayer);

        this.selectionLayer = new SelectionLayer(this.rowHideShowLayer);
        this.viewportLayer = new ViewportLayer(this.selectionLayer);

        IDataProvider colDataProvider = new DummyColumnHeaderDataProvider(bodyDataProvider);
        ColumnHeaderLayer colHeader = new ColumnHeaderLayer(
                new DataLayer(colDataProvider), this.viewportLayer, this.selectionLayer);

        IDataProvider rowDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
        RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(
                new DataLayer(rowDataProvider), this.viewportLayer, this.selectionLayer);

        CornerLayer cornerLayer = new CornerLayer(
                new DataLayer(new DefaultCornerDataProvider(colDataProvider, rowDataProvider)),
                rowHeaderLayer, colHeader);

        GridLayer grid = new GridLayer(this.viewportLayer, colHeader, rowHeaderLayer, cornerLayer);
        this.natTable = new NatTable(new Shell(), grid);
        this.natTable.setSize(600, 600);
    }

    @Test
    public void testInit() {
        // test start order: 0 1 2 3 4
        assertEquals(0, this.viewportLayer.getRowIndexByPosition(0));
        assertEquals(1, this.viewportLayer.getRowIndexByPosition(1));
        assertEquals(2, this.viewportLayer.getRowIndexByPosition(2));
        assertEquals(3, this.viewportLayer.getRowIndexByPosition(3));
        assertEquals(4, this.viewportLayer.getRowIndexByPosition(4));

        assertEquals("one", this.viewportLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.viewportLayer.getDataValueByPosition(0, 1));
        assertEquals("three", this.viewportLayer.getDataValueByPosition(0, 2));
        assertEquals("four", this.viewportLayer.getDataValueByPosition(0, 3));
        assertEquals("five", this.viewportLayer.getDataValueByPosition(0, 4));
    }

    @Test
    public void testReorder() {
        testInit();

        // reorder to inverse order: 4 3 2 1 0
        this.natTable.doCommand(new RowReorderCommand(this.viewportLayer, 4, 0));
        this.natTable.doCommand(new RowReorderCommand(this.viewportLayer, 4, 1));
        this.natTable.doCommand(new RowReorderCommand(this.viewportLayer, 4, 2));
        this.natTable.doCommand(new RowReorderCommand(this.viewportLayer, 4, 3));

        assertEquals(4, this.viewportLayer.getRowIndexByPosition(0));
        assertEquals(3, this.viewportLayer.getRowIndexByPosition(1));
        assertEquals(2, this.viewportLayer.getRowIndexByPosition(2));
        assertEquals(1, this.viewportLayer.getRowIndexByPosition(3));
        assertEquals(0, this.viewportLayer.getRowIndexByPosition(4));

        assertEquals("five", this.viewportLayer.getDataValueByPosition(0, 0));
        assertEquals("four", this.viewportLayer.getDataValueByPosition(0, 1));
        assertEquals("three", this.viewportLayer.getDataValueByPosition(0, 2));
        assertEquals("two", this.viewportLayer.getDataValueByPosition(0, 3));
        assertEquals("one", this.viewportLayer.getDataValueByPosition(0, 4));
    }

    @Test
    public void testHideShow() {
        testInit();

        // hide row at position 2: 0 1 3 4
        this.natTable.doCommand(new RowHideCommand(this.viewportLayer, 2));

        assertEquals(4, this.viewportLayer.getRowCount());

        assertEquals(0, this.viewportLayer.getRowIndexByPosition(0));
        assertEquals(1, this.viewportLayer.getRowIndexByPosition(1));
        assertEquals(3, this.viewportLayer.getRowIndexByPosition(2));
        assertEquals(4, this.viewportLayer.getRowIndexByPosition(3));
        assertEquals(-1, this.viewportLayer.getRowIndexByPosition(4));

        assertEquals("one", this.viewportLayer.getDataValueByPosition(0, 0));
        assertEquals("two", this.viewportLayer.getDataValueByPosition(0, 1));
        assertEquals("four", this.viewportLayer.getDataValueByPosition(0, 2));
        assertEquals("five", this.viewportLayer.getDataValueByPosition(0, 3));
    }

    @Test
    public void testReorderHide() {
        testInit();

        // reorder to inverse order: 4 3 2 1 0
        this.natTable.doCommand(new RowReorderCommand(this.viewportLayer, 4, 0));
        this.natTable.doCommand(new RowReorderCommand(this.viewportLayer, 4, 1));
        this.natTable.doCommand(new RowReorderCommand(this.viewportLayer, 4, 2));
        this.natTable.doCommand(new RowReorderCommand(this.viewportLayer, 4, 3));

        // hide row at position 2: 0 1 3 4
        this.natTable.doCommand(new RowHideCommand(this.viewportLayer, 2));

        assertEquals(4, this.viewportLayer.getRowCount());

        assertEquals(4, this.viewportLayer.getRowIndexByPosition(0));
        assertEquals(3, this.viewportLayer.getRowIndexByPosition(1));
        assertEquals(1, this.viewportLayer.getRowIndexByPosition(2));
        assertEquals(0, this.viewportLayer.getRowIndexByPosition(3));
        assertEquals(-1, this.viewportLayer.getRowIndexByPosition(4));

        assertEquals("five", this.viewportLayer.getDataValueByPosition(0, 0));
        assertEquals("four", this.viewportLayer.getDataValueByPosition(0, 1));
        assertEquals("two", this.viewportLayer.getDataValueByPosition(0, 2));
        assertEquals("one", this.viewportLayer.getDataValueByPosition(0, 3));
    }

    @Test
    public void testHideReorder() {
        testInit();

        // hide row at position 2: 0 1 3 4
        this.natTable.doCommand(new RowHideCommand(this.viewportLayer, 2));

        // reorder to inverse order: 4 3 1 0
        this.natTable.doCommand(new RowReorderCommand(this.viewportLayer, 3, 0));
        this.natTable.doCommand(new RowReorderCommand(this.viewportLayer, 3, 1));
        this.natTable.doCommand(new RowReorderCommand(this.viewportLayer, 3, 2));

        assertEquals(4, this.viewportLayer.getRowCount());

        assertEquals(4, this.viewportLayer.getRowIndexByPosition(0));
        assertEquals(3, this.viewportLayer.getRowIndexByPosition(1));
        assertEquals(1, this.viewportLayer.getRowIndexByPosition(2));
        assertEquals(0, this.viewportLayer.getRowIndexByPosition(3));
        assertEquals(-1, this.viewportLayer.getRowIndexByPosition(4));

        assertEquals("five", this.viewportLayer.getDataValueByPosition(0, 0));
        assertEquals("four", this.viewportLayer.getDataValueByPosition(0, 1));
        assertEquals("two", this.viewportLayer.getDataValueByPosition(0, 2));
        assertEquals("one", this.viewportLayer.getDataValueByPosition(0, 3));
    }

    @Test
    public void testDeleteLastRow() {
        testInit();

        // delete last row
        int index = this.contents.size() - 1;
        this.contents.remove(index);
        this.underlyingLayer.fireLayerEvent(new RowDeleteEvent(this.underlyingLayer, index));

        assertEquals(4, this.viewportLayer.getRowCount());
    }

    @Test
    public void shouldUpdateOnInsertAndDelete() {
        this.contents.add("six");
        this.underlyingLayer.fireLayerEvent(new RowInsertEvent(this.underlyingLayer, 5));

        this.viewportLayer.doCommand(new RowReorderCommand(this.viewportLayer, 3, 6));
        this.viewportLayer.doCommand(new RowReorderCommand(this.viewportLayer, 3, 5));
        this.viewportLayer.doCommand(new MultiRowHideCommand(this.viewportLayer, new int[] { 2, 3, 5 }));

        assertEquals("[0, 1, 2, 5, 4, 3]", this.rowReorderLayer.getRowIndexOrder().toString());
        assertEquals("[2, 3, 5]", this.rowHideShowLayer.getHiddenRowIndexes().toString());

        this.contents.add(3, "test");
        this.underlyingLayer.fireLayerEvent(new RowInsertEvent(this.underlyingLayer, 3));

        assertEquals("[0, 1, 2, 3, 6, 5, 4]", this.rowReorderLayer.getRowIndexOrder().toString());
        assertEquals("[2, 4, 6]", this.rowHideShowLayer.getHiddenRowIndexes().toString());

        this.contents.remove(3);
        this.underlyingLayer.fireLayerEvent(new RowDeleteEvent(this.underlyingLayer, 3));

        assertEquals("[0, 1, 2, 5, 4, 3]", this.rowReorderLayer.getRowIndexOrder().toString());
        assertEquals("[2, 3, 5]", this.rowHideShowLayer.getHiddenRowIndexes().toString());
    }
}
