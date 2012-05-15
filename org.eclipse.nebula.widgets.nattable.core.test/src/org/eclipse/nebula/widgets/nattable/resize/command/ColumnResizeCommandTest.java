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
package org.eclipse.nebula.widgets.nattable.resize.command;


import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiColumnResizeCommand;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ColumnResizeCommandTest {

	private DataLayer dataLayer;
	
	@Before
	public void setup() {
		dataLayer = new DataLayer(new DummyBodyDataProvider(10, 10));
	}
	
	@Test
	public void testHandleColumnResizeCommand() {
		Assert.assertEquals(100, dataLayer.getColumnWidthByPosition(3));
		
		int columnPosition = 3;
		int newWidth = 150;
		ColumnResizeCommand columnResizeCommand = new ColumnResizeCommand(dataLayer, columnPosition, newWidth);
		
		dataLayer.doCommand(columnResizeCommand);
		
		Assert.assertEquals(150, dataLayer.getColumnWidthByPosition(3));
	}
	
	@Test
	public void shouldResizeAllSelectedColumns() {		
		int columnPositions[] = new int[]{3, 2, 4};
		int newWidth = 250;
		MultiColumnResizeCommand columnResizeCommand = new MultiColumnResizeCommand(dataLayer, columnPositions, newWidth);
		
		dataLayer.doCommand(columnResizeCommand);
		
		for (int columnPosition : columnPositions) {
			Assert.assertEquals(newWidth, dataLayer.getColumnWidthByPosition(columnPosition));
		}
	}
}
