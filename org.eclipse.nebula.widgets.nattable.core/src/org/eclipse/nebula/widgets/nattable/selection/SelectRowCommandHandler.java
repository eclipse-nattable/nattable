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
package org.eclipse.nebula.widgets.nattable.selection;

import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.bothShiftAndControl;
import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.isControlOnly;
import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.isShiftOnly;
import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.noShiftOrControl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.IValueIterator;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.coordinate.RangeList;
import org.eclipse.nebula.widgets.nattable.coordinate.RangeList.ValueIterator;
import org.eclipse.nebula.widgets.nattable.coordinate.Rectangle;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;


public class SelectRowCommandHandler implements ILayerCommandHandler<SelectRowsCommand> {

	private final SelectionLayer selectionLayer;

	public SelectRowCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}

	@Override
	public boolean doCommand(ILayer targetLayer, SelectRowsCommand command) {
		if (command.convertToTargetLayer(selectionLayer)) {
			selectRows(command.getColumnPosition(), command.getPositions(), command.isWithShiftMask(), command.isWithControlMask(), command.getRowPositionToMoveIntoViewport());
			return true;
		}
		return false;
	}

	protected void selectRows(int columnPosition, Collection<Range> rowPositions, boolean withShiftMask, boolean withControlMask, int rowPositionToMoveIntoViewport) {
		final RangeList changedRows = new RangeList();
		
		for (final IValueIterator rowIter = new ValueIterator(rowPositions); rowIter.hasNext(); ) {
			changedRows.addAll(internalSelectRow(columnPosition, rowIter.nextValue(),
					withShiftMask, withControlMask ));
		}
		
		selectionLayer.fireLayerEvent(new RowSelectionEvent(selectionLayer, changedRows, rowPositionToMoveIntoViewport));
	}

	private Set<Range> internalSelectRow(int columnPosition, int rowPosition, boolean withShiftMask, boolean withControlMask) {
		Set<Range> changedRowRanges = new HashSet<Range>();
		
		if (noShiftOrControl(withShiftMask, withControlMask)) {
			changedRowRanges.addAll(selectionLayer.getSelectedRowPositions());
			selectionLayer.clear(false);
			selectionLayer.selectCell(0, rowPosition, withShiftMask, withControlMask);
			selectionLayer.selectRegion(0, rowPosition, Integer.MAX_VALUE, 1);
			selectionLayer.moveSelectionAnchor(columnPosition, rowPosition);
			changedRowRanges.add(new Range(rowPosition));
		} else if (bothShiftAndControl(withShiftMask, withControlMask)) {
			changedRowRanges.add(selectRowWithShiftKey(rowPosition));
		} else if (isShiftOnly(withShiftMask, withControlMask)) {
			changedRowRanges.add(selectRowWithShiftKey(rowPosition));
		} else if (isControlOnly(withShiftMask, withControlMask)) {
			changedRowRanges.add(selectRowWithCtrlKey(columnPosition, rowPosition));
		}

		selectionLayer.lastSelectedCell.columnPosition = columnPosition;
		selectionLayer.lastSelectedCell.rowPosition = rowPosition;
		
		return changedRowRanges;
	}

	private Range selectRowWithCtrlKey(int columnPosition, int rowPosition) {
		Rectangle selectedRowRectangle = new Rectangle(0, rowPosition, Integer.MAX_VALUE, 1);

		if (selectionLayer.isRowPositionFullySelected(rowPosition)) {
			selectionLayer.clearSelection(selectedRowRectangle);
			if (selectionLayer.lastSelectedRegion != null && selectionLayer.lastSelectedRegion.equals(selectedRowRectangle)) {
				selectionLayer.lastSelectedRegion = null;
			}
		} else {
			// Preserve last selected region
			if (selectionLayer.lastSelectedRegion != null) {
				selectionLayer.selectionModel.addSelection(
						new Rectangle(selectionLayer.lastSelectedRegion.x,
								selectionLayer.lastSelectedRegion.y,
								selectionLayer.lastSelectedRegion.width,
								selectionLayer.lastSelectedRegion.height));
			}
			selectionLayer.selectRegion(0, rowPosition, Integer.MAX_VALUE, 1);
			selectionLayer.moveSelectionAnchor(columnPosition, rowPosition);
		}
		
		return new Range(rowPosition);
	}

	private Range selectRowWithShiftKey(int rowPosition) {
		int numOfRowsToIncludeInRegion = 1;
		int startRowPosition = rowPosition;

		//if multiple selection is disabled, we need to ensure to only select the current rowPosition
		//modifying the selection anchor here ensures that the anchor also moves
		if (!selectionLayer.getSelectionModel().isMultipleSelectionAllowed()) {
			selectionLayer.selectionAnchor.rowPosition = rowPosition;
		}
		
		if (selectionLayer.lastSelectedRegion != null) {
			numOfRowsToIncludeInRegion = Math.abs(selectionLayer.selectionAnchor.rowPosition - rowPosition) + 1;
			if (startRowPosition < selectionLayer.selectionAnchor.rowPosition) {
				// Selecting above
				startRowPosition = rowPosition;
			} else {
				// Selecting below
				startRowPosition = selectionLayer.selectionAnchor.rowPosition;
			}
		}
		selectionLayer.selectRegion(0, startRowPosition, Integer.MAX_VALUE, numOfRowsToIncludeInRegion);
		
		return new Range(startRowPosition, startRowPosition + numOfRowsToIncludeInRegion);
	}

	@Override
	public Class<SelectRowsCommand> getCommandClass() {
		return SelectRowsCommand.class;
	}

}
