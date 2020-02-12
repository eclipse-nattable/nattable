/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;

/**
 * Event indicating that one or multiple columns are moved to a new position.
 */
public class ColumnReorderEvent extends ColumnStructuralChangeEvent {

    private ILayer beforeLayer;

    private Collection<Range> beforeFromColumnPositionRanges;
    private MutableIntList beforeFromColumnIndexes;

    private int beforeToColumnPosition;
    private int beforeToColumnIndex;

    private boolean reorderToLeftEdge;

    /**
     *
     * @param layer
     *            The layer to which the column positions match.
     * @param beforeFromColumnPosition
     *            The column position that was reordered, before the reorder
     *            operation was performed.
     * @param beforeToColumnPosition
     *            The position of the column to which the reorder operation was
     *            performed, before the reorder operation was performed
     * @param reorderToLeftEdge
     *            whether the reorder operation was performed to the left or the
     *            right edge.
     *
     * @deprecated Use constructor with explicit index parameters.
     */
    @Deprecated
    public ColumnReorderEvent(ILayer layer, int beforeFromColumnPosition, int beforeToColumnPosition, boolean reorderToLeftEdge) {
        this(layer, beforeFromColumnPosition, beforeFromColumnPosition, beforeToColumnPosition, beforeToColumnPosition, reorderToLeftEdge);
    }

    /**
     *
     * @param layer
     *            The layer to which the column positions match.
     * @param beforeFromColumnPosition
     *            The column position that was reordered, before the reorder
     *            operation was performed.
     * @param beforeFromColumnIndex
     *            The index of the reordered position.
     * @param beforeToColumnPosition
     *            The position of the column to which the reorder operation was
     *            performed, before the reorder operation was performed
     * @param beforeToColumnIndex
     *            The index of the column to which the reorder operation was
     *            performed.
     * @param reorderToLeftEdge
     *            whether the reorder operation was performed to the left or the
     *            right edge.
     *
     * @since 1.6
     */
    public ColumnReorderEvent(ILayer layer,
            int beforeFromColumnPosition,
            int beforeFromColumnIndex,
            int beforeToColumnPosition,
            int beforeToColumnIndex,
            boolean reorderToLeftEdge) {
        this(layer,
                new int[] { beforeFromColumnPosition },
                new int[] { beforeFromColumnIndex },
                beforeToColumnPosition,
                beforeToColumnIndex,
                reorderToLeftEdge);
    }

    /**
     *
     * @param layer
     *            The layer to which the column positions match.
     * @param beforeFromColumnPositions
     *            The column positions that were reordered, before the reorder
     *            operation was performed.
     * @param beforeToColumnPosition
     *            The position of the column to which the reorder operation was
     *            performed, before the reorder operation was performed
     * @param reorderToLeftEdge
     *            whether the reorder operation was performed to the left or the
     *            right edge.
     *
     * @deprecated Use constructor with explicit index parameters.
     */
    @Deprecated
    public ColumnReorderEvent(ILayer layer,
            List<Integer> beforeFromColumnPositions,
            int beforeToColumnPosition,
            boolean reorderToLeftEdge) {
        this(layer, beforeFromColumnPositions, beforeFromColumnPositions, beforeToColumnPosition, beforeToColumnPosition, reorderToLeftEdge);
    }

    /**
     *
     * @param layer
     *            The layer to which the column positions match.
     * @param beforeFromColumnPositions
     *            The column positions that were reordered, before the reorder
     *            operation was performed.
     * @param beforeFromColumnIndexes
     *            The indexes of the reordered positions.
     * @param beforeToColumnPosition
     *            The position of the column to which the reorder operation was
     *            performed, before the reorder operation was performed
     * @param beforeToColumnIndex
     *            The index of the column to which the reorder operation was
     *            performed.
     * @param reorderToLeftEdge
     *            whether the reorder operation was performed to the left or the
     *            right edge.
     *
     * @since 1.6
     */
    public ColumnReorderEvent(ILayer layer,
            List<Integer> beforeFromColumnPositions,
            List<Integer> beforeFromColumnIndexes,
            int beforeToColumnPosition,
            int beforeToColumnIndex,
            boolean reorderToLeftEdge) {

        this(layer,
                beforeFromColumnPositions.stream().mapToInt(Integer::intValue).toArray(),
                beforeFromColumnIndexes.stream().mapToInt(Integer::intValue).toArray(),
                beforeToColumnPosition,
                beforeToColumnIndex,
                reorderToLeftEdge);
    }

