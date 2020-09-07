/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.copy.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
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

            IUniqueIndexLayer pasteLayer = getPasteLayer(this.clipboard.getCopiedCells());
            if (pasteLayer != this.selectionLayer) {
                // if the paste layer is not the SelectionLayer we need to
                // perform a conversion
                pasteColumn = LayerUtil.convertColumnPosition(this.selectionLayer, pasteColumn, pasteLayer);
                pasteRow = LayerUtil.convertRowPosition(this.selectionLayer, pasteRow, pasteLayer);
                coord = new PositionCoordinate(pasteLayer, pasteColumn, pasteRow);
            }

            for (ILayerCell[] cells : this.clipboard.getCopiedCells()) {
                for (ILayerCell cell : cells) {
                    ILayerCell targetCell = pasteLayer.getCellByPosition(pasteColumn, pasteRow);

                    if (isPasteAllowed(cell, targetCell, command.configRegistry)) {
                        pasteLayer.doCommand(
                                new UpdateDataCommand(
                                        pasteLayer,
                                        pasteColumn,
                                        pasteRow,
                                        getPasteValue(cell, pasteColumn, pasteRow)));
                    }

                    pasteColumn++;

                    if (pasteColumn >= pasteLayer.getColumnCount()) {
                        break;
                    }
                }
                ILayerCell targetCell = pasteLayer.getCellByPosition(coord.getColumnPosition(), pasteRow);
                pasteRow += targetCell.getRowSpan();
                pasteColumn = coord.getColumnPosition();
            }

            postInternalPaste();
        }
        return true;
    }

}
