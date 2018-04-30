/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
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
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class FreezeSelectionStrategy implements IFreezeCoordinatesProvider {

    private final FreezeLayer freezeLayer;

    private final ViewportLayer viewportLayer;

    private final SelectionLayer selectionLayer;

    private final boolean include;

    public FreezeSelectionStrategy(
            FreezeLayer freezeLayer, ViewportLayer viewportLayer, SelectionLayer selectionLayer) {
        this(freezeLayer, viewportLayer, selectionLayer, false);
    }

    /**
     *
     * @param freezeLayer
     *            The {@link FreezeLayer} on which the freeze state should be
     *            applied. Needed for coordinate definition.
     * @param viewportLayer
     *            The {@link ViewportLayer} needed to handle the scrolled state.
     * @param selectionLayer
     *            The {@link SelectionLayer} to identify the last selected cell
     *            position.
     * @param include
     *            Whether the last selected cell should be included in the
     *            freeze region or not. Include means the freeze borders will be
     *            to the right and bottom, while exclude means the freeze
     *            borders are to the left and top. Default is
     *            <code>false</code>.
     *
     * @since 1.6
     */
    public FreezeSelectionStrategy(
            FreezeLayer freezeLayer, ViewportLayer viewportLayer, SelectionLayer selectionLayer, boolean include) {
        this.freezeLayer = freezeLayer;
        this.viewportLayer = viewportLayer;
        this.selectionLayer = selectionLayer;
        this.include = include;
    }

    @Override
    public PositionCoordinate getTopLeftPosition() {
        PositionCoordinate lastSelectedCellPosition = this.selectionLayer.getLastSelectedCellPosition();
        if (lastSelectedCellPosition == null) {
            return null;
        }

        // ensure that the selected column position is based on the same layer
        // as the
        // scrollable layer of the ViewportLayer
        int selectedColumnPosition = getUnderlyingColumnPosition(lastSelectedCellPosition.columnPosition);
        int selectedRowPosition = getUnderlyingRowPosition(lastSelectedCellPosition.rowPosition);

        ILayerCell lastSelectedCell =
                this.selectionLayer.getCellByPosition(lastSelectedCellPosition.columnPosition, lastSelectedCellPosition.rowPosition);
        int columnPosition = this.viewportLayer.getScrollableLayer().getColumnPositionByX(this.viewportLayer.getOrigin().getX());
        if (columnPosition > 0
                && columnPosition >= selectedColumnPosition) {

            if (!this.include) {
                columnPosition = selectedColumnPosition - 1;
            } else {
                columnPosition = selectedColumnPosition + lastSelectedCell.getColumnSpan() - 1;
            }
        }

        int rowPosition = this.viewportLayer.getScrollableLayer().getRowPositionByY(this.viewportLayer.getOrigin().getY());
        if (rowPosition > 0
                && rowPosition >= selectedRowPosition) {

            if (!this.include) {
                rowPosition = selectedRowPosition - 1;
            } else {
                rowPosition = selectedRowPosition - lastSelectedCell.getRowSpan() - 1;
            }
        }

        return new PositionCoordinate(this.freezeLayer, columnPosition, rowPosition);
    }

    @Override
    public PositionCoordinate getBottomRightPosition() {
        if (this.selectionLayer.getSelectedCells().size() > 1) {
            if (this.selectionLayer.getFullySelectedColumnPositions().length > 0) {
                // if columns are fully selected we will freeze the columns to
                // the left including the selected column with the greatest
                // index
                int columnPosition = 0;
                int[] selColPos = this.selectionLayer.getFullySelectedColumnPositions();
                for (int col : selColPos) {
                    columnPosition = Math.max(columnPosition, col);
                }
                return new PositionCoordinate(this.freezeLayer, getUnderlyingColumnPosition(columnPosition), -1);
            } else if (this.selectionLayer.getFullySelectedRowPositions().length > 0) {
                // if rows are fully selected we will freeze the rows to the top
                // including the selected row with the greatest index
                int rowPosition = 0;
                int[] selRowPos = this.selectionLayer.getFullySelectedRowPositions();
                for (int row : selRowPos) {
                    rowPosition = Math.max(rowPosition, row);
                }
                return new PositionCoordinate(this.freezeLayer, -1, getUnderlyingRowPosition(rowPosition));
            } else {
                // find the selected cell that is most to the left and to the
                // top of the selection
                int columnPosition = -1;
                int rowPosition = -1;
                PositionCoordinate[] coords = this.selectionLayer.getSelectedCellPositions();
                for (PositionCoordinate coord : coords) {
                    if (columnPosition < 0) {
                        columnPosition = coord.columnPosition;
                    } else {
                        if (!this.include) {
                            columnPosition = Math.min(columnPosition, coord.columnPosition);
                        } else {
                            columnPosition = Math.max(columnPosition, coord.columnPosition);
                        }
                    }
                    if (rowPosition < 0) {
                        rowPosition = coord.rowPosition;
                    } else {
                        if (!this.include) {
                            rowPosition = Math.min(rowPosition, coord.rowPosition);
                        } else {
                            rowPosition = Math.max(rowPosition, coord.rowPosition);
                        }
                    }
                }
                int column = getUnderlyingColumnPosition(columnPosition);
                int row = getUnderlyingRowPosition(rowPosition);
                return new PositionCoordinate(
                        this.freezeLayer,
                        !this.include ? column - 1 : column,
                        !this.include ? row - 1 : row);
            }
        } else {
            PositionCoordinate selectionAnchor = this.selectionLayer.getSelectionAnchor();
            if (selectionAnchor != null) {
                int column = getUnderlyingColumnPosition(selectionAnchor.columnPosition);
                int row = getUnderlyingRowPosition(selectionAnchor.rowPosition);
                if (!this.include) {
                    return new PositionCoordinate(this.freezeLayer,
                            column - 1,
                            row - 1);
                } else {
                    ILayerCell selectionAnchorCell =
                            this.selectionLayer.getCellByPosition(selectionAnchor.columnPosition, selectionAnchor.rowPosition);
                    return new PositionCoordinate(this.freezeLayer,
                            column + selectionAnchorCell.getColumnSpan() - 1,
                            row + selectionAnchorCell.getRowSpan() - 1);
                }
            }
        }
        return null;
    }

    /**
     * Returns the column position to be used for the FreezeLayer position.
     * Typically no conversion is needed when the FreezeLayer is build on top of
     * the SelectionLayer. But if there is a layer in between that adds a
     * transformation (e.g. adding a column like the HierarchicalTreeLayer) the
     * SelectionLayer based position needs to be converted.
     *
     * @param columnPosition
     *            The SelectionLayer based column position.
     * @return The column position based on the scrollable layer below the
     *         ViewportLayer.
     *
     * @since 1.6
     */
    protected int getUnderlyingColumnPosition(int columnPosition) {
        if (this.viewportLayer.getScrollableLayer() == this.selectionLayer) {
            // no transformation necessary
            return columnPosition;
        }
        return LayerUtil.convertColumnPosition(this.selectionLayer, columnPosition, this.viewportLayer.getScrollableLayer());
    }

    /**
     * Returns the row position to be used for the FreezeLayer position.
     * Typically no conversion is needed when the FreezeLayer is build on top of
     * the SelectionLayer. But if there is a layer in between that adds a
     * transformation the SelectionLayer based position needs to be converted.
     *
     * @param rowPosition
     *            The SelectionLayer based row position.
     * @return The row position based on the scrollable layer below the
     *         ViewportLayer.
     *
     * @since 1.6
     */
    protected int getUnderlyingRowPosition(int rowPosition) {
        if (this.viewportLayer.getScrollableLayer() == this.selectionLayer) {
            // no transformation necessary
            return rowPosition;
        }
        return LayerUtil.convertRowPosition(this.selectionLayer, rowPosition, this.viewportLayer.getScrollableLayer());
    }
}
