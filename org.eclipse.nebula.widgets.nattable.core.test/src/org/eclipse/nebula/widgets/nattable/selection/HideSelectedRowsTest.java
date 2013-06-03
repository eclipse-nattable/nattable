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


import java.util.Arrays;

import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiRowHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.RowHideCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HideSelectedRowsTest {
	private SelectionLayer selectionLayer;
	private RowHideShowLayer rowHideShowLayer;
	
	@Before
	public void setUp() {
		rowHideShowLayer = new RowHideShowLayer(new DataLayerFixture());
		selectionLayer = new SelectionLayer(rowHideShowLayer);
	}
	
	@Test
	public void shouldAlsoHideRowWhichIsNotSelectedButHasAMouseOverIt() {
		selectionLayer.doCommand(new MultiRowHideCommand(selectionLayer, 2));
		Assert.assertTrue(rowHideShowLayer.isRowIndexHidden(2));
	}
	
	@Test
	public void shouldHideRowForSelectedCell() {
		// Select cell in row we want to hide
		selectionLayer.setSelectedCell(3, 0);

		// Hide selection
		selectionLayer.doCommand(new RowHideCommand(selectionLayer, 3));
		
		// The previously selected row should be hidden
		Assert.assertTrue(rowHideShowLayer.isRowIndexHidden(3));
		Assert.assertEquals(6, selectionLayer.getRowCount());
	}
	
	@Test
	public void shouldHideSelectedRow() {
		// Select row to hide
		new SelectRowCommandHandler(selectionLayer).selectRows(0, Arrays.asList(Integer.valueOf(2)), false, false, 2);
		
		// Hide row
		selectionLayer.doCommand(new MultiRowHideCommand(selectionLayer, 2));
		
		// The previously selected row should be hidden
		Assert.assertTrue(rowHideShowLayer.isRowIndexHidden(2));
		Assert.assertEquals(6, selectionLayer.getRowCount());
	}
	
	@Test
	public void shouldHideAllSelectedRows() {
		// Select cells and rows 
		new SelectRowCommandHandler(selectionLayer).selectRows(0, Arrays.asList(Integer.valueOf(2)), false, false, 2);
		selectionLayer.selectCell(0, 1, false, true);
		selectionLayer.selectCell(4, 4, false, true);
		
		// Hide selection
		selectionLayer.doCommand(new MultiRowHideCommand(selectionLayer, new int[]{2, 0, 4}));
		
		// Previously selected rows should be hidden
		Assert.assertTrue(rowHideShowLayer.isRowIndexHidden(2));
		Assert.assertTrue(rowHideShowLayer.isRowIndexHidden(0));
		Assert.assertTrue(rowHideShowLayer.isRowIndexHidden(4));
	}
}
