/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.event;

import org.eclipse.nebula.widgets.nattable.grid.layer.DimensionallyDependentLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Specialization of the CellVisualChangeEvent. The only difference is the
 * handling of this type of event in the NatTable event handling. While the
 * CellVisualChangeEvent causes a whole redraw operation of the visible part
 * (which is necessary to update everything if a data value has change, for
 * example important for conditional styling), this event only forces to redraw
 * the specified cell itself. This is for example necessary for hover styling,
 * where redrawing everything is not necessary and would cause lags in applying
 * hover styling.
 *
 * @author Dirk Fauth
 *
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
    public CellVisualUpdateEvent(ILayer layer, int columnPosition,
            int rowPosition) {
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
        if (!(localLayer instanceof DimensionallyDependentLayer)) {
            this.columnPosition = localLayer.underlyingToLocalColumnPosition(
                    getLayer(), this.columnPosition);
            this.rowPosition = localLayer.underlyingToLocalRowPosition(getLayer(),
                    this.rowPosition);
        }

        this.layer = localLayer;

        return this.columnPosition >= 0 && this.rowPosition >= 0
                && this.columnPosition < this.layer.getColumnCount()
                && this.rowPosition < this.layer.getRowCount();
    }

}
