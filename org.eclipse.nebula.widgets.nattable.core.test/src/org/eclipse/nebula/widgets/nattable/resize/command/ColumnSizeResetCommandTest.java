/*******************************************************************************
 * Copyright (c) 2017, 2020 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.NoScalingDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnStructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Before;
import org.junit.Test;

public class ColumnSizeResetCommandTest {

    private DataLayer dataLayer;
    private LayerListenerFixture listener;

    @Before
    public void setup() {
        this.dataLayer = new DataLayer(new DummyBodyDataProvider(10, 10));
        this.listener = new LayerListenerFixture();

        this.dataLayer.addLayerListener(this.listener);
    }

    @Test
    public void testHandleColumnSizeResetCommand() {
        this.dataLayer.doCommand(new ColumnResizeCommand(this.dataLayer, 3, 50));

        assertEquals(50, this.dataLayer.getColumnWidthByPosition(3));
        assertEquals(1, this.listener.getEventsCount());

        this.dataLayer.doCommand(new ColumnWidthResetCommand());

        assertEquals(100, this.dataLayer.getColumnWidthByPosition(3));
        assertEquals(2, this.listener.getEventsCount());
        assertTrue(this.listener.containsInstanceOf(ColumnStructuralRefreshEvent.class));
    }

    @Test
    public void testHandleRowSizeResetCommandWithoutEvent() {
        this.dataLayer.doCommand(new ColumnResizeCommand(this.dataLayer, 3, 50));

        assertEquals(50, this.dataLayer.getColumnWidthByPosition(3));
        assertEquals(1, this.listener.getEventsCount());

        this.dataLayer.doCommand(new ColumnWidthResetCommand(false));

        assertEquals(100, this.dataLayer.getColumnWidthByPosition(3));
        assertEquals(1, this.listener.getEventsCount());
        assertFalse(this.listener.containsInstanceOf(ColumnStructuralRefreshEvent.class));
    }

    @Test
    public void testResetAllRegions() {
        DummyGridLayerStack dummyGridLayerStack = new DummyGridLayerStack();
        NatTable natTable = new NatTableFixture(dummyGridLayerStack);
        natTable.doCommand(new ConfigureScalingCommand(new NoScalingDpiConverter()));

        assertEquals(40, dummyGridLayerStack.getRowHeaderDataLayer().getColumnWidthByPosition(0));
        assertEquals(100, dummyGridLayerStack.getBodyDataLayer().getColumnWidthByPosition(2));

        ((DataLayer) dummyGridLayerStack.getRowHeaderDataLayer()).setColumnWidthByPosition(0, 100);
        ((DataLayer) dummyGridLayerStack.getBodyDataLayer()).setColumnWidthByPosition(2, 50);

        assertEquals(100, dummyGridLayerStack.getRowHeaderDataLayer().getColumnWidthByPosition(0));
        assertEquals(50, dummyGridLayerStack.getBodyDataLayer().getColumnWidthByPosition(2));

        natTable.doCommand(new ColumnWidthResetCommand());

        assertEquals(40, dummyGridLayerStack.getRowHeaderDataLayer().getColumnWidthByPosition(0));
        assertEquals(100, dummyGridLayerStack.getBodyDataLayer().getColumnWidthByPosition(2));
    }

    @Test
    public void testResetOnlyBody() {
        DummyGridLayerStack dummyGridLayerStack = new DummyGridLayerStack();
        NatTable natTable = new NatTableFixture(dummyGridLayerStack);
        natTable.doCommand(new ConfigureScalingCommand(new NoScalingDpiConverter()));

        assertEquals(40, dummyGridLayerStack.getRowHeaderDataLayer().getColumnWidthByPosition(0));
        assertEquals(100, dummyGridLayerStack.getBodyDataLayer().getColumnWidthByPosition(2));

        ((DataLayer) dummyGridLayerStack.getRowHeaderDataLayer()).setColumnWidthByPosition(0, 100);
        ((DataLayer) dummyGridLayerStack.getBodyDataLayer()).setColumnWidthByPosition(2, 50);

        assertEquals(100, dummyGridLayerStack.getRowHeaderDataLayer().getColumnWidthByPosition(0));
        assertEquals(50, dummyGridLayerStack.getBodyDataLayer().getColumnWidthByPosition(2));

        natTable.doCommand(new ColumnWidthResetCommand(GridRegion.BODY));

        assertEquals(100, dummyGridLayerStack.getRowHeaderDataLayer().getColumnWidthByPosition(0));
        assertEquals(100, dummyGridLayerStack.getBodyDataLayer().getColumnWidthByPosition(2));
    }

}
