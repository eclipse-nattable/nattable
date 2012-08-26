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
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;

/**
 * Handles updating of the Column Group Model when a column belonging to
 * a group is reordered. The actual reordering of the column is delegated to the lower layers.
 */
public class GroupColumnReorderCommandHandler extends AbstractLayerCommandHandler<ColumnReorderCommand> {

	private final ColumnGroupReorderLayer columnGroupReorderLayer;

	private final ColumnGroupModel model;

	public GroupColumnReorderCommandHandler(ColumnGroupReorderLayer columnGroupReorderLayer) {
		this.columnGroupReorderLayer = columnGroupReorderLayer;
		this.model = columnGroupReorderLayer.getModel();
	}

	public Class<ColumnReorderCommand> getCommandClass() {
		return ColumnReorderCommand.class;
	}

	@Override
	protected boolean doCommand(ColumnReorderCommand command) {
		int fromColumnPosition = command.getFromColumnPosition();
		int toColumnPosition = command.getToColumnPosition();

		if (fromColumnPosition == -1 || toColumnPosition == -1) {
			System.err.println("Invalid reorder positions, fromPosition: " + fromColumnPosition + ", toPosition: " + toColumnPosition); //$NON-NLS-1$ //$NON-NLS-2$
		}
		ILayer underlyingLayer = columnGroupReorderLayer.getUnderlyingLayer();
		int fromColumnIndex = underlyingLayer.getColumnIndexByPosition(fromColumnPosition);
		int toColumnIndex = underlyingLayer.getColumnIndexByPosition(toColumnPosition);

		MoveDirectionEnum moveDirection = ColumnGroupUtils.getMoveDirection(fromColumnPosition, toColumnPosition);

		String leftEdgeGroupName = null;
		String rightEdgeGroupName = null;

		if (MoveDirectionEnum.RIGHT == moveDirection) {
			rightEdgeGroupName = movedToLeftEdgeOfAGroup(toColumnPosition, toColumnIndex);
		}
		if (MoveDirectionEnum.LEFT == moveDirection) {
			leftEdgeGroupName = movedToRightEdgeOfAGroup(toColumnPosition, toColumnIndex);
		}

		if(updateModel(fromColumnIndex, toColumnIndex, leftEdgeGroupName, rightEdgeGroupName)){
			return underlyingLayer.doCommand(command);
		}else{
			return false;
		}
	}

	private boolean updateModel(int fromColumnIndex, int toColumnIndex, String leftEdgeGroupName, String rightEdgeGroupName) {
		ColumnGroup fromColumnGroup = model.getColumnGroupByIndex(fromColumnIndex);
		ColumnGroup toColumnGroup = model.getColumnGroupByIndex(toColumnIndex);

		// If moved to the RIGHT edge of a group - remove from group
		if (rightEdgeGroupName != null) {
			return (model.isPartOfAGroup(fromColumnIndex)) ? fromColumnGroup.removeColumn(fromColumnIndex) : true;
		}

		// If moved to the LEFT edge of a column group - include in the group
		if (leftEdgeGroupName != null) {
			boolean removed = true;
			if (model.isPartOfAGroup(fromColumnIndex)){
				removed = fromColumnGroup.removeColumn(fromColumnIndex);
			}
			return removed && model.insertColumnIndexes(leftEdgeGroupName, fromColumnIndex);
		}

		// Move column INTO a group
		if (model.isPartOfAGroup(toColumnIndex) && !model.isPartOfAGroup(fromColumnIndex)) {
			String groupName = toColumnGroup.getName();
			return model.insertColumnIndexes(groupName, fromColumnIndex);
		}

		// Move column OUT of a group
		if (model.isPartOfAGroup(fromColumnIndex) && !model.isPartOfAGroup(toColumnIndex)) {
			return fromColumnGroup.removeColumn(fromColumnIndex);
		}

		// Move column BETWEEN groups
		if (model.isPartOfAGroup(toColumnIndex) && model.isPartOfAGroup(fromColumnIndex)) {
			String toGroupName = toColumnGroup.getName();
			String fromGroupName = fromColumnGroup.getName();

			if (fromGroupName.equals(toGroupName)) {
				return true;
			} else {
				return fromColumnGroup.removeColumn(fromColumnIndex) && model.insertColumnIndexes(toGroupName, fromColumnIndex);
			}
		}
		return true;
	}

	private String movedToRightEdgeOfAGroup(int dropColumnPosition, int dropColumnIndex){
		if(ColumnGroupUtils.isRightEdgeOfAColumnGroup(columnGroupReorderLayer, dropColumnPosition, dropColumnIndex, model)){
			return model.getColumnGroupByIndex(dropColumnIndex).getName();
		}
		return null;
	}

	private String movedToLeftEdgeOfAGroup(int dropColumnPosition, int dropColumnIndex){
		if(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(columnGroupReorderLayer, dropColumnPosition, dropColumnIndex, model)){
			return model.getColumnGroupByIndex(dropColumnIndex).getName();
		}
		return null;
	}

}
