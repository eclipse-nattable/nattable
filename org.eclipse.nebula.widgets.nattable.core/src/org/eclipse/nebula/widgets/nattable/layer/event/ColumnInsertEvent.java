/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;

/**
 * Event indicating that one ore more columns were inserted to the layer.
 *
 * @author Dirk Fauth
 *
 */
public class ColumnInsertEvent extends ColumnStructuralChangeEvent {

    /**
     * Creates a new ColumnInsertEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given column position matches.
     * @param columnPosition
     *            The column position of the column that was inserted.
     */
    public ColumnInsertEvent(ILayer layer, int columnPosition) {
        this(layer, new Range(columnPosition, columnPosition + 1));
    }

    /**
     * Creates a new ColumnInsertEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given column positions match.
     * @param columnPositionRanges
     *            The column position ranges for the columns that were inserted.
     */
    public ColumnInsertEvent(ILayer layer, Range... columnPositionRanges) {
        this(layer, Arrays.asList(columnPositionRanges));
    }

    /**
     * Creates a new ColumnInsertEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given column positions match.
     * @param columnPositionRanges
     *            The column position ranges for the columns that were inserted.
     */
    public ColumnInsertEvent(ILayer layer,
            Collection<Range> columnPositionRanges) {
        super(layer, columnPositionRanges);
    }

    /**
     * Creates a new ColumnInsertEvent based on the given instance. Mainly
     * needed for cloning.
     *
     * @param event
     *            The ColumnInsertEvent out of which the new instance should be
     *            created.
     */
    protected ColumnInsertEvent(ColumnStructuralChangeEvent event) {
        super(event);
    }

    @Override
    public Collection<StructuralDiff> getColumnDiffs() {
        Collection<StructuralDiff> columnDiffs = new ArrayList<StructuralDiff>();

        for (Range range : getColumnPositionRanges()) {
            columnDiffs.add(new StructuralDiff(DiffTypeEnum.ADD, new Range(
                    range.start, range.start), range));
        }

        return columnDiffs;
    }

    @Override
    public ColumnInsertEvent cloneEvent() {
        return new ColumnInsertEvent(this);
    }

}
