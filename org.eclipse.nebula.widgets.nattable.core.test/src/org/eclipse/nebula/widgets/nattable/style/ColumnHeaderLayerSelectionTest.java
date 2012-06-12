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

import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.InitializeClientAreaCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectColumnCommand;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ColumnHeaderLayerSelectionTest {
	
	private GridLayerFixture gridLayer;
	
	@Before
	public void setUp() {
		gridLayer = new GridLayerFixture();
		gridLayer.doCommand(new InitializeClientAreaCommandFixture());
	}
	
	@Test
	public void willSelectBodyCellAndShouldHaveColumnHeaderSelected() {
		// Select body cell
		// The column position is a grid layer position
		gridLayer.doCommand(new SelectCellCommand(gridLayer, 2, 2, false, false));
		
		// Get column header cell corresponding to the selected body cell
		ColumnHeaderLayer columnHeaderLayer = (ColumnHeaderLayer)gridLayer.getChildLayerByLayoutCoordinate(1, 0);
		// The column position is 1 because it takes into account the offset of the row header
		ILayerCell cell = columnHeaderLayer.getCellByPosition(1, 0);
		
		// Assert the cell is in selected state
		Assert.assertEquals(DisplayMode.SELECT, cell.getDisplayMode());
	}
	
	@Test
	public void shouldReturnFullySelectedStyle() {
		// Select full column
		gridLayer.doCommand(new ViewportSelectColumnCommand(gridLayer, 2, false, false));
		
		ColumnHeaderLayer columnHeaderLayer = (ColumnHeaderLayer) gridLayer.getChildLayerByLayoutCoordinate(1, 0);
		
		// Since I selected using grid coordinates, the column position should be 1 rather than 2
		int columnPosition = gridLayer.localToUnderlyingColumnPosition(2);
		final LabelStack labelStack = columnHeaderLayer.getConfigLabelsByPosition(columnPosition, 0);
		
		Assert.assertTrue(labelStack.hasLabel(SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE));
		
		columnPosition = gridLayer.localToUnderlyingColumnPosition(3);
		Assert.assertFalse(SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE.equals(labelStack));
	}
}
