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

public final class IndexCoordinate {
	
	public final int columnIndex;
	
	public final int rowIndex;

	public IndexCoordinate(int columnIndex, int rowIndex) {
		this.columnIndex = columnIndex;
		this.rowIndex = rowIndex;
	}
	
	public int getColumnIndex() {
		return columnIndex;
	}
	
	public int getRowIndex() {
		return rowIndex;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + columnIndex + "," + rowIndex + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if (obj instanceof IndexCoordinate == false) {
			return false;
		}
		
		IndexCoordinate that = (IndexCoordinate) obj;
		return this.getColumnIndex() == that.getColumnIndex()
			&& this.getRowIndex() == that.getRowIndex();
	}
	
	@Override
	public int hashCode() {
		int hash = 95;
		hash = 35 * hash + getColumnIndex();
		hash = 35 * hash + getRowIndex() + 87;
		return hash;
	}
}
