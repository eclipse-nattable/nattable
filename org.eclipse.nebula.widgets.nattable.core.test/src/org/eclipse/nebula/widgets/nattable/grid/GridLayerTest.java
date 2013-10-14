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
package org.eclipse.nebula.widgets.nattable.grid;


import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.InitializeClientAreaCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.command.LayerCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseDataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ViewportLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GridLayerTest {
	
	private GridLayer gridLayerUnderTest;
	
	private DataLayer bodyDataLayer;
	private DataLayer columnHeaderDataLayer;
	private DataLayer rowHeaderDataLayer;
	private DataLayer cornerDataLayer;
	
	@Before
	public void setup(){
		bodyDataLayer = new BaseDataLayerFixture();
		columnHeaderDataLayer = new BaseDataLayerFixture();
		rowHeaderDataLayer = new BaseDataLayerFixture();
		cornerDataLayer = new BaseDataLayerFixture();
		
		gridLayerUnderTest = new DefaultGridLayer(bodyDataLayer, columnHeaderDataLayer, rowHeaderDataLayer, cornerDataLayer);
	}
	
	@Test
	public void getLayers() throws Exception {
		Assert.assertNotNull(gridLayerUnderTest.getBodyLayer());
		Assert.assertNotNull(gridLayerUnderTest.getColumnHeaderLayer());
		Assert.assertNotNull(gridLayerUnderTest.getRowHeaderLayer());
		Assert.assertNotNull(gridLayerUnderTest.getCornerLayer());
	}
	
	@Test
	public void doCommandInvokesBodyFirst() throws Exception {
		DummyCommandHandler bodyCommandHandler = new DummyCommandHandler(true);
		DummyCommandHandler columnHeaderCommandHandler = new DummyCommandHandler(true);
		DummyCommandHandler rowHeaderCommandHandler = new DummyCommandHandler(true);
		DummyCommandHandler cornerCommandHandler = new DummyCommandHandler(true);
		
		bodyDataLayer.registerCommandHandler(bodyCommandHandler);
		columnHeaderDataLayer.registerCommandHandler(columnHeaderCommandHandler);
		rowHeaderDataLayer.registerCommandHandler(rowHeaderCommandHandler);
		cornerDataLayer.registerCommandHandler(cornerCommandHandler);
		
		final ILayerCommand command = new LayerCommandFixture();
		
		gridLayerUnderTest.doCommand(command);
		
		Assert.assertTrue(bodyCommandHandler.isCommandCaught());
		Assert.assertFalse(columnHeaderCommandHandler.isCommandCaught());
		Assert.assertFalse(rowHeaderCommandHandler.isCommandCaught());
		Assert.assertFalse(cornerCommandHandler.isCommandCaught());
	}

	@Test
	public void doCommandInvokesOtherLayers() throws Exception {
		DummyCommandHandler bodyCommandHandler = new DummyCommandHandler(false);
		DummyCommandHandler columnHeaderCommandHandler = new DummyCommandHandler(false);
		DummyCommandHandler rowHeaderCommandHandler = new DummyCommandHandler(false);
		DummyCommandHandler cornerCommandHandler = new DummyCommandHandler(true);
		
		bodyDataLayer.registerCommandHandler(bodyCommandHandler);
		columnHeaderDataLayer.registerCommandHandler(columnHeaderCommandHandler);
		rowHeaderDataLayer.registerCommandHandler(rowHeaderCommandHandler);
		cornerDataLayer.registerCommandHandler(cornerCommandHandler);
		
		final ILayerCommand command = new LayerCommandFixture();
		
		gridLayerUnderTest.doCommand(command);
		
		Assert.assertFalse(bodyCommandHandler.isCommandCaught());
		Assert.assertFalse(columnHeaderCommandHandler.isCommandCaught());
		Assert.assertFalse(rowHeaderCommandHandler.isCommandCaught());
		Assert.assertTrue(cornerCommandHandler.isCommandCaught());
	}
	
	// **** New tests using fixtures ****
	
	/**
	 * @see ViewportLayerFixture#DEFAULT_CLIENT_AREA
	 */
	@Test
	public void initBodyLayer() throws Exception {
		DefaultGridLayer gridLayer = new GridLayerFixture();

		ViewportLayer viewport = gridLayer.getBodyLayer().getViewportLayer();
		viewport.setClientAreaProvider(new IClientAreaProvider() {
			public Rectangle getClientArea() {
				return new Rectangle(0,0,160,80);
			}
		});
		
		//Client area gets init when this command is fired
		gridLayer.doCommand(new InitializeClientAreaCommandFixture());
		
		Assert.assertEquals(160, viewport.getClientAreaWidth());
		Assert.assertEquals(80, viewport.getClientAreaHeight());

		Assert.assertEquals(160, viewport.getWidth());
		Assert.assertEquals(80, viewport.getHeight());
	}

	@Test
	public void initRowHeaderHeight() throws Exception {
		GridLayer gridLayer = new GridLayerFixture();
		gridLayer.doCommand(new InitializeClientAreaCommandFixture());

		ILayer rowHeader = gridLayer.getRowHeaderLayer();
		// Only visible rows are counted
		Assert.assertEquals(100, rowHeader.getHeight());
		Assert.assertEquals(40, rowHeader.getWidth());
	}

	@Test
	public void initCorner() throws Exception {
		GridLayer gridLayer = new GridLayerFixture();

		ILayer colHeader = gridLayer.getCornerLayer();
		Assert.assertEquals(20, colHeader.getHeight());
		Assert.assertEquals(40, colHeader.getWidth());
	}
	
	class DummyCommandHandler extends AbstractLayerCommandHandler<LayerCommandFixture> {

		private final boolean catchCommand;
		
		private boolean caughtCommand;
		
		public DummyCommandHandler(boolean catchCommand) {
			this.catchCommand = catchCommand;
		}
		
		@Override
		public boolean doCommand(LayerCommandFixture command) {
			if (catchCommand) {
				caughtCommand = true;
				return true;
			} else {
				return false;
			}
		}

		public Class<LayerCommandFixture> getCommandClass() {
			return LayerCommandFixture.class;
		}
		
		public boolean isCommandCaught() {
			return caughtCommand;
		}
		
	}

}
