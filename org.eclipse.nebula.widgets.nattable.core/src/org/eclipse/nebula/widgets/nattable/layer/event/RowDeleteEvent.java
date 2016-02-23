/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;

/**
 * Event indicating that one ore more rows were deleted from the layer.
 */
public class RowDeleteEvent extends RowStructuralChangeEvent {

    /**
     * Creates a new RowDeleteEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given row position matches.
     * @param rowPosition
     *            The row position of the row that was deleted.
     */
    public RowDeleteEvent(ILayer layer, int rowPosition) {
        this(layer, new Range(rowPosition, rowPosition + 1));
    }

    /**
     * Creates a new RowDeleteEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given row positions match.
     * @param rowPositionRanges
     *            The row position ranges for the rows that were deleted.
     */
    public RowDeleteEvent(ILayer layer, Range... rowPositionRanges) {
        super(layer, Arrays.asList(rowPositionRanges));
    }

    /**
     * Creates a new RowDeleteEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given row positions match.
     * @param rowPositionRanges
     *            The row position ranges for the rows that were deleted.
     */
    public RowDeleteEvent(ILayer layer, Collection<Range> rowPositionRanges) {
        super(layer, rowPositionRanges);
    }

    /**
     * Creates a new RowDeleteEvent based on the given instance. Mainly needed
     * for cloning.
     *
     * @param event
     *            The RowDeleteEvent out of which the new instance should be
     *            created.
     */
    protected RowDeleteEvent(RowDeleteEvent event) {
        super(event);
    }

    @Override
    public RowDeleteEvent cloneEvent() {
        return new RowDeleteEvent(this);
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        return true;
    }

    public Collection<Integer> getDeletedRowIndexes() {
        Set<Integer> rowIndexes = new HashSet<Integer>();
        for (Range range : getRowPositionRanges()) {
            for (int i = range.start; i < range.end; i++) {
                rowIndexes.add(getLayer().getRowIndexByPosition(i));
            }
        }
        return getDeletedRowIndexes();
    }

    @Override
    public Collection<StructuralDiff> getRowDiffs() {
        Collection<StructuralDiff> rowDiffs = new ArrayList<StructuralDiff>();

        for (Range range : getRowPositionRanges()) {
            rowDiffs.add(new StructuralDiff(
                    DiffTypeEnum.DELETE,
                    range,
                    new Range(range.start, range.start)));
        }

        return rowDiffs;
    }

}
