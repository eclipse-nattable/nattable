/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRegionCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Command handler for the {@link SelectRegionCommand}.
 *
 * @since 1.6
 */
public class SelectRegionCommandHandler implements ILayerCommandHandler<SelectRegionCommand> {

    protected final SelectionLayer selectionLayer;

    /**
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} on which this handler should
     *            operate.
     */
    public SelectRegionCommandHandler(SelectionLayer selectionLayer) {
        this.selectionLayer = selectionLayer;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, SelectRegionCommand command) {
        if (command.convertToTargetLayer(this.selectionLayer)) {
            selectRegion(command.getRegion(), command.isShiftMask(), command.isControlMask());
            return true;
        }
        return false;
    }

    protected void selectRegion(Rectangle region, boolean withShiftMask, boolean withControlMask) {
        Set<Integer> changedRows = new HashSet<Integer>();

        if (SelectionUtils.noShiftOrControl(withShiftMask, withControlMask)) {
            // no modifier
            this.selectionLayer.clear(false);
            this.selectionLayer.selectCell(region.x, region.y, false, false);
            this.selectionLayer.selectRegion(region.x, region.y, region.width, region.height);
            this.selectionLayer.moveSelectionAnchor(region.x, region.y);

            // add rows that have changed
            for (int i = region.y; i < (region.y + region.height); i++) {
                changedRows.add(i);
            }
        } else if (SelectionUtils.bothShiftAndControl(withShiftMask, withControlMask)
                || SelectionUtils.isShiftOnly(withShiftMask, withControlMask)) {
            // SHIFT or CTRL + SHIFT modifier enabled
            changedRows.addAll(selectRegionWithShiftKey(region));
        } else if (SelectionUtils.isControlOnly(withShiftMask, withControlMask)) {
            // CTRL modifier enabled
            changedRows.addAll(selectRegionWithCtrlKey(region));
        }

        // Set last selected position to the recently clicked cell
        this.selectionLayer.setLastSelectedCell(region.x, region.y);

        this.selectionLayer.fireLayerEvent(
                new RowSelectionEvent(
                        this.selectionLayer,
                        changedRows,
                        this.selectionLayer.getSelectionAnchor().getRowPosition(),
                        withShiftMask,
                        withControlMask));

    }

    /**
     * Selects a region with SHIFT modifier enabled. That means the selection
     * range is calculated based on the current selection anchor and the corner
     * of the given region that is most away from the anchor.
     *
     * @param region
     *            The region to be selected.
     * @return The row positions that have gained selection.
     */
    protected Collection<Integer> selectRegionWithShiftKey(Rectangle region) {
        int startCol = region.x;
        int startRow = region.y;
        int noCol = region.width;
        int noRow = region.height;

        // This method selects the range based on the selection anchor and the
        // clicked position. Therefore the selection prior adding the newly
        // calculated selection needs to be cleared in advance.
        Rectangle lastSelectedRegion = this.selectionLayer.getLastSelectedRegion();
        if (lastSelectedRegion != null) {
            this.selectionLayer.getSelectionModel().clearSelection(lastSelectedRegion);
        } else {
            this.selectionLayer.getSelectionModel().clearSelection();
        }

        PositionCoordinate anchor = this.selectionLayer.getSelectionAnchor();
        if (anchor.columnPosition != SelectionLayer.NO_SELECTION
                && anchor.rowPosition != SelectionLayer.NO_SELECTION) {

            if (startCol < anchor.columnPosition) {
                noCol = Math.abs(anchor.columnPosition - startCol) + 1;
            } else {
                startCol = anchor.columnPosition;
                noCol = (region.x + region.width) - anchor.columnPosition;
            }

            if (startRow < anchor.rowPosition) {
                noRow = Math.abs(anchor.rowPosition - startRow) + 1;
            } else {
                startRow = anchor.rowPosition;
                noRow = (region.y + region.height) - anchor.rowPosition;
            }
        } else {
            // if there is no last selected region we need to set the anchor
            // for correct behavior on further actions
            this.selectionLayer.moveSelectionAnchor(startCol, startRow);
        }

        this.selectionLayer.selectRegion(startCol, startRow, noCol, noRow);

        // add rows that have changed
        Set<Integer> changedRows = new HashSet<Integer>();
        for (int i = startRow; i < (startRow + noRow); i++) {
            changedRows.add(i);
        }
        return changedRows;
    }

    /**
     * Selects a region with CTRL modifier enabled. That means the current
     * selection is extended by the given region.
     *
     * @param region
     *            The region to be selected.
     * @return The row positions that have gained selection.
     */
    protected Collection<Integer> selectRegionWithCtrlKey(Rectangle region) {
        if (this.selectionLayer.allCellsSelectedInRegion(region)) {
            // clear if all cells in the region are selected
            this.selectionLayer.clearSelection(region);
            this.selectionLayer.setLastSelectedRegion(null);

            // update anchor
            PositionCoordinate[] selectedCells = this.selectionLayer.getSelectedCellPositions();
            if (selectedCells.length > 0
                    && this.selectionLayer.getSelectionAnchor().columnPosition == SelectionLayer.NO_SELECTION
                    && this.selectionLayer.getSelectionAnchor().rowPosition == SelectionLayer.NO_SELECTION) {

                // determine column to move the anchor to

                // if another cell in the region.x column is selected, only
                // search for a new anchor in that column
                PositionCoordinate toPos = null;
                if (this.selectionLayer.isColumnPositionSelected(region.x)) {
                    for (int i = 0; i < selectedCells.length; i++) {
                        if (selectedCells[i].rowPosition < region.y
                                && selectedCells[i].columnPosition == region.x) {
                            toPos = selectedCells[i];
                        } else {
                            break;
                        }
                    }
                }
                // search for another selected cell as new anchor if there is
                // none in the same column
                if (toPos == null) {
                    toPos = selectedCells[0];
                    for (int i = 0; i < selectedCells.length; i++) {
                        if (selectedCells[i].rowPosition < region.y
                                || selectedCells[i].columnPosition < region.x) {
                            toPos = selectedCells[i];
                        } else {
                            break;
                        }
                    }
                }

                this.selectionLayer.moveSelectionAnchor(toPos.columnPosition, toPos.rowPosition);
            }
        } else {
            // if none or at least one cell in the region is already
            // selected, simply add
            this.selectionLayer.selectRegion(region.x, region.y, region.width, region.height);
            this.selectionLayer.moveSelectionAnchor(region.x, region.y);
        }

        // add rows that have changed
        Set<Integer> changedRows = new HashSet<Integer>();
        for (int i = region.y; i < (region.y + region.height); i++) {
            changedRows.add(i);
        }
        return changedRows;
    }

    @Override
    public Class<SelectRegionCommand> getCommandClass() {
        return SelectRegionCommand.class;
    }

}
