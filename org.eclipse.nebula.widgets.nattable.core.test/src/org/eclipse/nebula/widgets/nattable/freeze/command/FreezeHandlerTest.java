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
package org.eclipse.nebula.widgets.nattable.freeze.command;


import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.event.ColumnReorderEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FreezeHandlerTest {
	
	private FreezeCommandHandler commandHandler;
	private CompositeFreezeLayer compositeFreezeLayer;
	private FreezeLayer freezeLayer;
	private ViewportLayer viewportLayer;
	private SelectionLayer selectionLayer;
	
	@Before
	public void setUp() {
		final DataLayer bodyDataLayer = new DataLayer(new DummyBodyDataProvider(10, 10));
		final DefaultBodyLayerStack bodyLayer = new DefaultBodyLayerStack(bodyDataLayer);		
		selectionLayer = bodyLayer.getSelectionLayer();
		
		freezeLayer = new FreezeLayer(selectionLayer);
	    compositeFreezeLayer = new CompositeFreezeLayer(freezeLayer, bodyLayer.getViewportLayer(), bodyLayer.getSelectionLayer());
	    viewportLayer = bodyLayer.getViewportLayer();
		commandHandler = new FreezeCommandHandler(freezeLayer, viewportLayer, selectionLayer);
	    compositeFreezeLayer.registerCommandHandler(commandHandler);
	    
	    compositeFreezeLayer.setClientAreaProvider(new IClientAreaProvider() {
			@Override
			public Rectangle getClientArea() {
				return new Rectangle(0,0,600,400);
			}
	    });
	    
	    // Shoot this command so that the viewport can be initialized
	    compositeFreezeLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.H_SCROLL | SWT.V_SCROLL)));
	}
	
	@Test
	public void shouldFreezeFirstColumn() {
		// This is what would happen if we selected to freeze a column from some sort of menu action
		compositeFreezeLayer.doCommand(new FreezeColumnCommand(compositeFreezeLayer, 1));
		Assert.assertEquals(0, freezeLayer.getTopLeftPosition().columnPosition);
		Assert.assertEquals(-1, freezeLayer.getTopLeftPosition().rowPosition);
		Assert.assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
		Assert.assertEquals(-1, freezeLayer.getBottomRightPosition().rowPosition);
		
		// Check viewport origin
		Assert.assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
		Assert.assertEquals(0, viewportLayer.getMinimumOriginRowPosition());
	}
	
	@Test
	public void shouldFreezeRowsAndColumnsBasedOnSelection() {
		compositeFreezeLayer.doCommand(new SelectCellCommand(compositeFreezeLayer, 2, 2, false, false));
		
		// Make sure selection layer processed command
		final PositionCoordinate lastSelectedCell = selectionLayer.getLastSelectedCellPosition();
		Assert.assertEquals(2, lastSelectedCell.columnPosition);
		Assert.assertEquals(2, lastSelectedCell.rowPosition);
		
		// This is what would happen if we selected to freeze from a selected cell
		compositeFreezeLayer.doCommand(new FreezeSelectionCommand());
		Assert.assertEquals(0, freezeLayer.getTopLeftPosition().columnPosition);
		Assert.assertEquals(0, freezeLayer.getTopLeftPosition().rowPosition);
		Assert.assertNotNull(freezeLayer.getBottomRightPosition());
		Assert.assertEquals(1, freezeLayer.getBottomRightPosition().columnPosition);
		Assert.assertEquals(1, freezeLayer.getBottomRightPosition().rowPosition);
		
		// Check viewport origin
		Assert.assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
		Assert.assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
	}
	
	@Test
	public void shouldFreezeAfterScrolling() {
		// Scroll the viewport to the first column
		viewportLayer.resetOrigin(viewportLayer.getStartXOfColumnPosition(0), viewportLayer.getStartYOfRowPosition(0));
		viewportLayer.setOriginX(viewportLayer.getStartXOfColumnPosition(1));
		Assert.assertEquals(1, viewportLayer.getColumnIndexByPosition(0));
				
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 3, 3, false, false));
		compositeFreezeLayer.doCommand(new FreezeSelectionCommand());
		
		// Freezelayer
		Assert.assertEquals(2, freezeLayer.getColumnCount());
		
		// Test Positions
		Assert.assertEquals(1, freezeLayer.getTopLeftPosition().columnPosition);
		Assert.assertEquals(0, freezeLayer.getTopLeftPosition().rowPosition);
		Assert.assertEquals(2, freezeLayer.getBottomRightPosition().columnPosition);
		Assert.assertEquals(2, freezeLayer.getBottomRightPosition().rowPosition);
		
		// Test indexes
		Assert.assertEquals(1, freezeLayer.getColumnIndexByPosition(0));
		Assert.assertEquals(1, freezeLayer.getTopLeftPosition().columnPosition);
		Assert.assertEquals(0, freezeLayer.getTopLeftPosition().rowPosition);
		
		Assert.assertEquals(2, freezeLayer.getColumnIndexByPosition(freezeLayer.getColumnCount() - 1));
		Assert.assertEquals(2, freezeLayer.getBottomRightPosition().columnPosition);
		Assert.assertEquals(2, freezeLayer.getBottomRightPosition().rowPosition);
		
		// Test viewport		
		Assert.assertEquals(3, viewportLayer.getMinimumOriginColumnPosition());
		Assert.assertEquals(3, viewportLayer.getMinimumOriginRowPosition());
		Assert.assertEquals(4, viewportLayer.getColumnIndexByPosition(0));
	}
	
	@Test
	public void shouldRestructureFrozenArea() {
		final ReorderListener reorderListener = new ReorderListener();
		viewportLayer.addLayerListener(reorderListener);

		// Scroll the viewport to the first column
		viewportLayer.resetOrigin(viewportLayer.getStartXOfColumnPosition(0), viewportLayer.getStartYOfRowPosition(0));
		viewportLayer.setOriginX(viewportLayer.getStartXOfColumnPosition(1));
		Assert.assertEquals(1, viewportLayer.getColumnIndexByPosition(0));
				
		selectionLayer.doCommand(new SelectCellCommand(selectionLayer, 3, 3, false, false));
		compositeFreezeLayer.doCommand(new FreezeSelectionCommand());
		
		// Move right edge out of frozen area
		Assert.assertEquals(2, freezeLayer.getColumnCount());
		compositeFreezeLayer.doCommand(new ColumnReorderCommand(compositeFreezeLayer, 1, 3));
		
		Assert.assertEquals(1, freezeLayer.getColumnCount());
	}
	
	class ReorderListener implements ILayerListener {
		
		private ColumnReorderEvent reorderEvent;

		@Override
		public void handleLayerEvent(ILayerEvent event) {
			if (event instanceof ColumnReorderEvent) {
				reorderEvent = (ColumnReorderEvent)event;
			}
		}
		
		public int getReorderToColumnPosition() {
			return reorderEvent.getBeforeToColumnPosition();
		}
	};
}
