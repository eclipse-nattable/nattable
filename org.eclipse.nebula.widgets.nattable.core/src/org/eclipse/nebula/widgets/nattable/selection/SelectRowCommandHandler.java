/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Jonas Hugo <Jonas.Hugo@jeppesen.com>,
 *       Markus Wahl <Markus.Wahl@jeppesen.com> - Use getters and setters for
 *         the markers of SelectionLayer instead of the fields.
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
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;
import org.eclipse.swt.graphics.Rectangle;

public class SelectRowCommandHandler implements ILayerCommandHandler<SelectRowsCommand> {

	private final SelectionLayer selectionLayer;

	public SelectRowCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}

	@Override
	public boolean doCommand(ILayer targetLayer, SelectRowsCommand command) {
		if (command.convertToTargetLayer(selectionLayer)) {
			selectRows(command.getColumnPosition(), command.getRowPositions(), command.isWithShiftMask(), command.isWithControlMask(), command.getRowPositionToMoveIntoViewport());
			return true;
		}
		return false;
	}

	protected void selectRows(int columnPosition, Collection<Integer> rowPositions, boolean withShiftMask, boolean withControlMask, int rowPositionToMoveIntoViewport) {
		Set<Range> changedRowRanges = new HashSet<Range>();
		
		for (int rowPosition : rowPositions) {
			changedRowRanges.addAll(internalSelectRow(columnPosition, rowPosition, withShiftMask, withControlMask));
		}

		Set<Integer> changedRows = new HashSet<Integer>();
		for (Range range : changedRowRanges) {
			for (int i = range.start; i < range.end; i++) {
				changedRows.add(Integer.valueOf(i));
			}
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
			changedRowRanges.add(new Range(rowPosition, rowPosition + 1));
		} else if (bothShiftAndControl(withShiftMask, withControlMask)) {
			changedRowRanges.add(selectRowWithShiftKey(rowPosition));
		} else if (isShiftOnly(withShiftMask, withControlMask)) {
			changedRowRanges.add(selectRowWithShiftKey(rowPosition));
		} else if (isControlOnly(withShiftMask, withControlMask)) {
			changedRowRanges.add(selectRowWithCtrlKey(columnPosition, rowPosition));
		}

		selectionLayer.setLastSelectedCell(columnPosition, rowPosition);

		return changedRowRanges;
	}

	private Range selectRowWithCtrlKey(int columnPosition, int rowPosition) {
		Rectangle selectedRowRectangle = new Rectangle(0, rowPosition, Integer.MAX_VALUE, 1);

		if (selectionLayer.isRowPositionFullySelected(rowPosition)) {
			selectionLayer.clearSelection(selectedRowRectangle);
			if (selectionLayer.getLastSelectedRegion() != null && selectionLayer.getLastSelectedRegion().equals(selectedRowRectangle)) {
				selectionLayer.setLastSelectedRegion(null);
			}
		} else {
			// Preserve last selected region
			if (selectionLayer.getLastSelectedRegion() != null) {
				selectionLayer.selectionModel.addSelection(new Rectangle(
						selectionLayer.getLastSelectedRegion().x,
							selectionLayer.getLastSelectedRegion().y,
							selectionLayer.getLastSelectedRegion().width,
							selectionLayer.getLastSelectedRegion().height));
			}
			selectionLayer.selectRegion(0, rowPosition, Integer.MAX_VALUE, 1);
			selectionLayer.moveSelectionAnchor(columnPosition, rowPosition);
		}
		
		return new Range(rowPosition, rowPosition + 1);
	}

	private Range selectRowWithShiftKey(int rowPosition) {
		int numOfRowsToIncludeInRegion = 1;
		int startRowPosition = rowPosition;

		//if multiple selection is disabled, we need to ensure to only select the current rowPosition
		//modifying the selection anchor here ensures that the anchor also moves
		if (!selectionLayer.getSelectionModel().isMultipleSelectionAllowed()) {
			selectionLayer.getSelectionAnchor().rowPosition = rowPosition;
		}

		if (selectionLayer.getLastSelectedRegion() != null) {
			numOfRowsToIncludeInRegion = Math.abs(selectionLayer.getSelectionAnchor().rowPosition - rowPosition) + 1;
			if (startRowPosition < selectionLayer.getSelectionAnchor().rowPosition) {
				// Selecting above
				startRowPosition = rowPosition;
			} else {
				// Selecting below
				startRowPosition = selectionLayer.getSelectionAnchor().rowPosition;
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
