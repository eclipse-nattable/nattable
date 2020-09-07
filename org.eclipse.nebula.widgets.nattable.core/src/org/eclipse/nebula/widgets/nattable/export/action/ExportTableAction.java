/*******************************************************************************
 * Copyright (c) 2017, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Thanh Liem PHAN (ALL4TEC) <thanhliem.phan@all4tec.net> - Bug 509361
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.export.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.export.command.ExportTableCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.swt.events.KeyEvent;

/**
 * Action class to run the {@link ExportTableCommand}.
 *
 * @since 1.5
 */
public class ExportTableAction implements IKeyAction {

    @Override
    public void run(NatTable natTable, KeyEvent event) {
        natTable.doCommand(new ExportTableCommand(natTable.getConfigRegistry(), natTable.getShell()));
    }

}
