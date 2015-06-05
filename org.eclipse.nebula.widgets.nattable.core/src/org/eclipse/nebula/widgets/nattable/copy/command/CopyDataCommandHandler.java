/*******************************************************************************
 * Copyright (c) 2012, 2013, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 455318
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.copy.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    private final ILayer columnHeaderDataLayer;
    /**
     * The row header layer of the grid, needed to also copy the row header
     * data.
     */
    private final ILayer rowHeaderDataLayer;
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
     * @param columnHeaderDataLayer
     *            The column header data layer within the NatTable grid. Can be
     *            <code>null</code>.
     * @param rowHeaderDataLayer
     *            The row header data layer within the NatTable grid. Can be
     *            <code>null</code>.
     */
    public CopyDataCommandHandler(SelectionLayer selectionLayer,
            ILayer columnHeaderDataLayer, ILayer rowHeaderDataLayer) {
        assert selectionLayer != null : "The SelectionLayer can not be null on creating a CopyDataCommandHandler"; //$NON-NLS-1$
        this.selectionLayer = selectionLayer;
        this.columnHeaderDataLayer = columnHeaderDataLayer;
        this.rowHeaderDataLayer = rowHeaderDataLayer;
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
        ISerializer serializer = this.copyFormattedText
                ? new CopyFormattedTextToClipboardSerializer(assembleCopiedDataStructure(), command)
        : new CopyDataToClipboardSerializer(assembleCopiedDataStructure(), command);
                serializer.serialize();
                return true;
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
        final ILayerCell[][] copiedCells = assembleColumnHeaders();

        // cleanup the row positions to copy
        // this is needed because taking only the Range.start into account leads
        // to overriding values in the array instead of adding if there are
        // multiple Ranges returned
        List<Integer> selectedRowPositions = new ArrayList<Integer>();
        for (Range range : selectedRows) {
            for (int rowPosition = range.start; rowPosition < range.end; rowPosition++) {
                selectedRowPositions.add(rowPosition);
            }
        }
        // ensure the correct order as a Set is not ordered at all and we want
        // to paste the values in the same order we copied them.
        Collections.sort(selectedRowPositions);

        final int rowOffset = this.columnHeaderDataLayer != null ? this.columnHeaderDataLayer.getRowCount() : 0;
        for (int i = 0; i < selectedRowPositions.size(); i++) {
            Integer rowPos = selectedRowPositions.get(i);
            copiedCells[i + rowOffset] = assembleBody(rowPos);
        }

        return copiedCells;
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
        final int rowOffset = this.columnHeaderDataLayer != null ? this.columnHeaderDataLayer.getRowCount() : 0;
        final int columnOffset = this.rowHeaderDataLayer != null ? this.rowHeaderDataLayer.getColumnCount() : 0;

        final ILayerCell[][] copiedCells = new ILayerCell[this.selectionLayer.getSelectedRowCount() + rowOffset][1];

        if (this.columnHeaderDataLayer != null) {
            int[] selectedColumnPositions = this.selectionLayer.getSelectedColumnPositions();
            for (int i = 0; i < rowOffset; i++) {
                final ILayerCell[] cells = new ILayerCell[selectedColumnPositions.length + columnOffset];
                for (int columnPosition = 0; columnPosition < selectedColumnPositions.length; columnPosition++) {
                    // Pad the width of the vertical layer
                    cells[columnPosition + columnOffset] =
                            this.columnHeaderDataLayer.getCellByPosition(selectedColumnPositions[columnPosition], i);
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
        final int[] selectedColumns = this.selectionLayer.getSelectedColumnPositions();
        final int columnOffset = this.rowHeaderDataLayer != null ? this.rowHeaderDataLayer.getColumnCount() : 0;
        final ILayerCell[] bodyCells = new ILayerCell[selectedColumns.length + columnOffset];

        if (this.rowHeaderDataLayer != null) {
            for (int i = 0; i < this.rowHeaderDataLayer.getColumnCount(); i++) {
                bodyCells[i] = this.rowHeaderDataLayer.getCellByPosition(i, currentRowPosition);
            }
        }

        for (int columnPosition = 0; columnPosition < selectedColumns.length; columnPosition++) {
            final int selectedColumnPosition = selectedColumns[columnPosition];
            if (this.selectionLayer.isCellPositionSelected(selectedColumnPosition, currentRowPosition)) {
                if (this.copyLayer == null) {
                    bodyCells[columnPosition + columnOffset] =
                            this.selectionLayer.getCellByPosition(selectedColumnPosition, currentRowPosition);
                }
                else {
                    int copyColPos = LayerUtil.convertColumnPosition(this.selectionLayer, selectedColumnPosition, this.copyLayer);
                    int copyRowPos = LayerUtil.convertRowPosition(this.selectionLayer, currentRowPosition, this.copyLayer);

                    bodyCells[columnPosition + columnOffset] =
                            this.copyLayer.getCellByPosition(copyColPos, copyRowPos);
                }
            }
        }
        return bodyCells;
    }

}
