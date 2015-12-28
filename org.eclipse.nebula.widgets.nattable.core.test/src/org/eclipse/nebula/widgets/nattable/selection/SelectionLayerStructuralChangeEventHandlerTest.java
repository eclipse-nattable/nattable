/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.selection.event.SelectionLayerStructuralChangeEventHandler;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class SelectionLayerStructuralChangeEventHandlerTest {

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
    public void shouldClearSelectionIfASelectedRowIsModified() throws Exception {
        this.selectionModel.addSelection(2, 3);

        SelectionLayerStructuralChangeEventHandler handler = new SelectionLayerStructuralChangeEventHandler(
                this.selectionLayer);
        handler.handleLayerEvent(new RowDeleteEvent(this.dataLayer, 3));

        Assert.assertTrue(this.selectionModel.isEmpty());
    }

    @Test
    public void shouldLeaveSelectionUnchangedIfASelectedRowIsNotModified()
            throws Exception {
        this.selectionModel.addSelection(2, 3);

        SelectionLayerStructuralChangeEventHandler handler = new SelectionLayerStructuralChangeEventHandler(
                this.selectionLayer);
        handler.handleLayerEvent(new RowDeleteEvent(this.dataLayer, 5));

        Assert.assertFalse(this.selectionModel.isEmpty());
        Assert.assertTrue(this.selectionModel.isRowPositionSelected(3));
    }

    @Test
    public void shouldLeaveSelectionUnchangedIfTheFollowingRowIsModified()
            throws Exception {
        this.selectionModel.addSelection(3, 4);

        SelectionLayerStructuralChangeEventHandler handler = new SelectionLayerStructuralChangeEventHandler(
                this.selectionLayer);
        handler.handleLayerEvent(new RowDeleteEvent(this.dataLayer, 5));

        Assert.assertFalse(this.selectionModel.isEmpty());
        Assert.assertTrue(this.selectionModel.isRowPositionSelected(4));
    }

    @Test
    public void shouldClearSelectionIfListIsCleared() {
        this.selectionModel.addSelection(3, 4);

        SelectionLayerStructuralChangeEventHandler handler = new SelectionLayerStructuralChangeEventHandler(
                this.selectionLayer);
        handler.handleLayerEvent(new RowDeleteEvent(this.dataLayer, new Range(0, 9)));

        Assert.assertTrue(this.selectionModel.isEmpty());
    }

    @Test
    public void shouldClearSelectionIfAllRowsAreHidden() {
        this.selectionModel.addSelection(3, 4);

        SelectionLayerStructuralChangeEventHandler handler = new SelectionLayerStructuralChangeEventHandler(
                this.selectionLayer);
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
        handler.handleLayerEvent(new HideRowPositionsEvent(this.dataLayer, rows));

        Assert.assertTrue(this.selectionModel.isEmpty());
    }
}
