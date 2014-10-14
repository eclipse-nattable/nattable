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
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;

public class ColumnResizeEvent extends ColumnStructuralChangeEvent {

    public ColumnResizeEvent(ILayer layer, int columnPosition) {
        super(layer, new Range(columnPosition, columnPosition + 1));
    }

    public ColumnResizeEvent(ILayer layer, Range columnPositionRange) {
        super(layer, columnPositionRange);
    }

    protected ColumnResizeEvent(ColumnResizeEvent event) {
        super(event);
    }

    @Override
    public ColumnResizeEvent cloneEvent() {
        return new ColumnResizeEvent(this);
    }

    @Override
    public Collection<StructuralDiff> getColumnDiffs() {
        Collection<StructuralDiff> rowDiffs = new ArrayList<StructuralDiff>();

        for (Range range : getColumnPositionRanges()) {
            rowDiffs.add(new StructuralDiff(DiffTypeEnum.CHANGE, range, range));
        }

        return rowDiffs;
    }

}
