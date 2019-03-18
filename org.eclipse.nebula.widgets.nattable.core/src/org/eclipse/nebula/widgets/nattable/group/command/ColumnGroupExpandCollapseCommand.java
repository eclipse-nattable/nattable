/*******************************************************************************
 * Copyright (c) 2012, 2019 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to trigger expand/collapse on a column group at the specified
 * coordinates, based on the current expand/collapse state.
 */
public class ColumnGroupExpandCollapseCommand extends AbstractColumnCommand {

    /**
     * We carry the row position in here as separate member instead of making
     * this command a AbstractPositionCommand. The reason for this is that
     * otherwise the position transformation will fail and the command will not
     * get executed.
     */
    private final RowPositionCoordinate rowPositionCoordinate;

    /**
     * Create the command for a given column position and row position 0;
     *
     * @param layer
     *            The layer to which the positions match.
     * @param columnPosition
     *            The column position to identify the column group.
     */
    public ColumnGroupExpandCollapseCommand(ILayer layer, int columnPosition) {
        this(layer, columnPosition, 0);
    }

    /**
     * Create the command for the given column and row positions.
     *
     * @param layer
     *            The layer to which the positions match.
     * @param columnPosition
     *            The column position to identify the column group.
     * @param rowPosition
     *            The row position to identify the column group in a multi-level
     *            configuration.
     */
    public ColumnGroupExpandCollapseCommand(ILayer layer, int columnPosition, int rowPosition) {
        super(layer, columnPosition);
        this.rowPositionCoordinate = new RowPositionCoordinate(layer, rowPosition);
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected ColumnGroupExpandCollapseCommand(ColumnGroupExpandCollapseCommand command) {
        super(command);
        this.rowPositionCoordinate = command.rowPositionCoordinate;
    }

    @Override
    public ColumnGroupExpandCollapseCommand cloneCommand() {
        return new ColumnGroupExpandCollapseCommand(this);
    }

    /**
     *
     * @return The layer to which the row position matches.
     * @since 1.6
     */
    public ILayer getRowPositionLayer() {
        return this.rowPositionCoordinate.getLayer();
    }

    /**
     * @return the rowPosition
     */
    public int getRowPosition() {
        return this.rowPositionCoordinate.getRowPosition();
    }

    /**
     * Converts the locally transported row position from its origin layer to
     * the given target layer.
     *
     * @param targetLayer
     *            The target layer to convert the command to.
     * @return The row position converted to the given target layer or -1 in
     *         case the transformation failed.
     * @since 1.6
     */
    public int getLocalRowPosition(ILayer targetLayer) {
        RowPositionCoordinate positionCoordinate = LayerCommandUtil.convertRowPositionToTargetContext(this.rowPositionCoordinate, targetLayer);
        if (positionCoordinate != null) {
            return positionCoordinate.getRowPosition();
        }
        // conversion fails, so we return the initial row, needed in case the
        // command was fired not from the NatTable or grid layer
        return this.rowPositionCoordinate.getRowPosition();
    }
}
