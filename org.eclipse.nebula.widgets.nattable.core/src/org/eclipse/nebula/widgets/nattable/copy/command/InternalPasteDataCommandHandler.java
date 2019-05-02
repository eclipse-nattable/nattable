/*****************************************************************************
 * Copyright (c) 2015, 2019 CEA LIST.
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
package org.eclipse.nebula.widgets.nattable.copy.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.edit.command.EditUtils;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * {@link ILayerCommandHandler} for handling {@link PasteDataCommand}s using the
 * {@link InternalCellClipboard}.
 *
 * @since 1.4
 */
public class InternalPasteDataCommandHandler extends AbstractLayerCommandHandler<PasteDataCommand> {

    protected SelectionLayer selectionLayer;
    protected InternalCellClipboard clipboard;

    /**
     *
     * @param selectionLayer
     *            {@link SelectionLayer} that is needed to determine the
     *            position to paste the values to.
     * @param clipboard
     *            The {@link InternalCellClipboard} that contains the values
     *            that should be pasted.
     */
    public InternalPasteDataCommandHandler(
            SelectionLayer selectionLayer,
            InternalCellClipboard clipboard) {

        this.selectionLayer = selectionLayer;
        this.clipboard = clipboard;
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
                pasteRow++;
                pasteColumn = coord.getColumnPosition();
            }

            postInternalPaste();
        }
        return true;
    }

    /**
     * Checks if the cell at the target coordinates supports the paste operation
     * or not.
     * <p>
     * Note: The coordinates need to be related to the SelectionLayer, otherwise
     * the wrong cell will be used for the check.
     * </p>
     *
     * @param sourceCell
     *            The {@link ILayerCell} that is copied and should be pasted to
     *            the target cell.
     * @param targetCell
     *            The {@link ILayerCell} to which the content of the source cell
     *            should be pasted to.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to access the configuration
     *            values.
     * @return <code>true</code> if the cell supports the paste operation,
     *         <code>false</code> if not
     * @since 1.6
     */
    protected boolean isPasteAllowed(ILayerCell sourceCell, ILayerCell targetCell, IConfigRegistry configRegistry) {
        return EditUtils.isCellEditable(
                new PositionCoordinate(targetCell.getLayer(), targetCell.getColumnPosition(), targetCell.getRowPosition()),
                configRegistry);
    }

    /**
     * Returns the value that should be pasted.
     *
     * @param cell
     *            The {@link ILayerCell} from which to retrieve the value to
     *            paste from.
     * @param pasteColumn
     *            The column position of the cell to paste to.
     * @param pasteRow
     *            The row position of the cell to paste to.
     * @return The value that should be pasted.
     */
    protected Object getPasteValue(ILayerCell cell, int pasteColumn, int pasteRow) {
        return cell != null ? cell.getDataValue() : null;
    }

    /**
     * Identifies the {@link IUniqueIndexLayer} from which the cells are copied.
     *
     * @param copiedCells
     *            The copied cells from the internal clipboard.
     * @return The {@link IUniqueIndexLayer} if the copied cells are collected
     *         from a different layer, or the locally configured
     *         {@link SelectionLayer} in case there are no copied cells in the
     *         internal cell clipboard.
     *
     * @since 1.6
     */
    public IUniqueIndexLayer getPasteLayer(ILayerCell[][] copiedCells) {
        if (copiedCells != null && copiedCells.length > 0 && copiedCells[0].length > 0) {
            for (ILayerCell[] cells : this.clipboard.getCopiedCells()) {
                for (ILayerCell cell : cells) {
                    if (cell != null && cell.getLayer() instanceof IUniqueIndexLayer) {
                        return (IUniqueIndexLayer) cell.getLayer();
                    }
                }
            }
        }
        return this.selectionLayer;
    }

    /**
     * Perform actions prior pasting values from the internal clipboard. E.g.
     * disabling formula evaluation.
     */
    protected void preInternalPaste() {
    }

    /**
     * Perform actions after pasting values from the internal clipboard. E.g.
     * enabling formula evaluation.
     */
    protected void postInternalPaste() {
    }

    @Override
    public Class<PasteDataCommand> getCommandClass() {
        return PasteDataCommand.class;
    }

}
