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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.action;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByHeaderLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.command.GroupByColumnIndexCommand;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.swt.events.MouseEvent;

public class GroupByDragMode implements IDragMode {

	private MouseEvent initialEvent;

	public void mouseDown(NatTable natTable, MouseEvent event) {
		initialEvent = event;
	}

	public void mouseMove(NatTable natTable, MouseEvent event) {
	}

	public void mouseUp(NatTable natTable, MouseEvent event) {
		LabelStack regionLabels = natTable.getRegionLabelsByXY(event.x, event.y);
		if (regionLabels != null && regionLabels.hasLabel(GroupByHeaderLayer.GROUP_BY_REGION)) {
			int columnPosition = natTable.getColumnPositionByX(initialEvent.x);
			int columnIndex = natTable.getColumnIndexByPosition(columnPosition);
			natTable.doCommand(new GroupByColumnIndexCommand(columnIndex));
		}
	}
	
}
