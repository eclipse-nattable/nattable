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
package org.eclipse.nebula.widgets.nattable.hover.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;

/**
 * Command handler that is responsible for handling the HoverStylingCommand.
 * Will set the current hovered cell position to the connected HoverLayer to
 * apply hover styling accordingly.
 *
 * @author Dirk Fauth
 *
 * @see HoverLayer
 * @see HoverStylingCommand
 */
public class HoverStylingCommandHandler extends
        AbstractLayerCommandHandler<HoverStylingCommand> {

    /**
     * The HoverLayer this command handler is connected to.
     */
    private final HoverLayer layer;

    /**
     *
     * @param layer
     *            The HoverLayer this command handler is connected to.
     */
    public HoverStylingCommandHandler(HoverLayer layer) {
        this.layer = layer;
    }

    @Override
    protected boolean doCommand(HoverStylingCommand command) {
        if (this.layer.equals(command.getHoverLayer())) {
            this.layer.setCurrentHoveredCellPosition(
                    command.getColumnPosition(), command.getRowPosition());
            return true;
        }
        return false;
    }

    @Override
    public Class<HoverStylingCommand> getCommandClass() {
        return HoverStylingCommand.class;
    }

}
