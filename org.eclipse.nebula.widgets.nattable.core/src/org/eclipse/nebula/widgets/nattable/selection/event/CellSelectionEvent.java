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
package org.eclipse.nebula.widgets.nattable.selection.event;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

public class CellSelectionEvent extends CellVisualChangeEvent implements
        ISelectionEvent {

    private final SelectionLayer selectionLayer;

    // The state of the keys when the event was raised
    private boolean withShiftMask = false;
    private boolean withControlMask = false;

    public CellSelectionEvent(SelectionLayer selectionLayer,
            int columnPosition, int rowPosition, boolean withShiftMask,
            boolean withControlMask) {
        super(selectionLayer, columnPosition, rowPosition);
        this.selectionLayer = selectionLayer;
        this.withControlMask = withControlMask;
        this.withShiftMask = withShiftMask;
    }

    // Copy constructor
    protected CellSelectionEvent(CellSelectionEvent event) {
        super(event);
        this.selectionLayer = event.selectionLayer;
        this.withControlMask = event.withControlMask;
        this.withShiftMask = event.withShiftMask;
    }

    @Override
    public SelectionLayer getSelectionLayer() {
        return this.selectionLayer;
    }

    @Override
    public CellSelectionEvent cloneEvent() {
        return new CellSelectionEvent(this);
    }

    public boolean isWithShiftMask() {
        return this.withShiftMask;
    }

    public boolean isWithControlMask() {
        return this.withControlMask;
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        if (this.columnPosition == SelectionLayer.NO_SELECTION
                || this.rowPosition == SelectionLayer.NO_SELECTION) {
            return true;
        }
        this.columnPosition = localLayer.underlyingToLocalColumnPosition(getLayer(),
                this.columnPosition);
        this.rowPosition = localLayer.underlyingToLocalRowPosition(getLayer(),
                this.rowPosition);

        this.layer = localLayer;

        return this.columnPosition >= 0 && this.rowPosition >= 0
                && this.columnPosition < this.layer.getColumnCount()
                && this.rowPosition < this.layer.getRowCount();
    }

}
