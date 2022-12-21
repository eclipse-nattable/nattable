/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.test.fixture.InitializeClientAreaCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SelectionDragModeTest {

    NatTable natTable;
    CellSelectionDragMode dragMode;
    MouseEvent mouseEvent;
    private DummyGridLayerStack gridLayer;
    private LayerListenerFixture listener;

    @BeforeEach
    public void setup() {
        this.gridLayer = new DummyGridLayerStack();
        this.natTable = new NatTable(new Shell(Display.getDefault()), this.gridLayer);
        this.natTable.setSize(400, 400);
        this.natTable.doCommand(new InitializeClientAreaCommandFixture());
        this.dragMode = new CellSelectionDragMode();
        Event event = new Event();
        event.widget = new Shell();
        event.x = 100;
        event.y = 100;
        this.mouseEvent = new MouseEvent(event);

        this.listener = new LayerListenerFixture();
        this.gridLayer.addLayerListener(this.listener);
    }

    @Test
    public void mouseDownShouldNotFireCommand() throws Exception {
        this.dragMode.mouseDown(this.natTable, this.mouseEvent);

        List<ILayerEvent> receivedEvents = this.listener.getReceivedEvents();
        assertNotNull(receivedEvents);

        assertEquals(0, receivedEvents.size());
    }

}
