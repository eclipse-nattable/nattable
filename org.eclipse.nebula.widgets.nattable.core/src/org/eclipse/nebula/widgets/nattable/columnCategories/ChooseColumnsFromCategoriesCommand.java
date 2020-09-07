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
package org.eclipse.nebula.widgets.nattable.columnCategories;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.swt.widgets.Shell;

public class ChooseColumnsFromCategoriesCommand extends AbstractContextFreeCommand {

    private final NatTable natTable;

    public ChooseColumnsFromCategoriesCommand(NatTable natTable) {
        this.natTable = natTable;
    }

    public NatTable getNatTable() {
        return this.natTable;
    }

    public Shell getShell() {
        return this.natTable.getShell();
    }

}
