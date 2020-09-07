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

import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * Handler class for copying selected data within the {@link SelectionLayer} to
 * the clipboard. Will treat cells with row spanning as a single cell and will
 * not create gaps for rows with no cell to copy.
 *
 * @since 1.6
 */
public class RowSpanningCopyDataCommandHandler extends CopyDataCommandHandler {

    protected InternalCellClipboard clipboard;

    /**
     * Creates an instance that only checks the {@link SelectionLayer} for data
     * to add to the clipboard.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} within the NatTable. Can not be
     *            <code>null</code>.
     * @param clipboard
     *            The {@link InternalCellClipboard} that should be used for
     *            copy/paste operations within a NatTable instance.
     */
    public RowSpanningCopyDataCommandHandler(SelectionLayer selectionLayer, InternalCellClipboard clipboard) {
        super(selectionLayer);
        this.clipboard = clipboard;
    }

    /**
     * Creates an instance that checks the {@link SelectionLayer} and the column
     * header layer if given for data to add to the clipboard.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} within the NatTable. Can not be
     *            <code>null</code>.
     * @param columnHeaderLayer
     *            The column header layer within the NatTable grid. Can be
     *            <code>null</code>.
     * @param clipboard
     *            The {@link InternalCellClipboard} that should be used for
     *            copy/paste operations within a NatTable instance.
     */
    public RowSpanningCopyDataCommandHandler(
            SelectionLayer selectionLayer, ILayer columnHeaderLayer, InternalCellClipboard clipboard) {
        super(selectionLayer, columnHeaderLayer, null);
        this.clipboard = clipboard;
    }

    @Override
    protected void internalDoCommand(CopyDataToClipboardCommand command, ILayerCell[][] assembledCopiedDataStructure) {
        // copy to clipboard
        super.internalDoCommand(command, assembledCopiedDataStructure);

        preInternalCopy();

        // remember cells to copy to support paste
        if (this.clipboard != null) {
            this.clipboard.setCopiedCells(assembledCopiedDataStructure);
        }

        postInternalCopy();
    }

    /**
     * Collects and assembles the selected data per row position that should be
     * copied to the clipboard. For cells with row spanning only the origin cell
     * will be tracked.
     *
     * @param currentRowPosition
     *            The row position of which the selected cells should be
     *            collected.
     * @return An array containing the selected cells that should be copied to
     *         the clipboard.
     */
    @Override
    protected ILayerCell[] assembleBody(int currentRowPosition) {
        final int[] selectedColumns = getSelectedColumnPositions();
        final ILayerCell[] bodyCells = new ILayerCell[selectedColumns.length];

        for (int columnPosition = 0; columnPosition < selectedColumns.length; columnPosition++) {
            final int selectedColumnPosition = selectedColumns[columnPosition];
            if (this.selectionLayer.isCellPositionSelected(selectedColumnPosition, currentRowPosition)) {
                ILayerCell cell = null;
                if (getCopyLayer() == null) {
                    cell = this.selectionLayer.getCellByPosition(selectedColumnPosition, currentRowPosition);
                } else {
                    int copyColPos = LayerUtil.convertColumnPosition(this.selectionLayer, selectedColumnPosition, getCopyLayer());
                    int copyRowPos = LayerUtil.convertRowPosition(this.selectionLayer, currentRowPosition, getCopyLayer());

                    cell = getCopyLayer().getCellByPosition(copyColPos, copyRowPos);
                }

                if (cell != null && cell.getOriginRowPosition() == cell.getRowPosition() && isCopyAllowed(cell)) {
                    bodyCells[columnPosition] = cell;
                }
            }
        }

        if (!isEmpty(bodyCells)) {
            return bodyCells;
        } else {
            return null;
        }
    }

    /**
     * Perform actions prior copying values to the internal clipboard. E.g.
     * disabling formula evaluation.
     */
    protected void preInternalCopy() {
    }

    /**
     * Perform actions after copying values to the internal clipboard. E.g.
     * enabling formula evaluation.
     */
    protected void postInternalCopy() {
        this.selectionLayer.fireLayerEvent(new VisualRefreshEvent(this.selectionLayer));
    }
}
