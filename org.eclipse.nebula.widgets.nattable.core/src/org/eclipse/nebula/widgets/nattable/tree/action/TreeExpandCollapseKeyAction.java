/*******************************************************************************
 * Copyright (c) 2014 Roman Flueckiger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Roman Flueckiger <roman.flueckiger@mac.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.tree.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandCollapseCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.swt.events.KeyEvent;

/**
 * This action toggles the state of a tree node (expanded &lt;-&gt; collapsed)
 * located at the current selection anchor position. It has the same effect as
 * {@link TreeExpandCollapseAction}.
 */
public class TreeExpandCollapseKeyAction implements IKeyAction {

    protected final SelectionLayer selectionLayer;

    /**
     * Create a {@link TreeExpandCollapseKeyAction} configured with the given
     * {@link SelectionLayer}.
     *
     * @param selectionLayer
     *            the action will be run based on the selection anchor of the
     *            given {@link SelectionLayer}.
     */
    public TreeExpandCollapseKeyAction(SelectionLayer selectionLayer) {
        if (selectionLayer == null) {
            throw new IllegalArgumentException("selectionLayer must not be null."); //$NON-NLS-1$
        }

        this.selectionLayer = selectionLayer;
    }

    @Override
    public void run(NatTable natTable, KeyEvent event) {
        PositionCoordinate anchorPosition = this.selectionLayer.getSelectionAnchor();

        if (anchorPosition.rowPosition != SelectionLayer.NO_SELECTION && anchorPosition.columnPosition != SelectionLayer.NO_SELECTION) {
            int rowIndex = this.selectionLayer.getRowIndexByPosition(anchorPosition.rowPosition);

            TreeExpandCollapseCommand command = new TreeExpandCollapseCommand(rowIndex);
            natTable.doCommand(command);
        }
    }

}
