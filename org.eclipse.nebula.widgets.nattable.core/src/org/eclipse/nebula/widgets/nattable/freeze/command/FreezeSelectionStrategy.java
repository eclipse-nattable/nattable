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
package org.eclipse.nebula.widgets.nattable.freeze.command;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class FreezeSelectionStrategy implements IFreezeCoordinatesProvider {

    private final FreezeLayer freezeLayer;

    private final ViewportLayer viewportLayer;

    private final SelectionLayer selectionLayer;

    public FreezeSelectionStrategy(FreezeLayer freezeLayer,
            ViewportLayer viewportLayer, SelectionLayer selectionLayer) {
        this.freezeLayer = freezeLayer;
        this.viewportLayer = viewportLayer;
        this.selectionLayer = selectionLayer;
    }

    @Override
    public PositionCoordinate getTopLeftPosition() {
        PositionCoordinate lastSelectedCellPosition = this.selectionLayer
                .getLastSelectedCellPosition();
        if (lastSelectedCellPosition == null) {
            return null;
        }

        int columnPosition = this.viewportLayer.getScrollableLayer()
                .getColumnPositionByX(this.viewportLayer.getOrigin().getX());
        if (columnPosition > 0
                && columnPosition >= lastSelectedCellPosition.columnPosition) {
            columnPosition = lastSelectedCellPosition.columnPosition - 1;
        }

        int rowPosition = this.viewportLayer.getScrollableLayer().getRowPositionByY(
                this.viewportLayer.getOrigin().getY());
        if (rowPosition > 0
                && rowPosition >= lastSelectedCellPosition.rowPosition) {
            rowPosition = lastSelectedCellPosition.rowPosition - 1;
        }

        return new PositionCoordinate(this.freezeLayer, columnPosition, rowPosition);
    }

    @Override
    public PositionCoordinate getBottomRightPosition() {
        if (this.selectionLayer.getSelectedCells().size() > 1) {
            if (this.selectionLayer.getFullySelectedColumnPositions().length > 0) {
                // if columns are fully selected we will freeze the columns to
                // the left
                // including the selected column with the greatest index
                int columnPosition = 0;
                int[] selColPos = this.selectionLayer
                        .getFullySelectedColumnPositions();
                for (int col : selColPos) {
                    columnPosition = Math.max(columnPosition, col);
                }
                return new PositionCoordinate(this.freezeLayer, columnPosition, -1);
            } else if (this.selectionLayer.getFullySelectedRowPositions().length > 0) {
                // if rows are fully selected we will freeze the rows to the top
                // including the selected row with the greatest index
                int rowPosition = 0;
                int[] selRowPos = this.selectionLayer.getFullySelectedRowPositions();
                for (int row : selRowPos) {
                    rowPosition = Math.max(rowPosition, row);
                }
                return new PositionCoordinate(this.freezeLayer, -1, rowPosition);
            } else {
                // find the selected cell that is most to the left and to the
                // top of the selection
                int columnPosition = -1;
                int rowPosition = -1;
                PositionCoordinate[] coords = this.selectionLayer
                        .getSelectedCellPositions();
                for (PositionCoordinate coord : coords) {
                    if (columnPosition < 0) {
                        columnPosition = coord.columnPosition;
                    } else {
                        columnPosition = Math.min(columnPosition,
                                coord.columnPosition);
                    }
                    if (rowPosition < 0) {
                        rowPosition = coord.rowPosition;
                    } else {
                        rowPosition = Math.min(rowPosition, coord.rowPosition);
                    }
                }
                return new PositionCoordinate(this.freezeLayer, columnPosition - 1,
                        rowPosition - 1);
            }
        } else {
            PositionCoordinate selectionAnchor = this.selectionLayer
                    .getSelectionAnchor();
            if (selectionAnchor != null) {
                return new PositionCoordinate(this.freezeLayer,
                        selectionAnchor.columnPosition - 1,
                        selectionAnchor.rowPosition - 1);
            }
        }
        return null;
    }

}
