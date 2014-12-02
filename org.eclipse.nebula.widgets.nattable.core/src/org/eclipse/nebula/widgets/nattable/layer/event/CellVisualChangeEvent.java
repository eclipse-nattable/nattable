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
package org.eclipse.nebula.widgets.nattable.layer.event;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.graphics.Rectangle;

public class CellVisualChangeEvent implements IVisualChangeEvent {

    protected ILayer layer;

    protected int columnPosition;

    protected int rowPosition;

    public CellVisualChangeEvent(ILayer layer, int columnPosition,
            int rowPosition) {
        this.layer = layer;
        this.columnPosition = columnPosition;
        this.rowPosition = rowPosition;
    }

    protected CellVisualChangeEvent(CellVisualChangeEvent event) {
        this.layer = event.layer;
        this.columnPosition = event.columnPosition;
        this.rowPosition = event.rowPosition;
    }

    @Override
    public ILayer getLayer() {
        return this.layer;
    }

    public int getColumnPosition() {
        return this.columnPosition;
    }

    public int getRowPosition() {
        return this.rowPosition;
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        this.columnPosition = localLayer.underlyingToLocalColumnPosition(getLayer(),
                this.columnPosition);
        this.rowPosition = localLayer.underlyingToLocalRowPosition(getLayer(),
                this.rowPosition);

        this.layer = localLayer;

        return this.columnPosition >= 0 && this.rowPosition >= 0
                && this.columnPosition < this.layer.getColumnCount()
                && this.rowPosition < this.layer.getRowCount();
    }

    @Override
    public Collection<Rectangle> getChangedPositionRectangles() {
        return Arrays.asList(new Rectangle[] { new Rectangle(this.columnPosition,
                this.rowPosition, 1, 1) });
    }

    @Override
    public CellVisualChangeEvent cloneEvent() {
        return new CellVisualChangeEvent(this);
    }

}
