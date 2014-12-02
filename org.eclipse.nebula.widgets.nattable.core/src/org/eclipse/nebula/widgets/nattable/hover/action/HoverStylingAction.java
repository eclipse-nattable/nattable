/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hover.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.hover.command.ClearHoverStylingCommand;
import org.eclipse.nebula.widgets.nattable.hover.command.HoverStylingCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.ClearCursorAction;
import org.eclipse.swt.events.MouseEvent;

/**
 * Action that will execute the HoverStylingCommand which applies hover styling
 * in a NatTable.
 * <p>
 * Will also clear any set cursor by default.
 *
 * @author Dirk Fauth
 *
 * @see HoverLayer
 * @see HoverStylingCommand
 */
public class HoverStylingAction extends ClearCursorAction {

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
    public HoverStylingAction(HoverLayer hoverLayer) {
        this.hoverLayer = hoverLayer;
    }

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        // clear the cursor on hovering
        super.run(natTable, event);

        // ensure to clear the hover styling in other possible HoverLayer in the
        // composition
        natTable.doCommand(new ClearHoverStylingCommand(this.hoverLayer));

        int natColumnPos = natTable.getColumnPositionByX(event.x);
        int natRowPos = natTable.getRowPositionByY(event.y);

        natTable.doCommand(new HoverStylingCommand(natTable, natColumnPos,
                natRowPos, this.hoverLayer));
    }
}
