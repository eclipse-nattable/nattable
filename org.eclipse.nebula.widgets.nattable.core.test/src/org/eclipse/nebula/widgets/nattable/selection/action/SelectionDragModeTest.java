/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.action;

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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SelectionDragModeTest {
	
	NatTable natTable;
	CellSelectionDragMode dragMode;
	MouseEvent mouseEvent;
	private DummyGridLayerStack gridLayer;
	private LayerListenerFixture listener;
	
	@Before
	public void setup() {
		gridLayer = new DummyGridLayerStack();
		natTable = new NatTable(new Shell(Display.getDefault()), gridLayer);
		natTable.setSize(400,400);
		natTable.doCommand(new InitializeClientAreaCommandFixture());
		dragMode = new CellSelectionDragMode();
		Event event = new Event();
		event.widget = new Shell();
		event.x = 100;
		event.y = 100;
		mouseEvent = new MouseEvent(event);
		
		listener = new LayerListenerFixture();
		gridLayer.addLayerListener(listener);
	}
	
	@Test
	public void mouseDownShouldNotFireCommand() throws Exception {
		dragMode.mouseDown(natTable, mouseEvent);
		
		List<ILayerEvent> receivedEvents = listener.getReceivedEvents();
		Assert.assertNotNull(receivedEvents);

		Assert.assertEquals(0, receivedEvents.size());
	}

}
