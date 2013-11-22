/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.copy.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.IValueIterator;
import org.eclipse.nebula.widgets.nattable.coordinate.RangeList;
import org.eclipse.nebula.widgets.nattable.copy.serializing.CopyDataToClipboardSerializer;
import org.eclipse.nebula.widgets.nattable.copy.serializing.CopyFormattedTextToClipboardSerializer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.serializing.ISerializer;

/**
 * Handler class for copying selected data within the {@link SelectionLayer} to the clipboard.
 * This handler is registered by default with the {@link SelectionLayer}, without references
 * to the header regions. You can override the copy data behaviour by registering an instance
 * of this handler to a layer above the {@link SelectionLayer}. This way the registered custom
 * instance will consume a {@link CopyDataToClipboardCommand} and the registered default handler
 * won't be called.
 */
public class CopyDataCommandHandler extends AbstractLayerCommandHandler<CopyDataToClipboardCommand> {

	/**
	 * The SelectionLayer needed to retrieve the selected data to copy to the clipboard.
	 */
	private final SelectionLayer selectionLayer;
	/**
	 * The column header layer of the grid, needed to also copy the column header data.
	 */
	private final ILayer columnHeaderDataLayer;
	/**
	 * The row header layer of the grid, needed to also copy the row header data.
	 */
	private final ILayer rowHeaderDataLayer;
	/**
	 * Flag to specify which serializer should be used for copying the data.
	 * <code>false</code> will use the CopyDataToClipboardSerializer which simply calls
	 * <code>toString()</code> to serialize the data to copy, <code>true</code> will
	 * use the CopyFormattedTextToClipboardSerializer which will use the configured
	 * IDisplayConverter to get the String representation of the value to copy.
	 */
	private boolean copyFormattedText;

	/**
	 * Creates an instance that only checks the {@link SelectionLayer} for data to add to the
	 * clipboard.
	 * @param selectionLayer The {@link SelectionLayer} within the NatTable. Can not be <code>null</code>.
	 */
	public CopyDataCommandHandler(SelectionLayer selectionLayer) {
		this(selectionLayer, null, null);
	}
	
	/**
	 * Creates an instance that checks the {@link SelectionLayer} and the header layers if they are given.
	 * @param selectionLayer The {@link SelectionLayer} within the NatTable. Can not be <code>null</code>.
	 * @param columnHeaderDataLayer The column header data layer within the NatTable grid. Can be <code>null</code>.
	 * @param rowHeaderDataLayer The row header data layer within the NatTable grid. Can be <code>null</code>.
	 */
	public CopyDataCommandHandler(SelectionLayer selectionLayer, ILayer columnHeaderDataLayer, ILayer rowHeaderDataLayer) {
 		assert selectionLayer != null : "The SelectionLayer can not be null on creating a CopyDataCommandHandler"; //$NON-NLS-1$
		this.selectionLayer = selectionLayer;
		this.columnHeaderDataLayer = columnHeaderDataLayer;
		this.rowHeaderDataLayer = rowHeaderDataLayer;
	}
	
	/**
	 * Specify which serializer to use for copying.
	 * @param copyFormattedText <code>false</code> will use the CopyDataToClipboardSerializer 
	 * 			which simply calls <code>toString()</code> to serialize the data to copy, 
	 * 			<code>true</code> will use the CopyFormattedTextToClipboardSerializer which 
	 * 			will use the configured IDisplayConverter to get the String representation of 
	 * 			the value to copy
	 */
	public void setCopyFormattedText(boolean copyFormattedText) {
		this.copyFormattedText = copyFormattedText;
	}
	
	@Override
	public boolean doCommand(CopyDataToClipboardCommand command) {
		ISerializer serializer = copyFormattedText ?
				new CopyFormattedTextToClipboardSerializer(assembleCopiedDataStructure(), command) :
				new CopyDataToClipboardSerializer(assembleCopiedDataStructure(), command);
		serializer.serialize();
		return true;
	}

	@Override
	public Class<CopyDataToClipboardCommand> getCommandClass() {
		return CopyDataToClipboardCommand.class;
	}

