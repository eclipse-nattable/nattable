/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Jonas Hugo <Jonas.Hugo@jeppesen.com>,
 *       Markus Wahl <Markus.Wahl@jeppesen.com> - Test delegation of markers to
 *         model iff model is an IMarkerSelectionModel. Test getters and setters
 *         for marker fields.
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.test.LayerAssert;
import org.eclipse.nebula.widgets.nattable.test.fixture.TestLayer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Before;
import org.junit.Test;

public class SelectionLayerTest {

	private TestLayer testLayer;
	private SelectionLayer selectionLayer;

	private StubbedMarkerSelectionModel markerSelectionModel = new StubbedMarkerSelectionModel();

	@Before
	public void setup() {
		String columnInfo = "0:0;100 | 1:1;100 | 2:2;100 | 3:3;100";
		String rowInfo =    "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40";
		
		String cellInfo = 
			"A0 | <  | C0 | D0 \n" +
			"^  | <  | C1 | D1 \n" +
			"A2 | B2 | C2 | D2 \n" +
			"A3 | B3 | C3 | D3 \n";
		
		testLayer = new TestLayer(4, 4, columnInfo, rowInfo, cellInfo);
		
		selectionLayer = new SelectionLayer(testLayer);
	}
	
	@Test
	public void testIdentityLayerTransform() {
		LayerAssert.assertLayerEquals(testLayer, selectionLayer);
	}

	// Clear

	@Test
	public void testClearAllClearsAllMarkers() throws Exception {
		selectionLayer.selectAll();

		selectionLayer.clear();

		assertNull(selectionLayer.getLastSelectedCellPosition());
		assertEquals(0, selectionLayer.getLastSelectedRegion().width);
		assertEquals(0, selectionLayer.getLastSelectedRegion().height);

		assertEquals(SelectionLayer.NO_SELECTION, selectionLayer.getSelectionAnchor().columnPosition);
		assertEquals(SelectionLayer.NO_SELECTION, selectionLayer.getSelectionAnchor().rowPosition);
	}

	@Test
	public void testClearSingleCellClearsNoMarkers() throws Exception {
		selectionLayer.selectAll();

		selectionLayer.clearSelection(1, 1);

		assertNotNull(selectionLayer.getLastSelectedCellPosition());
		assertTrue(selectionLayer.getLastSelectedRegion().width > 0);
		assertTrue(selectionLayer.getLastSelectedRegion().height > 0);

		assertFalse(selectionLayer.getSelectionAnchor().columnPosition == SelectionLayer.NO_SELECTION);
		assertFalse(selectionLayer.getSelectionAnchor().rowPosition == SelectionLayer.NO_SELECTION);
	}

	@Test
	public void testClearAnchorRectangleClearsOnlyAnchor() throws Exception {
		selectionLayer.selectAll();

		selectionLayer.clearSelection(new Rectangle(0, 0, 1, 1));

		assertNotNull(selectionLayer.getLastSelectedCellPosition());
		assertTrue(selectionLayer.getLastSelectedRegion().width > 0);
		assertTrue(selectionLayer.getLastSelectedRegion().height > 0);

		assertEquals(SelectionLayer.NO_SELECTION, selectionLayer.getSelectionAnchor().columnPosition);
		assertEquals(SelectionLayer.NO_SELECTION, selectionLayer.getSelectionAnchor().rowPosition);
	}

	@Test
	public void testClearOutsideAnchorRectangleClearsNoMarkers() throws Exception {
		selectionLayer.selectAll();

		selectionLayer.clearSelection(new Rectangle(1, 1, 1, 1));

		assertFalse(selectionLayer.getSelectionAnchor().columnPosition == SelectionLayer.NO_SELECTION);
		assertFalse(selectionLayer.getSelectionAnchor().rowPosition == SelectionLayer.NO_SELECTION);
	}

	// Last Selected Region

	@Test
	public void testGetLastSelectedRegionDoesNotDelegateToModel() throws Exception {
		Rectangle lastSelectedRegion = new Rectangle(22, 22, 22, 22);
		selectionLayer.lastSelectedRegion = lastSelectedRegion;

		assertSame(lastSelectedRegion, selectionLayer.getLastSelectedRegion());
	}

