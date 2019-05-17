/*******************************************************************************
 * Copyright (c) 2012, 2019 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.group.command.RowGroupExpandCollapseCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseClickAction;
import org.eclipse.swt.events.MouseEvent;

public class RowGroupExpandCollapseAction implements IMouseClickAction {

    private boolean exclusive = true;

    /**
     * Creates a non-exclusive RowGroupExpandCollapseAction.
     */
    public RowGroupExpandCollapseAction() {
        this(false);
    }

    /**
     *
     * @param exclusive
     *            <code>true</code> if the expand action should be exclusive,
     *            which means the action to select the rows is not triggered. If
     *            set to <code>false</code> the selection will be triggered
     *            together with the expand/collapse action.
     *
     * @since 1.6
     */
    public RowGroupExpandCollapseAction(boolean exclusive) {
        this.exclusive = exclusive;
    }

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        RowGroupExpandCollapseCommand command =
                new RowGroupExpandCollapseCommand(
                        natTable,
                        natTable.getRowPositionByY(event.y),
                        natTable.getColumnPositionByX(event.x));
        natTable.doCommand(command);
    }

    @Override
    public boolean isExclusive() {
        return this.exclusive;
    }

}
