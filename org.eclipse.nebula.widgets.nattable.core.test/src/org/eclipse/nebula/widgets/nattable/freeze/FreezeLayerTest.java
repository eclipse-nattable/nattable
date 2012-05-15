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
package org.eclipse.nebula.widgets.nattable.freeze;


import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommand;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseDataLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FreezeLayerTest {

	private FreezeLayer freezeLayer;
	private ColumnHideShowLayer hideShowLayer;
	private ColumnReorderLayer reorderLayer;
	
	@Before
	public void setup() {
		reorderLayer = new ColumnReorderLayer(new BaseDataLayerFixture(10,10));
		hideShowLayer = new ColumnHideShowLayer(reorderLayer);
		freezeLayer = new FreezeLayer(hideShowLayer);
		freezeLayer.setTopLeftPosition(1, 0);
		freezeLayer.setBottomRightPosition(3, 3);
	}
	
	@Test
	public void testSetupColumns() {
		Assert.assertEquals(3, freezeLayer.getColumnCount());
		Assert.assertEquals(1, freezeLayer.getColumnIndexByPosition(0));
		Assert.assertEquals(2, freezeLayer.getColumnIndexByPosition(1));
		Assert.assertEquals(3, freezeLayer.getColumnIndexByPosition(2));
	}
	
	@Test
	public void testSetupRows() {
		Assert.assertEquals(4, freezeLayer.getRowCount());
		Assert.assertEquals(0, freezeLayer.getRowIndexByPosition(0));
		Assert.assertEquals(1, freezeLayer.getRowIndexByPosition(1));
		Assert.assertEquals(2, freezeLayer.getRowIndexByPosition(2));
		Assert.assertEquals(3, freezeLayer.getRowIndexByPosition(3));
	}
	
	@Test
	public void testReorderInInteriorColumn() {
		hideShowLayer.doCommand(new ColumnReorderCommand(hideShowLayer, 5, 2));
		
		Assert.assertEquals(4, freezeLayer.getColumnCount());
		Assert.assertEquals(1, freezeLayer.getColumnIndexByPosition(0));
		Assert.assertEquals(5, freezeLayer.getColumnIndexByPosition(1));
		Assert.assertEquals(2, freezeLayer.getColumnIndexByPosition(2));
		Assert.assertEquals(3, freezeLayer.getColumnIndexByPosition(3));
	}
	
	@Test
	public void testReorderingIntoTopLeftCoordinate() {
		hideShowLayer.doCommand(new ColumnReorderCommand(hideShowLayer, 5, 1));
		
		Assert.assertEquals(4, freezeLayer.getColumnCount());
		Assert.assertEquals(5, freezeLayer.getColumnIndexByPosition(0));
		Assert.assertEquals(1, freezeLayer.getColumnIndexByPosition(1));
		Assert.assertEquals(2, freezeLayer.getColumnIndexByPosition(2));
		Assert.assertEquals(3, freezeLayer.getColumnIndexByPosition(3));
	}	
	
	@Test
	public void testReorderOutInteriorColumn() {
		hideShowLayer.doCommand(new ColumnReorderCommand(hideShowLayer, 2, 5));
		
		Assert.assertEquals(2, freezeLayer.getColumnCount());
		Assert.assertEquals(1, freezeLayer.getColumnIndexByPosition(0));
		Assert.assertEquals(3, freezeLayer.getColumnIndexByPosition(1));
	}
	
	@Test
	public void testReorderingRightBottomCornerOutOfFrozenArea() {
		hideShowLayer.doCommand(new ColumnReorderCommand(hideShowLayer, 3, 5));	
		
		Assert.assertEquals(2, freezeLayer.getColumnCount());
		Assert.assertEquals(2, freezeLayer.getBottomRightPosition().columnPosition);
		Assert.assertEquals(1, freezeLayer.getColumnIndexByPosition(0));
		Assert.assertEquals(2, freezeLayer.getColumnIndexByPosition(1));
	}
	
	@Test
	public void testHideShowInteriorColumn() {
		hideShowLayer.doCommand(new ColumnHideCommand(hideShowLayer, 2));
		
		Assert.assertEquals(2, freezeLayer.getColumnCount());
		Assert.assertEquals(1, freezeLayer.getColumnIndexByPosition(0));
		Assert.assertEquals(3, freezeLayer.getColumnIndexByPosition(1));
		
		hideShowLayer.doCommand(new ShowAllColumnsCommand());

		Assert.assertEquals(3, freezeLayer.getColumnCount());
		Assert.assertEquals(1, freezeLayer.getColumnIndexByPosition(0));
		Assert.assertEquals(2, freezeLayer.getColumnIndexByPosition(1));
		Assert.assertEquals(3, freezeLayer.getColumnIndexByPosition(2));
	}
	
	@Test
	public void testMovingAroundColumns() {				
		//---------------------- Move into middle of frozen area
		// Frozen Columns:	1	5	2	3
		// 	  Frozen Rows:	0	3	3	3
		hideShowLayer.doCommand(new ColumnReorderCommand(hideShowLayer, 5, 2));		
		
		// Test positions
		Assert.assertEquals(1, freezeLayer.getTopLeftPosition().columnPosition);
		Assert.assertEquals(0, freezeLayer.getTopLeftPosition().rowPosition);
		Assert.assertEquals(4, freezeLayer.getBottomRightPosition().columnPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().rowPosition);
		
		// Test indexes
		Assert.assertEquals(4, freezeLayer.getColumnCount());
		Assert.assertEquals(1, freezeLayer.getTopLeftPosition().columnPosition);
		Assert.assertEquals(0, freezeLayer.getTopLeftPosition().rowPosition);
		Assert.assertEquals(4, freezeLayer.getBottomRightPosition().columnPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().rowPosition);
		
		Assert.assertEquals(1, freezeLayer.getColumnIndexByPosition(0));
		Assert.assertEquals(5, freezeLayer.getColumnIndexByPosition(1));
		Assert.assertEquals(2, freezeLayer.getColumnIndexByPosition(2));
		Assert.assertEquals(3, freezeLayer.getColumnIndexByPosition(3));

		//---------------------- Move right edge out of frozen area
		// Frozen Columns:	1	5	2
		// 	  Frozen Rows:	0	3	3
		hideShowLayer.doCommand(new ColumnReorderCommand(hideShowLayer, 4, 6));		
		
		// Test indexes
		Assert.assertEquals(3, freezeLayer.getColumnCount());
		Assert.assertEquals(1, freezeLayer.getTopLeftPosition().columnPosition);
		Assert.assertEquals(0, freezeLayer.getTopLeftPosition().rowPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().columnPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().rowPosition);
		
		// Test positions
		Assert.assertEquals(1, freezeLayer.getTopLeftPosition().columnPosition);
		Assert.assertEquals(0, freezeLayer.getTopLeftPosition().rowPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().columnPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().rowPosition);
		
		//---------------------- Swap right edge with preceeding column
		// Frozen Columns:	1	2	5
		// 	  Frozen Rows:	0	3	3
		hideShowLayer.doCommand(new ColumnReorderCommand(hideShowLayer, 3, 2));		
		
		// Test indexes
		Assert.assertEquals(3, freezeLayer.getColumnCount());
		Assert.assertEquals(1, freezeLayer.getTopLeftPosition().columnPosition);
		Assert.assertEquals(0, freezeLayer.getTopLeftPosition().rowPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().columnPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().rowPosition);
		Assert.assertEquals(1, freezeLayer.getColumnIndexByPosition(0));
		Assert.assertEquals(2, freezeLayer.getColumnIndexByPosition(1));
		Assert.assertEquals(5, freezeLayer.getColumnIndexByPosition(2));
		
		// Test positions
		Assert.assertEquals(1, freezeLayer.getTopLeftPosition().columnPosition);
		Assert.assertEquals(0, freezeLayer.getTopLeftPosition().rowPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().columnPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().rowPosition);
		
		//---------------------- Move new right edge out
		// Frozen Columns:	1	2
		// 	  Frozen Rows:	0	3
		hideShowLayer.doCommand(new ColumnReorderCommand(hideShowLayer, 3, 5));		
		
		// Test indexes
		Assert.assertEquals(2, freezeLayer.getColumnCount());
		Assert.assertEquals(1, freezeLayer.getTopLeftPosition().columnPosition);
		Assert.assertEquals(0, freezeLayer.getTopLeftPosition().rowPosition);
		Assert.assertEquals(2, freezeLayer.getBottomRightPosition().columnPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().rowPosition);
		Assert.assertEquals(1, freezeLayer.getColumnIndexByPosition(0));
		Assert.assertEquals(2, freezeLayer.getColumnIndexByPosition(1));
		
		// Test positions
		Assert.assertEquals(1, freezeLayer.getTopLeftPosition().columnPosition);
		Assert.assertEquals(0, freezeLayer.getTopLeftPosition().rowPosition);
		Assert.assertEquals(2, freezeLayer.getBottomRightPosition().columnPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().rowPosition);
		
		//---------------------- Move column into frozen area replacing top left index
		// Frozen Columns:	8	1	2
		// 	  Frozen Rows:	1	3	3
		hideShowLayer.doCommand(new ColumnReorderCommand(hideShowLayer, 8, 1));		
		
		// Test indexes
		Assert.assertEquals(3, freezeLayer.getColumnCount());
		Assert.assertEquals(1, freezeLayer.getTopLeftPosition().columnPosition);
		Assert.assertEquals(0, freezeLayer.getTopLeftPosition().rowPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().columnPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().rowPosition);
		Assert.assertEquals(8, freezeLayer.getColumnIndexByPosition(0));
		Assert.assertEquals(1, freezeLayer.getColumnIndexByPosition(1));
		Assert.assertEquals(2, freezeLayer.getColumnIndexByPosition(2));
		
		// Test positions
		Assert.assertEquals(1, freezeLayer.getTopLeftPosition().columnPosition);
		Assert.assertEquals(0, freezeLayer.getTopLeftPosition().rowPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().columnPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().rowPosition);
		
		
		//---------------------- Move right edge out
		// Frozen Columns:	8	1
		// 	  Frozen Rows:	1	3
		hideShowLayer.doCommand(new ColumnReorderCommand(hideShowLayer, 3, 5));		
		Assert.assertEquals(2, freezeLayer.getColumnCount());
		Assert.assertEquals(1, freezeLayer.getTopLeftPosition().columnPosition);
		Assert.assertEquals(0, freezeLayer.getTopLeftPosition().rowPosition);
		Assert.assertEquals(2, freezeLayer.getBottomRightPosition().columnPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().rowPosition);
		Assert.assertEquals(8, freezeLayer.getColumnIndexByPosition(0));
		Assert.assertEquals(1, freezeLayer.getColumnIndexByPosition(1));
		
		// Test positions
		Assert.assertEquals(1, freezeLayer.getTopLeftPosition().columnPosition);
		Assert.assertEquals(0, freezeLayer.getTopLeftPosition().rowPosition);
		Assert.assertEquals(2, freezeLayer.getBottomRightPosition().columnPosition);
		Assert.assertEquals(3, freezeLayer.getBottomRightPosition().rowPosition);
	}
	
}
