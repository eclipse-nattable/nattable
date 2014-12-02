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
package org.eclipse.nebula.widgets.nattable.edit.event;

import org.eclipse.nebula.widgets.nattable.edit.EditController;
import org.eclipse.nebula.widgets.nattable.edit.command.EditSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;

/**
 * Event handler for handling {@link InlineCellEditEvent}s. Used to activate
 * editors for inline editing.
 *
 * @see InlineCellEditEvent
 * @see EditSelectionCommandHandler
 */
public class InlineCellEditEventHandler implements
        ILayerEventHandler<InlineCellEditEvent> {

    /**
     * The layer this event handler is associated with. Needed for the
     * conversion of cell position coordinates.Usually this is a grid layer
     * because this is the main cause for this event handler is needed.
     */
    private final ILayer layer;

    /**
     * @param layer
     *            The layer this event handler is associated with. Needed for
     *            the conversion of cell position coordinates.
     */
    public InlineCellEditEventHandler(ILayer layer) {
        this.layer = layer;
    }

    @Override
    public Class<InlineCellEditEvent> getLayerEventClass() {
        return InlineCellEditEvent.class;
    }

    @Override
    public void handleLayerEvent(InlineCellEditEvent event) {
        if (event.convertToLocal(this.layer)) {
            ILayerCell cell = this.layer.getCellByPosition(
                    event.getColumnPosition(), event.getRowPosition());
            EditController.editCell(cell, event.getParent(),
                    event.getInitialValue(), event.getConfigRegistry());
        }
    }
}
