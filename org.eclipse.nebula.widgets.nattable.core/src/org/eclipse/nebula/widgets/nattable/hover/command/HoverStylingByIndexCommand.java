/*******************************************************************************
 * Copyright (c) 2022 Dirk Fauth and others.
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

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;

/**
 * Command that is used to apply hover styling in a NatTable.
 * <p>
 * This command needs to know about the HoverLayer on which it is executed
 * because there might be several HoverLayer involved in a grid composition and
 * therefore the command might be consumed by the wrong HoverLayer if we
 * wouldn't know about the layer to handle it.
 *
 * @see HoverLayer
 * @see HoverStylingByIndexCommandHandler
 *
 * @since 2.1
 */
public class HoverStylingByIndexCommand extends AbstractContextFreeCommand {

    /**
     * The column index of the cell to apply the hover styling.
     */
    private final int columnIndex;
    /**
     * The row index of the cell to apply the hover styling.
     */
    private final int rowIndex;

    /**
     * The HoverLayer that should handle the command.
     */
    private final HoverLayer hoverLayer;

    /**
     * @param columnIndex
     *            The column index of the cell to apply the hover styling.
     * @param rowIndex
     *            The row index of the cell to apply the hover styling.
     * @param hoverLayer
     *            The HoverLayer that should handle the command. Necessary to
     *            avoid that other HoverLayer instances in a grid composition
     *            handle and consume the command.
     */
    public HoverStylingByIndexCommand(int columnIndex, int rowIndex, HoverLayer hoverLayer) {
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
        this.hoverLayer = hoverLayer;
    }

    /**
     * Constructor used for cloning purposes.
     *
     * @param command
     *            The command that should be used to create a new instance.
     */
    protected HoverStylingByIndexCommand(HoverStylingByIndexCommand command) {
        this.columnIndex = command.getColumnIndex();
        this.rowIndex = command.getRowIndex();
        this.hoverLayer = command.getHoverLayer();
    }

    @Override
    public HoverStylingByIndexCommand cloneCommand() {
        return new HoverStylingByIndexCommand(this);
    }

    /**
     * @return The column index of the cell to apply the hover styling.
     */
    public int getColumnIndex() {
        return this.columnIndex;
    }

    /**
     * @return The row index of the cell to apply the hover styling.
     */
    public int getRowIndex() {
        return this.rowIndex;
    }

    /**
     * @return The HoverLayer that should handle the command.
     */
    public HoverLayer getHoverLayer() {
        return this.hoverLayer;
    }

}
