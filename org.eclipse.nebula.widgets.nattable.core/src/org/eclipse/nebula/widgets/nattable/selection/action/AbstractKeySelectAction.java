/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 459029
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

public abstract class AbstractKeySelectAction implements IKeyAction {

    private boolean shiftMask = false;
    private boolean controlMask = false;
    private boolean isStateMaskSpecified = false;
    private final MoveDirectionEnum direction;

    public AbstractKeySelectAction(MoveDirectionEnum direction) {
        this.direction = direction;
    }

    public AbstractKeySelectAction(MoveDirectionEnum direction, boolean shiftMask, boolean ctrlMask) {
        this.direction = direction;
        this.shiftMask = shiftMask;
        this.controlMask = ctrlMask;
        this.isStateMaskSpecified = true;
    }

    @Override
    public void run(NatTable natTable, KeyEvent event) {
        if (!this.isStateMaskSpecified) {
            this.shiftMask = (event.stateMask & SWT.MOD2) != 0;
            this.controlMask = (event.stateMask & SWT.MOD1) != 0;
        }
    }

    protected boolean isShiftMask() {
        return this.shiftMask;
    }

    protected boolean isControlMask() {
        return this.controlMask;
    }

    public void setShiftMask(boolean shiftMask) {
        this.shiftMask = shiftMask;
    }

    public void setControlMask(boolean controlMask) {
        this.controlMask = controlMask;
    }

    public MoveDirectionEnum getDirection() {
        return this.direction;
    }

}
