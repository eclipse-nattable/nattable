/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRegionCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RegionSelectionTest {

    private SelectionLayer selectionLayer;

    @Before
    public void setUp() {
        this.selectionLayer = new SelectionLayer(new DataLayerFixture(10, 10, 100, 20));
    }

    @After
    public void cleanUp() {
        this.selectionLayer.clear();
    }

    @Test
    public void shouldSelectRegion() {
        this.selectionLayer.doCommand(
                new SelectRegionCommand(this.selectionLayer, 2, 3, 3, 3, false, false));

        PositionCoordinate[] cellPositions = this.selectionLayer.getSelectedCellPositions();
        assertEquals(9, cellPositions.length);

        Rectangle region = new Rectangle(2, 3, 3, 3);
        assertTrue("not all cells in region selected", this.selectionLayer.allCellsSelectedInRegion(region));

        region = new Rectangle(1, 1, 2, 2);
        assertFalse("all cells selected", this.selectionLayer.allCellsSelectedInRegion(region));

        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(3, this.selectionLayer.getSelectionAnchor().getRowPosition());
    }

    @Test
    public void shouldSelectOtherRegion() {
        this.selectionLayer.doCommand(
                new SelectRegionCommand(this.selectionLayer, 2, 3, 3, 3, false, false));

        this.selectionLayer.doCommand(
                new SelectRegionCommand(this.selectionLayer, 1, 2, 2, 2, false, false));

        PositionCoordinate[] cellPositions = this.selectionLayer.getSelectedCellPositions();
        assertEquals(4, cellPositions.length);

        Rectangle region = new Rectangle(1, 2, 2, 2);
        assertTrue("not all cells in region selected", this.selectionLayer.allCellsSelectedInRegion(region));

        region = new Rectangle(2, 3, 3, 3);
        assertFalse("all cells selected", this.selectionLayer.allCellsSelectedInRegion(region));

        assertEquals(1, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(2, this.selectionLayer.getSelectionAnchor().getRowPosition());
    }

    @Test
    public void shouldExtendSelectedRegionDownWithShift() {
        this.selectionLayer.doCommand(
                new SelectRegionCommand(this.selectionLayer, 2, 3, 3, 3, false, false));

        this.selectionLayer.doCommand(
                new SelectRegionCommand(this.selectionLayer, 2, 7, 3, 2, true, false));

        PositionCoordinate[] cellPositions = this.selectionLayer.getSelectedCellPositions();
        assertEquals(18, cellPositions.length);

        Rectangle region = new Rectangle(2, 3, 3, 6);
        assertTrue("not all cells in region selected", this.selectionLayer.allCellsSelectedInRegion(region));

        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(3, this.selectionLayer.getSelectionAnchor().getRowPosition());
    }

    @Test
    public void shouldUpdateSelectedRegionUpWithShift() {
        this.selectionLayer.doCommand(
                new SelectRegionCommand(this.selectionLayer, 2, 3, 3, 3, false, false));

        this.selectionLayer.doCommand(
                new SelectRegionCommand(this.selectionLayer, 2, 0, 3, 1, true, false));

        PositionCoordinate[] cellPositions = this.selectionLayer.getSelectedCellPositions();
        assertEquals(12, cellPositions.length);

        Rectangle region = new Rectangle(2, 0, 3, 3);
        assertTrue("not all cells in region selected", this.selectionLayer.allCellsSelectedInRegion(region));

        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(3, this.selectionLayer.getSelectionAnchor().getRowPosition());
    }

    @Test
    public void shouldExtendSelectedRegionRightWithShift() {
        this.selectionLayer.doCommand(
                new SelectRegionCommand(this.selectionLayer, 2, 3, 3, 3, false, false));

        this.selectionLayer.doCommand(
                new SelectRegionCommand(this.selectionLayer, 6, 3, 2, 3, true, false));

        PositionCoordinate[] cellPositions = this.selectionLayer.getSelectedCellPositions();
        assertEquals(18, cellPositions.length);

        Rectangle region = new Rectangle(2, 3, 6, 3);
        assertTrue("not all cells in region selected", this.selectionLayer.allCellsSelectedInRegion(region));

        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(3, this.selectionLayer.getSelectionAnchor().getRowPosition());
    }

    @Test
    public void shouldUpdateSelectedRegionLeftWithShift() {
        this.selectionLayer.doCommand(
                new SelectRegionCommand(this.selectionLayer, 2, 3, 3, 3, false, false));

        this.selectionLayer.doCommand(
                new SelectRegionCommand(this.selectionLayer, 0, 3, 2, 3, true, false));

        PositionCoordinate[] cellPositions = this.selectionLayer.getSelectedCellPositions();
        assertEquals(9, cellPositions.length);

        Rectangle region = new Rectangle(0, 3, 3, 3);
        assertTrue("not all cells in region selected", this.selectionLayer.allCellsSelectedInRegion(region));

        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(3, this.selectionLayer.getSelectionAnchor().getRowPosition());
    }

    @Test
    public void shouldAppendSelectedRegionWithCtrl() {
        this.selectionLayer.doCommand(
                new SelectRegionCommand(this.selectionLayer, 2, 3, 3, 3, false, false));

        this.selectionLayer.doCommand(
                new SelectRegionCommand(this.selectionLayer, 2, 7, 3, 1, false, true));

        PositionCoordinate[] cellPositions = this.selectionLayer.getSelectedCellPositions();
        assertEquals(12, cellPositions.length);

        Rectangle region = new Rectangle(2, 3, 3, 3);
        assertTrue("not all cells in region selected", this.selectionLayer.allCellsSelectedInRegion(region));

        region = new Rectangle(2, 7, 3, 1);
        assertTrue("not all cells in region selected", this.selectionLayer.allCellsSelectedInRegion(region));

        region = new Rectangle(2, 3, 3, 5);
        assertFalse("all cells in region selected", this.selectionLayer.allCellsSelectedInRegion(region));

        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(7, this.selectionLayer.getSelectionAnchor().getRowPosition());
    }

    @Test
    public void shouldUpdateAnchorOnDeselectRegion() {
        this.selectionLayer.doCommand(
                new SelectRegionCommand(this.selectionLayer, 2, 3, 3, 1, false, false));

        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(3, this.selectionLayer.getSelectionAnchor().getRowPosition());

        this.selectionLayer.doCommand(
                new SelectRegionCommand(this.selectionLayer, 2, 5, 3, 1, false, true));

        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(5, this.selectionLayer.getSelectionAnchor().getRowPosition());

        this.selectionLayer.doCommand(
                new SelectRegionCommand(this.selectionLayer, 2, 4, 3, 1, false, true));

        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(4, this.selectionLayer.getSelectionAnchor().getRowPosition());

        // after de-selection the anchor should move up in the same column
        this.selectionLayer.doCommand(
                new SelectRegionCommand(this.selectionLayer, 2, 4, 3, 1, false, true));

        assertEquals(2, this.selectionLayer.getSelectionAnchor().getColumnPosition());
        assertEquals(3, this.selectionLayer.getSelectionAnchor().getRowPosition());
    }
}
