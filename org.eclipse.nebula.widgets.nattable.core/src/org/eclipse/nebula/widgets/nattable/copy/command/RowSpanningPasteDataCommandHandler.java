/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.copy.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * {@link ILayerCommandHandler} for handling {@link PasteDataCommand}s using the
 * {@link InternalCellClipboard}. Will treat cells with row spanning as a single
 * cell.
 *
 * @since 1.6
 */
public class RowSpanningPasteDataCommandHandler extends InternalPasteDataCommandHandler {

    /**
     *
     * @param selectionLayer
     *            {@link SelectionLayer} that is needed to determine the
     *            position to paste the values to.
     * @param clipboard
     *            The {@link InternalCellClipboard} that contains the values
     *            that should be pasted.
     */
    public RowSpanningPasteDataCommandHandler(
            SelectionLayer selectionLayer,
            InternalCellClipboard clipboard) {
        super(selectionLayer, clipboard);
    }

    @Override
    protected boolean doCommand(PasteDataCommand command) {
        if (this.clipboard.getCopiedCells() != null) {

            preInternalPaste();

            PositionCoordinate coord = this.selectionLayer.getSelectionAnchor();
            int pasteColumn = coord.getColumnPosition();
            int pasteRow = coord.getRowPosition();

            for (ILayerCell[] cells : this.clipboard.getCopiedCells()) {
                for (ILayerCell cell : cells) {
                    if (isPasteAllowed(cell, pasteColumn, pasteRow, command.configRegistry)) {
                        this.selectionLayer.doCommand(
                                new UpdateDataCommand(
                                        this.selectionLayer,
                                        pasteColumn,
                                        pasteRow,
                                        getPasteValue(cell, pasteColumn, pasteRow)));
                    }

                    pasteColumn++;

                    if (pasteColumn >= this.selectionLayer.getColumnCount()) {
                        break;
                    }
                }
                ILayerCell targetCell = this.selectionLayer.getCellByPosition(coord.getColumnPosition(), coord.getRowPosition());
                pasteRow += targetCell.getRowSpan();
                pasteColumn = coord.getColumnPosition();
            }

            postInternalPaste();
        }
        return true;
    }

}
