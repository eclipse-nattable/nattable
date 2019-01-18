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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * Handler class for copying selected data within the {@link SelectionLayer} to
 * the clipboard. Will treat cells with row spanning as a single cell.
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

    @Override
    public boolean doCommand(CopyDataToClipboardCommand command) {
        // copy to clipboard
        super.doCommand(command);

        preInternalCopy();

        // remember cells to copy to support paste
        if (this.clipboard != null) {
            this.clipboard.setCopiedCells(assembleCopiedDataStructure());
        }

        postInternalCopy();

        return true;
    }

    /**
     * Collects and assembles the selected data that should be copied to the
     * clipboard.
     *
     * @return A two dimensional array containing the selected cells to copy to
     *         the clipboard. The first level of this array represent the row
     *         positions of the cells, while the second level contains the cells
     *         itself based on the column position.
     */
    @Override
    protected ILayerCell[][] assembleCopiedDataStructure() {
        final Set<Range> selectedRows = this.selectionLayer.getSelectedRowPositions();

        List<ILayerCell[]> copiedCells = new ArrayList<ILayerCell[]>();

        // cleanup the row positions to copy
        // this is needed because taking only the Range.start into account leads
        // to overriding values in the array instead of adding if there are
        // multiple Ranges returned
        Set<Integer> selectedRowPositions = new TreeSet<Integer>();
        for (Range range : selectedRows) {
            for (int rowPosition = range.start; rowPosition < range.end; rowPosition++) {
                if (this.selectionLayer.getRowHeightByPosition(rowPosition) > 0) {
                    selectedRowPositions.add(rowPosition);
                }
            }
        }

        for (int rowPos : selectedRowPositions) {
            ILayerCell[] body = assembleBody(rowPos);
            if (body != null) {
                copiedCells.add(body);
            }
        }

        ILayerCell[][] result = new ILayerCell[copiedCells.size()][1];
        for (int i = 0; i < copiedCells.size(); i++) {
            result[i] = copiedCells.get(i);
        }
        return result;
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

                if (cell != null && cell.getOriginRowPosition() == cell.getRowPosition()) {
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
     * Checks if the given array contains a value or if it only contains
     * <code>null</code> values. If all array positions point to
     * <code>null</code>it is considered to be empty.
     *
     * @param layerCells
     *            The array to check.
     * @return <code>true</code> if all values in the array are
     *         <code>null</code>, <code>false</code> if at least one real value
     *         is contained.
     */
    protected boolean isEmpty(ILayerCell[] layerCells) {
        if (layerCells != null) {
            for (ILayerCell cell : layerCells) {
                if (cell != null) {
                    return false;
                }
            }
        }
        return true;
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
