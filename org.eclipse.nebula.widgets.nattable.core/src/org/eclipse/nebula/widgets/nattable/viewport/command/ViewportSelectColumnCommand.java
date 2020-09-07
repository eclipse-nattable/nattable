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

import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class ViewportSelectColumnCommand extends AbstractColumnCommand {

    private final boolean withShiftMask;
    private final boolean withControlMask;

    public ViewportSelectColumnCommand(ILayer layer, int columnPosition, boolean withShiftMask, boolean withControlMask) {
        super(layer, columnPosition);
        this.withShiftMask = withShiftMask;
        this.withControlMask = withControlMask;
    }

    protected ViewportSelectColumnCommand(ViewportSelectColumnCommand command) {
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
    public ViewportSelectColumnCommand cloneCommand() {
        return new ViewportSelectColumnCommand(this);
    }

}
