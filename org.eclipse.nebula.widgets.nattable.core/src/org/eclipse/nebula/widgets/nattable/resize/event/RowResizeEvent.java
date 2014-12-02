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
package org.eclipse.nebula.widgets.nattable.resize.event;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;

public class RowResizeEvent extends RowStructuralChangeEvent {

    public RowResizeEvent(ILayer layer, int rowPosition) {
        super(layer, new Range(rowPosition, rowPosition + 1));
    }

    public RowResizeEvent(ILayer layer, Range rowPositionRange) {
        super(layer, rowPositionRange);
    }

    protected RowResizeEvent(RowResizeEvent event) {
        super(event);
    }

    @Override
    public RowResizeEvent cloneEvent() {
        return new RowResizeEvent(this);
    }

    @Override
    public Collection<StructuralDiff> getRowDiffs() {
        Collection<StructuralDiff> rowDiffs = new ArrayList<StructuralDiff>();

        for (Range range : getRowPositionRanges()) {
            new StructuralDiff(DiffTypeEnum.CHANGE, range, range);
        }

        return rowDiffs;
    }

}
