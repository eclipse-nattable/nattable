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
package org.eclipse.nebula.widgets.nattable.filterrow.event;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowPainter;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.swt.events.MouseEvent;

public class ClearFilterIconMouseEventMatcher extends FilterRowMouseEventMatcher {

	private final FilterRowPainter filterRowPainter;

	public ClearFilterIconMouseEventMatcher(FilterRowPainter filterRowPainter) {
		this.filterRowPainter = filterRowPainter;
	}
	
	@Override
	public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
		ILayerCell cell = natTable.getCellByPosition(natTable.getColumnPositionByX(event.x), natTable.getRowPositionByY(event.y));
		
		if (cell == null)
			return false;
		
		IConfigRegistry configRegistry = natTable.getConfigRegistry();
		
		boolean clearFilterIconClicked = false;
		
		if (ObjectUtils.isNotNull(cell.getDataValue())) {
			clearFilterIconClicked = filterRowPainter.containsRemoveFilterImage(event.x, event.y, cell, configRegistry);
		}
		return (super.matches(natTable, event, regionLabels) && clearFilterIconClicked);		
	}
}