	/**
	 * Collects and assembles the selected data that should be copied to the clipboard.
	 * 
	 * Creates the two dimensional array whose dimensions are calculated based on the selection
	 * within the {@link SelectionLayer} and the configured column and row headers.
	 * 
	 * @return A two dimensional array containing the selected cells to copy to the clipboard.
	 * 			The first level of this array represent the row positions of the cells, while the
	 * 			second level contains the cells itself based on the column position.
	 */
	protected ILayerCell[][] assembleCopiedDataStructure() {
		final RangeList selectedRowPositions = selectionLayer.getSelectedRowPositions();
		final RangeList selectedColumnPositions = selectionLayer.getSelectedColumnPositions();
		
		final int rowOffset = (columnHeaderDataLayer != null) ? columnHeaderDataLayer.getRowCount() : 0;
		final int columnOffset = (rowHeaderDataLayer != null) ? rowHeaderDataLayer.getColumnCount() : 0;
		
		final ILayerCell[][] cells = new ILayerCell[selectedRowPositions.values().size() + rowOffset][];
		
		int cellsIdx = 0;
		while (cellsIdx < rowOffset) {
			cells[cellsIdx++] = assembleColumnHeader(selectedColumnPositions, columnOffset, cellsIdx);
		}
		for (final IValueIterator rowIter = selectedRowPositions.values().iterator(); rowIter.hasNext(); ) {
			final int rowPosition = rowIter.nextValue();
			cells[cellsIdx++] = assembleBody(selectedColumnPositions, columnOffset, rowPosition);
		}
		
		return cells;
	}
	
	/**
	 * Collects and assembles the column header information for the specified column header row.
	 * 
	 * If there is no column header configured an empty array with the matching dimensions will be returned.
	 * 
	 * @param selectedColumnPositions The column positions of which the information should be collected
	 * @param columnOffset The column offset of table body
	 * @param headerRowPosition The row position in the column header of which the information should be collected
	 * @return An array containing the column header information
	 */
	protected ILayerCell[] assembleColumnHeader(final RangeList selectedColumnPositions, final int columnOffset,
			final int headerRowPosition) {
		final ILayerCell[] headerCells = new ILayerCell[selectedColumnPositions.values().size() + columnOffset];
		
		int headerIdx = columnOffset;
		if (columnHeaderDataLayer != null) {
			for (final IValueIterator columnIter = selectedColumnPositions.values().iterator(); columnIter.hasNext(); headerIdx++) {
				final int columnPosition = columnIter.nextValue();
				headerCells[headerIdx] = columnHeaderDataLayer.getCellByPosition(columnPosition, headerRowPosition);
			}
		}
		
		return headerCells;
	}
	
	/**
	 * Collects and assembles the selected data per row position that should be copied to the clipboard.
	 * If there is a row header layer configured for this handler, the row header cells of the selected
	 * row position are also added to the resulting array.
	 * 
	 * @param selectedColumnPositions The column positions of which the information should be collected
	 * @param columnOffset The column offset of table body
	 * @param currentRowPosition The row position of which the selected cells should be collected
	 * @return An array containing the specified cells
	 */
	protected ILayerCell[] assembleBody(final RangeList selectedColumnPositions,  final int columnOffset,
			final int currentRowPosition) {
		final ILayerCell[] bodyCells = new ILayerCell[selectedColumnPositions.values().size() + columnOffset];
		
		int bodyIdx = 0;
		if (rowHeaderDataLayer != null) {
			for (; bodyIdx < columnOffset; bodyIdx++) {
				bodyCells[bodyIdx] = rowHeaderDataLayer.getCellByPosition(bodyIdx, currentRowPosition);
			}
		}
		for (final IValueIterator columnIter = selectedColumnPositions.values().iterator(); columnIter.hasNext(); bodyIdx++) {
			final int columnPosition = columnIter.nextValue();
			if (selectionLayer.isCellPositionSelected(columnPosition, currentRowPosition)) {
				bodyCells[bodyIdx] = selectionLayer.getCellByPosition(columnPosition, currentRowPosition);
			}
		}
		
		return bodyCells;
	}
	
}