	@Test
	public void testGetLastSelectedRegionDelegatesToAnchorModel() throws Exception {
		selectionLayer.setSelectionModel(markerSelectionModel);

		Rectangle lastSelectedRegion = new Rectangle(22, 22, 22, 22);
		markerSelectionModel.setLastSelectedRegion(lastSelectedRegion);

		assertEquals(lastSelectedRegion, selectionLayer.getLastSelectedRegion());
	}

	@Test
	public void testSetLastSelectedRegionDelegatesToAnchorModel() throws Exception {
		selectionLayer.setSelectionModel(markerSelectionModel);

		Rectangle region = new Rectangle(23454234, 123123, 12, 5);
		selectionLayer.setLastSelectedRegion(region);

		assertSame(region, markerSelectionModel.getLastSelectedRegion());
		assertNull(selectionLayer.lastSelectedRegion);
	}

	@Test
	public void testSetLastSelectedRegionDoesNotDelegateToModel() throws Exception {
		Rectangle region = new Rectangle(23454234, 123123, 12, 5);
		selectionLayer.setLastSelectedRegion(region);

		assertSame(region, selectionLayer.lastSelectedRegion);
	}

	@Test
	public void testSetLastSelectedRegionPreservesNULL() throws Exception {
		selectionLayer.setLastSelectedRegion(null);

		assertNull(selectionLayer.lastSelectedRegion);
	}

	@Test
	public void testSetLastSelectedRegionFieldsDelegatesToAnchorModel() throws Exception {
		selectionLayer.setSelectionModel(markerSelectionModel);

		Rectangle region = new Rectangle(23454234, 123123, 12, 5);
		selectionLayer.setLastSelectedRegion(region.x, region.y, region.width, region.height);

		assertEquals(region, markerSelectionModel.getLastSelectedRegion());
		assertNull(selectionLayer.lastSelectedRegion);
	}

	@Test
	public void testSetLastSelectedRegionFieldsDoesNotDelegateToModel() throws Exception {
		selectionLayer.selectAll();

		Rectangle existingRegion = selectionLayer.lastSelectedRegion;

		Rectangle region = new Rectangle(23454234, 123123, 12, 5);
		selectionLayer.setLastSelectedRegion(region.x, region.y, region.width, region.height);

		assertEquals(region, selectionLayer.lastSelectedRegion);
		assertSame(existingRegion, selectionLayer.lastSelectedRegion);
	}

	// Selection Anchor

	@Test
	public void testGetAnchorDoesNotDelegateToModel() throws Exception {
		PositionCoordinate existingAnchor = selectionLayer.selectionAnchor;

		assertSame(existingAnchor, selectionLayer.getSelectionAnchor());
	}

	@Test
	public void testGetAnchorDelegatesToAnchorModel() throws Exception {
		selectionLayer.setSelectionModel(markerSelectionModel);

		Point anchor = new Point(5, 7);
		markerSelectionModel.setSelectionAnchor(anchor);

		assertEquals(anchor.x, selectionLayer.getSelectionAnchor().columnPosition);
		assertEquals(anchor.y, selectionLayer.getSelectionAnchor().rowPosition);
	}

	@Test
	public void testSetSelectionAnchorDelegatesToAnchorModel() throws Exception {
		selectionLayer.setSelectionModel(markerSelectionModel);

		selectionLayer.setSelectionAnchor(456, 8);

		assertEquals(456, markerSelectionModel.getSelectionAnchor().x);
		assertEquals(8, markerSelectionModel.getSelectionAnchor().y);
		assertFalse(SelectionLayer.hasSelection(selectionLayer.selectionAnchor));
	}

	@Test
	public void testSetSelectionAnchorDoesNotDelegateToModel() throws Exception {
		selectionLayer.selectAll();

		selectionLayer.setSelectionAnchor(456, 8);

		assertEquals(456, selectionLayer.selectionAnchor.columnPosition);
		assertEquals(8, selectionLayer.selectionAnchor.rowPosition);
	}

	// Last Selected Cell

	@Test
	public void testSetLastSelectedCellDelegatesToAnchorModel() throws Exception {
		selectionLayer.setSelectionModel(markerSelectionModel);

		selectionLayer.setLastSelectedCell(456, 8);

		assertEquals(456, markerSelectionModel.getLastSelectedCell().x);
		assertEquals(8, markerSelectionModel.getLastSelectedCell().y);
		assertFalse(SelectionLayer.hasSelection(selectionLayer.lastSelectedCell));
	}

