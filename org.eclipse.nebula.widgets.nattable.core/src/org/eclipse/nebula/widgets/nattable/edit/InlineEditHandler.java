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
package org.eclipse.nebula.widgets.nattable.edit;

import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.command.MoveSelectionCommand;

/**
 * {@link ICellEditHandler} that handles inline editing of single values. On
 * commit it will execute an {@link UpdateDataCommand} for the specified value
 * and move the selection in the NatTable.
 */
public class InlineEditHandler implements ICellEditHandler {

    /**
     * The {@link ILayer} to which the column and row positions are related to
     * and on which the update command should be executed
     */
    private final ILayer layer;
    /**
     * The column position of the cell that is edited
     */
    private final int columnPosition;
    /**
     * The row position of the cell that is edited
     */
    private final int rowPosition;

    /**
     *
     * @param layer
     *            The {@link ILayer} to which the column and row positions are
     *            related to and on which the update command should be executed
     * @param columnPosition
     *            The column position of the cell that is edited
     * @param rowPosition
     *            The row position of the cell that is edited
     */
    public InlineEditHandler(ILayer layer, int columnPosition, int rowPosition) {
        this.layer = layer;
        this.columnPosition = columnPosition;
        this.rowPosition = rowPosition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.nebula.widgets.nattable.edit.ICellEditHandler#commit(java
     * .lang.Object,
     * org.eclipse.nebula.widgets.nattable.selection.SelectionLayer
     * .MoveDirectionEnum)
     */
    @Override
    public boolean commit(Object canonicalValue, MoveDirectionEnum direction) {
        boolean committed = this.layer.doCommand(new UpdateDataCommand(this.layer,
                this.columnPosition, this.rowPosition, canonicalValue));

        // only move the selection if the update succeeded, otherwise the editor
        // will stay open
        if (committed) {
            switch (direction) {
                case LEFT:
                    this.layer.doCommand(new MoveSelectionCommand(
                            MoveDirectionEnum.LEFT, false, false));
                    break;
                case UP:
                    this.layer.doCommand(new MoveSelectionCommand(
                            MoveDirectionEnum.UP, false, false));
                    break;
                case RIGHT:
                    this.layer.doCommand(new MoveSelectionCommand(
                            MoveDirectionEnum.RIGHT, false, false));
                    break;
                case DOWN:
                    this.layer.doCommand(new MoveSelectionCommand(
                            MoveDirectionEnum.DOWN, false, false));
                    break;
            }
        }

        return committed;
    }

}
