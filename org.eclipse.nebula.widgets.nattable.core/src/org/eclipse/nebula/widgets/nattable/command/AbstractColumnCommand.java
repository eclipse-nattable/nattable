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
package org.eclipse.nebula.widgets.nattable.command;

import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public abstract class AbstractColumnCommand implements ILayerCommand {

	private ColumnPositionCoordinate columnPositionCoordinate;

	protected AbstractColumnCommand(ILayer layer, int columnPosition) {
		columnPositionCoordinate = new ColumnPositionCoordinate(layer, columnPosition);
	}
	
	protected AbstractColumnCommand(AbstractColumnCommand command) {
		this.columnPositionCoordinate = command.columnPositionCoordinate;
	}

	public boolean convertToTargetLayer(ILayer targetLayer) {
		ColumnPositionCoordinate targetColumnPositionCoordinate = LayerCommandUtil.convertColumnPositionToTargetContext(columnPositionCoordinate, targetLayer);
		if (targetColumnPositionCoordinate != null) {
			columnPositionCoordinate = targetColumnPositionCoordinate;
			return true;
		} else {
			return false;
		}
	}
	
	public ILayer getLayer() {
		return columnPositionCoordinate.getLayer();
	}
	
	public int getColumnPosition() {
		return columnPositionCoordinate.getColumnPosition();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " columnPosition=" + columnPositionCoordinate.getColumnPosition(); //$NON-NLS-1$
	}
	
}
