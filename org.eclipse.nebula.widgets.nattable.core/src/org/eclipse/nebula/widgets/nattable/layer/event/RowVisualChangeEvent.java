/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.graphics.Rectangle;

public abstract class RowVisualChangeEvent implements IVisualChangeEvent {

    private ILayer layer;

    private Collection<Range> rowPositionRanges = new ArrayList<Range>();

    public RowVisualChangeEvent(ILayer layer, int rowPosition) {
        this(layer, new Range(rowPosition, rowPosition + 1));
    }

    public RowVisualChangeEvent(ILayer layer, Range... rowPositionRanges) {
        this(layer, Arrays.asList(rowPositionRanges));
    }

    public RowVisualChangeEvent(ILayer layer,
            Collection<Range> rowPositionRanges) {
        this.layer = layer;
        this.rowPositionRanges = rowPositionRanges;
    }

    // Copy constructor
    protected RowVisualChangeEvent(RowVisualChangeEvent event) {
        this.layer = event.layer;
        this.rowPositionRanges = event.rowPositionRanges;
    }

    @Override
    public ILayer getLayer() {
        return this.layer;
    }

    public Collection<Range> getRowPositionRanges() {
        return this.rowPositionRanges;
    }

    protected void setRowPositionRanges(Collection<Range> rowPositionRanges) {
        this.rowPositionRanges = rowPositionRanges;
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        this.rowPositionRanges = localLayer.underlyingToLocalRowPositions(this.layer,
                this.rowPositionRanges);
        this.layer = localLayer;

        return this.rowPositionRanges != null && this.rowPositionRanges.size() > 0;
    }

    @Override
    public Collection<Rectangle> getChangedPositionRectangles() {
        Collection<Rectangle> changedPositionRectangles = new ArrayList<Rectangle>();

        int columnCount = this.layer.getColumnCount();
        for (Range range : this.rowPositionRanges) {
            changedPositionRectangles.add(new Rectangle(0, range.start,
                    columnCount, range.end - range.start));
        }

        return changedPositionRectangles;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
