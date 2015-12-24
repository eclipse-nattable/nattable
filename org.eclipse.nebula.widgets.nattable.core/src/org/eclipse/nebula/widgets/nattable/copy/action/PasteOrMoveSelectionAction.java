/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
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
package org.eclipse.nebula.widgets.nattable.copy.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.copy.command.PasteDataCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.action.MoveSelectionAction;
import org.eclipse.swt.events.KeyEvent;

/**
 * Action implementation that performs a {@link PasteDataCommand} if there are
 * values in the {@link InternalCellClipboard}, otherwise it performs a
 * selection movement.
 *
 * @since 1.4
 */
public class PasteOrMoveSelectionAction extends MoveSelectionAction {

    private InternalCellClipboard clipboard;

    /**
     * Creates the action with {@link MoveDirectionEnum#DOWN}
     *
     * @param clipboard
     *            The clipboard that is used to check if a paste operation
     *            should be performed.
     */
    public PasteOrMoveSelectionAction(InternalCellClipboard clipboard) {
        this(clipboard, MoveDirectionEnum.DOWN);
    }

    /**
     * Creates the action with the given {@link MoveDirectionEnum}.
     *
     * @param clipboard
     *            The clipboard that is used to check if a paste operation
     *            should be performed.
     * @param direction
     *            The direction to move if no paste operation is performed.
     */
    public PasteOrMoveSelectionAction(InternalCellClipboard clipboard, MoveDirectionEnum direction) {
        super(direction);
        this.clipboard = clipboard;
    }

    @Override
    public void run(NatTable natTable, KeyEvent event) {
        if (this.clipboard.getCopiedCells() != null) {

            natTable.doCommand(new PasteDataCommand(natTable.getConfigRegistry()));

            this.clipboard.clear();
        } else {
            super.run(natTable, event);
        }
    }

}
