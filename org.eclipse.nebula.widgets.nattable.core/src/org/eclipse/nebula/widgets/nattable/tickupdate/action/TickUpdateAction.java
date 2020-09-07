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
package org.eclipse.nebula.widgets.nattable.tickupdate.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.tickupdate.command.TickUpdateCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.swt.events.KeyEvent;

/**
 * {@link IKeyAction} that will execute the {@link TickUpdateCommand} with the
 * additional information if the update increments or decrements the current
 * value.
 */
public class TickUpdateAction implements IKeyAction {

    /**
     * Flag to determine whether the current value in the data model should be
     * incremented or decremented.
     */
    private final boolean increment;

    /**
     * @param increment
     *            Flag to determine whether the current value in the data model
     *            should be incremented or decremented.
     */
    public TickUpdateAction(boolean increment) {
        this.increment = increment;
    }

    @Override
    public void run(NatTable natTable, KeyEvent event) {
        natTable.doCommand(new TickUpdateCommand(natTable.getConfigRegistry(),
                this.increment));
    }

}
