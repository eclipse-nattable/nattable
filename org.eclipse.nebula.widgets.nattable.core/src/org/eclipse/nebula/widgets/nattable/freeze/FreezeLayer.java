/*******************************************************************************
 * Copyright (c) 2012, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 451217
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.freeze;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.freeze.event.FreezeEventHandler;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;

public class FreezeLayer extends AbstractIndexLayerTransform {

    public static final String PERSISTENCE_TOP_LEFT_POSITION = ".freezeTopLeftPosition"; //$NON-NLS-1$
    public static final String PERSISTENCE_BOTTOM_RIGHT_POSITION = ".freezeBottomRightPosition"; //$NON-NLS-1$

    private PositionCoordinate topLeftPosition = new PositionCoordinate(this, -1, -1);

    private PositionCoordinate bottomRightPosition = new PositionCoordinate(this, -1, -1);

    public FreezeLayer(IUniqueIndexLayer underlyingLayer) {
        super(underlyingLayer);

        registerEventHandler(new FreezeEventHandler(this));
    }

    public boolean isFrozen() {
        return getColumnCount() > 0 || getRowCount() > 0;
    }

    // Coordinates

    public PositionCoordinate getTopLeftPosition() {
        return this.topLeftPosition;
    }

    public void setTopLeftPosition(int leftColumnPosition, int topRowPosition) {
        this.topLeftPosition =
                new PositionCoordinate(this, leftColumnPosition, topRowPosition);
    }

    public PositionCoordinate getBottomRightPosition() {
        return this.bottomRightPosition;
    }

    public void setBottomRightPosition(int rightColumnPosition, int bottomRowPosition) {
        this.bottomRightPosition =
                new PositionCoordinate(this, rightColumnPosition, bottomRowPosition);
    }

    // Column features

    @Override
    public int getColumnCount() {
        if (this.topLeftPosition.columnPosition >= 0
                && this.bottomRightPosition.columnPosition >= 0) {
            return this.bottomRightPosition.columnPosition
                    - this.topLeftPosition.columnPosition + 1;
        } else {
            return 0;
        }
    }

    @Override
    public int getPreferredColumnCount() {
        return getColumnCount();
    }

    @Override
    public int localToUnderlyingColumnPosition(int localColumnPosition) {
        if (this.topLeftPosition.columnPosition >= 0) {
            return this.topLeftPosition.columnPosition + localColumnPosition;
        }
        return -1;
    }

    @Override
    public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer,
            int underlyingColumnPosition) {
        if (sourceUnderlyingLayer != getUnderlyingLayer()) {
            return -1;
        }
        if (underlyingColumnPosition >= this.topLeftPosition.columnPosition
                && underlyingColumnPosition <= this.bottomRightPosition.columnPosition) {
            return underlyingColumnPosition - this.topLeftPosition.columnPosition;
        }
        return -1;
    }

    @Override
    public int getWidth() {
        int width = 0;
        for (int columnPosition = 0; columnPosition < getColumnCount(); columnPosition++) {
            width += getColumnWidthByPosition(columnPosition);
        }
        return width;
    }

    @Override
    public int getPreferredWidth() {
        return getWidth();
    }

    @Override
    public int getColumnPositionByX(int x) {
        IUniqueIndexLayer underlyingLayer = getUnderlyingLayer();
        int xOffset = underlyingLayer.getStartXOfColumnPosition(this.topLeftPosition.columnPosition);
        return underlyingToLocalColumnPosition(
                underlyingLayer, underlyingLayer.getColumnPositionByX(xOffset + x));
    }

    @Override
    public int getStartXOfColumnPosition(int columnPosition) {
        if (columnPosition < 0 || columnPosition >= getColumnCount()) {
            return -1;
        }
        IUniqueIndexLayer underlyingLayer = getUnderlyingLayer();
        final int underlyingColumnPosition =
                LayerUtil.convertColumnPosition(this, columnPosition, underlyingLayer);
        if (underlyingColumnPosition < 0) {
            return -1;
        }
        return underlyingLayer.getStartXOfColumnPosition(underlyingColumnPosition)
                - underlyingLayer.getStartXOfColumnPosition(this.topLeftPosition.columnPosition);
    }

    // Row features

    @Override
    public int getRowCount() {
        if (this.topLeftPosition.rowPosition >= 0
                && this.bottomRightPosition.rowPosition >= 0) {
            int frozenRowCount =
                    this.bottomRightPosition.rowPosition - this.topLeftPosition.rowPosition + 1;
            int underlyingRowCount = getUnderlyingLayer().getRowCount();
            return frozenRowCount <= underlyingRowCount ? frozenRowCount : underlyingRowCount;
        } else {
            return 0;
        }
    }

    @Override
    public int getPreferredRowCount() {
        return getRowCount();
    }

    @Override
    public int localToUnderlyingRowPosition(int localRowPosition) {
        if (this.topLeftPosition.rowPosition >= 0) {
            return this.topLeftPosition.rowPosition + localRowPosition;
        }
        return -1;
    }

    @Override
    public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer,
            int underlyingRowPosition) {
        if (sourceUnderlyingLayer != getUnderlyingLayer()) {
            return -1;
        }
        if (underlyingRowPosition >= this.topLeftPosition.rowPosition
                && underlyingRowPosition <= this.bottomRightPosition.rowPosition) {
            return underlyingRowPosition - this.topLeftPosition.rowPosition;
        }
        return -1;
    }

    @Override
    public int getHeight() {
        int height = 0;
        for (int rowPosition = 0; rowPosition < getRowCount(); rowPosition++) {
            height += getRowHeightByPosition(rowPosition);
        }
        return height;
    }

    @Override
    public int getPreferredHeight() {
        return getHeight();
    }

    @Override
    public int getRowPositionByY(int y) {
        IUniqueIndexLayer underlyingLayer = getUnderlyingLayer();
        int yOffset = underlyingLayer.getStartYOfRowPosition(this.topLeftPosition.rowPosition);
        return underlyingToLocalRowPosition(
                underlyingLayer, underlyingLayer.getRowPositionByY(yOffset + y));
    }

    @Override
    public int getStartYOfRowPosition(int rowPosition) {
        if (rowPosition < 0 || rowPosition >= getRowCount()) {
            return -1;
        }
        IUniqueIndexLayer underlyingLayer = getUnderlyingLayer();
        final int underlyingRowPosition =
                LayerUtil.convertRowPosition(this, rowPosition, underlyingLayer);
        if (underlyingRowPosition < 0) {
            return -1;
        }
        return underlyingLayer.getStartYOfRowPosition(underlyingRowPosition)
                - underlyingLayer.getStartYOfRowPosition(this.topLeftPosition.rowPosition);
    }
}