	@Test
	public void testSetLastSelectedCellDoesNotDelegateToModel() throws Exception {
		selectionLayer.selectAll();

		selectionLayer.setLastSelectedCell(456, 8);

		assertEquals(456, selectionLayer.lastSelectedCell.columnPosition);
		assertEquals(8, selectionLayer.lastSelectedCell.rowPosition);
	}

	@Test
	public void testGetLastSelectedCellDoesNotDelegateToModel() throws Exception {
		selectionLayer.selectAll();
		PositionCoordinate existingSelectedCell = selectionLayer.lastSelectedCell;

		assertSame(existingSelectedCell, selectionLayer.getLastSelectedCell());
	}

	@Test
	public void testGetLastSelectedCellDelegatesToAnchorModel() throws Exception {
		selectionLayer.setSelectionModel(markerSelectionModel);

		Point lastSelected = new Point(5, 7);
		markerSelectionModel.setLastSelectedCell(lastSelected);

		assertEquals(lastSelected.x, selectionLayer.getLastSelectedCell().columnPosition);
		assertEquals(lastSelected.y, selectionLayer.getLastSelectedCell().rowPosition);
	}

	@Test
	public void testGetLastSelectedCellPosition() throws Exception {
		selectionLayer.selectAll();
		PositionCoordinate existingSelectedCell = selectionLayer.getLastSelectedCell();

		assertSame(existingSelectedCell, selectionLayer.getLastSelectedCellPosition());
		assertNotNull(existingSelectedCell);
	}

	@Test
	public void testGetLastSelectedCellPositionReturnsNullWhenUnselected() throws Exception {
		assertNull(selectionLayer.getLastSelectedCellPosition());
	}

	public class StubbedMarkerSelectionModel implements IMarkerSelectionModel {

		private Rectangle lastSelectedRegion = new Rectangle(0, 0, 0, 0);
		private Point anchor;
		private Point lastSelectedCell;

		@Override
		public boolean isMultipleSelectionAllowed() {
			return false;
		}

		@Override
		public void setMultipleSelectionAllowed(boolean multipleSelectionAllowed) {
		}

		@Override
		public void addSelection(int columnPosition, int rowPosition) {
		}

		@Override
		public void addSelection(Rectangle range) {
		}

		@Override
		public void clearSelection() {
		}

		@Override
		public void clearSelection(int columnPosition, int rowPosition) {
		}

		@Override
		public void clearSelection(Rectangle removedSelection) {
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public List<Rectangle> getSelections() {
			return null;
		}

		@Override
		public boolean isCellPositionSelected(int columnPosition, int rowPosition) {
			return false;
		}

		@Override
		public int[] getSelectedColumnPositions() {
			return null;
		}

		@Override
		public boolean isColumnPositionSelected(int columnPosition) {
			return false;
		}

		@Override
		public int[] getFullySelectedColumnPositions(int columnHeight) {
			return null;
		}

		@Override
		public boolean isColumnPositionFullySelected(int columnPosition, int columnHeight) {
			return false;
		}

		@Override
		public int getSelectedRowCount() {
			return 0;
		}

		@Override
		public Set<Range> getSelectedRowPositions() {
			return null;
		}

		@Override
		public boolean isRowPositionSelected(int rowPosition) {
			return false;
		}

		@Override
		public int[] getFullySelectedRowPositions(int rowWidth) {
			return null;
		}

		@Override
		public boolean isRowPositionFullySelected(int rowPosition, int rowWidth) {
			return false;
		}

		@Override
		public Point getSelectionAnchor() {
			return anchor;
		}

		@Override
		public Point getLastSelectedCell() {
			return lastSelectedCell;
		}

		@Override
		public Rectangle getLastSelectedRegion() {
			return lastSelectedRegion;
		}

		@Override
		public void setSelectionAnchor(Point anchor) {
			this.anchor = anchor;
		}

		@Override
		public void setLastSelectedCell(Point lastSelectedCell) {
			this.lastSelectedCell = lastSelectedCell;
		}

		@Override
		public void setLastSelectedRegion(Rectangle region) {
			this.lastSelectedRegion = region;
		}

		@Override
		public void setLastSelectedRegion(int x, int y, int width, int height) {
			this.lastSelectedRegion.x = x;
			this.lastSelectedRegion.y = y;
			this.lastSelectedRegion.width = width;
			this.lastSelectedRegion.height = height;
		}

	}

}
