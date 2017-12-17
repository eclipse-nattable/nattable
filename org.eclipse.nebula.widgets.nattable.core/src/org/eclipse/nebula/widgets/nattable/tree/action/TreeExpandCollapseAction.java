/*******************************************************************************
 * Copyright (c) 2012, 2017 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tree.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandCollapseCommand;
import org.eclipse.nebula.widgets.nattable.tree.config.DefaultTreeLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.swt.events.MouseEvent;

/**
 * {@link IMouseAction} that triggers a {@link TreeExpandCollapseCommand} for
 * the clicked cell position. By default registered on click on the tree
 * expand/collapse icon.
 *
 * @see DefaultTreeLayerConfiguration
 */
public class TreeExpandCollapseAction implements IMouseAction {

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        int c = natTable.getColumnPositionByX(event.x);
        int r = natTable.getRowPositionByY(event.y);
        ILayerCell cell = natTable.getCellByPosition(c, r);
        int rowIndex = cell.getLayer().getRowIndexByPosition(cell.getOriginRowPosition());
        int columnIndex = cell.getLayer().getColumnIndexByPosition(cell.getOriginColumnPosition());
        TreeExpandCollapseCommand command = new TreeExpandCollapseCommand(rowIndex, columnIndex);
        natTable.doCommand(command);
    }
}
