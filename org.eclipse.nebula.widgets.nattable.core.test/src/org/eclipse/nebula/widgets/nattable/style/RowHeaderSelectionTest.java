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

import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.InitializeClientAreaCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectRowCommand;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RowHeaderSelectionTest {
	
	private GridLayerFixture gridLayer;
	
	@Before
	public void setUp() {
		gridLayer = new GridLayerFixture();
		gridLayer.doCommand(new InitializeClientAreaCommandFixture());
	}
	
	@Test
	public void willSelectBodyCellAndShouldHaveColumnHeaderSelected() {
		// Select body cell
		// The row position is a grid layer position
		gridLayer.doCommand(new SelectCellCommand(gridLayer, 2, 2, false, false));
		
		// Get row header cell corresponding to the selected body cell
		RowHeaderLayer rowHeaderLayer = (RowHeaderLayer)gridLayer.getChildLayerByLayoutCoordinate(0, 1);
		// The column position is 0 because it takes into account the offset of the row header
		ILayerCell cell = rowHeaderLayer.getCellByPosition(0, 1);
		
		// Assert the cell is in selected state
		Assert.assertEquals(DisplayMode.SELECT, cell.getDisplayMode());
	}
	

	@Test
	public void shouldReturnFullySelectedStyle() {
		// Select full column
		gridLayer.doCommand(new ViewportSelectRowCommand(gridLayer, 1, false, false));
		
		RowHeaderLayer rowHeaderLayer = (RowHeaderLayer)gridLayer.getChildLayerByLayoutCoordinate(0, 1);
		
		// Since I selected using grid coordinates, the column position should be 1 rather than 2
		int rowPosition = gridLayer.localToUnderlyingRowPosition(1);
		final LabelStack labelStack = rowHeaderLayer.getConfigLabelsByPosition(rowPosition, 0);
		Assert.assertTrue(labelStack.hasLabel(SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE));
		
		rowPosition = gridLayer.localToUnderlyingRowPosition(4);
		
		Assert.assertFalse("Should not have returned fully selected style.", SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE.equals(rowHeaderLayer.getConfigLabelsByPosition(0, rowPosition)));
	}
}
