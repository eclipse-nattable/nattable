/*******************************************************************************
 * Copyright (c) 2014, 2022 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Jonas Hugo <Jonas.Hugo@jeppesen.com>,
 *       Markus Wahl <Markus.Wahl@jeppesen.com> - Test delegation of markers to
 *         model if model is an IMarkerSelectionModel. Test getters and setters
 *         for marker fields.
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.TestLayer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MarkerSelectionModelTest {

    private TestLayer testLayer;
    private SelectionLayer selectionLayer;

    private StubbedMarkerSelectionModel markerSelectionModel = new StubbedMarkerSelectionModel();

    @BeforeEach
    public void setup() {
        String columnInfo = "0:0;100 | 1:1;100 | 2:2;100 | 3:3;100";
        String rowInfo = "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40";

        String cellInfo = "A0 | <  | C0 | D0 \n" + "^  | <  | C1 | D1 \n"
                + "A2 | B2 | C2 | D2 \n" + "A3 | B3 | C3 | D3 \n";

        this.testLayer = new TestLayer(4, 4, columnInfo, rowInfo, cellInfo);

        this.selectionLayer = new SelectionLayer(this.testLayer);
    }

    @Test
    public void testGetLastSelectedRegionDelegatesToAnchorModel() {
        this.selectionLayer.setSelectionModel(this.markerSelectionModel);

        Rectangle lastSelectedRegion = new Rectangle(22, 22, 22, 22);
        this.markerSelectionModel.setLastSelectedRegion(lastSelectedRegion);

        assertEquals(lastSelectedRegion, this.selectionLayer.getLastSelectedRegion());
    }

    @Test
    public void testSetLastSelectedRegionDelegatesToAnchorModel() {
        this.selectionLayer.setSelectionModel(this.markerSelectionModel);

        Rectangle region = new Rectangle(23454234, 123123, 12, 5);
        this.selectionLayer.setLastSelectedRegion(region);

        assertSame(region, this.markerSelectionModel.getLastSelectedRegion());
        assertNull(this.selectionLayer.lastSelectedRegion);
    }

    @Test
    public void testSetLastSelectedRegionFieldsDelegatesToAnchorModel() {
        this.selectionLayer.setSelectionModel(this.markerSelectionModel);

        Rectangle region = new Rectangle(23454234, 123123, 12, 5);
        this.selectionLayer.setLastSelectedRegion(region.x, region.y, region.width, region.height);

        assertEquals(region, this.markerSelectionModel.getLastSelectedRegion());
        assertNull(this.selectionLayer.lastSelectedRegion);
    }

    @Test
    public void testGetAnchorDelegatesToAnchorModel() {
        this.selectionLayer.setSelectionModel(this.markerSelectionModel);

        Point anchor = new Point(5, 7);
        this.markerSelectionModel.setSelectionAnchor(anchor);

        assertEquals(anchor.x, this.selectionLayer.getSelectionAnchor().columnPosition);
        assertEquals(anchor.y, this.selectionLayer.getSelectionAnchor().rowPosition);
    }

    @Test
    public void testSetSelectionAnchorDelegatesToAnchorModel() {
        this.selectionLayer.setSelectionModel(this.markerSelectionModel);

        this.selectionLayer.setSelectionAnchor(456, 8);

        assertEquals(456, this.markerSelectionModel.getSelectionAnchor().x);
        assertEquals(8, this.markerSelectionModel.getSelectionAnchor().y);
        assertFalse(SelectionLayer.hasSelection(this.selectionLayer.selectionAnchor));
    }

    @Test
    public void testSetLastSelectedCellDelegatesToAnchorModel() {
        this.selectionLayer.setSelectionModel(this.markerSelectionModel);

        this.selectionLayer.setLastSelectedCell(456, 8);

        assertEquals(456, this.markerSelectionModel.getLastSelectedCell().x);
        assertEquals(8, this.markerSelectionModel.getLastSelectedCell().y);
        assertFalse(SelectionLayer.hasSelection(this.selectionLayer.lastSelectedCell));
    }

    @Test
    public void testGetLastSelectedCellDelegatesToAnchorModel() {
        this.selectionLayer.setSelectionModel(this.markerSelectionModel);

        Point lastSelected = new Point(5, 7);
        this.markerSelectionModel.setLastSelectedCell(lastSelected);

        assertEquals(lastSelected.x, this.selectionLayer.getLastSelectedCell().columnPosition);
        assertEquals(lastSelected.y, this.selectionLayer.getLastSelectedCell().rowPosition);
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
        public boolean isCellPositionSelected(int columnPosition,
                int rowPosition) {
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
        public boolean isColumnPositionFullySelected(int columnPosition,
                int columnHeight) {
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
            return this.anchor;
        }

        @Override
        public Point getLastSelectedCell() {
            return this.lastSelectedCell;
        }

        @Override
        public Rectangle getLastSelectedRegion() {
            return this.lastSelectedRegion;
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

        @Override
        public void handleLayerEvent(IStructuralChangeEvent event) {
        }

        @Override
        public Class<IStructuralChangeEvent> getLayerEventClass() {
            return IStructuralChangeEvent.class;
        }
    }

}
