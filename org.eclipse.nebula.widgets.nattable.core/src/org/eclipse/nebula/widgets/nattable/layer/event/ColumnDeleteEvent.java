/*******************************************************************************
 * Copyright (c) 2013, 2016 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.event;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;

/**
 * Event indicating that one ore more columns were deleted from the layer.
 */
public class ColumnDeleteEvent extends ColumnStructuralChangeEvent {

    /**
     * Creates a new ColumnDeleteEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given column position matches.
     * @param columnPosition
     *            The column position of the column that was deleted.
     */
    public ColumnDeleteEvent(ILayer layer, int columnPosition) {
        this(layer, new Range(columnPosition, columnPosition + 1));
    }

    /**
     * Creates a new ColumnDeleteEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given column positions match.
     * @param columnPositionRanges
     *            The column position ranges for the columns that were deleted.
     */
    public ColumnDeleteEvent(ILayer layer, Range... columnPositionRanges) {
        super(layer, columnPositionRanges);
    }

    /**
     * Creates a new ColumnDeleteEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given column positions match.
     * @param columnPositionRanges
     *            The column position ranges for the columns that were deleted.
     */
    public ColumnDeleteEvent(ILayer layer, Collection<Range> columnPositionRanges) {
        super(layer, columnPositionRanges);
    }

    /**
     * Creates a new ColumnDeleteEvent based on the given instance. Mainly
     * needed for cloning.
     *
     * @param event
     *            The ColumnDeleteEvent out of which the new instance should be
     *            created.
     */
    protected ColumnDeleteEvent(ColumnStructuralChangeEvent event) {
        super(event);
    }

    @Override
    public Collection<StructuralDiff> getColumnDiffs() {
        Collection<StructuralDiff> columnDiffs = new ArrayList<StructuralDiff>();

        for (Range range : getColumnPositionRanges()) {
            columnDiffs.add(new StructuralDiff(
                    DiffTypeEnum.DELETE,
                    range,
                    new Range(range.start, range.start)));
        }

        return columnDiffs;
    }

    @Override
    public ILayerEvent cloneEvent() {
        return new ColumnDeleteEvent(this);
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        // Bug 478699 - don't modify the ranges on delete because the
        // transformation could result in wrong results
        return true;
    }

}
