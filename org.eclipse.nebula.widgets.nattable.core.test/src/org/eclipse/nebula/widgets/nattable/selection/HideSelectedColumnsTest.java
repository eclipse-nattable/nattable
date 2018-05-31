/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ResizeColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectAllCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.junit.Before;
import org.junit.Test;

public class HideSelectedColumnsTest {
    private SelectionLayer selectionLayer;
    private ColumnHideShowLayer columnHideShowLayer;

    @Before
    public void setUp() {
        this.columnHideShowLayer = new ColumnHideShowLayer(new DataLayerFixture());
        this.selectionLayer = new SelectionLayer(this.columnHideShowLayer);
    }

    @Test
    public void shouldAlsoHideColumnWhichIsNotSelectedButHasAMouseOverIt() {
        this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 2));
        assertTrue(this.columnHideShowLayer.isColumnIndexHidden(2));
    }

    @Test
    public void shouldHideColumnForSelectedCell() {
        // Select cell in column we want to hide
        this.selectionLayer.setSelectedCell(3, 0);

        // Hide selection
        this.selectionLayer.doCommand(new ColumnHideCommand(this.selectionLayer, 3));

        // The previously selected column should be hidden
        assertTrue(this.columnHideShowLayer.isColumnIndexHidden(3));
        assertEquals(4, this.selectionLayer.getColumnCount());
    }

    @Test
    public void shouldHideSelectedColumn() {
        // Select column to hide
        new SelectColumnCommandHandler(this.selectionLayer).selectColumn(2, 0, false, false);

        // Hide column
        this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 2));

        // The previously selected column should be hidden
        assertTrue(this.columnHideShowLayer.isColumnIndexHidden(2));
        assertEquals(4, this.selectionLayer.getColumnCount());
    }

    @Test
    public void shouldHideAllSelectedColumns() {
        // Select cells and columns
        new SelectColumnCommandHandler(this.selectionLayer).selectColumn(2, 0, false, true);
        this.selectionLayer.selectCell(1, 0, false, true);
        this.selectionLayer.selectCell(4, 4, false, true);

        // Hide selection
        this.selectionLayer.doCommand(new MultiColumnHideCommand(this.selectionLayer, 2, 0, 4));

        // Previously selected columns should be hidden
        assertTrue(this.columnHideShowLayer.isColumnIndexHidden(2));
        assertTrue(this.columnHideShowLayer.isColumnIndexHidden(0));
        assertTrue(this.columnHideShowLayer.isColumnIndexHidden(4));
    }

    @Test
    public void shouldFireSelectAllToTopAfterHide() {
        DataLayerFixture bodyDataLayer = new DataLayerFixture();
        SelectionLayer selectionLayer = new SelectionLayer(new ColumnHideShowLayer(bodyDataLayer));
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        // build the column header layer
        IDataProvider columnHeaderDataProvider =
                new DummyColumnHeaderDataProvider(bodyDataLayer.getDataProvider());
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(
                        columnHeaderDataLayer,
                        viewportLayer,
                        selectionLayer);

        // build the row header layer
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyDataLayer.getDataProvider());
        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer =
                new RowHeaderLayer(
                        rowHeaderDataLayer,
                        viewportLayer,
                        selectionLayer);

        // build the corner layer
        IDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(
                        columnHeaderDataProvider,
                        rowHeaderDataProvider);
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        ILayer cornerLayer =
                new CornerLayer(
                        cornerDataLayer,
                        rowHeaderLayer,
                        columnHeaderLayer);

        // build the grid layer
        GridLayer gridLayer =
                new GridLayer(
                        viewportLayer,
                        columnHeaderLayer,
                        rowHeaderLayer,
                        cornerLayer);

        NatTableFixture natTable = new NatTableFixture(gridLayer);

        // select 4 of 5 columns
        SelectColumnCommandHandler handler = new SelectColumnCommandHandler(selectionLayer);
        handler.selectColumn(0, 0, false, true);
        handler.selectColumn(1, 0, false, true);
        handler.selectColumn(3, 0, false, true);
        handler.selectColumn(4, 0, false, true);

        assertEquals(4, selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(0, selectionLayer.getSelectionAnchor().getRowPosition());

        // Hide selection
        selectionLayer.doCommand(new MultiColumnHideCommand(selectionLayer, 0, 2, 3, 4));

        assertEquals(SelectionLayer.NO_SELECTION, selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(SelectionLayer.NO_SELECTION, selectionLayer.getSelectionAnchor().getRowPosition());

        assertEquals(SelectionLayer.NO_SELECTION, selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(SelectionLayer.NO_SELECTION, selectionLayer.getLastSelectedCell().getRowPosition());

        LayerListenerFixture listener = new LayerListenerFixture();
        natTable.addLayerListener(listener);

        selectionLayer.doCommand(new SelectAllCommand());
        assertEquals(1, listener.getEventsCount());
        assertTrue(listener.getReceivedEvents().get(0) instanceof CellSelectionEvent);
    }

    @Test
    public void shouldFireSelectAllToTopAfterHideWithResizeColumnHide() {
        DataLayerFixture bodyDataLayer = new DataLayerFixture();
        SelectionLayer selectionLayer = new SelectionLayer(new ResizeColumnHideShowLayer(bodyDataLayer, bodyDataLayer));
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        // build the column header layer
        IDataProvider columnHeaderDataProvider =
                new DummyColumnHeaderDataProvider(bodyDataLayer.getDataProvider());
        DataLayer columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer =
                new ColumnHeaderLayer(
                        columnHeaderDataLayer,
                        viewportLayer,
                        selectionLayer);

        // build the row header layer
        IDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(bodyDataLayer.getDataProvider());
        DataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer =
                new RowHeaderLayer(
                        rowHeaderDataLayer,
                        viewportLayer,
                        selectionLayer);

        // build the corner layer
        IDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(
                        columnHeaderDataProvider,
                        rowHeaderDataProvider);
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        ILayer cornerLayer =
                new CornerLayer(
                        cornerDataLayer,
                        rowHeaderLayer,
                        columnHeaderLayer);

        // build the grid layer
        GridLayer gridLayer =
                new GridLayer(
                        viewportLayer,
                        columnHeaderLayer,
                        rowHeaderLayer,
                        cornerLayer);

        NatTableFixture natTable = new NatTableFixture(gridLayer);

        // select 4 of 5 columns
        SelectColumnCommandHandler handler = new SelectColumnCommandHandler(selectionLayer);
        handler.selectColumn(0, 0, false, true);
        handler.selectColumn(1, 0, false, true);
        handler.selectColumn(3, 0, false, true);
        handler.selectColumn(4, 0, false, true);

        assertEquals(4, selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(0, selectionLayer.getSelectionAnchor().getRowPosition());

        // Hide selection
        selectionLayer.doCommand(new MultiColumnHideCommand(selectionLayer, 0, 2, 3, 4));

        assertEquals(SelectionLayer.NO_SELECTION, selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(SelectionLayer.NO_SELECTION, selectionLayer.getSelectionAnchor().getRowPosition());

        assertEquals(SelectionLayer.NO_SELECTION, selectionLayer.getLastSelectedCell().getColumnPosition());
        assertEquals(SelectionLayer.NO_SELECTION, selectionLayer.getLastSelectedCell().getRowPosition());

        LayerListenerFixture listener = new LayerListenerFixture();
        natTable.addLayerListener(listener);

        selectionLayer.doCommand(new SelectAllCommand());
        assertEquals(1, listener.getEventsCount());
        assertTrue(listener.getReceivedEvents().get(0) instanceof CellSelectionEvent);
    }
}
