/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to trigger the selection of all columns belonging to a column group.
 *
 * @since 1.6
 */
public class ViewportSelectColumnGroupCommand extends AbstractColumnCommand {

    /**
     * The row position in the NatTable. Needed to determine the column group header
     * level, e.g. with two column group headers row 0 is the
     * ColumnGroupGroupHeaderLayer and row 1 is the ColumnGroupHeaderLayer. Will not
     * be transformed in processing as the position in the table is needed.
     */
    private final int natTableRowPosition;

    private final boolean withShiftMask;
    private final boolean withControlMask;

    /**
     *
     * @param layer
     *            The {@link ILayer} for the column position reference.
     * @param columnPosition
     *            The column position according to the given layer, that should be
     *            used to determine the column group whose columns should be
     *            selected.
     * @param rowPosition
     *            The row position according to the given layer, that should be used
     *            to determine the column group level. Especially needed in a layer
     *            composition with multiple column groups.
     * @param withShiftMask
     *            <code>true</code> if the selection should be processed as if the
     *            shift modifier is active.
     * @param withControlMask
     *            <code>true</code> if the selection should be processed as if the
     *            control modifier is active.
     */
    public ViewportSelectColumnGroupCommand(
            ILayer layer,
            int columnPosition,
            int rowPosition,
            boolean withShiftMask,
            boolean withControlMask) {
        super(layer, columnPosition);
        this.natTableRowPosition = rowPosition;
        this.withShiftMask = withShiftMask;
        this.withControlMask = withControlMask;
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected ViewportSelectColumnGroupCommand(ViewportSelectColumnGroupCommand command) {
        super(command);
        this.natTableRowPosition = command.natTableRowPosition;
        this.withShiftMask = command.withShiftMask;
        this.withControlMask = command.withControlMask;
    }

    /**
     *
     * @return The row position of the cell that was clicked in the NatTable.
     */
    public int getNatTableRowPosition() {
        return this.natTableRowPosition;
    }

    /**
     *
     * @return whether the shift modifier is active.
     */
    public boolean isWithShiftMask() {
        return this.withShiftMask;
    }

    /**
     *
     * @return whether the control modifier is active.
     */
    public boolean isWithControlMask() {
        return this.withControlMask;
    }

    @Override
    public ViewportSelectColumnGroupCommand cloneCommand() {
        return new ViewportSelectColumnGroupCommand(this);
    }

}