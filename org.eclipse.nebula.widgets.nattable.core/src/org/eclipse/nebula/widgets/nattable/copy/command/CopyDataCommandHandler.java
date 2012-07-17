/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.copy.command;

import java.util.Set;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.copy.serializing.CopyDataToClipboardSerializer;
import org.eclipse.nebula.widgets.nattable.copy.serializing.CopyFormattedTextToClipboardSerializer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.serializing.ISerializer;

public class CopyDataCommandHandler extends AbstractLayerCommandHandler<CopyDataToClipboardCommand> {

	private final SelectionLayer selectionLayer;
	private final ILayer columnHeaderLayer;
	private final ILayer rowHeaderLayer;
	private boolean copyFormattedText;

	public CopyDataCommandHandler(SelectionLayer selectionLayer) {
		this(selectionLayer, null, null);
	}
	
	public CopyDataCommandHandler(SelectionLayer selectionLayer, ILayer columnHeaderLayer, ILayer rowHeaderLayer) {
		this.selectionLayer = selectionLayer;
		this.columnHeaderLayer = columnHeaderLayer;
		this.rowHeaderLayer = rowHeaderLayer;
	}
	
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

	public Class<CopyDataToClipboardCommand> getCommandClass() {
		return CopyDataToClipboardCommand.class;
	}

	protected ILayerCell[][] assembleCopiedDataStructure() {
		final Set<Range> selectedRows = selectionLayer.getSelectedRowPositions();
		final int rowOffset = columnHeaderLayer != null ? columnHeaderLayer.getRowCount() : 0;
		// Add offset to rows, remember they need to include the column header as a row
		final ILayerCell[][] copiedCells = new ILayerCell[selectionLayer.getSelectedRowCount() + rowOffset][1];
		if (columnHeaderLayer != null) {
			copiedCells[0] = assembleColumnHeaders(selectionLayer.getSelectedColumnPositions());
		}
		for (Range range : selectedRows) {
			for (int rowPosition = range.start; rowPosition < range.end; rowPosition++) {
				copiedCells[(rowPosition - range.start) + rowOffset] = assembleBody(rowPosition);
			}
		}
		return copiedCells;
	}

	/**
	 * FIXME When we implement column groups, keep in mind this method assumes the ColumnHeaderLayer is has only a height of 1 row.
	 * @return
	 */
	protected ILayerCell[] assembleColumnHeaders(int... selectedColumnPositions) {
		final int columnOffset = rowHeaderLayer.getColumnCount();
		final ILayerCell[] cells = new ILayerCell[selectedColumnPositions.length + columnOffset];
		for (int columnPosition = 0; columnPosition < selectedColumnPositions.length; columnPosition++) {
			// Pad the width of the vertical layer
			cells[columnPosition + columnOffset] = columnHeaderLayer.getCellByPosition(selectedColumnPositions[columnPosition], 0);
		}
		return cells;
	}
	
	/**
	 * FIXME Assumes row headers have only one column.
	 * @param lastSelectedColumnPosition
	 * @param currentRowPosition
	 * @return
	 */
	protected ILayerCell[] assembleBody(int currentRowPosition) {		
		final int[] selectedColumns = selectionLayer.getSelectedColumnPositions();
		final int columnOffset = rowHeaderLayer != null ? rowHeaderLayer.getColumnCount() : 0;
		final ILayerCell[] bodyCells = new ILayerCell[selectedColumns.length + columnOffset];
		
		if (rowHeaderLayer != null) {
			bodyCells[0] = rowHeaderLayer.getCellByPosition(0, currentRowPosition);
		}
		
		for (int columnPosition = 0; columnPosition < selectedColumns.length; columnPosition++) {
			final int selectedColumnPosition = selectedColumns[columnPosition];
			if (selectionLayer.isCellPositionSelected(selectedColumnPosition, currentRowPosition)) {
				bodyCells[columnPosition + columnOffset] = selectionLayer.getCellByPosition(selectedColumnPosition, currentRowPosition);
			}
		}
		return bodyCells;
	}
	
}