    /**
     *
     * @param layer
     *            The layer to which the column positions match.
     * @param beforeFromColumnPositions
     *            The column positions that were reordered, before the reorder
     *            operation was performed.
     * @param beforeFromColumnIndexes
     *            The indexes of the reordered positions.
     * @param beforeToColumnPosition
     *            The position of the column to which the reorder operation was
     *            performed, before the reorder operation was performed
     * @param beforeToColumnIndex
     *            The index of the column to which the reorder operation was
     *            performed.
     * @param reorderToLeftEdge
     *            whether the reorder operation was performed to the left or the
     *            right edge.
     *
     * @since 2.0
     */
    public ColumnReorderEvent(ILayer layer,
            int[] beforeFromColumnPositions,
            int[] beforeFromColumnIndexes,
            int beforeToColumnPosition,
            int beforeToColumnIndex,
            boolean reorderToLeftEdge) {
        super(layer);
        this.beforeLayer = layer;
        this.beforeFromColumnPositionRanges = PositionUtil.getRanges(beforeFromColumnPositions);
        this.beforeFromColumnIndexes = IntLists.mutable.of(beforeFromColumnIndexes);
        this.beforeToColumnPosition = beforeToColumnPosition;
        this.beforeToColumnIndex = beforeToColumnIndex;
        this.reorderToLeftEdge = reorderToLeftEdge;

        MutableIntList allColumnPositions = IntLists.mutable.of(beforeFromColumnPositions);
        allColumnPositions.add(beforeToColumnPosition);
        setColumnPositionRanges(PositionUtil.getRanges(allColumnPositions.toSortedArray()));
    }

    /**
     * Constructor for internal use to clone this event.
     *
     * @param event
     *            The event out of which the new one should be created
     */
    public ColumnReorderEvent(ColumnReorderEvent event) {
        super(event);
        this.beforeLayer = event.beforeLayer;
        this.beforeFromColumnPositionRanges = new ArrayList<Range>(event.beforeFromColumnPositionRanges);
        this.beforeFromColumnIndexes = IntLists.mutable.ofAll(event.beforeFromColumnIndexes);
        this.beforeToColumnPosition = event.beforeToColumnPosition;
        this.beforeToColumnIndex = event.beforeToColumnIndex;
        this.reorderToLeftEdge = event.reorderToLeftEdge;
    }

    /**
     *
     * @return The indexes of the reordered columns.
     * @since 1.6
     */
    public Collection<Integer> getBeforeFromColumnIndexes() {
        return ArrayUtil.asIntegerList(this.beforeFromColumnIndexes.toSortedArray());
    }

    /**
     *
     * @return The indexes of the reordered columns.
     * @since 2.0
     */
    public int[] getBeforeFromColumnIndexesArray() {
        return this.beforeFromColumnIndexes.toSortedArray();
    }

    /**
     *
     * @return The position ranges of the reordered columns.
     */
    public Collection<Range> getBeforeFromColumnPositionRanges() {
        return this.beforeFromColumnPositionRanges;
    }

    /**
     *
     * @return The position of the column to which the reorder operation was
     *         performed.
     */
    public int getBeforeToColumnPosition() {
        return this.beforeToColumnPosition;
    }

    /**
     *
     * @return The index of the column to which the reorder operation was
     *         performed.
     * @since 1.6
     */
    public int getBeforeToColumnIndex() {
        return this.beforeToColumnIndex;
    }

    /**
     * Setter for the beforeToColumnIndex that needs to be called used in case a
     * reorder operation was performed to a hidden column.
     *
     * @param beforeIndex
     *            The index of the column to which the reorder operation was
     *            performed.
     * @since 1.6
     */
    public void setBeforeToColumnIndex(int beforeIndex) {
        this.beforeToColumnIndex = beforeIndex;
    }

    /**
     *
     * @return <code>true</code> if the columns were reordered to the left edge
     *         of the toColumnPosition, <code>false</code> if the reorder
     *         operation was performed on the right edge (e.g. at the end of a
     *         table)
     */
    public boolean isReorderToLeftEdge() {
        return this.reorderToLeftEdge;
    }

