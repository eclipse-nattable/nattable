/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 455318
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.copy.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.copy.serializing.CopyDataToClipboardSerializer;
import org.eclipse.nebula.widgets.nattable.copy.serializing.CopyFormattedTextToClipboardSerializer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.serializing.ISerializer;

/**
 * Handler class for copying selected data within the {@link SelectionLayer} to
 * the clipboard. This handler is registered by default with the
 * {@link SelectionLayer}, without references to the header regions. You can
 * override the copy data behaviour by registering an instance of this handler
 * to a layer above the {@link SelectionLayer}. This way the registered custom
 * instance will consume a {@link CopyDataToClipboardCommand} and the registered
 * default handler won't be called.
 */
public class CopyDataCommandHandler extends AbstractLayerCommandHandler<CopyDataToClipboardCommand> {

    /**
     * The {@link SelectionLayer} needed to retrieve the selected data to copy
     * to the clipboard.
     *
     * @since 1.4
     */
    protected final SelectionLayer selectionLayer;
    /**
     * The column header layer of the grid, needed to also copy the column
     * header data.
     */
    private final ILayer columnHeaderLayer;

    /**
     * The row header layer of the grid, needed to also copy the row header
     * data.
     */
    private final ILayer rowHeaderLayer;
    /**
     * The layer in the body region that should be used to copy. Only necessary
     * in case there are layers on top of the {@link SelectionLayer} that
     * introduce additional information, e.g. the TreeLayer for the tree column.
     */
    private IUniqueIndexLayer copyLayer;
    /**
     * Flag to specify which serializer should be used for copying the data.
     * <code>false</code> will use the CopyDataToClipboardSerializer which
     * simply calls <code>toString()</code> to serialize the data to copy,
     * <code>true</code> will use the CopyFormattedTextToClipboardSerializer
     * which will use the configured IDisplayConverter to get the String
     * representation of the value to copy.
     */
    private boolean copyFormattedText;

    /**
     * Creates an instance that only checks the {@link SelectionLayer} for data
     * to add to the clipboard.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} within the NatTable. Can not be
     *            <code>null</code>.
     */
    public CopyDataCommandHandler(SelectionLayer selectionLayer) {
        this(selectionLayer, null, null);
    }

    /**
     * Creates an instance that checks the {@link SelectionLayer} and the header
     * layers if they are given.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} within the NatTable. Can not be
     *            <code>null</code>.
     * @param columnHeaderLayer
     *            The column header layer within the NatTable grid. Can be
     *            <code>null</code>.
     * @param rowHeaderLayer
     *            The row header layer within the NatTable grid. Can be
     *            <code>null</code>.
     */
    public CopyDataCommandHandler(SelectionLayer selectionLayer, ILayer columnHeaderLayer, ILayer rowHeaderLayer) {
        if (selectionLayer == null) {
            throw new IllegalArgumentException("The SelectionLayer can not be null on creating a CopyDataCommandHandler"); //$NON-NLS-1$
        }
        this.selectionLayer = selectionLayer;
        this.columnHeaderLayer = columnHeaderLayer;
        this.rowHeaderLayer = rowHeaderLayer;
    }

    /**
     *
     * @param copyLayer
     *            The layer in the body region that should be used to copy. Only
     *            necessary in case there are layers on top of the
     *            {@link SelectionLayer} that introduce additional information,
     *            e.g. the TreeLayer for the tree column. Setting this to
     *            <code>null</code> will lead to using the
     *            {@link SelectionLayer} for retrieving the cells to copy.
     */
    public void setCopyLayer(IUniqueIndexLayer copyLayer) {
        this.copyLayer = copyLayer;
    }

    /**
     * Specify which serializer to use for copying.
     *
     * @param copyFormattedText
     *            <code>false</code> will use the CopyDataToClipboardSerializer
     *            which simply calls <code>toString()</code> to serialize the
     *            data to copy, <code>true</code> will use the
     *            CopyFormattedTextToClipboardSerializer which will use the
     *            configured IDisplayConverter to get the String representation
     *            of the value to copy
     */
    public void setCopyFormattedText(boolean copyFormattedText) {
        this.copyFormattedText = copyFormattedText;
    }

