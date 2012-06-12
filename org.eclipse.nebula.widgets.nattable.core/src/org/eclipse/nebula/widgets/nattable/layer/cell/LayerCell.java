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
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.swt.graphics.Rectangle;

public class LayerCell implements ILayerCell {
	
	private ILayer layer;

	private int columnPosition;
	private int rowPosition;

	private ILayer sourceLayer;
	
	private int originColumnPosition;
	private int originRowPosition;
	
	private int columnSpan;
	private int rowSpan;
	
	private boolean isDisplayModeCached = false;
	private String displayMode = null;

	private boolean isConfigLabelsCached = false;
	private LabelStack configLabels = null;

	private boolean isDataValueCached = false;
	private Object dataValue = null;

	private boolean isBoundsCached = false;
	private Rectangle bounds = null;

	public LayerCell(ILayer layer, int columnPosition, int rowPosition, DataCell cell) {
		this(layer, cell.columnPosition, cell.rowPosition, columnPosition, rowPosition, cell.columnSpan, cell.rowSpan);
	}

	public LayerCell(ILayer layer, int columnPosition, int rowPosition) {
		this(layer, columnPosition, rowPosition, columnPosition, rowPosition, 1, 1);
	}

	public LayerCell(ILayer layer, int originColumnPosition, int originRowPosition, int columnPosition, int rowPosition, int columnSpan, int rowSpan) {
		this.sourceLayer = layer;
		this.originColumnPosition = originColumnPosition;
		this.originRowPosition = originRowPosition;
		
		this.layer = layer;
		this.columnPosition = columnPosition;
		this.rowPosition = rowPosition;
		
		this.columnSpan = columnSpan;
		this.rowSpan = rowSpan;
	}

	private boolean isReadMode() {
		return isDisplayModeCached || isConfigLabelsCached || isDataValueCached || isBoundsCached;
	}

	public void updateLayer(ILayer layer) {
		if (isReadMode()) {
			throw new IllegalStateException("Cannot update cell once displayMode, configLabels, dataValue, or bounds have been read"); //$NON-NLS-1$
		}

		this.layer = layer;
	}

	public void updatePosition(ILayer layer, int originColumnPosition, int originRowPosition, int columnPosition, int rowPosition) {
		if (isReadMode()) {
			throw new IllegalStateException("Cannot update cell once displayMode, configLabels, dataValue, or bounds have been read"); //$NON-NLS-1$
		}

		this.layer = layer;
		this.originColumnPosition = originColumnPosition;
		this.originRowPosition = originRowPosition;
		this.columnPosition = columnPosition;
		this.rowPosition = rowPosition;
	}

	public void updateColumnSpan(int columnSpan) {
		if (isReadMode()) {
			throw new IllegalStateException("Cannot update cell once displayMode, configLabels, dataValue, or bounds have been read"); //$NON-NLS-1$
		}

		this.columnSpan = columnSpan;
	}

	public void updateRowSpan(int rowSpan) {
		if (isReadMode()) {
			throw new IllegalStateException("Cannot update cell once displayMode, configLabels, dataValue, or bounds have been read"); //$NON-NLS-1$
		}

		this.rowSpan = rowSpan;
	}

	public ILayer getSourceLayer() {
		return sourceLayer;
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
		return layer.getColumnIndexByPosition(columnPosition);
	}
	
	public int getRowIndex() {
		return layer.getRowIndexByPosition(rowPosition);
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

	public String getDisplayMode() {
		if (!isDisplayModeCached) {
			isDisplayModeCached = true;

			displayMode = layer.getDisplayModeByPosition(columnPosition, rowPosition);
		}

		return displayMode;
	}

	public LabelStack getConfigLabels() {
		if (!isConfigLabelsCached) {
			isConfigLabelsCached = true;

			configLabels = layer.getConfigLabelsByPosition(columnPosition, rowPosition);
		}

		return configLabels;
	}

	public Object getDataValue() {
		if (!isDataValueCached) {
			isDataValueCached = true;

			dataValue = layer.getDataValueByPosition(columnPosition, rowPosition);
		}

		return dataValue;
	}

	public Rectangle getBounds() {
		if (!isBoundsCached) {
			isBoundsCached = true;

			bounds = layer.getBoundsByPosition(columnPosition, rowPosition);
		}

		return bounds;
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
			.append(sourceLayer, rhs.sourceLayer)
			.append(originColumnPosition, rhs.originColumnPosition)
			.append(originRowPosition, rhs.originRowPosition)
			.append(columnSpan, rhs.columnSpan)
			.append(rowSpan, rhs.rowSpan)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(771, 855)
			.append(sourceLayer)
			.append(originColumnPosition)
			.append(originRowPosition)
			.append(columnSpan)
			.append(rowSpan)
			.toHashCode();
	}

	@Override
	public String toString() {
		return "LayerCell: [" //$NON-NLS-1$
			+ "Data: " + dataValue //$NON-NLS-1$
			+ ", sourceLayer: " + sourceLayer.getClass().getSimpleName() //$NON-NLS-1$
			+ ", originColumnPosition: " + originColumnPosition //$NON-NLS-1$
			+ ", originRowPosition: " + originRowPosition //$NON-NLS-1$
			+ ", columnSpan: " + columnSpan //$NON-NLS-1$
			+ ", rowSpan: " + rowSpan //$NON-NLS-1$
			+ "]"; //$NON-NLS-1$
	}
}
