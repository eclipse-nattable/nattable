/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
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

import org.eclipse.nebula.widgets.nattable.command.AbstractRowCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to trigger the selection of all rows belonging to a row group.
 *
 * @since 1.6
 */
public class ViewportSelectRowGroupCommand extends AbstractRowCommand {

    /**
     * The origin row position which is the starting position of the row group
     * that is selected.
     */
    private int originRowPosition;

    /**
     * The spanning of the row group needed for region selection.
     */
    private final int rowSpan;

    private final boolean withShiftMask;
    private final boolean withControlMask;

    /**
     *
     * @param layer
     *            The {@link ILayer} for the row position reference.
     * @param rowPosition
     *            The row position of the first row in a group according to the
     *            given layer.
     * @param originRowPosition
     *            The origin row position which is the starting position of the
     *            row group that is selected.
     * @param rowSpan
     *            The spanning of the row group needed for region selection.
     * @param withShiftMask
     *            <code>true</code> if the selection should be processed as if
     *            the shift modifier is active.
     * @param withControlMask
     *            <code>true</code> if the selection should be processed as if
     *            the control modifier is active.
     */
    public ViewportSelectRowGroupCommand(
            ILayer layer,
            int rowPosition,
            int originRowPosition,
            int rowSpan,
            boolean withShiftMask,
            boolean withControlMask) {
        super(layer, rowPosition);
        this.originRowPosition = originRowPosition;
        this.rowSpan = rowSpan;
        this.withShiftMask = withShiftMask;
        this.withControlMask = withControlMask;
    }

    /**
     * Clone constructor.
     *
     * @param command
     *            The command to clone.
     */
    protected ViewportSelectRowGroupCommand(ViewportSelectRowGroupCommand command) {
        super(command);
        this.originRowPosition = command.originRowPosition;
        this.rowSpan = command.rowSpan;
        this.withShiftMask = command.withShiftMask;
        this.withControlMask = command.withControlMask;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        int prev = getRowPosition();
        if (super.convertToTargetLayer(targetLayer)) {
            // update the origin row position this way
            // needed to correctly select a row group in a scrolled state
            this.originRowPosition -= (prev - getRowPosition());
            return true;
        }
        return false;
    }

    /**
     *
     * @return The origin row position which is the starting position of the row
     *         group that is selected.
     */
    public int getOriginRowPosition() {
        return this.originRowPosition;
    }

    /**
     *
     * @return The spanning of the row group needed for region selection.
     */
    public int getRowSpan() {
        return this.rowSpan;
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
    public ViewportSelectRowGroupCommand cloneCommand() {
        return new ViewportSelectRowGroupCommand(this);
    }

}