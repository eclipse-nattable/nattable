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
package org.eclipse.nebula.widgets.nattable.test.fixture;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.DataCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.junit.Ignore;

@Ignore
public class TestLayerCell extends LayerCell {

	public TestLayerCell(ILayer layer, int columnPosition, int rowPosition) {
		super(layer, columnPosition, rowPosition);
	}
	
	public TestLayerCell(ILayer layer, int columnPosition, int rowPosition, DataCell cell) {
		super(layer, columnPosition, rowPosition, cell);
	}

	public TestLayerCell(ILayer layer, int originColumnPosition, int originRowPosition, int columnPosition, int rowPosition, int columnSpan, int rowSpan) {
		super(layer, originColumnPosition, originRowPosition, columnPosition, rowPosition, columnSpan, rowSpan);
	}
	
	public TestLayerCell(ILayerCell cell) {
		super(cell.getLayer(), cell.getOriginColumnPosition(), cell.getOriginRowPosition(), cell.getColumnPosition(), cell.getRowPosition(), cell.getColumnSpan(), cell.getRowSpan());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ILayerCell == false) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		ILayerCell rhs = (ILayerCell) obj;
		return new EqualsBuilder()
			.append(getOriginColumnPosition(), rhs.getOriginColumnPosition())
			.append(getOriginRowPosition(), rhs.getOriginRowPosition())
			.append(getColumnSpan(), rhs.getColumnSpan())
			.append(getRowSpan(), rhs.getRowSpan())
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(771, 855)
			.append(getOriginColumnPosition())
			.append(getOriginRowPosition())
			.append(getColumnSpan())
			.append(getRowSpan())
			.toHashCode();
	}
	
}
