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

public final class RowPositionCoordinate {
	
	private ILayer layer;
	
	public int rowPosition;

	public RowPositionCoordinate(ILayer layer, int rowPosition) {
		this.layer = layer;
		this.rowPosition = rowPosition;
	}
	
	public ILayer getLayer() {
		return layer;
	}
	
	public int getRowPosition() {
	    return rowPosition;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + layer + ":" + rowPosition + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if (obj instanceof RowPositionCoordinate == false) {
			return false;
		}
		
		RowPositionCoordinate that = (RowPositionCoordinate) obj;
		
		return new EqualsBuilder()
			.append(this.layer, that.layer)
			.append(this.rowPosition, that.rowPosition)
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(647, 579)
			.append(layer)
			.append(rowPosition)
			.toHashCode();
	}
	
}
