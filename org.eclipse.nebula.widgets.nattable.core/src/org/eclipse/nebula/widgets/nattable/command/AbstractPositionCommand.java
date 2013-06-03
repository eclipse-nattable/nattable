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

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public abstract class AbstractPositionCommand implements ILayerCommand {

	private PositionCoordinate positionCoordinate;
	
	protected AbstractPositionCommand(ILayer layer, int columnPosition, int rowPosition) {
		positionCoordinate = new PositionCoordinate(layer, columnPosition, rowPosition);
	}
	
	protected AbstractPositionCommand(AbstractPositionCommand command) {
		this.positionCoordinate = command.positionCoordinate;
	}
	
	public boolean convertToTargetLayer(ILayer targetLayer) {
		PositionCoordinate targetPositionCoordinate = LayerCommandUtil.convertPositionToTargetContext(positionCoordinate, targetLayer);
		if (targetPositionCoordinate != null) {
			positionCoordinate = targetPositionCoordinate;
			return true;
		} else {
			return false;
		}
	}
	
	public int getColumnPosition() {
		return positionCoordinate.getColumnPosition();
	}
	
	public int getRowPosition() {
		return positionCoordinate.getRowPosition();
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " columnPosition=" + positionCoordinate.getColumnPosition() + ", rowPosition=" + positionCoordinate.getRowPosition(); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
