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
package org.eclipse.nebula.widgets.nattable.hideshow.event;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;

public class ShowColumnPositionsEvent extends ColumnStructuralChangeEvent {

    public ShowColumnPositionsEvent(IUniqueIndexLayer layer,
            Collection<Integer> columnPositions) {
        super(layer, PositionUtil.getRanges(columnPositions));
    }

    // Copy constructor
    public ShowColumnPositionsEvent(ShowColumnPositionsEvent event) {
        super(event);
    }

    @Override
    public Collection<StructuralDiff> getColumnDiffs() {
        Collection<StructuralDiff> columnDiffs = new ArrayList<StructuralDiff>();

        int offset = 0;
        for (Range range : getColumnPositionRanges()) {
            columnDiffs.add(new StructuralDiff(DiffTypeEnum.ADD, new Range(
                    range.start - offset, range.start - offset), range));
            offset += range.size();
        }

        return columnDiffs;
    }

    @Override
    public ShowColumnPositionsEvent cloneEvent() {
        return new ShowColumnPositionsEvent(this);
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        super.convertToLocal(localLayer);
        return true;
    }

}
