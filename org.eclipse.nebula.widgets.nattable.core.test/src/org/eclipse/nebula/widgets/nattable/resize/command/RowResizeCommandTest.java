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
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RowResizeCommandTest {

	private DataLayer dataLayer;
	
	@Before
	public void setup() {
		dataLayer = new DataLayer(new DummyBodyDataProvider(10, 10));
	}
	
	@Test
	public void testHandleRowResizeCommand() {
		Assert.assertEquals(20, dataLayer.getRowHeightByPosition(3));
		
		int rowPosition = 3;
		int newHeight = 50;
		RowResizeCommand rowResizeCommand = new RowResizeCommand(dataLayer, rowPosition, newHeight);
		
		dataLayer.doCommand(rowResizeCommand);
		
		Assert.assertEquals(50, dataLayer.getRowHeightByPosition(3));
	}
	
}
