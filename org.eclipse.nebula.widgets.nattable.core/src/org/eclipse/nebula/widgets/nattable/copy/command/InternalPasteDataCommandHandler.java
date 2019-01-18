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
     *            the target coordinates.
     * @param targetColumn
     *            The column position of the cell on the {@link SelectionLayer}
     *            to which the source cell value should be pasted to.
     * @param targetRow
     *            The row position of the cell on the {@link SelectionLayer} to
     *            which the source cell value should be pasted to.
     * @param configRegistry
     *            The {@link IConfigRegistry} needed to access the configuration
     *            values.
     * @return <code>true</code> if the cell supports the paste operation,
     *         <code>false</code> if not
     * @since 1.6
     */
    protected boolean isPasteAllowed(ILayerCell sourceCell, int targetColumn, int targetRow, IConfigRegistry configRegistry) {
        return EditUtils.isCellEditable(
                this.selectionLayer,
                configRegistry,
                new PositionCoordinate(this.selectionLayer, targetColumn, targetRow));
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
        return cell.getDataValue();
    }

    /**
     * Perform actions prior pasting values from the internal clipboard. E.g.
     * disabling formula evaluation.
     */
    protected void preInternalPaste() {}

    /**
     * Perform actions after pasting values from the internal clipboard. E.g.
     * enabling formula evaluation.
     */
    protected void postInternalPaste() {}

    @Override
    public Class<PasteDataCommand> getCommandClass() {
        return PasteDataCommand.class;
    }

}
