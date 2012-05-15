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
package org.eclipse.nebula.widgets.nattable.layer;


import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DataLayerTest {

	private DataLayer dataLayer;
	
	@Before
	public void setup() {
		dataLayer = new DataLayerFixture();
	}
	
	// Horizontal features
	
	// Columns
	
	@Test
	public void testColumnCount() {
		Assert.assertEquals(5, dataLayer.getColumnCount());
	}
	
	@Test
	public void testColumnIndexByPosition() {
		Assert.assertEquals(-1, dataLayer.getColumnIndexByPosition(-1));

		Assert.assertEquals(0, dataLayer.getColumnIndexByPosition(0));
		Assert.assertEquals(1, dataLayer.getColumnIndexByPosition(1));
		Assert.assertEquals(2, dataLayer.getColumnIndexByPosition(2));
		Assert.assertEquals(3, dataLayer.getColumnIndexByPosition(3));
		Assert.assertEquals(4, dataLayer.getColumnIndexByPosition(4));
		
		Assert.assertEquals(-1, dataLayer.getColumnIndexByPosition(5));
	}
	
	@Test
	public void testColumnPositionByIndex() {
		Assert.assertEquals(-1, dataLayer.getColumnPositionByIndex(-1));

		Assert.assertEquals(0, dataLayer.getColumnPositionByIndex(0));
		Assert.assertEquals(1, dataLayer.getColumnPositionByIndex(1));
		Assert.assertEquals(2, dataLayer.getColumnPositionByIndex(2));
		Assert.assertEquals(3, dataLayer.getColumnPositionByIndex(3));
		Assert.assertEquals(4, dataLayer.getColumnPositionByIndex(4));
		
		Assert.assertEquals(-1, dataLayer.getColumnPositionByIndex(5));
	}
	
	// Width
	
	@Test
	public void testWidth() {
		Assert.assertEquals(465, dataLayer.getWidth());
	}
	
	@Test
	public void testWidthAfterModify() {
		testWidth();
		
		dataLayer.setColumnWidthByPosition(0, 120);
		dataLayer.setColumnWidthByPosition(2, 40);
		
		Assert.assertEquals(440, dataLayer.getWidth());
	}
	
	@Test
	public void testColumnWidthByPosition() {
		Assert.assertEquals(150, dataLayer.getColumnWidthByPosition(0));
		Assert.assertEquals(100, dataLayer.getColumnWidthByPosition(1));
		Assert.assertEquals(35, dataLayer.getColumnWidthByPosition(2));
		Assert.assertEquals(100, dataLayer.getColumnWidthByPosition(3));
		Assert.assertEquals(80, dataLayer.getColumnWidthByPosition(4));
	}
	
	@Test
	public void testColumnWidthByPositionAfterModify() {
		testColumnWidthByPosition();
		
		dataLayer.setColumnWidthByPosition(0, 120);
		dataLayer.setColumnWidthByPosition(2, 40);
		
		Assert.assertEquals(120, dataLayer.getColumnWidthByPosition(0));
		Assert.assertEquals(100, dataLayer.getColumnWidthByPosition(1));
		Assert.assertEquals(40, dataLayer.getColumnWidthByPosition(2));
		Assert.assertEquals(100, dataLayer.getColumnWidthByPosition(3));
		Assert.assertEquals(80, dataLayer.getColumnWidthByPosition(4));
	}
	
	@Test
	public void testPreferredColumnWidth() {
		testColumnWidthByPosition();
		
		dataLayer.setDefaultColumnWidth(200);
		
		Assert.assertEquals(150, dataLayer.getColumnWidthByPosition(0));
		Assert.assertEquals(200, dataLayer.getColumnWidthByPosition(1));
		Assert.assertEquals(35, dataLayer.getColumnWidthByPosition(2));
		Assert.assertEquals(200, dataLayer.getColumnWidthByPosition(3));
		Assert.assertEquals(80, dataLayer.getColumnWidthByPosition(4));
		Assert.assertEquals(665, dataLayer.getWidth());
	}
	
	@Test
	public void testPreferredColumnWidthByPosition() {
		testColumnWidthByPosition();
		
		dataLayer.setDefaultColumnWidthByPosition(1, 75);
		dataLayer.setDefaultColumnWidthByPosition(3, 45);
		
		Assert.assertEquals(150, dataLayer.getColumnWidthByPosition(0));
		Assert.assertEquals(75, dataLayer.getColumnWidthByPosition(1));
		Assert.assertEquals(35, dataLayer.getColumnWidthByPosition(2));
		Assert.assertEquals(45, dataLayer.getColumnWidthByPosition(3));
		Assert.assertEquals(80, dataLayer.getColumnWidthByPosition(4));
		
		dataLayer.setColumnWidthByPosition(1, 30);
		
		Assert.assertEquals(150, dataLayer.getColumnWidthByPosition(0));
		Assert.assertEquals(30, dataLayer.getColumnWidthByPosition(1));
		Assert.assertEquals(35, dataLayer.getColumnWidthByPosition(2));
		Assert.assertEquals(45, dataLayer.getColumnWidthByPosition(3));
		Assert.assertEquals(80, dataLayer.getColumnWidthByPosition(4));
	}
	
	// Column resize
	
	@Test
	public void testColumnsResizableByDefault() {
		testColumnWidthByPosition();
		
		dataLayer.setColumnWidthByPosition(0, 35);
		Assert.assertEquals(35, dataLayer.getColumnWidthByPosition(0));
		
		dataLayer.setColumnsResizableByDefault(false);
		dataLayer.setColumnWidthByPosition(0, 85);
		Assert.assertEquals(35, dataLayer.getColumnWidthByPosition(0));
		
		dataLayer.setColumnsResizableByDefault(true);
		dataLayer.setColumnWidthByPosition(0, 65);
		Assert.assertEquals(65, dataLayer.getColumnWidthByPosition(0));
	}
	
	@Test
	public void testColumnPositionResizable() {
		testColumnWidthByPosition();
		
		Assert.assertTrue(dataLayer.isColumnPositionResizable(0));
		dataLayer.setColumnWidthByPosition(0, 35);
		Assert.assertEquals(35, dataLayer.getColumnWidthByPosition(0));
		
		dataLayer.setColumnPositionResizable(0, false);
		Assert.assertFalse(dataLayer.isColumnPositionResizable(0));
		dataLayer.setColumnWidthByPosition(0, 85);
		Assert.assertEquals(35, dataLayer.getColumnWidthByPosition(0));
		
		dataLayer.setColumnsResizableByDefault(false);
		dataLayer.setColumnPositionResizable(0, true);
		Assert.assertTrue(dataLayer.isColumnPositionResizable(0));
		dataLayer.setColumnWidthByPosition(0, 65);
		Assert.assertEquals(65, dataLayer.getColumnWidthByPosition(0));
	}
	
	// X
	
	@Test
	public void testColumnPositionByX() {
		Assert.assertEquals(-1, dataLayer.getColumnPositionByX(-1));
		
		Assert.assertEquals(0, dataLayer.getColumnPositionByX(0));
		Assert.assertEquals(0, dataLayer.getColumnPositionByX(149));
		Assert.assertEquals(1, dataLayer.getColumnPositionByX(150));
		Assert.assertEquals(1, dataLayer.getColumnPositionByX(170));
		Assert.assertEquals(2, dataLayer.getColumnPositionByX(250));
		Assert.assertEquals(2, dataLayer.getColumnPositionByX(284));
		Assert.assertEquals(3, dataLayer.getColumnPositionByX(285));
		Assert.assertEquals(3, dataLayer.getColumnPositionByX(384));
		Assert.assertEquals(4, dataLayer.getColumnPositionByX(385));
		Assert.assertEquals(4, dataLayer.getColumnPositionByX(464));
	}
	
	@Test
	public void testColumnPositionByXAfterModify() {
		testColumnPositionByX();
		
		dataLayer.setColumnWidthByPosition(1, 50);
		
		Assert.assertEquals(-1, dataLayer.getColumnPositionByX(-1));
		
		Assert.assertEquals(0, dataLayer.getColumnPositionByX(0));
		Assert.assertEquals(0, dataLayer.getColumnPositionByX(149));
		Assert.assertEquals(1, dataLayer.getColumnPositionByX(150));
		Assert.assertEquals(1, dataLayer.getColumnPositionByX(170));
		Assert.assertEquals(2, dataLayer.getColumnPositionByX(200));
		Assert.assertEquals(2, dataLayer.getColumnPositionByX(234));
		Assert.assertEquals(3, dataLayer.getColumnPositionByX(235));
		Assert.assertEquals(3, dataLayer.getColumnPositionByX(334));
		Assert.assertEquals(4, dataLayer.getColumnPositionByX(335));
		Assert.assertEquals(4, dataLayer.getColumnPositionByX(414));
	}
	
	@Test
	public void testStartXOfColumnPosition() {
		Assert.assertEquals(0, dataLayer.getStartXOfColumnPosition(0));
		Assert.assertEquals(150, dataLayer.getStartXOfColumnPosition(1));
		Assert.assertEquals(250, dataLayer.getStartXOfColumnPosition(2));
		Assert.assertEquals(285, dataLayer.getStartXOfColumnPosition(3));
		Assert.assertEquals(385, dataLayer.getStartXOfColumnPosition(4));
	}
	
	@Test
	public void testStartXOfColumnPositionAfterModify() {
		testStartXOfColumnPosition();
		
		dataLayer.setColumnWidthByPosition(1, 50);
		
		Assert.assertEquals(0, dataLayer.getStartXOfColumnPosition(0));
		Assert.assertEquals(150, dataLayer.getStartXOfColumnPosition(1));
		Assert.assertEquals(200, dataLayer.getStartXOfColumnPosition(2));
		Assert.assertEquals(235, dataLayer.getStartXOfColumnPosition(3));
		Assert.assertEquals(335, dataLayer.getStartXOfColumnPosition(4));
	}
	
	// Vertical features
	
	// Rows
	
	@Test
	public void testRowCount() {
		Assert.assertEquals(7, dataLayer.getRowCount());
	}
	
	@Test
	public void testRowIndexByPosition() {
		Assert.assertEquals(-1, dataLayer.getRowIndexByPosition(-1));

		Assert.assertEquals(0, dataLayer.getRowIndexByPosition(0));
		Assert.assertEquals(1, dataLayer.getRowIndexByPosition(1));
		Assert.assertEquals(2, dataLayer.getRowIndexByPosition(2));
		Assert.assertEquals(3, dataLayer.getRowIndexByPosition(3));
		Assert.assertEquals(4, dataLayer.getRowIndexByPosition(4));
		Assert.assertEquals(5, dataLayer.getRowIndexByPosition(5));
		Assert.assertEquals(6, dataLayer.getRowIndexByPosition(6));
		
		Assert.assertEquals(-1, dataLayer.getRowIndexByPosition(7));
	}
	
	@Test
	public void testRowPositionByIndex() {
		Assert.assertEquals(-1, dataLayer.getRowPositionByIndex(-1));

		Assert.assertEquals(0, dataLayer.getRowPositionByIndex(0));
		Assert.assertEquals(1, dataLayer.getRowPositionByIndex(1));
		Assert.assertEquals(2, dataLayer.getRowPositionByIndex(2));
		Assert.assertEquals(3, dataLayer.getRowPositionByIndex(3));
		Assert.assertEquals(4, dataLayer.getRowPositionByIndex(4));
		Assert.assertEquals(5, dataLayer.getRowPositionByIndex(5));
		Assert.assertEquals(6, dataLayer.getRowPositionByIndex(6));
		
		Assert.assertEquals(-1, dataLayer.getRowPositionByIndex(7));
	}
	
	// Height
	
	@Test
	public void testHeight() {
		Assert.assertEquals(365, dataLayer.getHeight());
	}
	
	@Test
	public void testHeightAfterModify() {
		testHeight();
		
		dataLayer.setRowHeightByPosition(0, 20);
		dataLayer.setRowHeightByPosition(2, 30);
		
		Assert.assertEquals(350, dataLayer.getHeight());
	}
	
	@Test
	public void testRowHeightByPosition() {
		Assert.assertEquals(40, dataLayer.getRowHeightByPosition(0));
		Assert.assertEquals(70, dataLayer.getRowHeightByPosition(1));
		Assert.assertEquals(25, dataLayer.getRowHeightByPosition(2));
		Assert.assertEquals(40, dataLayer.getRowHeightByPosition(3));
		Assert.assertEquals(50, dataLayer.getRowHeightByPosition(4));
		Assert.assertEquals(40, dataLayer.getRowHeightByPosition(5));
		Assert.assertEquals(100, dataLayer.getRowHeightByPosition(6));
		
		Assert.assertEquals(365, dataLayer.getHeight());
	}
	
	@Test
	public void testRowHeightByPositionAfterModify() {
		testRowHeightByPosition();
		
		dataLayer.setRowHeightByPosition(0, 20);
		dataLayer.setRowHeightByPosition(3, 30);
		
		Assert.assertEquals(20, dataLayer.getRowHeightByPosition(0));
		Assert.assertEquals(70, dataLayer.getRowHeightByPosition(1));
		Assert.assertEquals(25, dataLayer.getRowHeightByPosition(2));
		Assert.assertEquals(30, dataLayer.getRowHeightByPosition(3));
		Assert.assertEquals(50, dataLayer.getRowHeightByPosition(4));
		Assert.assertEquals(40, dataLayer.getRowHeightByPosition(5));
		Assert.assertEquals(100, dataLayer.getRowHeightByPosition(6));
		
		Assert.assertEquals(335, dataLayer.getHeight());
	}
	
	@Test
	public void testPreferredRowHeight() {
		testRowHeightByPosition();
		
		dataLayer.setDefaultRowHeight(50);
		
		Assert.assertEquals(50, dataLayer.getRowHeightByPosition(0));
		Assert.assertEquals(70, dataLayer.getRowHeightByPosition(1));
		Assert.assertEquals(25, dataLayer.getRowHeightByPosition(2));
		Assert.assertEquals(50, dataLayer.getRowHeightByPosition(3));
		Assert.assertEquals(50, dataLayer.getRowHeightByPosition(4));
		Assert.assertEquals(50, dataLayer.getRowHeightByPosition(5));
		Assert.assertEquals(100, dataLayer.getRowHeightByPosition(6));
		
		Assert.assertEquals(395, dataLayer.getHeight());
	}
	
	@Test
	public void testPreferredRowHeightByPosition() {
		testRowHeightByPosition();
		
		dataLayer.setDefaultRowHeightByPosition(1, 75);
		dataLayer.setDefaultRowHeightByPosition(3, 45);
		
		Assert.assertEquals(40, dataLayer.getRowHeightByPosition(0));
		Assert.assertEquals(70, dataLayer.getRowHeightByPosition(1));
		Assert.assertEquals(25, dataLayer.getRowHeightByPosition(2));
		Assert.assertEquals(45, dataLayer.getRowHeightByPosition(3));
		Assert.assertEquals(50, dataLayer.getRowHeightByPosition(4));
		Assert.assertEquals(40, dataLayer.getRowHeightByPosition(5));
		Assert.assertEquals(100, dataLayer.getRowHeightByPosition(6));
		
		dataLayer.setRowHeightByPosition(1, 30);
		
		Assert.assertEquals(40, dataLayer.getRowHeightByPosition(0));
		Assert.assertEquals(30, dataLayer.getRowHeightByPosition(1));
		Assert.assertEquals(25, dataLayer.getRowHeightByPosition(2));
		Assert.assertEquals(45, dataLayer.getRowHeightByPosition(3));
		Assert.assertEquals(50, dataLayer.getRowHeightByPosition(4));
		Assert.assertEquals(40, dataLayer.getRowHeightByPosition(5));
		Assert.assertEquals(100, dataLayer.getRowHeightByPosition(6));
	}
	
	// Row resize
	
	@Test
	public void testRowsResizableByDefault() {
		testRowHeightByPosition();
		
		dataLayer.setRowHeightByPosition(0, 35);
		Assert.assertEquals(35, dataLayer.getRowHeightByPosition(0));
		
		dataLayer.setRowsResizableByDefault(false);
		dataLayer.setRowHeightByPosition(0, 85);
		Assert.assertEquals(35, dataLayer.getRowHeightByPosition(0));
		
		dataLayer.setRowsResizableByDefault(true);
		dataLayer.setRowHeightByPosition(0, 65);
		Assert.assertEquals(65, dataLayer.getRowHeightByPosition(0));
	}
	
	@Test
	public void testRowPositionResizable() {
		testRowHeightByPosition();
		
		Assert.assertTrue(dataLayer.isRowPositionResizable(0));
		dataLayer.setRowHeightByPosition(0, 35);
		Assert.assertEquals(35, dataLayer.getRowHeightByPosition(0));
		
		dataLayer.setRowPositionResizable(0, false);
		Assert.assertFalse(dataLayer.isRowPositionResizable(0));
		dataLayer.setRowHeightByPosition(0, 85);
		Assert.assertEquals(35, dataLayer.getRowHeightByPosition(0));
		
		dataLayer.setRowsResizableByDefault(false);
		dataLayer.setRowPositionResizable(0, true);
		Assert.assertTrue(dataLayer.isRowPositionResizable(0));
		dataLayer.setRowHeightByPosition(0, 65);
		Assert.assertEquals(65, dataLayer.getRowHeightByPosition(0));
	}
	
	// Y
	
	@Test
	public void testRowPositionByY() {
		Assert.assertEquals(-1, dataLayer.getRowPositionByY(-1));
		
		Assert.assertEquals(0, dataLayer.getRowPositionByY(0));
		Assert.assertEquals(0, dataLayer.getRowPositionByY(39));
		Assert.assertEquals(1, dataLayer.getRowPositionByY(40));
		Assert.assertEquals(1, dataLayer.getRowPositionByY(109));
		Assert.assertEquals(2, dataLayer.getRowPositionByY(110));
		Assert.assertEquals(2, dataLayer.getRowPositionByY(134));
		Assert.assertEquals(3, dataLayer.getRowPositionByY(135));
		Assert.assertEquals(3, dataLayer.getRowPositionByY(174));
		Assert.assertEquals(4, dataLayer.getRowPositionByY(175));
		Assert.assertEquals(4, dataLayer.getRowPositionByY(224));
		Assert.assertEquals(5, dataLayer.getRowPositionByY(225));
		Assert.assertEquals(5, dataLayer.getRowPositionByY(264));
		Assert.assertEquals(6, dataLayer.getRowPositionByY(265));
		Assert.assertEquals(6, dataLayer.getRowPositionByY(364));
	}
	
	@Test
	public void testRowPositionByYAfterModify() {
		testRowPositionByY();
		
		dataLayer.setRowHeightByPosition(2, 100);
		
		Assert.assertEquals(-1, dataLayer.getRowPositionByY(-1));
		
		Assert.assertEquals(0, dataLayer.getRowPositionByY(0));
		Assert.assertEquals(0, dataLayer.getRowPositionByY(39));
		Assert.assertEquals(1, dataLayer.getRowPositionByY(40));
		Assert.assertEquals(1, dataLayer.getRowPositionByY(109));
		Assert.assertEquals(2, dataLayer.getRowPositionByY(110));
		Assert.assertEquals(2, dataLayer.getRowPositionByY(134));
		Assert.assertEquals(2, dataLayer.getRowPositionByY(135));
		Assert.assertEquals(2, dataLayer.getRowPositionByY(209));
		Assert.assertEquals(3, dataLayer.getRowPositionByY(210));
		Assert.assertEquals(3, dataLayer.getRowPositionByY(249));
		Assert.assertEquals(4, dataLayer.getRowPositionByY(250));
		Assert.assertEquals(4, dataLayer.getRowPositionByY(299));
		Assert.assertEquals(5, dataLayer.getRowPositionByY(300));
		Assert.assertEquals(5, dataLayer.getRowPositionByY(339));
		Assert.assertEquals(6, dataLayer.getRowPositionByY(340));
		Assert.assertEquals(6, dataLayer.getRowPositionByY(439));
	}
	
	@Test
	public void testStartYOfRowPosition() {
		Assert.assertEquals(0, dataLayer.getStartYOfRowPosition(0));
		Assert.assertEquals(40, dataLayer.getStartYOfRowPosition(1));
		Assert.assertEquals(110, dataLayer.getStartYOfRowPosition(2));
		Assert.assertEquals(135, dataLayer.getStartYOfRowPosition(3));
		Assert.assertEquals(175, dataLayer.getStartYOfRowPosition(4));
		Assert.assertEquals(225, dataLayer.getStartYOfRowPosition(5));
		Assert.assertEquals(265, dataLayer.getStartYOfRowPosition(6));
	}
	
	@Test
	public void testStartYOfRowPositionAfterModify() {
		testStartYOfRowPosition();
		
		dataLayer.setRowHeightByPosition(2, 100);
		
		Assert.assertEquals(0, dataLayer.getStartYOfRowPosition(0));
		Assert.assertEquals(40, dataLayer.getStartYOfRowPosition(1));
		Assert.assertEquals(110, dataLayer.getStartYOfRowPosition(2));
		Assert.assertEquals(210, dataLayer.getStartYOfRowPosition(3));
		Assert.assertEquals(250, dataLayer.getStartYOfRowPosition(4));
		Assert.assertEquals(300, dataLayer.getStartYOfRowPosition(5));
		Assert.assertEquals(340, dataLayer.getStartYOfRowPosition(6));
	}
	
}
