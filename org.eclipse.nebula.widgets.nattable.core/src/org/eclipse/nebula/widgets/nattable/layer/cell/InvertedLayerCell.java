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

    @Override
    public int getOriginColumnPosition() {
        return this.layerCell.getOriginRowPosition();
    }

    @Override
    public int getOriginRowPosition() {
        return this.layerCell.getOriginColumnPosition();
    }

    @Override
    public ILayer getLayer() {
        return this.layerCell.getLayer();
    }

    @Override
    public int getColumnPosition() {
        return this.layerCell.getRowPosition();
    }

    @Override
    public int getRowPosition() {
        return this.layerCell.getColumnPosition();
    }

    @Override
    public int getColumnIndex() {
        return this.layerCell.getRowIndex();
    }

    @Override
    public int getRowIndex() {
        return this.layerCell.getColumnIndex();
    }

    @Override
    public int getColumnSpan() {
        return this.layerCell.getRowSpan();
    }

    @Override
    public int getRowSpan() {
        return this.layerCell.getColumnSpan();
    }

    @Override
    public boolean isSpannedCell() {
        return this.layerCell.isSpannedCell();
    }

    @Override
    public String getDisplayMode() {
        return this.layerCell.getDisplayMode();
    }

    @Override
    public LabelStack getConfigLabels() {
        return this.layerCell.getConfigLabels();
    }

    @Override
    public Object getDataValue() {
        return this.layerCell.getDataValue();
    }

    @Override
    public Rectangle getBounds() {
        return InvertUtil.invertRectangle(this.layerCell.getBounds());
    }

}
