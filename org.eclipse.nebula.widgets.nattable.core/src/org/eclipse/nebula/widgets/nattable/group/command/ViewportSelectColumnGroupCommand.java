/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
     * The origin column position which is the starting position of the column
     * group that is selected.
     */
    private int originColumnPosition;

    /**
     * The spanning of the column group needed for region selection.
     */
    private final int columnSpan;

    private final boolean withShiftMask;
    private final boolean withControlMask;

    /**
     *
     * @param layer
     *            The {@link ILayer} for the column position reference.
     * @param columnPosition
     *            The column position of the first column in a group according
     *            to the given layer.
     * @param originColumnPosition
     *            The origin column position which is the starting position of
     *            the column group that is selected.
     * @param columnSpan
     *            The spanning of the column group needed for region selection.
     * @param withShiftMask
     *            <code>true</code> if the selection should be processed as if
     *            the shift modifier is active.
     * @param withControlMask
     *            <code>true</code> if the selection should be processed as if
     *            the control modifier is active.
     */
    public ViewportSelectColumnGroupCommand(
            ILayer layer,
            int columnPosition,
            int originColumnPosition,
            int columnSpan,
            boolean withShiftMask,
            boolean withControlMask) {
        super(layer, columnPosition);
        this.originColumnPosition = originColumnPosition;
        this.columnSpan = columnSpan;
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
        this.originColumnPosition = command.originColumnPosition;
        this.columnSpan = command.columnSpan;
        this.withShiftMask = command.withShiftMask;
        this.withControlMask = command.withControlMask;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        int prev = getColumnPosition();
        if (super.convertToTargetLayer(targetLayer)) {
            // update the origin column position this way
            // needed to correctly select a column group in a scrolled state
            this.originColumnPosition -= (prev - getColumnPosition());
            return true;
        }
        return false;
    }

    /**
     *
     * @return The origin column position which is the starting position of the
     *         column group that is selected.
     */
    public int getOriginColumnPosition() {
        return this.originColumnPosition;
    }

    /**
     *
     * @return The spanning of the column group needed for region selection.
     */
    public int getColumnSpan() {
        return this.columnSpan;
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