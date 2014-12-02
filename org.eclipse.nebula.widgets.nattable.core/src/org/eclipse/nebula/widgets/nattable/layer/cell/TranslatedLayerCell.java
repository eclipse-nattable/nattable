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

/**
 * A transformed LayerCell that translates the position of the underlying cell.
 */
public class TranslatedLayerCell extends TransformedLayerCell {

    private ILayer layer;
    private int originColumnPosition;
    private int originRowPosition;
    private int columnPosition;
    private int rowPosition;

    public TranslatedLayerCell(ILayerCell cell, ILayer layer,
            int originColumnPosition, int originRowPosition,
            int columnPosition, int rowPosition) {
        super(cell);
        this.layer = layer;
        this.originColumnPosition = originColumnPosition;
        this.originRowPosition = originRowPosition;
        this.columnPosition = columnPosition;
        this.rowPosition = rowPosition;
    }

    @Override
    public ILayer getLayer() {
        return this.layer;
    }

    @Override
    public int getOriginColumnPosition() {
        return this.originColumnPosition;
    }

    @Override
    public int getOriginRowPosition() {
        return this.originRowPosition;
    }

    @Override
    public int getColumnPosition() {
        return this.columnPosition;
    }

    @Override
    public int getRowPosition() {
        return this.rowPosition;
    }

}
