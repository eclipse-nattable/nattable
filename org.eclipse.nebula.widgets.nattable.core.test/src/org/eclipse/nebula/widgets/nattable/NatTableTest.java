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
package org.eclipse.nebula.widgets.nattable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.LayerEventFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.command.AnyCommandHandlerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;


public class NatTableTest {

	private NatTable natTable;
	private DataLayerFixture underlyingLayerFixture;

	@Before
	public void setup(){
		underlyingLayerFixture = new DataLayerFixture(10,5,100,20);
		natTable = new NatTable(new Shell(Display.getDefault()), underlyingLayerFixture);
	}

	@Test
	public void shouldPassOnLayerEventsToListeners() throws Exception {
		LayerListenerFixture listener = new LayerListenerFixture();

		natTable.addLayerListener(listener);
		natTable.handleLayerEvent(new LayerEventFixture());

		assertTrue(listener.containsInstanceOf(LayerEventFixture.class));
	}

	@Test
	public void shouldFireDisposeCommandOnDisposal() throws Exception {
		AnyCommandHandlerFixture commandHandler = new AnyCommandHandlerFixture();
		underlyingLayerFixture.registerCommandHandler(commandHandler);

		natTable.dispose();

		assertEquals(1, commandHandler.getNumberOfCommandsHandled());
		assertTrue(commandHandler.getCommadHandled() instanceof DisposeResourcesCommand);
	}
}
