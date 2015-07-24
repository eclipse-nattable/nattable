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
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;

public class AbstractMouseSelectionAction implements IMouseAction {

    private boolean withShiftMask;
    private boolean withControlMask;
    private int gridColumnPosition;
    private int gridRowPosition;

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        this.withShiftMask = (event.stateMask & SWT.MOD2) != 0;
        this.withControlMask = (event.stateMask & SWT.MOD1) != 0;

        this.gridColumnPosition = natTable.getColumnPositionByX(event.x);
        this.gridRowPosition = natTable.getRowPositionByY(event.y);

        natTable.forceFocus();
    }

    public boolean isWithShiftMask() {
        return this.withShiftMask;
    }

    public boolean isWithControlMask() {
        return this.withControlMask;
    }

    public int getGridColumnPosition() {
        return this.gridColumnPosition;
    }

    public int getGridRowPosition() {
        return this.gridRowPosition;
    }
}