    @Override
    public Collection<StructuralDiff> getColumnDiffs() {
        Collection<StructuralDiff> columnDiffs = new ArrayList<StructuralDiff>();

        Collection<Range> beforeFromColumnPositionRanges = getBeforeFromColumnPositionRanges();

        final int beforeToColumnPosition = (this.reorderToLeftEdge)
                ? this.beforeToColumnPosition
                : (this.beforeToColumnPosition + 1);
        int afterAddColumnPosition = beforeToColumnPosition;
        for (Range beforeFromColumnPositionRange : beforeFromColumnPositionRanges) {
            if (beforeFromColumnPositionRange.start < beforeToColumnPosition) {
                afterAddColumnPosition -=
                        Math.min(beforeFromColumnPositionRange.end, beforeToColumnPosition) - beforeFromColumnPositionRange.start;
            } else {
                break;
            }
        }
        int cumulativeAddSize = 0;
        for (Range beforeFromColumnPositionRange : beforeFromColumnPositionRanges) {
            cumulativeAddSize += beforeFromColumnPositionRange.size();
        }

        int offset = 0;
        for (Range beforeFromColumnPositionRange : beforeFromColumnPositionRanges) {
            int afterDeleteColumnPosition = beforeFromColumnPositionRange.start - offset;
            if (afterAddColumnPosition < afterDeleteColumnPosition) {
                afterDeleteColumnPosition += cumulativeAddSize;
            }
            columnDiffs.add(new StructuralDiff(
                    DiffTypeEnum.DELETE,
                    beforeFromColumnPositionRange,
                    new Range(afterDeleteColumnPosition, afterDeleteColumnPosition)));
            offset += beforeFromColumnPositionRange.size();
        }
        Range beforeAddRange = new Range(beforeToColumnPosition, beforeToColumnPosition);
        offset = 0;
        for (Range beforeFromColumnPositionRange : beforeFromColumnPositionRanges) {
            int size = beforeFromColumnPositionRange.size();
            columnDiffs.add(new StructuralDiff(
                    DiffTypeEnum.ADD,
                    beforeAddRange,
                    new Range(afterAddColumnPosition + offset, afterAddColumnPosition + offset + size)));
            offset += size;
        }

        return columnDiffs;
    }

    /**
     * This method is intended to be used for position conversion of the before
     * positions. The locally stored before positions are the positions of the
     * reordered columns positions <b>before</b> the reorder is applied. Needed
     * in case the conversion needs to be done before other states in the given
     * layer are updated, and therefore the local before position cannot be
     * determined anymore.
     *
     * @param layer
     *            The layer that performed the conversion of the before
     *            positions.
     * @param fromColumnPositionRanges
     *            The converted column position ranges that were reordered
     *            before the reorder happened.
     * @param toColumnPosition
     *            The converted column position to which a reorder happened
     *            before the reordering was performed.
     *
     * @since 1.6
     */
    public void setConvertedBeforePositions(ILayer layer, Collection<Range> fromColumnPositionRanges, int toColumnPosition) {
        this.beforeLayer = layer;
        this.beforeFromColumnPositionRanges = fromColumnPositionRanges;
        this.beforeToColumnPosition = toColumnPosition;
    }

    @Override
    public boolean convertToLocal(ILayer targetLayer) {
        // if the conversion was done before, e.g. by an
        // AbstractColumnHideShowLayer, we don't need to perform the conversion
        // again
        if (this.beforeLayer != targetLayer) {
            this.beforeFromColumnPositionRanges =
                    targetLayer.underlyingToLocalColumnPositions(getLayer(), this.beforeFromColumnPositionRanges);
            this.beforeToColumnPosition =
                    targetLayer.underlyingToLocalColumnPosition(getLayer(), this.beforeToColumnPosition);
            this.beforeLayer = targetLayer;
        }

        if (this.beforeToColumnPosition >= 0) {
            return super.convertToLocal(targetLayer);
        } else {
            return false;
        }
    }

    @Override
    public ColumnReorderEvent cloneEvent() {
        return new ColumnReorderEvent(this);
    }

}
