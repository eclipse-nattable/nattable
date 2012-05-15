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

public final class ColumnPositionCoordinate {
	
	private ILayer layer;
	
	public int columnPosition;

	public ColumnPositionCoordinate(ILayer layer, int columnPosition) {
		this.layer = layer;
		this.columnPosition = columnPosition;
	}
	
	public ILayer getLayer() {
		return layer;
	}
	
    public int getColumnPosition() {
        return columnPosition;
    }
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + layer + ":" + columnPosition + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if (obj instanceof ColumnPositionCoordinate == false) {
			return false;
		}
		
		ColumnPositionCoordinate that = (ColumnPositionCoordinate) obj;
		
		return new EqualsBuilder()
			.append(this.layer, that.layer)
			.append(this.columnPosition, that.columnPosition)
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(133, 95)
			.append(layer)
			.append(columnPosition)
			.toHashCode();
	}
	
}
