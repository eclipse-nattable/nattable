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
package org.eclipse.nebula.widgets.nattable.layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.List;


import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.PersistableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.PropertiesFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.command.CommandHandlerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.command.LayerCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AbstractLayerTest {

	private DataLayerFixture dataLayer;
	private LayerListenerFixture firstListener;

	@Before
	public void setup() {
		dataLayer = new DataLayerFixture();
		
		firstListener = new LayerListenerFixture();
		dataLayer.addLayerListener(firstListener);
	}
	
	@Test
	public void testFireOriginalEventIfOnlyOneListener() {
		ILayerEvent event = new ColumnResizeEvent(dataLayer, 2);
		dataLayer.fireLayerEvent(event);
		
		List<ILayerEvent> receivedEvents = firstListener.getReceivedEvents();
		assertNotNull(receivedEvents);
		assertEquals(1, receivedEvents.size());
		assertSame(event, receivedEvents.get(0));
	}
	
	@Test
	public void testFireClonedEventIfMultipleListeners() {
		LayerListenerFixture secondListener = new LayerListenerFixture();
		dataLayer.addLayerListener(secondListener);
		
		ILayerEvent event = new ColumnResizeEvent(dataLayer, 2);
		dataLayer.fireLayerEvent(event);
		
		List<ILayerEvent> receivedEvents = firstListener.getReceivedEvents();
		assertNotNull(receivedEvents);
		assertEquals(1, receivedEvents.size());
		assertNotSame(event, receivedEvents.get(0));
		
		receivedEvents = secondListener.getReceivedEvents();
		assertNotNull(receivedEvents);
		assertEquals(1, receivedEvents.size());
		assertSame(event, receivedEvents.get(0));
	}
	
	@Test
	public void persistablesAreSaved() throws Exception {
		PersistableFixture persistable = new PersistableFixture();
		PropertiesFixture properties = new PropertiesFixture();

		dataLayer.registerPersistable(persistable);
		dataLayer.saveState("test_prefix", properties);

		Assert.assertTrue(persistable.stateSaved);
	}
	
	@Test
	public void commandHandlerRegistration() throws Exception {
		LayerCommandFixture command = new LayerCommandFixture();
		CommandHandlerFixture commandHandler = new CommandHandlerFixture();

		dataLayer.registerCommandHandler(commandHandler);
		dataLayer.doCommand(command);
		
		assertNotNull(commandHandler.getLastCommandHandled());
		commandHandler.clearLastCommandHandled();
		
		dataLayer.unregisterCommandHandler(command.getClass());
		dataLayer.doCommand(command);
		assertNull(commandHandler.getLastCommandHandled());
	}
}
