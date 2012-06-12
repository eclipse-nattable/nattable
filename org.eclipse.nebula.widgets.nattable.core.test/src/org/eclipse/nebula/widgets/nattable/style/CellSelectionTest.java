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
package org.eclipse.nebula.widgets.nattable.style;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.InitializeClientAreaCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CellSelectionTest {
	private GridLayerFixture gridLayer;
	
	@Before
	public void setUp() {
		gridLayer = new GridLayerFixture();
		gridLayer.doCommand(new InitializeClientAreaCommandFixture());
	}
	
	@Test
	public void willSelectBodyCellAndShouldHaveColumnHeaderSelected() {
		// Select body cell
		// The cell position is a grid layer position
		gridLayer.doCommand(new SelectCellCommand(gridLayer, 2, 2, false, false));
		
		// Get body layer cell corresponding to the selected body cell
		ILayer bodyLayer = gridLayer.getBodyLayer();
		// The column position is 1 because it takes into account the offset of the row header
		ILayerCell cell = bodyLayer.getCellByPosition(1, 1);
		
		// Assert the cell is in selected state
		Assert.assertEquals(DisplayMode.SELECT, cell.getDisplayMode());
	}
}
