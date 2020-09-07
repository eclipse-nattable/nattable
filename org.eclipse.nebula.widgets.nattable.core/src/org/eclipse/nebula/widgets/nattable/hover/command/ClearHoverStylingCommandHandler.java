/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
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
package org.eclipse.nebula.widgets.nattable.hover.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;

/**
 * Command handler that is responsible for handling the
 * ClearHoverStylingCommand. Will clear the current hovered cell position set
 * within the connected HoverLayer to remove the hover styling accordingly.
 *
 * @author Dirk Fauth
 *
 * @see HoverLayer
 * @see ClearHoverStylingCommand
 */
public class ClearHoverStylingCommandHandler extends
        AbstractLayerCommandHandler<ClearHoverStylingCommand> {

    /**
     * The HoverLayer this command handler is connected to.
     */
    private final HoverLayer layer;

    /**
     * @param layer
     *            The HoverLayer this command handler is connected to.
     */
    public ClearHoverStylingCommandHandler(HoverLayer layer) {
        this.layer = layer;
    }

    @Override
    protected boolean doCommand(ClearHoverStylingCommand command) {
        if (command.getHoverLayer() == null) {
            // simply clear the hover styling of the layer this handler is
            // associated to
            this.layer.clearCurrentHoveredCellPosition();
        } else if (!command.getHoverLayer().equals(this.layer)) {
            this.layer.clearCurrentHoveredCellPosition();
        }

        // as there might be more than one HoverLayer involved in the layer
        // composition
        // e.g. in both headers and the body region, we need to ensure that this
        // command
        // is fired further down the layer stack
        return false;
    }

    @Override
    public Class<ClearHoverStylingCommand> getCommandClass() {
        return ClearHoverStylingCommand.class;
    }

}
