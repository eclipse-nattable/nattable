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
package org.eclipse.nebula.widgets.nattable.group.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import org.eclipse.nebula.widgets.nattable.Messages;
import org.eclipse.nebula.widgets.nattable.columnRename.ColumnRenameDialog;
import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.IValueIterator;
import org.eclipse.nebula.widgets.nattable.coordinate.RangeList;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.event.GroupColumnsEvent;
import org.eclipse.nebula.widgets.nattable.group.event.UngroupColumnsEvent;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;


public class ColumnGroupsCommandHandler extends AbstractLayerCommandHandler<IColumnGroupCommand>  {
	
	private final ColumnGroupModel model;
	private final SelectionLayer selectionLayer;
	private final ColumnGroupHeaderLayer contextLayer;
	private Map<Integer, Integer> columnIndexesToPositionsMap;

	public ColumnGroupsCommandHandler(ColumnGroupModel model, SelectionLayer selectionLayer, ColumnGroupHeaderLayer contextLayer) {
		this.model = model;
		this.selectionLayer = selectionLayer;
		this.contextLayer = contextLayer;
	}

	public boolean doCommand(IColumnGroupCommand command) {
		if (command instanceof CreateColumnGroupCommand) {
			if (columnIndexesToPositionsMap.size() > 0) {
				handleGroupColumnsCommand(((CreateColumnGroupCommand)command).getColumnGroupName());
				columnIndexesToPositionsMap.clear();
				return true;
			}
		} else if (command instanceof OpenCreateColumnGroupDialog) {
			OpenCreateColumnGroupDialog openDialogCommand = (OpenCreateColumnGroupDialog) command;
			loadSelectedColumnsIndexesWithPositions();
			if (!selectionLayer.getFullySelectedColumnPositions().isEmpty() && columnIndexesToPositionsMap.size() > 0) {
				openDialogCommand.openDialog(contextLayer);
			} else {				
				openDialogCommand.openErrorBox(Messages.getString("ColumnGroups.selectNonGroupedColumns"));				 //$NON-NLS-1$
			}
			return true;
		} else if (command instanceof UngroupColumnCommand) {
			handleUngroupCommand();
			return true;
		} else if (command instanceof RemoveColumnGroupCommand) {
			RemoveColumnGroupCommand removeColumnGroupCommand = (RemoveColumnGroupCommand) command;
			int columnIndex = removeColumnGroupCommand.getColumnIndex();
			handleRemoveColumnGroupCommand(columnIndex);
			return true;
		} else if (command instanceof DisplayColumnGroupRenameDialogCommand) {
			return displayColumnGroupRenameDialog((DisplayColumnGroupRenameDialogCommand) command);
		}
		return false;
	}

	private boolean displayColumnGroupRenameDialog(DisplayColumnGroupRenameDialogCommand command) {
		int columnPosition = command.getColumnPosition();

		ColumnRenameDialog dialog = new ColumnRenameDialog(Display.getDefault().getActiveShell(), null, null);
		Rectangle colHeaderBounds = contextLayer.getBoundsByPosition(columnPosition, 0);
		Point point = new Point(colHeaderBounds.x, colHeaderBounds.y + colHeaderBounds.height);
        dialog.setLocation(command.toDisplayCoordinates(point));
		dialog.open();

		if (!dialog.isCancelPressed()) {
			int columnIndex = contextLayer.getColumnIndexByPosition(columnPosition);
			ColumnGroup columnGroup = model.getColumnGroupByIndex(columnIndex);
			columnGroup.setName(dialog.getNewColumnLabel());
		}
		
		return true;
	}

	public Class<IColumnGroupCommand> getCommandClass() {
		return IColumnGroupCommand.class;
	}
	
