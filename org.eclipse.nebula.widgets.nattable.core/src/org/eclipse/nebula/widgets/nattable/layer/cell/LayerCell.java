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
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class LayerCell extends AbstractLayerCell {
	
	private ILayer layer;

	private int columnPosition;
	private int rowPosition;

	private int originColumnPosition;
	private int originRowPosition;
	
	private int columnSpan;
	private int rowSpan;

	public LayerCell(ILayer layer, int columnPosition, int rowPosition, DataCell cell) {
		this(layer, cell.columnPosition, cell.rowPosition, columnPosition, rowPosition, cell.columnSpan, cell.rowSpan);
	}

	public LayerCell(ILayer layer, int columnPosition, int rowPosition) {
		this(layer, columnPosition, rowPosition, columnPosition, rowPosition, 1, 1);
	}

	public LayerCell(ILayer layer, int originColumnPosition, int originRowPosition, int columnPosition, int rowPosition, int columnSpan, int rowSpan) {
		this.layer = layer;
		
		this.originColumnPosition = originColumnPosition;
		this.originRowPosition = originRowPosition;
		
		this.columnPosition = columnPosition;
		this.rowPosition = rowPosition;
		
		this.columnSpan = columnSpan;
		this.rowSpan = rowSpan;
	}

	public int getOriginColumnPosition() {
		return originColumnPosition;
	}
	
	public int getOriginRowPosition() {
		return originRowPosition;
	}
	
	public ILayer getLayer() {
		return layer;
	}
	
	public int getColumnPosition() {
		return columnPosition;
	}
	
	public int getRowPosition() {
		return rowPosition;
	}
	
	public int getColumnIndex() {
		return getLayer().getColumnIndexByPosition(getColumnPosition());
	}
	
	public int getRowIndex() {
		return getLayer().getRowIndexByPosition(getRowPosition());
	}
	
	public int getColumnSpan() {
		return columnSpan;
	}
	
	public int getRowSpan() {
		return rowSpan;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LayerCell == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		LayerCell rhs = (LayerCell) obj;
		return new EqualsBuilder()
			.append(layer, rhs.layer)
			.append(originColumnPosition, rhs.originColumnPosition)
			.append(originRowPosition, rhs.originRowPosition)
			.append(columnSpan, rhs.columnSpan)
			.append(rowSpan, rhs.rowSpan)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(771, 855)
			.append(layer)
			.append(originColumnPosition)
			.append(originRowPosition)
			.append(columnSpan)
			.append(rowSpan)
			.toHashCode();
	}

	@Override
	public String toString() {
		return "LayerCell: [" //$NON-NLS-1$
			+ "Data: " + getDataValue() //$NON-NLS-1$
			+ ", layer: " + getLayer().getClass().getSimpleName() //$NON-NLS-1$
			+ ", originColumnPosition: " + getOriginColumnPosition() //$NON-NLS-1$
			+ ", originRowPosition: " + getOriginRowPosition() //$NON-NLS-1$
			+ ", columnSpan: " + getColumnSpan() //$NON-NLS-1$
			+ ", rowSpan: " + getRowSpan() //$NON-NLS-1$
			+ "]"; //$NON-NLS-1$
	}
}
