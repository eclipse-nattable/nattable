/*******************************************************************************
 * Copyright (c) 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SelectionModelStructuralChangeEventHandlerTest {

    private ISelectionModel selectionModel;
    private DataLayerFixture dataLayer;
    private SelectionLayer selectionLayer;

    @Before
    public void setup() {
        this.dataLayer = new DataLayerFixture(10, 10);
        DefaultBodyLayerStack bodyLayer = new DefaultBodyLayerStack(this.dataLayer);

        this.selectionLayer = bodyLayer.getSelectionLayer();
        this.selectionModel = this.selectionLayer.getSelectionModel();
    }

    @Test
    public void shouldClearSelectionIfASelectedRowIsModified() {
        this.selectionModel.addSelection(2, 3);

        this.selectionModel.handleLayerEvent(new RowDeleteEvent(this.dataLayer, 3));

        Assert.assertTrue(this.selectionModel.isEmpty());
    }

    @Test
    public void shouldLeaveSelectionUnchangedIfASelectedRowIsNotModified() {
        this.selectionModel.addSelection(2, 3);

        this.selectionModel.handleLayerEvent(new RowDeleteEvent(this.dataLayer, 5));

        Assert.assertFalse(this.selectionModel.isEmpty());
        Assert.assertTrue(this.selectionModel.isRowPositionSelected(3));
    }

    @Test
    public void shouldLeaveSelectionUnchangedIfTheFollowingRowIsModified() {
        this.selectionModel.addSelection(3, 4);

        this.selectionModel.handleLayerEvent(new RowDeleteEvent(this.dataLayer, 5));

        Assert.assertFalse(this.selectionModel.isEmpty());
        Assert.assertTrue(this.selectionModel.isRowPositionSelected(4));
    }

    @Test
    public void shouldClearSelectionIfListIsCleared() {
        this.selectionModel.addSelection(3, 4);

        this.selectionModel.handleLayerEvent(new RowDeleteEvent(this.dataLayer, new Range(0, 9)));

        Assert.assertTrue(this.selectionModel.isEmpty());
    }

    @Test
    public void shouldClearSelectionOnStructuralChanges() {
        this.selectionModel.addSelection(3, 4);
        Assert.assertFalse(this.selectionModel.isEmpty());

        this.selectionModel.handleLayerEvent(new StructuralRefreshEvent(this.dataLayer));
        Assert.assertTrue(this.selectionModel.isEmpty());
    }

    @Test
    public void shouldNotClearSelectionOnStructuralChanges() {
        ((SelectionModel) this.selectionModel).setClearSelectionOnChange(false);
        this.selectionModel.addSelection(3, 4);
        Assert.assertFalse(this.selectionModel.isEmpty());

        this.selectionModel.handleLayerEvent(new StructuralRefreshEvent(this.dataLayer));
        Assert.assertFalse(this.selectionModel.isEmpty());
    }

    @Test
    public void shouldClearSelectionIfAllRowsAreHidden() {
        this.selectionModel.addSelection(3, 4);

        List<Integer> rows = new ArrayList<Integer>();
        rows.add(0);
        rows.add(1);
        rows.add(2);
        rows.add(3);
        rows.add(4);
        rows.add(5);
        rows.add(6);
        rows.add(7);
        rows.add(8);
        rows.add(9);
        this.selectionModel.handleLayerEvent(new HideRowPositionsEvent(this.dataLayer, rows));

        Assert.assertTrue(this.selectionModel.isEmpty());
    }

    @Test
    public void shouldClearSelectionIfSelectedColumnIsHidden() {
        this.selectionModel.addSelection(3, 4);

        List<Integer> columns = new ArrayList<Integer>();
        columns.add(3);
        this.selectionModel.handleLayerEvent(new HideColumnPositionsEvent(this.dataLayer, columns));

        Assert.assertTrue(this.selectionModel.isEmpty());
    }

    @Test
    public void shouldNotClearSelectionIfOtherColumnIsHidden() {
        this.selectionModel.addSelection(3, 4);

        List<Integer> columns = new ArrayList<Integer>();
        columns.add(2);
        this.selectionModel.handleLayerEvent(new HideColumnPositionsEvent(this.dataLayer, columns));

        Assert.assertTrue(this.selectionModel.isEmpty());
    }

    @Test
    public void shouldClearSelectionOnDataUpdates() throws Exception {
        List<RowDataFixture> listFixture = RowDataListFixture.getList(10);
        IRowDataProvider<RowDataFixture> bodyDataProvider = new ListDataProvider<RowDataFixture>(listFixture,
                new ReflectiveColumnPropertyAccessor<RowDataFixture>(
                        RowDataListFixture.getPropertyNames()));

        GridLayerFixture gridLayer = new GridLayerFixture(bodyDataProvider);
        NatTable nattable = new NatTableFixture(gridLayer, false);

        DataLayer bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();
        SelectionLayer selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();

        // test SelectionModel updates
        assertEquals(0, selectionLayer.getFullySelectedRowPositions().length);

        nattable.doCommand(new SelectRowsCommand(nattable, 1, 1, false, false));
        assertEquals(1, selectionLayer.getFullySelectedRowPositions().length);

        // Ford motor at top and selected
        assertEquals("B Ford Motor", nattable.getDataValueByPosition(2, 1).toString());

        Range selection = selectionLayer.getSelectedRowPositions().iterator().next();
        assertEquals("B Ford Motor", listFixture.get(selection.start).getSecurity_description());

        listFixture.add(0, RowDataFixture.getInstance("Tata motors", "A"));

        // fire event to trigger structural refresh
        bodyDataLayer.fireLayerEvent(new StructuralRefreshEvent(bodyDataLayer));

        assertEquals(0, selectionLayer.getFullySelectedRowPositions().length);
    }

    @Test
    public void shouldClearSelectionOnClearingTableWithStructuralRefresh() throws Exception {
        List<RowDataFixture> listFixture = RowDataListFixture.getList(10);
        IRowDataProvider<RowDataFixture> bodyDataProvider = new ListDataProvider<RowDataFixture>(listFixture,
                new ReflectiveColumnPropertyAccessor<RowDataFixture>(
                        RowDataListFixture.getPropertyNames()));

        GridLayerFixture gridLayer = new GridLayerFixture(bodyDataProvider);
        NatTable nattable = new NatTableFixture(gridLayer, false);

        DataLayer bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();
        SelectionLayer selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();

        // test SelectionModel updates
        assertEquals(0, selectionLayer.getFullySelectedRowPositions().length);

        nattable.doCommand(new SelectRowsCommand(nattable, 1, 1, false, false));
        assertEquals(1, selectionLayer.getFullySelectedRowPositions().length);

        // Ford motor at top and selected
        assertEquals("B Ford Motor", nattable.getDataValueByPosition(2, 1).toString());

        Range selection = selectionLayer.getSelectedRowPositions().iterator().next();
        assertEquals("B Ford Motor", listFixture.get(selection.start).getSecurity_description());

        // clear the table
        listFixture.clear();

        // fire event to trigger structural refresh
        bodyDataLayer.fireLayerEvent(new StructuralRefreshEvent(bodyDataLayer));

        // row count of 1 for NatTable because of header
        assertEquals(1, nattable.getRowCount());
        assertTrue("selection is not empty", selectionLayer.getSelectedCells().isEmpty());
        assertTrue("selection model is not empty", selectionLayer.getSelectionModel().getSelections().isEmpty());
    }

    @Test
    public void shouldClearSelectionOnClearingTableWithRowStructuralRefresh() throws Exception {
        List<RowDataFixture> listFixture = RowDataListFixture.getList(10);
        IRowDataProvider<RowDataFixture> bodyDataProvider = new ListDataProvider<RowDataFixture>(listFixture,
                new ReflectiveColumnPropertyAccessor<RowDataFixture>(
                        RowDataListFixture.getPropertyNames()));

        GridLayerFixture gridLayer = new GridLayerFixture(bodyDataProvider);
        NatTable nattable = new NatTableFixture(gridLayer, false);

        DataLayer bodyDataLayer = (DataLayer) gridLayer.getBodyDataLayer();
        SelectionLayer selectionLayer = gridLayer.getBodyLayer().getSelectionLayer();

        // test SelectionModel updates
        assertEquals(0, selectionLayer.getFullySelectedRowPositions().length);

        nattable.doCommand(new SelectRowsCommand(nattable, 1, 1, false, false));
        assertEquals(1, selectionLayer.getFullySelectedRowPositions().length);

        // Ford motor at top and selected
        assertEquals("B Ford Motor", nattable.getDataValueByPosition(2, 1).toString());

        Range selection = selectionLayer.getSelectedRowPositions().iterator().next();
        assertEquals("B Ford Motor", listFixture.get(selection.start).getSecurity_description());

        // clear the table
        listFixture.clear();

        // fire event to trigger structural refresh
        bodyDataLayer.fireLayerEvent(new RowStructuralRefreshEvent(bodyDataLayer));

        // row count of 1 for NatTable because of header
        assertEquals(1, nattable.getRowCount());
        assertTrue("selection is not empty", selectionLayer.getSelectedCells().isEmpty());
        assertTrue("selection model is not empty", selectionLayer.getSelectionModel().getSelections().isEmpty());
    }

}
