/*******************************************************************************
 * Copyright (c) 2012 Edwin Park and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Edwin Park - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.cell;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.InvertUtil;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.swt.graphics.Rectangle;

public class InvertedLayerCell implements ILayerCell {

	private final ILayerCell layerCell;

	public InvertedLayerCell(ILayerCell layerCell) {
		this.layerCell = layerCell;
	}

	public int getOriginColumnPosition() {
		return layerCell.getOriginRowPosition();
	}

	public int getOriginRowPosition() {
		return layerCell.getOriginColumnPosition();
	}

	public ILayer getLayer() {
		return layerCell.getLayer();
	}

	public int getColumnPosition() {
		return layerCell.getRowPosition();
	}

	public int getRowPosition() {
		return layerCell.getColumnPosition();
	}

	public int getColumnIndex() {
		return layerCell.getRowIndex();
	}

	public int getRowIndex() {
		return layerCell.getColumnIndex();
	}

	public int getColumnSpan() {
		return layerCell.getRowSpan();
	}

	public int getRowSpan() {
		return layerCell.getColumnSpan();
	}

	public boolean isSpannedCell() {
		return layerCell.isSpannedCell();
	}

	public String getDisplayMode() {
		return layerCell.getDisplayMode();
	}

	public LabelStack getConfigLabels() {
		return layerCell.getConfigLabels();
	}

	public Object getDataValue() {
		return layerCell.getDataValue();
	}

	public Rectangle getBounds() {
		return InvertUtil.invertRectangle(layerCell.getBounds());
	}
	
}
