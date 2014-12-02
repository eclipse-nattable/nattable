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
package org.eclipse.nebula.widgets.nattable.grid.layer;

import org.eclipse.nebula.widgets.nattable.grid.layer.event.ColumnHeaderSelectionEvent;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.ColumnSelectionEvent;

/**
 * Marks the ColumnHeader as selected in response to a
 * {@link ColumnSelectionEvent}
 */
public class ColumnHeaderSelectionListener implements ILayerListener {

    private ColumnHeaderLayer columnHeaderLayer;

    public ColumnHeaderSelectionListener(ColumnHeaderLayer columnHeaderLayer) {
        this.columnHeaderLayer = columnHeaderLayer;
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof ColumnSelectionEvent) {
            ColumnSelectionEvent selectionEvent = (ColumnSelectionEvent) event;
            ColumnHeaderSelectionEvent colHeaderSelectionEvent = new ColumnHeaderSelectionEvent(
                    this.columnHeaderLayer, selectionEvent.getColumnPositionRanges());
            this.columnHeaderLayer.fireLayerEvent(colHeaderSelectionEvent);
        }
    }
}
