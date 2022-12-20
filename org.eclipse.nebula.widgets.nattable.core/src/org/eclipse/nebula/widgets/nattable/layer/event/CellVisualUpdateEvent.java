/*******************************************************************************
 * Copyright (c) 2013, 2022 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.event;

import org.eclipse.nebula.widgets.nattable.grid.layer.DimensionallyDependentLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

/**
 * Specialization of the CellVisualChangeEvent. The only difference is the
 * handling of this type of event in the NatTable event handling. While the
 * CellVisualChangeEvent causes a whole redraw operation of the visible part
 * (which is necessary to update everything if a data value has change, for
 * example important for conditional styling), this event only forces to redraw
 * the specified cell itself. This is for example necessary for hover styling,
 * where redrawing everything is not necessary and would cause lags in applying
 * hover styling.
 */
public class CellVisualUpdateEvent extends CellVisualChangeEvent {

    /**
     * Create a new CellVisualUpdateEvent based on the given information.
     *
     * @param layer
     *            The layer to which the given column and row position belong.
     * @param columnPosition
     *            The column position of the cell that needs to be redrawn.
     * @param rowPosition
     *            The row position of the cell that needs to be redrawn.
     */
    public CellVisualUpdateEvent(ILayer layer, int columnPosition, int rowPosition) {
        super(layer, columnPosition, rowPosition);
    }

    /**
     * Create a new CellVisualUpdateEvent out of the given event. Used
     * internally for cloning purposes.
     *
     * @param event
     *            The event to create the clone from.
     */
    protected CellVisualUpdateEvent(CellVisualChangeEvent event) {
        super(event);
    }

    @Override
    public CellVisualUpdateEvent cloneEvent() {
        return new CellVisualUpdateEvent(this);
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        int columnPos = this.columnPosition;
        int rowPos = this.rowPosition;

        if (!(localLayer instanceof DimensionallyDependentLayer)) {
            columnPos = localLayer.underlyingToLocalColumnPosition(getLayer(), this.columnPosition);
            rowPos = localLayer.underlyingToLocalRowPosition(getLayer(), this.rowPosition);
        } else {
            DimensionallyDependentLayer ddl = (DimensionallyDependentLayer) localLayer;

            if (ddl.getHorizontalLayerDependency() instanceof IUniqueIndexLayer) {
                int columnIndex =
                        this.layer.getColumnIndexByPosition(this.columnPosition);
                columnPos = ((IUniqueIndexLayer) ddl.getHorizontalLayerDependency()).getColumnPositionByIndex(columnIndex);
            }
            if (ddl.getVerticalLayerDependency() instanceof IUniqueIndexLayer) {
                int rowIndex =
                        this.layer.getRowIndexByPosition(this.rowPosition);
                rowPos = ((IUniqueIndexLayer) ddl.getVerticalLayerDependency()).getRowPositionByIndex(rowIndex);
            }
        }

        if (columnPos < 0 || rowPos < 0) {
            ILayerCell cell = getLayer().getCellByPosition(this.columnPosition, this.rowPosition);
            if (cell != null && cell.isSpannedCell()) {
                // check if a cell in the spanning is still valid
                for (int column = cell.getOriginColumnPosition(); column < cell.getOriginColumnPosition() + cell.getColumnSpan(); column++) {
                    columnPos = localLayer.underlyingToLocalColumnPosition(getLayer(), column);
                    if (columnPos >= 0) {
                        break;
                    }
                }
                for (int row = cell.getOriginRowPosition(); row < cell.getOriginRowPosition() + cell.getRowSpan(); row++) {
                    rowPos = localLayer.underlyingToLocalRowPosition(getLayer(), row);
                    if (rowPos >= 0) {
                        break;
                    }
                }
            }
        }

        this.columnPosition = columnPos;
        this.rowPosition = rowPos;
        this.layer = localLayer;

        return this.columnPosition >= 0
                && this.rowPosition >= 0
                && this.columnPosition < this.layer.getColumnCount()
                && this.rowPosition < this.layer.getRowCount();
    }

}
