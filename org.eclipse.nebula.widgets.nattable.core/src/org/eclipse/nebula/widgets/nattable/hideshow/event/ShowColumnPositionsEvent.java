/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

/**
 * Structural change event to indicate that columns are made visible again.
 */
public class ShowColumnPositionsEvent extends ColumnStructuralChangeEvent {

    /**
     * Constructor.
     *
     * @param layer
     *            The layer to which the given column positions match.
     * @param columnPositions
     *            The column positions that are made visible again.
     */
    public ShowColumnPositionsEvent(IUniqueIndexLayer layer, Collection<Integer> columnPositions) {
        super(layer, PositionUtil.getRanges(columnPositions));
    }

    /**
     * Constructor.
     *
     * @param layer
     *            The layer to which the given column positions match.
     * @param columnPositions
     *            The column positions that are made visible again.
     * @since 2.0
     */
    public ShowColumnPositionsEvent(ILayer layer, int... columnPositions) {
        super(layer, PositionUtil.getRanges(columnPositions));
    }

    /**
     * Clone constructor.
     *
     * @param event
     *            The {@link ShowColumnPositionsEvent} to clone.
     */
    public ShowColumnPositionsEvent(ShowColumnPositionsEvent event) {
        super(event);
    }

    @Override
    public Collection<StructuralDiff> getColumnDiffs() {
        Collection<StructuralDiff> columnDiffs =
                new ArrayList<>(getColumnPositionRanges().size());

        int offset = 0;
        for (Range range : getColumnPositionRanges()) {
            columnDiffs.add(new StructuralDiff(
                    DiffTypeEnum.ADD,
                    new Range(range.start - offset, range.start - offset),
                    range));
            offset += range.size();
        }

        return columnDiffs;
    }

    @Override
    public ShowColumnPositionsEvent cloneEvent() {
        return new ShowColumnPositionsEvent(this);
    }

}
