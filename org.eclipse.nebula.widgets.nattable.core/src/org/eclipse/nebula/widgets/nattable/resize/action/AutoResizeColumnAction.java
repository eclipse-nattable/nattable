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
package org.eclipse.nebula.widgets.nattable.resize.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeDetectUtil;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;

public class AutoResizeColumnAction implements IMouseAction {

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        Point clickPoint = new Point(event.x, event.y);
        int column = CellEdgeDetectUtil.getColumnPositionToResize(natTable, clickPoint);

        InitializeAutoResizeColumnsCommand command =
                new InitializeAutoResizeColumnsCommand(
                        natTable,
                        column,
                        natTable.getConfigRegistry(),
                        new GCFactory(natTable));
        natTable.doCommand(command);
    }

}
