/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractRowCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to trigger expand/collapse on a row group at the specified
 * coordinates, based on the current expand/collapse state.
 */
public class RowGroupExpandCollapseCommand extends AbstractRowCommand {

    /**
     * We carry the column position in here as separate member instead of making
     * this command a AbstractPositionCommand. The reason for this is that
     * otherwise the position transformation will fail and the command will not
     * get executed.
     */
    private final ColumnPositionCoordinate columnPositionCoordinate;

    /**
     * Create the command for a given row position and column position 0;
     *
     * @param layer
     *            The layer to which the positions match.
     * @param rowPosition
     *            The row position to identify the row group.
     */
    public RowGroupExpandCollapseCommand(ILayer layer, int rowPosition) {
        this(layer, rowPosition, 0);
    }

    /**
     * Create the command for the given row and column positions.
     *
     * @param layer
     *            The layer to which the positions match.
     * @param rowPosition
     *            The row position to identify the row group.
     * @param columnPosition
     *            The column position to identify the row group in a multi-level
     *            configuration.
     *
     * @since 1.6
     */
    public RowGroupExpandCollapseCommand(ILayer layer, int rowPosition, int columnPosition) {
        super(layer, rowPosition);
        this.columnPositionCoordinate = new ColumnPositionCoordinate(layer, columnPosition);
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected RowGroupExpandCollapseCommand(RowGroupExpandCollapseCommand command) {
        super(command);
        this.columnPositionCoordinate = command.columnPositionCoordinate;
    }

    @Override
    public RowGroupExpandCollapseCommand cloneCommand() {
        return new RowGroupExpandCollapseCommand(this);
    }

    /**
     *
     * @return The layer to which the column position matches.
     * @since 1.6
     */
    public ILayer getColumnPositionLayer() {
        return this.columnPositionCoordinate.getLayer();
    }

    /**
     * @return the columnPosition
     * @since 1.6
     */
    public int getColumnPosition() {
        return this.columnPositionCoordinate.getColumnPosition();
    }

    /**
     * Converts the locally transported column position from its origin layer to
     * the given target layer.
     *
     * @param targetLayer
     *            The target layer to convert the command to.
     * @return The column position converted to the given target layer or -1 in
     *         case the transformation failed.
     * @since 1.6
     */
    public int getLocalColumnPosition(ILayer targetLayer) {
        ColumnPositionCoordinate positionCoordinate = LayerCommandUtil.convertColumnPositionToTargetContext(this.columnPositionCoordinate, targetLayer);
        if (positionCoordinate != null) {
            return positionCoordinate.getColumnPosition();
        }
        // conversion fails, so we return the initial column, needed in case the
        // command was fired not from the NatTable or grid layer
        return this.columnPositionCoordinate.getColumnPosition();
    }
}
