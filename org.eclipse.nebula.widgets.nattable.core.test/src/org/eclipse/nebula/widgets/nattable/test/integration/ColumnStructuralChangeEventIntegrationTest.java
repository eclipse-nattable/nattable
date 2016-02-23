/*******************************************************************************
 * Copyright (c) 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.integration;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.grid.data.DummyModifiableBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnInsertEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

public class ColumnStructuralChangeEventIntegrationTest {

    DummyModifiableBodyDataProvider provider = new DummyModifiableBodyDataProvider(6, 6);
    DummyGridLayerStack grid = new DummyGridLayerStack(this.provider);
    NatTableFixture natTableFixture;
    LayerListenerFixture listener;

    @Before
    public void setup() {
        this.natTableFixture = new NatTableFixture(this.grid);
        this.listener = new LayerListenerFixture();
        this.natTableFixture.addLayerListener(this.listener);
    }

    @Test
    public void shouldUpdateReorderOnInsert() {
        DefaultBodyLayerStack body = this.grid.getBodyLayer();
        ColumnReorderLayer reorderLayer = body.getColumnReorderLayer();

        body.doCommand(new ColumnReorderCommand(body, 3, 6));
        body.doCommand(new ColumnReorderCommand(body, 3, 5));

        assertEquals("[0, 1, 2, 5, 4, 3]", reorderLayer.getColumnIndexOrder().toString());

        this.provider.setColumnCount(7);
        this.grid.getBodyDataLayer().fireLayerEvent(new ColumnInsertEvent(this.grid.getBodyDataLayer(), 3));

        assertEquals("[0, 1, 2, 3, 6, 5, 4]", reorderLayer.getColumnIndexOrder().toString());
    }

    @Test
    public void shouldUpdateReorderOnInsertAndDelete() {
        DefaultBodyLayerStack body = this.grid.getBodyLayer();
        ColumnReorderLayer reorderLayer = body.getColumnReorderLayer();

        body.doCommand(new ColumnReorderCommand(body, 3, 6));
        body.doCommand(new ColumnReorderCommand(body, 3, 5));

        assertEquals("[0, 1, 2, 5, 4, 3]", reorderLayer.getColumnIndexOrder().toString());

        this.provider.setColumnCount(7);
        this.grid.getBodyDataLayer().fireLayerEvent(new ColumnInsertEvent(this.grid.getBodyDataLayer(), 3));

        assertEquals("[0, 1, 2, 3, 6, 5, 4]", reorderLayer.getColumnIndexOrder().toString());

        this.provider.setColumnCount(6);
        this.grid.getBodyDataLayer().fireLayerEvent(new ColumnDeleteEvent(this.grid.getBodyDataLayer(), 3));

        assertEquals("[0, 1, 2, 5, 4, 3]", reorderLayer.getColumnIndexOrder().toString());
    }

    @Test
    public void shouldUpdateHiddenOnInsert() {
        DefaultBodyLayerStack body = this.grid.getBodyLayer();
        ColumnHideShowLayer hideShowLayer = body.getColumnHideShowLayer();

        body.doCommand(new MultiColumnHideCommand(body, new int[] { 2, 3, 5 }));

        assertEquals("[2, 3, 5]", hideShowLayer.getHiddenColumnIndexes().toString());

        this.provider.setColumnCount(7);
        this.grid.getBodyDataLayer().fireLayerEvent(new ColumnInsertEvent(this.grid.getBodyDataLayer(), 3));

        assertEquals("[2, 4, 6]", hideShowLayer.getHiddenColumnIndexes().toString());
    }

    @Test
    public void shouldUpdateHiddenOnInsertAndDelete() {
        DefaultBodyLayerStack body = this.grid.getBodyLayer();
        ColumnHideShowLayer hideShowLayer = body.getColumnHideShowLayer();

        body.doCommand(new MultiColumnHideCommand(body, new int[] { 2, 3, 5 }));

        assertEquals("[2, 3, 5]", hideShowLayer.getHiddenColumnIndexes().toString());

        this.provider.setColumnCount(7);
        this.grid.getBodyDataLayer().fireLayerEvent(new ColumnInsertEvent(this.grid.getBodyDataLayer(), 3));

        assertEquals("[2, 4, 6]", hideShowLayer.getHiddenColumnIndexes().toString());

        this.provider.setColumnCount(6);
        this.grid.getBodyDataLayer().fireLayerEvent(new ColumnDeleteEvent(this.grid.getBodyDataLayer(), 3));

        assertEquals("[2, 3, 5]", hideShowLayer.getHiddenColumnIndexes().toString());
    }

    @Test
    public void shouldUpdateOnInsert() {
        DefaultBodyLayerStack body = this.grid.getBodyLayer();
        ColumnReorderLayer reorderLayer = body.getColumnReorderLayer();
        ColumnHideShowLayer hideShowLayer = body.getColumnHideShowLayer();

        body.doCommand(new ColumnReorderCommand(body, 3, 6));
        body.doCommand(new ColumnReorderCommand(body, 3, 5));
        body.doCommand(new MultiColumnHideCommand(body, new int[] { 2, 3, 5 }));

        assertEquals("[0, 1, 2, 5, 4, 3]", reorderLayer.getColumnIndexOrder().toString());
        assertEquals("[2, 3, 5]", hideShowLayer.getHiddenColumnIndexes().toString());

        this.provider.setColumnCount(7);
        this.grid.getBodyDataLayer().fireLayerEvent(new ColumnInsertEvent(this.grid.getBodyDataLayer(), 3));

        assertEquals("[0, 1, 2, 3, 6, 5, 4]", reorderLayer.getColumnIndexOrder().toString());
        assertEquals("[2, 4, 6]", hideShowLayer.getHiddenColumnIndexes().toString());
    }

    @Test
    public void shouldUpdateOnInsertAndDelete() {
        DefaultBodyLayerStack body = this.grid.getBodyLayer();
        ColumnReorderLayer reorderLayer = body.getColumnReorderLayer();
        ColumnHideShowLayer hideShowLayer = body.getColumnHideShowLayer();

        body.doCommand(new ColumnReorderCommand(body, 3, 6));
        body.doCommand(new ColumnReorderCommand(body, 3, 5));
        body.doCommand(new MultiColumnHideCommand(body, new int[] { 2, 3, 5 }));

        assertEquals("[0, 1, 2, 5, 4, 3]", reorderLayer.getColumnIndexOrder().toString());
        assertEquals("[2, 3, 5]", hideShowLayer.getHiddenColumnIndexes().toString());

        this.provider.setColumnCount(7);
        this.grid.getBodyDataLayer().fireLayerEvent(new ColumnInsertEvent(this.grid.getBodyDataLayer(), 3));

        assertEquals("[0, 1, 2, 3, 6, 5, 4]", reorderLayer.getColumnIndexOrder().toString());
        assertEquals("[2, 4, 6]", hideShowLayer.getHiddenColumnIndexes().toString());

        this.provider.setColumnCount(6);
        this.grid.getBodyDataLayer().fireLayerEvent(new ColumnDeleteEvent(this.grid.getBodyDataLayer(), 3));

        assertEquals("[0, 1, 2, 5, 4, 3]", reorderLayer.getColumnIndexOrder().toString());
        assertEquals("[2, 3, 5]", hideShowLayer.getHiddenColumnIndexes().toString());
    }
}
