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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;


public class ColumnGroupExpandCollapseCommandHandler extends AbstractLayerCommandHandler<ColumnGroupExpandCollapseCommand> {

	private final ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer;

	public ColumnGroupExpandCollapseCommandHandler(ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer) {
		this.columnGroupExpandCollapseLayer = columnGroupExpandCollapseLayer;
	}
	
	public Class<ColumnGroupExpandCollapseCommand> getCommandClass() {
		return ColumnGroupExpandCollapseCommand.class;
	}

	@Override
	protected boolean doCommand(ColumnGroupExpandCollapseCommand command) {
		
		int columnIndex = columnGroupExpandCollapseLayer.getColumnIndexByPosition(command.getColumnPosition());
		ColumnGroupModel model = columnGroupExpandCollapseLayer.getModel();
		ColumnGroup columnGroup = model.getColumnGroupByIndex(columnIndex);
		
		// if group of columnIndex is not collapseable return without any 
		// further operation ...
		if (!columnGroup.isCollapseable()) {
			return true;
		}
		
		boolean wasCollapsed = columnGroup.isCollapsed();
		columnGroup.toggleCollapsed();
		
		List<Integer> columnPositions = new ArrayList<Integer>(columnGroup.getMembers());
		columnPositions.removeAll(columnGroup.getStaticColumnIndexes());
		
		ILayerEvent event;
		if (wasCollapsed) {
			event = new ShowColumnPositionsEvent(columnGroupExpandCollapseLayer, columnPositions);
		} else {
			event = new HideColumnPositionsEvent(columnGroupExpandCollapseLayer, columnPositions);
		}
		
		columnGroupExpandCollapseLayer.fireLayerEvent(event);
		
		return true;
	}

}
