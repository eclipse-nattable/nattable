/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hierarchical.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.hierarchical.command.HierarchicalTreeExpandCollapseCommand;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.swt.events.MouseEvent;

/**
 * {@link IMouseAction} that triggers a
 * {@link HierarchicalTreeExpandCollapseCommand} for the clicked cell position
 * to the specified level. By default registered on click on the tree
 * expand/collapse icon.
 *
 * @since 1.6
 */
public class HierarchicalTreeExpandCollapseAction implements IMouseAction {

    private int toLevel;

    /**
     * Creates the {@link HierarchicalTreeExpandCollapseAction} that only
     * expand/collapse the node at the coordinate where the action was
     * triggered.
     */
    public HierarchicalTreeExpandCollapseAction() {
        this.toLevel = -1;
    }

    /**
     * Creates a {@link HierarchicalTreeExpandCollapseAction} that
     * expand/collapse a node to the given level.
     *
     * @param toLevel
     *            The level to which a node should be expanded.
     */
    public HierarchicalTreeExpandCollapseAction(int toLevel) {
        this.toLevel = toLevel;
    }

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        int c = natTable.getColumnPositionByX(event.x);
        int r = natTable.getRowPositionByY(event.y);
        ILayerCell cell = natTable.getCellByPosition(c, r);
        int rowIndex = cell.getLayer().getRowIndexByPosition(cell.getOriginRowPosition());
        int columnIndex = cell.getLayer().getColumnIndexByPosition(cell.getOriginColumnPosition());
        HierarchicalTreeExpandCollapseCommand command =
                new HierarchicalTreeExpandCollapseCommand(rowIndex, columnIndex, this.toLevel);
        natTable.doCommand(command);
    }
}