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
package org.eclipse.nebula.widgets.nattable.edit.event;


import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.swt.widgets.Composite;

public class InlineCellEditEvent implements ILayerEvent {

	private final PositionCoordinate cellCoordinate;
	private ILayer layer;
	private final Composite parent;
	private final IConfigRegistry configRegistry;
	private final Character initialValue;

	public InlineCellEditEvent(ILayer layer, PositionCoordinate cellCoordinate, Composite parent, IConfigRegistry configRegistry, Character initialValue) {
		this.layer = layer;
		this.cellCoordinate = cellCoordinate;
		this.parent = parent;
		this.configRegistry = configRegistry;
		this.initialValue = initialValue;
	}
	
	public boolean convertToLocal(ILayer localLayer) {
		cellCoordinate.columnPosition = localLayer.underlyingToLocalColumnPosition(layer, cellCoordinate.columnPosition);
		if (cellCoordinate.columnPosition < 0 || cellCoordinate.columnPosition >= localLayer.getColumnCount()) {
			return false;
		}
		
		cellCoordinate.rowPosition = localLayer.underlyingToLocalRowPosition(layer, cellCoordinate.rowPosition);
		if (cellCoordinate.rowPosition < 0 || cellCoordinate.rowPosition >= localLayer.getRowCount()) {
			return false;
		}
		
		this.layer = localLayer;
		return true;
	}

	public int getColumnPosition() {
		return cellCoordinate.columnPosition;
	}
	
	public int getRowPosition() {
		return cellCoordinate.rowPosition;
	}
	
	public Composite getParent() {
		return parent;
	}
	
	public IConfigRegistry getConfigRegistry() {
		return configRegistry;
	}
	
	public Character getInitialValue() {
		return initialValue;
	}
	
	public InlineCellEditEvent cloneEvent() {
		return new InlineCellEditEvent(layer, cellCoordinate, parent, configRegistry, initialValue);
	}
	
}
