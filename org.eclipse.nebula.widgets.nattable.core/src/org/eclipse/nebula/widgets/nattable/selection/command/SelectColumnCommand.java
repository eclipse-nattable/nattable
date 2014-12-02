/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
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

public class SelectColumnCommand extends AbstractPositionCommand {

    private final boolean withShiftMask;
    private final boolean withControlMask;

    public SelectColumnCommand(ILayer layer, int columnPosition,
            int rowPosition, boolean withShiftMask, boolean withControlMask) {
        super(layer, columnPosition, rowPosition);
        this.withShiftMask = withShiftMask;
        this.withControlMask = withControlMask;
    }

    protected SelectColumnCommand(SelectColumnCommand command) {
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
    public SelectColumnCommand cloneCommand() {
        return new SelectColumnCommand(this);
    }

}
