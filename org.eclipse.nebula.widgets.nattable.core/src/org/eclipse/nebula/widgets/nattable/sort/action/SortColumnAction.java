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
package org.eclipse.nebula.widgets.nattable.sort.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.sort.command.SortColumnCommand;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.swt.events.MouseEvent;

public class SortColumnAction implements IMouseAction {

    private final boolean accumulate;

    public SortColumnAction(boolean accumulate) {
        this.accumulate = accumulate;
    }

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        int columnPosition = ((NatEventData) event.data).getColumnPosition();
        natTable.doCommand(new SortColumnCommand(natTable, columnPosition, this.accumulate));
    }
}
