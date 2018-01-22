/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.command.MoveSelectionCommand;
import org.eclipse.swt.events.KeyEvent;

/**
 * Action that is used to perform a selection movement on pressing the END key.
 *
 * @since 1.6
 */
public class MoveToEndAction extends AbstractKeySelectAction {

    public MoveToEndAction() {
        super(MoveDirectionEnum.RIGHT);
    }

    @Override
    public void run(NatTable natTable, KeyEvent event) {
        super.run(natTable, event);

        if (!isControlMask()) {
            // no CTRL key pressed, simply move selection to the first column
            natTable.doCommand(
                    new MoveSelectionCommand(
                            MoveDirectionEnum.RIGHT,
                            SelectionLayer.MOVE_ALL,
                            isShiftMask(),
                            isControlMask()));
        } else {
            // if the CTRL key is pressed, we need to move the selection to the first cell
            natTable.doCommand(
                    new MoveSelectionCommand(
                            MoveDirectionEnum.RIGHT,
                            SelectionLayer.MOVE_ALL,
                            isShiftMask(),
                            false));
            natTable.doCommand(
                    new MoveSelectionCommand(
                            MoveDirectionEnum.DOWN,
                            SelectionLayer.MOVE_ALL,
                            isShiftMask(),
                            false));
        }
    }

}
