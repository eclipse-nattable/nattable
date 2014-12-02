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