    @Override
    public boolean doCommand(CopyDataToClipboardCommand command) {
        internalDoCommand(command, assembleCopiedDataStructure());
        return true;
    }

    /**
     * Internal implementation of the command handling that additionally takes
     * the assembled data structure to copy as parameter to avoid multiple
     * assemble operations.
     *
     * @param command
     *            The {@link CopyDataToClipboardCommand} to handle.
     * @param assembledCopiedDataStructure
     *            The assembled data structure to copy.
     *
     * @since 1.6
     */
    protected void internalDoCommand(CopyDataToClipboardCommand command, ILayerCell[][] assembledCopiedDataStructure) {
        ISerializer serializer = this.copyFormattedText
                ? new CopyFormattedTextToClipboardSerializer(assembledCopiedDataStructure, command)
                : new CopyDataToClipboardSerializer(assembledCopiedDataStructure, command);
        serializer.serialize();
    }

    @Override
    public Class<CopyDataToClipboardCommand> getCommandClass() {
        return CopyDataToClipboardCommand.class;
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
    protected ILayerCell[][] assembleCopiedDataStructure() {
        final Set<Range> selectedRows = this.selectionLayer.getSelectedRowPositions();
        final List<ILayerCell[]> copiedCells = new ArrayList<ILayerCell[]>();

        final ILayerCell[][] columnHeaderCells = assembleColumnHeaders();
        for (ILayerCell[] cells : columnHeaderCells) {
            if (!isEmpty(cells)) {
                copiedCells.add(cells);
            }
        }

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
        ILayerCell[] cells = null;
        boolean atLeastOne = false;
        for (int i = 0; i < copiedCells.size(); i++) {
            cells = copiedCells.get(i);
            if (!atLeastOne && !isEmpty(cells)) {
                atLeastOne = true;
            }
            result[i] = cells;
        }
        return atLeastOne ? result : null;
    }

    /**
     * Creates the two dimensional array whose dimensions are calculated based
     * on the selection within the {@link SelectionLayer} and the configured
     * column and row headers. If there is a column header configured for this
     * handler, the column header information will be added to the resulting
     * array in here. If there is no column header configured an empty array
     * with the matching dimensions will be returned.
     *
     * @return A two dimensional array with the dimensions to store the selected
     *         data to copy to the clipboard. Will also contain the column
     *         header information for the copy operation if there is one
     *         configured.
     */
    protected ILayerCell[][] assembleColumnHeaders() {
        // Add offset to rows, remember they need to include the column header
        // rows
        final int rowOffset = this.columnHeaderLayer != null ? this.columnHeaderLayer.getRowCount() : 0;
        final int columnOffset = this.rowHeaderLayer != null ? this.rowHeaderLayer.getColumnCount() : 0;

        final ILayerCell[][] copiedCells = new ILayerCell[this.selectionLayer.getSelectedRowCount() + rowOffset][1];

        if (this.columnHeaderLayer != null) {
            int[] selectedColumnPositions = getSelectedColumnPositions();
            ILayerCell cellToCopy = null;
            for (int i = 0; i < rowOffset; i++) {
                final ILayerCell[] cells = new ILayerCell[selectedColumnPositions.length + columnOffset];
                for (int columnPosition = 0; columnPosition < selectedColumnPositions.length; columnPosition++) {
                    // Pad the width of the vertical layer
                    if (this.copyLayer == null) {
                        cellToCopy = this.columnHeaderLayer.getCellByPosition(selectedColumnPositions[columnPosition], i);
                    } else {
                        int copyColPos = LayerUtil.convertColumnPosition(this.selectionLayer, selectedColumnPositions[columnPosition], this.copyLayer);
                        cellToCopy = this.columnHeaderLayer.getCellByPosition(copyColPos, i);
                    }
                    if (isCopyAllowed(cellToCopy)) {
                        cells[columnPosition + columnOffset] = cellToCopy;
                    }
                }

                copiedCells[i] = cells;
            }
        }

        return copiedCells;
    }

    /**
     * Collects and assembles the selected data per row position that should be
     * copied to the clipboard. If there is a row header layer configured for
     * this handler, the row header cells of the selected row position are also
     * added to the resulting array.
     *
     * @param currentRowPosition
     *            The row position of which the selected cells should be
     *            collected.
     * @return An array containing the selected cells that should be copied to
     *         the clipboard.
     */
    protected ILayerCell[] assembleBody(int currentRowPosition) {
        final int[] selectedColumns = getSelectedColumnPositions();
        final int columnOffset = this.rowHeaderLayer != null ? this.rowHeaderLayer.getColumnCount() : 0;
        final ILayerCell[] bodyCells = new ILayerCell[selectedColumns.length + columnOffset];

        ILayerCell cellToCopy = null;

        if (this.rowHeaderLayer != null) {
            for (int i = 0; i < this.rowHeaderLayer.getColumnCount(); i++) {
                cellToCopy = this.rowHeaderLayer.getCellByPosition(i, currentRowPosition);
                if (isCopyAllowed(cellToCopy)) {
                    bodyCells[i] = cellToCopy;
                }
            }
        }

        for (int columnPosition = 0; columnPosition < selectedColumns.length; columnPosition++) {
            final int selectedColumnPosition = selectedColumns[columnPosition];
            if (this.selectionLayer.isCellPositionSelected(selectedColumnPosition, currentRowPosition)) {
                if (this.copyLayer == null) {
                    cellToCopy = this.selectionLayer.getCellByPosition(selectedColumnPosition, currentRowPosition);
                    if (isCopyAllowed(cellToCopy)) {
                        bodyCells[columnPosition + columnOffset] = cellToCopy;
                    }

                } else {
                    int copyColPos = LayerUtil.convertColumnPosition(this.selectionLayer, selectedColumnPosition, this.copyLayer);
                    int copyRowPos = LayerUtil.convertRowPosition(this.selectionLayer, currentRowPosition, this.copyLayer);

                    cellToCopy = this.copyLayer.getCellByPosition(copyColPos, copyRowPos);
                    if (isCopyAllowed(cellToCopy)) {
                        bodyCells[columnPosition + columnOffset] = cellToCopy;
                    }

                }
            }
        }

        // no empty check because this command handler supports gaps
        return bodyCells;
    }

    /**
     * Returns the array of visible selected column positions. For this it gets
     * all selected column positions, inspects the column width per position and
     * only consider positions whose width is greater than 0.
     *
     * @return Array of visible selected column positions.
     *
     * @since 1.6
     */
    protected int[] getSelectedColumnPositions() {
        List<Integer> selected = new ArrayList<Integer>();
        for (int pos : this.selectionLayer.getSelectedColumnPositions()) {
            if (this.selectionLayer.getColumnWidthByPosition(pos) > 0) {
                selected.add(pos);
            }
        }
        int[] selectedColumns = new int[selected.size()];
        int index = 0;
        for (int pos : selected) {
            selectedColumns[index] = pos;
            index++;
        }
        return selectedColumns;
    }

    /**
     *
     * @return The column header layer of the grid, needed to also copy the
     *         column header data.
     * @since 1.6
     */
    public ILayer getColumnHeaderLayer() {
        return this.columnHeaderLayer;
    }

    /**
     *
     * @return The row header layer of the grid, needed to also copy the row
     *         header data.
     * @since 1.6
     */
    public ILayer getRowHeaderLayer() {
        return this.rowHeaderLayer;
    }

    /**
     *
     * @return The layer in the body region that should be used to copy. Only
     *         necessary in case there are layers on top of the
     *         {@link SelectionLayer} that introduce additional information,
     *         e.g. the TreeLayer for the tree column.
     * @since 1.6
     */
    public IUniqueIndexLayer getCopyLayer() {
        return this.copyLayer;
    }

    /**
     * Checks if the given cell can be copied.
     *
     * @param cellToCopy
     *            The {@link ILayerCell} that should be copied.
     * @return <code>true</code> if the cell can be copied, <code>false</code>
     *         if a copy operation for that cell should be avoided.
     * @since 1.6
     */
    protected boolean isCopyAllowed(ILayerCell cellToCopy) {
        return true;
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
     * @since 1.6
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
}
