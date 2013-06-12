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


import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.selection.event.SelectionLayerStructuralChangeEventHandler;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SelectionLayerStructuralChangeEventHandlerTest {

	private ISelectionModel selectionModel;
	private DataLayerFixture dataLayer;
	private SelectionLayer selectionLayer;

	@Before
	public void setup(){
		dataLayer = new DataLayerFixture(10, 10);
		DefaultBodyLayerStack bodyLayer = new DefaultBodyLayerStack(dataLayer);
		
		selectionLayer = bodyLayer.getSelectionLayer();
		selectionModel = selectionLayer.getSelectionModel();
	}
	
	@Test
	public void shouldClearSelectionIfASelectedRowIsModified() throws Exception {
		selectionModel.addSelection(2, 3);
		
		SelectionLayerStructuralChangeEventHandler handler = new SelectionLayerStructuralChangeEventHandler(selectionLayer, selectionModel);
		handler.handleLayerEvent(new RowDeleteEvent(dataLayer, 3));
		
		Assert.assertTrue(selectionModel.isEmpty());
	}

	@Test
	public void shouldLeaveSelectionUnchangedIfASelectedRowIsNotModified() throws Exception {
		selectionModel.addSelection(2, 3);
		
		SelectionLayerStructuralChangeEventHandler handler = new SelectionLayerStructuralChangeEventHandler(selectionLayer, selectionModel);
		handler.handleLayerEvent(new RowDeleteEvent(dataLayer, 5));
		
		Assert.assertFalse(selectionModel.isEmpty());
		Assert.assertTrue(selectionModel.isRowPositionSelected(3));
	}

	@Test
	public void shouldLeaveSelectionUnchangedIfTheFollowingRowIsModified() throws Exception {
		selectionModel.addSelection(3, 4);
		
		SelectionLayerStructuralChangeEventHandler handler = new SelectionLayerStructuralChangeEventHandler(selectionLayer, selectionModel);
		handler.handleLayerEvent(new RowDeleteEvent(dataLayer, 5));
		
		Assert.assertFalse(selectionModel.isEmpty());
		Assert.assertTrue(selectionModel.isRowPositionSelected(4));
	}
}
