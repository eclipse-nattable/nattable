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
package org.eclipse.nebula.widgets.nattable.viewport.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractRowCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command to select a row. Note: The row position is in top level composite
 * Layer (NatTable) coordinates
 */
public class ViewportSelectRowCommand extends AbstractRowCommand {

    private final boolean withShiftMask;
    private final boolean withControlMask;

    public ViewportSelectRowCommand(ILayer layer, int rowPosition, boolean withShiftMask, boolean withControlMask) {
        super(layer, rowPosition);
        this.withShiftMask = withShiftMask;
        this.withControlMask = withControlMask;
    }

    protected ViewportSelectRowCommand(ViewportSelectRowCommand command) {
        super(command);
        this.withShiftMask = command.withShiftMask;
        this.withControlMask = command.withControlMask;
    }

    public boolean isWithShiftMask() {
        return this.withShiftMask;
    }

    public boolean isWithControlMask() {
        return this.withControlMask;
    }

    @Override
    public ViewportSelectRowCommand cloneCommand() {
        return new ViewportSelectRowCommand(this);
    }

}
