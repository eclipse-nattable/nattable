/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        int column = CellEdgeDetectUtil.getColumnPositionToResize(natTable,
                clickPoint);

        InitializeAutoResizeColumnsCommand command = new InitializeAutoResizeColumnsCommand(
                natTable, column, natTable.getConfigRegistry(), new GCFactory(
                        natTable));
        natTable.doCommand(command);
    }

}
