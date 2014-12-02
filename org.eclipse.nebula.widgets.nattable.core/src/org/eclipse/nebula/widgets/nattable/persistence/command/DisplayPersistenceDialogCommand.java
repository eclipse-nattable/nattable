/*******************************************************************************
 * Copyright (c) 2012 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.persistence.command;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;

/**
 * @author Dirk Fauth
 *
 */
public class DisplayPersistenceDialogCommand extends AbstractContextFreeCommand {

    /**
     * The NatTable instance to call the PersistenceDialog for.
     */
    private final NatTable natTable;

    /**
     *
     * @param natTable
     *            The NatTable instance to call the PersistenceDialog for.
     */
    public DisplayPersistenceDialogCommand(NatTable natTable) {
        this.natTable = natTable;
    }

    /**
     * @return The NatTable instance to call the PersistenceDialog for.
     */
    public NatTable getNatTable() {
        return this.natTable;
    }

}
