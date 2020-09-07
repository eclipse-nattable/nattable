/*******************************************************************************
 * Copyright (c) 2012, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.persistence.command;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;

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
