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
package org.eclipse.nebula.widgets.nattable.resize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.layer.stack.DummyGridLayerStack;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectAllCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AutoResizeColumnsTest {

	private ConfigRegistry configRegistry;
	private Image img;
	private GCFactory gcFactory;

	@Before
	public void setUp() {
		configRegistry = new ConfigRegistry();
		new DefaultNatTableStyleConfiguration().configureRegistry(configRegistry);

		img = new Image(Display.getDefault(), new Rectangle(0, 0, 200, 150));
		gcFactory = new GCFactory(img);

		// Use a common, foxed width font to avoid failing the test on a different platform
		Font normalFont = GUIHelper.getFont(new FontData("Courier", 8, SWT.NORMAL));
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, normalFont);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL);
	}

	@After
	public void tearDown() {
		img.dispose();
	}

	private void setClientAreaProvider(ILayer layer) {
		layer.setClientAreaProvider(new IClientAreaProvider() {
			public Rectangle getClientArea() {
				return new Rectangle(0,0,1050,250);
			}
		});
		layer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));
	}

	/**
	 * These sequence of actions were causing a nasty bug in AutoResize
	 */
	@Test
	public void autoResizeOneColumn() throws Exception {
		GridLayer gridLayer = new DummyGridLayerStack();
		setClientAreaProvider(gridLayer);

		// Resize column
		gridLayer.doCommand(new ColumnResizeCommand(gridLayer, 2, 10));
		assertEquals(10, gridLayer.getColumnWidthByPosition(2));

		// Auto resize the one column
		InitializeAutoResizeColumnsCommand command = new InitializeAutoResizeColumnsCommand(gridLayer, 2, configRegistry, gcFactory);
		gridLayer.doCommand(command);
		// Note: the actual resized width is platform specific (font dependency),
		// hence we can't compare against a fixed value.
		int columnWidth = gridLayer.getColumnWidthByPosition(2);
		assertTrue(columnWidth > 10);

		// Reorder columns
		gridLayer.doCommand(new ColumnReorderCommand(gridLayer, 2, 1));
		assertEquals(columnWidth, gridLayer.getColumnWidthByPosition(1));

		// Select all columns
		gridLayer.doCommand(new SelectAllCommand());

		// Resize all selected columns
		command = new InitializeAutoResizeColumnsCommand(gridLayer, 1, configRegistry, gcFactory);
		gridLayer.doCommand(command);

		for (int columnPosition = 1; columnPosition <= 20; columnPosition++) {
			assertTrue("column " + columnPosition + " should have been resized, but it is still its original width", gridLayer.getColumnWidthByPosition(columnPosition) != 100);
		}
	}

	/**
	 * Scenario: Multiple columns are selected but a non selected column is auto resized.
	 */
	@Test
	public void shouldAutoResizeCorrectlyIfMultipleColumnsAreSelected() throws Exception {
		GridLayer gridLayer = new DefaultGridLayer(RowDataListFixture.getList(), RowDataListFixture.getPropertyNames(), RowDataListFixture.getPropertyToLabelMap());
		setClientAreaProvider(gridLayer);

		// Resize grid column 1, 2
		gridLayer.doCommand(new ColumnResizeCommand(gridLayer, 1, 10));
		gridLayer.doCommand(new ColumnResizeCommand(gridLayer, 2, 10));
		assertEquals(10, gridLayer.getColumnWidthByPosition(1));
		assertEquals(10, gridLayer.getColumnWidthByPosition(2));

		// Fully select columns 1, 2
		SelectionLayer selectionLayer = ((DefaultBodyLayerStack) gridLayer.getBodyLayer()).getSelectionLayer();
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 0, 0, false, false));
		selectionLayer.doCommand(new SelectColumnCommand(selectionLayer, 1, 0, true, false));
		assertEquals(2, selectionLayer.getFullySelectedColumnPositions().length);

		// Resize grid column 5
		gridLayer.doCommand(new ColumnResizeCommand(gridLayer, 5, 10));
		assertEquals(10, gridLayer.getColumnWidthByPosition(5));

		// Auto resize column 5
		InitializeAutoResizeColumnsCommand command = new InitializeAutoResizeColumnsCommand(gridLayer, 5, configRegistry, gcFactory);
		gridLayer.doCommand(command);

		// Columns 1 and 2 should not be resized
		assertEquals(10, gridLayer.getColumnWidthByPosition(1));
		assertEquals(10, gridLayer.getColumnWidthByPosition(2));
		assertTrue(gridLayer.getColumnWidthByPosition(5) > 10);
	}
}
