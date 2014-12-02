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
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;

/**
 * Event indicating that one ore more rows were inserted to the layer.
 */
public class RowInsertEvent extends RowStructuralChangeEvent {

    /**
     * Creates a new RowInsertEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given row position matches.
     * @param rowPosition
     *            The row position of the row that was inserted.
     */
    public RowInsertEvent(ILayer layer, int rowPosition) {
        this(layer, new Range(rowPosition, rowPosition + 1));
    }

    /**
     * Creates a new RowInsertEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given row positions match.
     * @param rowPositionRanges
     *            The row position ranges for the rows that were inserted.
     */
    public RowInsertEvent(ILayer layer, Range... rowPositionRanges) {
        super(layer, Arrays.asList(rowPositionRanges));
    }

    /**
     * Creates a new RowInsertEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given row positions match.
     * @param rowPositionRanges
     *            The row position ranges for the rows that were inserted.
     */
    public RowInsertEvent(ILayer layer, Collection<Range> rowPositionRanges) {
        super(layer, rowPositionRanges);
    }

    /**
     * Creates a new RowInsertEvent based on the given instance. Mainly needed
     * for cloning.
     *
     * @param event
     *            The RowInsertEvent out of which the new instance should be
     *            created.
     */
    protected RowInsertEvent(RowInsertEvent event) {
        super(event);
    }

    @Override
    public RowInsertEvent cloneEvent() {
        return new RowInsertEvent(this);
    }

    @Override
    public Collection<StructuralDiff> getRowDiffs() {
        Collection<StructuralDiff> rowDiffs = new ArrayList<StructuralDiff>();

        for (Range range : getRowPositionRanges()) {
            rowDiffs.add(new StructuralDiff(DiffTypeEnum.ADD, new Range(
                    range.start, range.start), range));
        }

        return rowDiffs;
    }

}
