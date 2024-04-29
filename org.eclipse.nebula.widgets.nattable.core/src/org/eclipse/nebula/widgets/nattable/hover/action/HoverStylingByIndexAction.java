/*******************************************************************************
 * Copyright (c) 2022, 2024 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hover.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.hover.command.HoverStylingByIndexCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.swt.events.MouseEvent;

/**
 * Action that will execute the HoverStylingByIndexCommand which applies hover
 * styling in a NatTable.
 * <p>
 * Will also clear any set cursor by default.
 *
 * @see HoverLayer
 * @see HoverStylingByIndexCommand
 *
 * @since 2.1
 */
public class HoverStylingByIndexAction implements IMouseAction {

    /**
     * The HoverLayer that is responsible for handling the hover styling
     * command.
     */
    private final HoverLayer hoverLayer;

    /**
     * @param hoverLayer
     *            The HoverLayer that is responsible for handling the hover
     *            styling command. Necessary to avoid that other HoverLayer
     *            instances in a grid composition handle and consume the
     *            command.
     */
    public HoverStylingByIndexAction(HoverLayer hoverLayer) {
        this.hoverLayer = hoverLayer;
    }

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        int natColumnPos = natTable.getColumnPositionByX(event.x);
        int natRowPos = natTable.getRowPositionByY(event.y);

        natTable.doCommand(
                new HoverStylingByIndexCommand(
                        natTable.getColumnIndexByPosition(natColumnPos),
                        natTable.getRowIndexByPosition(natRowPos),
                        this.hoverLayer));
    }
}
