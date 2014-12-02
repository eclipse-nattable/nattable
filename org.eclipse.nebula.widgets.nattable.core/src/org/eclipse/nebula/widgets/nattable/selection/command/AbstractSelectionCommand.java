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

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;

public abstract class AbstractSelectionCommand extends
        AbstractContextFreeCommand {

    private boolean shiftMask;
    private boolean controlMask;

    public AbstractSelectionCommand(boolean shiftMask, boolean controlMask) {
        this.shiftMask = shiftMask;
        this.controlMask = controlMask;
    }

    public boolean isShiftMask() {
        return this.shiftMask;
    }

    public boolean isControlMask() {
        return this.controlMask;
    }

}
