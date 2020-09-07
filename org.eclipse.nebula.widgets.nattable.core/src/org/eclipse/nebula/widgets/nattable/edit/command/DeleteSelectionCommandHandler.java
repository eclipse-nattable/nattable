/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.edit.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
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
    private IUniqueIndexLayer upperLayer;

    /**
     * Creates a new {@link DeleteSelectionCommandHandler}.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} needed to determine the current
     *            selected cells.
     */
    public DeleteSelectionCommandHandler(SelectionLayer selectionLayer) {
        this(selectionLayer, null);
    }

    /**
     * Creates a new {@link DeleteSelectionCommandHandler} that performs the
     * edit checks based on the given upper layer. Needed for example if the
     * upper layer adds information that is needed for checks, e.g. a tree
     * layer.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} needed to determine the current
     *            selected cells.
     * @param upperLayer
     *            The layer on top of the given {@link SelectionLayer} to which
     *            the selection should be converted to. Can be <code>null</code>
     *            which causes the resulting selected cells to be related to the
     *            {@link SelectionLayer}.
     *
     * @since 1.6
     */
    public DeleteSelectionCommandHandler(SelectionLayer selectionLayer, IUniqueIndexLayer upperLayer) {
        if (selectionLayer == null) {
            throw new IllegalArgumentException("SelectionLayer can not be null"); //$NON-NLS-1$
        }
        this.selectionLayer = selectionLayer;
        this.upperLayer = upperLayer;
    }

    @Override
    public boolean doCommand(ILayer layer, DeleteSelectionCommand command) {
        if (EditUtils.allCellsEditable(this.selectionLayer, this.upperLayer, command.getConfigRegistry())) {
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