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
package org.eclipse.nebula.widgets.nattable.coordinate;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public final class PositionCoordinate {
	
	private ILayer layer;
	
	public int columnPosition;

	public int rowPosition;

	public PositionCoordinate(ILayer layer, int columnPosition, int rowPosition) {
		this.layer = layer;
		this.columnPosition = columnPosition;
		this.rowPosition = rowPosition;
	}
	
	public PositionCoordinate(PositionCoordinate coord) {
		this(coord.layer, coord.columnPosition, coord.rowPosition);
	}
	
	public ILayer getLayer() {
		return layer;
	}
	
	public int getColumnPosition() {
		return columnPosition;
	}
	
	public void setColumnPosition(int columnPosition) {
		this.columnPosition = columnPosition;
	}
	
	public int getRowPosition() {
		return rowPosition;
	}
	
	public void setRowPosition(int rowPosition) {
		this.rowPosition = rowPosition;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + layer + ":" + columnPosition + "," + rowPosition + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if (obj instanceof PositionCoordinate == false) {
			return false;
		}
		
		PositionCoordinate that = (PositionCoordinate) obj;
		
		return new EqualsBuilder()
			.append(this.layer, that.layer)
			.append(this.columnPosition, that.columnPosition)
			.append(this.rowPosition, that.rowPosition)
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(31, 59)
			.append(layer)
			.append(columnPosition)
			.append(rowPosition)
			.toHashCode();
	}
	
}
