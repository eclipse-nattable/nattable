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
package org.eclipse.nebula.widgets.nattable.selection.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractPositionCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Event indicating that the user has selected a specific cell in the data grid.
 * This command should be used for implementing all selection handling by
 * layers.
 *
 * <strong>Note that this command takes a Grid PositionCoordinate describing a
 * cell on the screen on which the user has clicked. Do not pass it anything
 * else or you will introduce very subtle and very difficult to debug bugs into
 * the code and then we will have to pay you a visit on one random Sunday
 * morning when you least expect it.</strong>
 */
public class SelectCellCommand extends AbstractPositionCommand {

    private boolean shiftMask;
    private boolean controlMask;
    private boolean forcingEntireCellIntoViewport = false;

    public SelectCellCommand(ILayer layer, int columnPosition, int rowPosition,
            boolean shiftMask, boolean controlMask) {
        super(layer, columnPosition, rowPosition);
        this.shiftMask = shiftMask;
        this.controlMask = controlMask;
    }

    protected SelectCellCommand(SelectCellCommand command) {
        super(command);
        this.shiftMask = command.shiftMask;
        this.controlMask = command.controlMask;
        this.forcingEntireCellIntoViewport = command.forcingEntireCellIntoViewport;
    }

    public boolean isShiftMask() {
        return this.shiftMask;
    }

    public boolean isControlMask() {
        return this.controlMask;
    }

    public boolean isForcingEntireCellIntoViewport() {
        return this.forcingEntireCellIntoViewport;
    }

    public void setForcingEntireCellIntoViewport(
            boolean forcingEntireCellIntoViewport) {
        this.forcingEntireCellIntoViewport = forcingEntireCellIntoViewport;
    }

    @Override
    public SelectCellCommand cloneCommand() {
        return new SelectCellCommand(this);
    }

}
