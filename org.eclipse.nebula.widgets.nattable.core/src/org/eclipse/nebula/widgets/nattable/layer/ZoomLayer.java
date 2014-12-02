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
package org.eclipse.nebula.widgets.nattable.layer;

public class ZoomLayer extends AbstractLayerTransform {

    private float zoomFactor = 1;

    public ZoomLayer(ILayer underlyingILayer) {
        super(underlyingILayer);
    }

    public float getZoomFactor() {
        return this.zoomFactor;
    }

    public void setZoomFactor(float zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    // Horizontal features

    // Width

    @Override
    public int getWidth() {
        return (int) (this.zoomFactor * super.getWidth());
    }

    @Override
    public int getPreferredWidth() {
        return (int) (this.zoomFactor * super.getPreferredWidth());
    }

    @Override
    public int getColumnWidthByPosition(int columnPosition) {
        return (int) (this.zoomFactor * super
                .getColumnWidthByPosition(columnPosition));
    }

    // X

    @Override
    public int getColumnPositionByX(int x) {
        return super.getColumnPositionByX((int) (x / this.zoomFactor));
    }

    @Override
    public int getStartXOfColumnPosition(int columnPosition) {
        return (int) (this.zoomFactor * super
                .getStartXOfColumnPosition(columnPosition));
    }

    // Vertical features

    // Height

    @Override
    public int getHeight() {
        return (int) (this.zoomFactor * super.getHeight());
    }

    @Override
    public int getPreferredHeight() {
        return (int) (this.zoomFactor * super.getPreferredHeight());
    }

    @Override
    public int getRowHeightByPosition(int rowPosition) {
        return (int) (this.zoomFactor * super.getRowHeightByPosition(rowPosition));
    }

    // Y

    @Override
    public int getRowPositionByY(int y) {
        return super.getRowPositionByY((int) (y / this.zoomFactor));
    }

    @Override
    public int getStartYOfRowPosition(int rowPosition) {
        return (int) (this.zoomFactor * super.getStartYOfRowPosition(rowPosition));
    }

    @Override
    public LabelStack getRegionLabelsByXY(int x, int y) {
        return super.getRegionLabelsByXY((int) (x / this.zoomFactor),
                (int) (y / this.zoomFactor));
    }

}
