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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @see ColumnStructuralChangeEvent
 */
public abstract class RowStructuralChangeEvent extends RowVisualChangeEvent
        implements IStructuralChangeEvent {

    public RowStructuralChangeEvent(ILayer layer, Range... rowPositionRanges) {
        this(layer, Arrays.asList(rowPositionRanges));
    }

    public RowStructuralChangeEvent(ILayer layer,
            Collection<Range> rowPositionRanges) {
        super(layer, rowPositionRanges);
    }

    protected RowStructuralChangeEvent(RowStructuralChangeEvent event) {
        super(event);
    }

    @Override
    public Collection<Rectangle> getChangedPositionRectangles() {
        Collection<Rectangle> changedPositionRectangles = new ArrayList<Rectangle>();

        int columnCount = getLayer().getColumnCount();
        int rowCount = getLayer().getRowCount();
        for (Range range : getRowPositionRanges()) {
            changedPositionRectangles.add(new Rectangle(0, range.start,
                    columnCount, rowCount - range.start));
        }

        return changedPositionRectangles;
    }

    @Override
    public boolean isHorizontalStructureChanged() {
        return false;
    }

    @Override
    public Collection<StructuralDiff> getColumnDiffs() {
        return null;
    }

    @Override
    public boolean isVerticalStructureChanged() {
        return true;
    }

}
