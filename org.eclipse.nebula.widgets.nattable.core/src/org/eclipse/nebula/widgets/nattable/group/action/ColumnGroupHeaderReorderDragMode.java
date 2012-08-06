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
package org.eclipse.nebula.widgets.nattable.group.action;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnGroupEndCommand;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnGroupStartCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.action.ColumnReorderDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.events.MouseEvent;

/**
 * Default {@link IDragMode} invoked for 'left click + drag' on the column group header.<br/>
 *
 * It overrides the isValidTargetColumnPosition() to calculate if a destination position is valid
 * for the column group to be reordered to.<br/>
 *
 * Example, a column group cannot only be reordered to be inside another column group.
 * @see ColumnGroupHeaderReorderDragModeTest
 */
public class ColumnGroupHeaderReorderDragMode extends ColumnReorderDragMode {

	private final ColumnGroupModel model;
	private MouseEvent event;

	public ColumnGroupHeaderReorderDragMode(ColumnGroupModel model) {
		this.model = model;
	}

	@Override
	protected boolean isValidTargetColumnPosition(ILayer natLayer, int fromGridColumnPosition, int toGridColumnPosition, MouseEvent event) {
		this.event = event;
		toGridColumnPosition = natLayer.getColumnPositionByX(event.x);
		return isValidTargetColumnPosition(natLayer, fromGridColumnPosition, toGridColumnPosition);
	}

	/**
	 * Work off the event coordinates since the drag {@link ColumnReorderDragMode} adjusts the
	 * 'to' column positions (for on screen semantics)
	 */
	protected boolean isValidTargetColumnPosition(ILayer natLayer, int fromGridColumnPosition, int toGridColumnPosition) {
		int toColumnIndex = natLayer.getColumnIndexByPosition(toGridColumnPosition);

		boolean betweenGroups = false;
		if(event != null){
			int minX = event.x -  GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
			int maxX = event.x +  GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
			betweenGroups = ColumnGroupUtils.isBetweenTwoGroups(natLayer, minX, maxX, model);
		}

		return (!model.isPartOfAGroup(toColumnIndex)) || betweenGroups;
	}
	
	@Override
	protected void fireMoveStartCommand(NatTable natTable, int dragFromGridColumnPosition) {
		natTable.doCommand(new ReorderColumnGroupStartCommand(natTable, dragFromGridColumnPosition));
	}
	
	@Override
	protected void fireMoveEndCommand(NatTable natTable, int dragToGridColumnPosition) {
		natTable.doCommand(new ReorderColumnGroupEndCommand(natTable, dragToGridColumnPosition));
	}
}