	protected void loadSelectedColumnsIndexesWithPositions() {
		columnIndexesToPositionsMap = new LinkedHashMap<Integer, Integer>();
		final RangeList fullySelectedColumns = selectionLayer.getFullySelectedColumnPositions();
		
		for (final IValueIterator columnIter = fullySelectedColumns.values().iterator(); columnIter.hasNext(); ) {
			final int position = columnIter.nextValue();
			int index = selectionLayer.getColumnIndexByPosition(position);
			if (model.isPartOfAGroup(index)) {
				columnIndexesToPositionsMap.clear();
				break;
			}
			columnIndexesToPositionsMap.put(Integer.valueOf(index), Integer.valueOf(position));
		}
	}

	public void handleGroupColumnsCommand(String columnGroupName) {
		try {
			List<Integer> selectedPositions = new ArrayList<Integer>();
			int[] fullySelectedColumns = new int[columnIndexesToPositionsMap.size()];
			int count = 0;
			for (Integer columnIndex : columnIndexesToPositionsMap.keySet()) {
				fullySelectedColumns[count++] = columnIndex.intValue();
				selectedPositions.add(columnIndexesToPositionsMap.get(columnIndex));
			}
			model.addColumnsIndexesToGroup(columnGroupName, fullySelectedColumns);
			selectionLayer.doCommand(new MultiColumnReorderCommand(selectionLayer, selectedPositions, selectedPositions.get(0).intValue()));
			selectionLayer.clear();
		} catch (Throwable t) {
		}
		contextLayer.fireLayerEvent(new GroupColumnsEvent(contextLayer));
	}

	public void handleUngroupCommand() {
		// Grab fully selected column positions
		final RangeList fullySelectedColumns = selectionLayer.getFullySelectedColumnPositions();
		Map<String, Integer> toColumnPositions = new HashMap<String, Integer>();
		
		for (final IValueIterator columnIter = fullySelectedColumns.values().iterator(); columnIter.hasNext(); ) {
			// Pick the ones which belong to a group and remove them from the group
			final int position = columnIter.nextValue();
			int index = selectionLayer.getColumnIndexByPosition(position);
			if (model.isPartOfAGroup(index) && !model.isPartOfAnUnbreakableGroup(index)) {
				handleRemovalFromGroup(toColumnPositions, index);
			}
		}
		if (!toColumnPositions.isEmpty()) {
			// The groups which were affected should be reordered to the start position, this should group all columns together
			for (final Integer position : toColumnPositions.values()) {
				selectionLayer.doCommand(new ReorderColumnGroupCommand(selectionLayer, position, position));
			}
			selectionLayer.clear();
		}
		
		contextLayer.fireLayerEvent(new UngroupColumnsEvent(contextLayer));
	}

	private void handleRemovalFromGroup(Map<String, Integer> toColumnPositions, int columnIndex) {
		ColumnGroup columnGroup = model.getColumnGroupByIndex(columnIndex);
		
		final String columnGroupName = columnGroup.getName();
		final List<Integer> columnIndexesInGroup = columnGroup.getMembers();
		final int columnGroupSize = columnIndexesInGroup.size();
		if (!toColumnPositions.containsKey(columnGroupName)) {
			for (int colGroupIndex : columnIndexesInGroup) {
				if (ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(colGroupIndex, contextLayer, selectionLayer, model)) {
					int toPosition = selectionLayer.getColumnPositionByIndex(colGroupIndex);
					if (colGroupIndex == columnIndex) {
						if (columnGroupSize == 1) {
							break;
						} else {
							toPosition++;
						}
					}
					toColumnPositions.put(columnGroupName, Integer.valueOf(toPosition));
					break;
				}
			}
		} else {
			if (columnGroupSize - 1 <= 0) {
				toColumnPositions.remove(columnGroupName);
			}
		}
		columnGroup.removeColumn(columnIndex);
	}	
	
	private void handleRemoveColumnGroupCommand(int columnIndex) {
		ColumnGroup columnGroup = model.getColumnGroupByIndex(columnIndex);
		model.removeColumnGroup(columnGroup);
	}

}
