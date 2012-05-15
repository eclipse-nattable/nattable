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
package org.eclipse.nebula.widgets.nattable.layer.cell;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class DataCell {

	protected int columnPosition;
	
	protected int rowPosition;
	
	protected int columnSpan;
	
	protected int rowSpan;
	
	public DataCell(int columnPosition, int rowPosition) {
		this(columnPosition, rowPosition, 1, 1);
	}	
	
	public DataCell(int columnPosition, int rowPosition, int columnSpan, int rowSpan) {
		this.columnPosition = columnPosition;
		this.rowPosition = rowPosition;
		this.columnSpan = columnSpan;
		this.rowSpan = rowSpan;
	}
	
	public int getColumnPosition() {
		return columnPosition;
	}
	
	public int getRowPosition() {
		return rowPosition;
	}
	
	public int getColumnSpan() {
		return columnSpan;
	}
	
	public int getRowSpan() {
		return rowSpan;
	}
	
	public boolean isSpannedCell() {
		return columnSpan > 1 || rowSpan > 1;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DataCell == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		DataCell rhs = (DataCell) obj;
		return new EqualsBuilder()
			.append(columnPosition, rhs.columnPosition)
			.append(rowPosition, rhs.rowPosition)
			.append(columnSpan, rhs.columnSpan)
			.append(rowSpan, rhs.rowSpan)
			.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(423, 971)
			.append(columnPosition)
			.append(rowPosition)
			.append(columnSpan)
			.append(rowSpan)
			.toHashCode();
	}
	
}
