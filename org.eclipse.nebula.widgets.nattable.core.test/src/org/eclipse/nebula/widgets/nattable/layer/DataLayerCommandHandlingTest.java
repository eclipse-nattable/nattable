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


import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DataLayerCommandHandlingTest {
	
	private static final String TEST_VALUE = "New Value";
	
	private DataLayer dataLayer;
	private UpdateDataCommand command;
	
	@Before
	public void setup() {
		dataLayer = new DataLayerFixture();
		command = new UpdateDataCommand(dataLayer, 2, 2, TEST_VALUE);
	}
	
	@Test
	public void handleUpdateDataCommand() throws Exception {
		dataLayer.doCommand(command);
		Assert.assertEquals(TEST_VALUE,dataLayer.getDataProvider().getDataValue(2, 2));
	}
	
	@Test
	public void handleUpdateDataCommandRaisesEvents() throws Exception {
		LayerListenerFixture listener = new LayerListenerFixture();
		dataLayer.addLayerListener(listener);
		dataLayer.doCommand(command);
		Assert.assertTrue(listener.getReceivedEvents().get(0) instanceof CellVisualChangeEvent);
	}	

	@Test
	public void handleSameUpdateDataCommandRaisesNoEvents() throws Exception {
		LayerListenerFixture listener = new LayerListenerFixture();
		dataLayer.addLayerListener(listener);
		dataLayer.doCommand(command);
		Assert.assertTrue(listener.getReceivedEvents().size() == 1);
		Assert.assertTrue(listener.getReceivedEvents().get(0) instanceof CellVisualChangeEvent);

		//as calling the UpdateCommand with the same value should not trigger any event
		//the size of the received events will stay 1 (the one event from before which is cached)
		dataLayer.doCommand(command);
		Assert.assertTrue(listener.getReceivedEvents().size() == 1);
	}	
}
