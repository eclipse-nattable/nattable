/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.edit.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * {@link ILayerCommandHandler} for handling {@link DeleteSelectionCommand}s.
 * Sets the values of the current selected cells to <code>null</code>.
 *
 * @see DeleteSelectionCommand
 *
 * @since 1.4
 */
public class DeleteSelectionCommandHandler implements ILayerCommandHandler<DeleteSelectionCommand> {

    private final SelectionLayer selectionLayer;

    /**
     * Creates a new {@link DeleteSelectionCommandHandler}.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} needed to determine the current
     *            selected cells.
     */
    public DeleteSelectionCommandHandler(SelectionLayer selectionLayer) {
        if (selectionLayer == null) {
            throw new IllegalArgumentException("SelectionLayer can not be null"); //$NON-NLS-1$
        }
        this.selectionLayer = selectionLayer;
    }

    @Override
    public boolean doCommand(ILayer layer, DeleteSelectionCommand command) {
        if (EditUtils.allCellsEditable(this.selectionLayer, command.getConfigRegistry())) {
            for (PositionCoordinate coord : this.selectionLayer.getSelectedCellPositions()) {
                coord.getLayer().doCommand(new UpdateDataCommand(coord.getLayer(),
                        coord.getColumnPosition(),
                        coord.getRowPosition(), null));
            }
        }
        return true;
    }

    @Override
    public Class<DeleteSelectionCommand> getCommandClass() {
        return DeleteSelectionCommand.class;
    }

}