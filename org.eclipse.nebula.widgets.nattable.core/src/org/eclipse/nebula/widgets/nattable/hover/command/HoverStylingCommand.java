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

import org.eclipse.nebula.widgets.nattable.command.AbstractPositionCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Command that is used to apply hover styling in a NatTable.
 * <p>
 * This command needs to know about the HoverLayer on which it is executed
 * because there might be several HoverLayer involved in a grid composition and
 * therefore the command might be consumed by the wrong HoverLayer if we
 * wouldn't know about the layer to handle it.
 *
 * @author Dirk Fauth
 *
 * @see HoverLayer
 * @see HoverStylingCommandHandler
 */
public class HoverStylingCommand extends AbstractPositionCommand {

    /**
     * The HoverLayer that should handle the command.
     */
    private final HoverLayer hoverLayer;

    /**
     * @param layer
     *            The layer to which the given cell position coordinates are
     *            related to.
     * @param columnPosition
     *            The column position of the cell to apply the hover styling.
     * @param rowPosition
     *            The row position of the cell to apply the hover styling.
     * @param hoverLayer
     *            The HoverLayer that should handle the command. Necessary to
     *            avoid that other HoverLayer instances in a grid composition
     *            handle and consume the command.
     */
    public HoverStylingCommand(ILayer layer, int columnPosition,
            int rowPosition, HoverLayer hoverLayer) {
        super(layer, columnPosition, rowPosition);
        this.hoverLayer = hoverLayer;
    }

    /**
     * Constructor used for cloning purposes.
     *
     * @param command
     *            The command that should be used to create a new instance.
     */
    protected HoverStylingCommand(HoverStylingCommand command) {
        super(command);
        this.hoverLayer = command.getHoverLayer();
    }

    @Override
    public ILayerCommand cloneCommand() {
        return new HoverStylingCommand(this);
    }

    /**
     * @return The HoverLayer that should handle the command.
     */
    public HoverLayer getHoverLayer() {
        return this.hoverLayer;
    }

}
