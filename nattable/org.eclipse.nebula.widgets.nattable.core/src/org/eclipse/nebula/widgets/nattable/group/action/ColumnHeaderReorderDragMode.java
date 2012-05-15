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


import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.reorder.action.ColumnReorderDragMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.events.MouseEvent;

/**
 * Extends the regular column drag functionality to work with Column groups.<br/>
 * It does the following checks:<br/>
 * <ol>
 * <li>Checks that the destination is not part of a Unbreakable column group</li>
 * <li>Checks if the destination is between two adjoining column groups</li>
 * </ol>
 */
public class ColumnHeaderReorderDragMode extends ColumnReorderDragMode {

	private final ColumnGroupModel model;
	private MouseEvent event;

	public ColumnHeaderReorderDragMode(ColumnGroupModel model) {
		this.model = model;
	}

	public boolean isValidTargetColumnPosition(ILayer natLayer, int fromGridColumnPosition, int toGridColumnPosition) {
		int toColumnIndex = natLayer.getColumnIndexByPosition(toGridColumnPosition);
		int fromColumnIndex = natLayer.getColumnIndexByPosition(fromGridColumnPosition);

		// Allow moving within the unbreakable group
		if (model.isPartOfAnUnbreakableGroup(fromColumnIndex)){
			return ColumnGroupUtils.isInTheSameGroup(fromColumnIndex, toColumnIndex, model);
		}

		boolean betweenTwoGroups = false;
		if (event != null) {
			int minX = event.x - GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
			int maxX = event.x + GUIHelper.DEFAULT_RESIZE_HANDLE_SIZE;
			betweenTwoGroups = ColumnGroupUtils.isBetweenTwoGroups(natLayer, minX, maxX, model);
		}

		return (!model.isPartOfAnUnbreakableGroup(toColumnIndex)) || betweenTwoGroups;
	}

	@Override
	public boolean isValidTargetColumnPosition(ILayer natLayer, int fromGridColumnPosition, int toGridColumnPosition, MouseEvent event) {
		this.event = event;
		toGridColumnPosition = natLayer.getColumnPositionByX(event.x);
		return isValidTargetColumnPosition(natLayer, fromGridColumnPosition, toGridColumnPosition);
	}
}
