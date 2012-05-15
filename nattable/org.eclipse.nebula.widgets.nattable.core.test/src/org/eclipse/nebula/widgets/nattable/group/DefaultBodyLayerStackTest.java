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
package org.eclipse.nebula.widgets.nattable.group;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class DefaultBodyLayerStackTest {

	private DefaultBodyLayerStack layerStack;

	@Before
	public void setup() {
		layerStack = new DefaultBodyLayerStack(new DataLayerFixture(10, 5, 100, 20));
		layerStack.setClientAreaProvider(new IClientAreaProvider() {
			public Rectangle getClientArea() {
				return new Rectangle(0, 0, 2000, 250);
			}
		});
		layerStack.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));
	}

	/*
	 * Data Layer:       0   1   2   3   4   5   6   7   8   9
	 *             -----------------------------------------------------
	 */
	@Test
	public void hideColumnsAndReorder() throws Exception {
		//Hide 3, 4
		layerStack.doCommand(new ColumnHideCommand(layerStack, 3));
		layerStack.doCommand(new ColumnHideCommand(layerStack, 3));

		assertEquals(0, layerStack.getColumnIndexByPosition(0));
		assertEquals(1, layerStack.getColumnIndexByPosition(1));
		assertEquals(2, layerStack.getColumnIndexByPosition(2));
		assertEquals(5, layerStack.getColumnIndexByPosition(3));
		assertEquals(6, layerStack.getColumnIndexByPosition(4));
		assertEquals(7, layerStack.getColumnIndexByPosition(5));
		assertEquals(8, layerStack.getColumnIndexByPosition(6));
		assertEquals(9, layerStack.getColumnIndexByPosition(7));
		assertEquals(-1, layerStack.getColumnIndexByPosition(8));

		//Reorder 0 -> 4
		layerStack.doCommand(new ColumnReorderCommand(layerStack, 0, 4));

		assertEquals(1, layerStack.getColumnIndexByPosition(0));
		assertEquals(2, layerStack.getColumnIndexByPosition(1));
		assertEquals(5, layerStack.getColumnIndexByPosition(2));
		assertEquals(0, layerStack.getColumnIndexByPosition(3));
		assertEquals(6, layerStack.getColumnIndexByPosition(4));
		assertEquals(7, layerStack.getColumnIndexByPosition(5));
		assertEquals(8, layerStack.getColumnIndexByPosition(6));
		assertEquals(9, layerStack.getColumnIndexByPosition(7));
		assertEquals(-1, layerStack.getColumnIndexByPosition(8));
	}

	@Test
	public void resizeAColumnAndHideIt() throws Exception {
		assertEquals(10, layerStack.getColumnCount());
		assertEquals(1000, layerStack.getWidth());

		// Resize 2
		layerStack.doCommand(new ColumnResizeCommand(layerStack, 2, 500));
		assertEquals(1400, layerStack.getWidth());

		assertEquals(1, layerStack.getColumnIndexByPosition(1));
		assertEquals(100, layerStack.getColumnWidthByPosition(1));
		assertEquals(100, layerStack.getStartXOfColumnPosition(1));

		assertEquals(2, layerStack.getColumnIndexByPosition(2));
		assertEquals(500, layerStack.getColumnWidthByPosition(2));
		assertEquals(200, layerStack.getStartXOfColumnPosition(2));

		assertEquals(3, layerStack.getColumnIndexByPosition(3));
		assertEquals(100, layerStack.getColumnWidthByPosition(3));
		assertEquals(700, layerStack.getStartXOfColumnPosition(3));

		// Hide 2
		layerStack.doCommand(new ColumnHideCommand(layerStack, 2));
		assertEquals(9, layerStack.getColumnCount());

		assertEquals(1, layerStack.getColumnIndexByPosition(1));
		assertEquals(100, layerStack.getColumnWidthByPosition(1));
		assertEquals(100, layerStack.getStartXOfColumnPosition(1));

		assertEquals(3, layerStack.getColumnIndexByPosition(2));
		assertEquals(100, layerStack.getColumnWidthByPosition(2));
		assertEquals(200, layerStack.getStartXOfColumnPosition(2));

		assertEquals(4, layerStack.getColumnIndexByPosition(3));
		assertEquals(100, layerStack.getColumnWidthByPosition(3));
		assertEquals(300, layerStack.getStartXOfColumnPosition(3));

		assertEquals(9, layerStack.getColumnIndexByPosition(8));
		assertEquals(100, layerStack.getColumnWidthByPosition(8));
		assertEquals(800, layerStack.getStartXOfColumnPosition(8));
	}

	@Test
	public void resizeAColumnAndReorderIt() throws Exception {
		assertEquals(10, layerStack.getColumnCount());
		assertEquals(1000, layerStack.getWidth());

		// Resize 2
		layerStack.doCommand(new ColumnResizeCommand(layerStack, 2, 500));
		assertEquals(1400, layerStack.getWidth());

		// Reorder 2 -> 4
		layerStack.doCommand(new ColumnReorderCommand(layerStack, 2, 4));

		assertEquals(0, layerStack.getColumnIndexByPosition(0));
		assertEquals(1, layerStack.getColumnIndexByPosition(1));
		assertEquals(3, layerStack.getColumnIndexByPosition(2));
		assertEquals(2, layerStack.getColumnIndexByPosition(3));
		assertEquals(4, layerStack.getColumnIndexByPosition(4));
		assertEquals(5, layerStack.getColumnIndexByPosition(5));
		assertEquals(6, layerStack.getColumnIndexByPosition(6));
		assertEquals(7, layerStack.getColumnIndexByPosition(7));
		assertEquals(8, layerStack.getColumnIndexByPosition(8));
	}
}
