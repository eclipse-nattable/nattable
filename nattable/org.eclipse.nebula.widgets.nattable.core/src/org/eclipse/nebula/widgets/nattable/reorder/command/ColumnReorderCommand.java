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
package org.eclipse.nebula.widgets.nattable.reorder.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class ColumnReorderCommand implements ILayerCommand {
	
	private ColumnPositionCoordinate fromColumnPositionCoordinate;
	private ColumnPositionCoordinate toColumnPositionCoordinate;
	private boolean reorderToLeftEdge;
	
	public ColumnReorderCommand(ILayer layer, int fromColumnPosition, int toColumnPosition) {
		fromColumnPositionCoordinate = new ColumnPositionCoordinate(layer, fromColumnPosition);
		
		if (toColumnPosition < layer.getColumnCount()) {
 			reorderToLeftEdge = true;
		} else {
			reorderToLeftEdge = false;
			toColumnPosition--;
		}
		
		toColumnPositionCoordinate = new ColumnPositionCoordinate(layer, toColumnPosition);
	}
	
	protected ColumnReorderCommand(ColumnReorderCommand command) {
		this.fromColumnPositionCoordinate = command.fromColumnPositionCoordinate;
		this.toColumnPositionCoordinate = command.toColumnPositionCoordinate;
		this.reorderToLeftEdge = command.reorderToLeftEdge;
	}
	
	public int getFromColumnPosition() {
		return fromColumnPositionCoordinate.getColumnPosition();
	}
	
	public int getToColumnPosition() {
		return toColumnPositionCoordinate.getColumnPosition();
	}
	
	public boolean isReorderToLeftEdge() {
		return reorderToLeftEdge;
	}
	
	public boolean convertToTargetLayer(ILayer targetLayer) {
		fromColumnPositionCoordinate = LayerCommandUtil.convertColumnPositionToTargetContext(fromColumnPositionCoordinate, targetLayer);
		toColumnPositionCoordinate = LayerCommandUtil.convertColumnPositionToTargetContext(toColumnPositionCoordinate, targetLayer);
		return fromColumnPositionCoordinate != null && toColumnPositionCoordinate != null;
	}
	
	public ColumnReorderCommand cloneCommand() {
		return new ColumnReorderCommand(this);
	}
	
}
