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
package org.eclipse.nebula.widgets.nattable.selection;

import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SelectAllTest {	
	private SelectionLayer selectionLayer;
	
	@Before
	public void setUp() {
		selectionLayer = new SelectionLayer(new DataLayerFixture());
		// Selection all cells in grid
		selectionLayer.selectAll();
	}
	
	@After
	public void cleanUp() {
		selectionLayer.clear();
	}
	
	@Test
	public void shouldHaveAllCellsSelected() {
		for (int columnPosition = 0; columnPosition < selectionLayer.getColumnCount(); columnPosition++) {
			for (int rowPosition = 0; rowPosition < selectionLayer.getRowCount(); rowPosition++) {
				ILayerCell cell = selectionLayer.getCellByPosition(columnPosition, rowPosition);
				Assert.assertEquals(DisplayMode.SELECT, cell.getDisplayMode());
			}
		}	
	}
}
