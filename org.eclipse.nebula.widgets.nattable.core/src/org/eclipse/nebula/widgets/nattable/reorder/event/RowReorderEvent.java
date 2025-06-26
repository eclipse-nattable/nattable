/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.collections.api.factory.primitive.IntLists;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;

/**
 * Event indicating that one or multiple rows are moved to a new position.
 */
public class RowReorderEvent extends RowStructuralChangeEvent {

    private ILayer beforeLayer;

    private Collection<Range> beforeFromRowPositionRanges;
    private MutableIntList beforeFromRowIndexes;

    private int beforeToRowPosition;
    private int beforeToRowIndex;

    private boolean reorderToTopEdge;

    /**
     *
     * @param layer
     *            The layer to which the row positions match.
     * @param beforeFromRowPosition
     *            The row position that was reordered, before the reorder
     *            operation was performed.
     * @param beforeFromRowIndex
     *            The index of the reordered position.
     * @param beforeToRowPosition
     *            The position of the row to which the reorder operation was
     *            performed, before the reorder operation was performed
     * @param beforeToRowIndex
     *            The index of the row to which the reorder operation was
     *            performed.
     * @param reorderToTopEdge
     *            whether the reorder operation was performed to the top or the
     *            bottom edge.
     *
     * @since 1.6
     */
    public RowReorderEvent(ILayer layer,
            int beforeFromRowPosition,
            int beforeFromRowIndex,
            int beforeToRowPosition,
            int beforeToRowIndex,
            boolean reorderToTopEdge) {
        this(layer,
                new int[] { beforeFromRowPosition },
                new int[] { beforeFromRowIndex },
                beforeToRowPosition,
                beforeToRowIndex,
                reorderToTopEdge);
    }

    /**
     *
     * @param layer
     *            The layer to which the row positions match.
     * @param beforeFromRowPositions
     *            The row positions that were reordered, before the reorder
     *            operation was performed.
     * @param beforeFromRowIndexes
     *            The indexes of the reordered positions.
     * @param beforeToRowPosition
     *            The position of the row to which the reorder operation was
     *            performed, before the reorder operation was performed
     * @param beforeToRowIndex
     *            The index of the row to which the reorder operation was
     *            performed.
     * @param reorderToTopEdge
     *            whether the reorder operation was performed to the top or the
     *            bottom edge.
     *
     * @since 1.6
     */
    public RowReorderEvent(ILayer layer,
            List<Integer> beforeFromRowPositions,
            List<Integer> beforeFromRowIndexes,
            int beforeToRowPosition,
            int beforeToRowIndex,
            boolean reorderToTopEdge) {

        this(layer,
                beforeFromRowPositions.stream().mapToInt(Integer::intValue).toArray(),
                beforeFromRowIndexes.stream().mapToInt(Integer::intValue).toArray(),
                beforeToRowPosition,
                beforeToRowIndex,
                reorderToTopEdge);
    }

    /**
     *
     * @param layer
     *            The layer to which the row positions match.
     * @param beforeFromRowPositions
     *            The row positions that were reordered, before the reorder
     *            operation was performed.
     * @param beforeFromRowIndexes
     *            The indexes of the reordered positions.
     * @param beforeToRowPosition
     *            The position of the row to which the reorder operation was
     *            performed, before the reorder operation was performed
     * @param beforeToRowIndex
     *            The index of the row to which the reorder operation was
     *            performed.
     * @param reorderToTopEdge
     *            whether the reorder operation was performed to the top or the
     *            bottom edge.
     *
     * @since 2.0
     */
    public RowReorderEvent(ILayer layer,
            int[] beforeFromRowPositions,
            int[] beforeFromRowIndexes,
            int beforeToRowPosition,
            int beforeToRowIndex,
            boolean reorderToTopEdge) {
        super(layer);
        this.beforeLayer = layer;
        this.beforeFromRowPositionRanges = PositionUtil.getRanges(beforeFromRowPositions);
        this.beforeFromRowIndexes = IntLists.mutable.of(beforeFromRowIndexes);
        this.beforeToRowPosition = beforeToRowPosition;
        this.beforeToRowIndex = beforeToRowIndex;
        this.reorderToTopEdge = reorderToTopEdge;

        MutableIntList allColumnPositions = IntLists.mutable.of(beforeFromRowPositions);
        allColumnPositions.add(beforeToRowPosition);
        setRowPositionRanges(PositionUtil.getRanges(allColumnPositions.toSortedArray()));
    }

    /**
     * Constructor for internal use to clone this event.
     *
     * @param event
     *            The event out of which the new one should be created
     */
    public RowReorderEvent(RowReorderEvent event) {
        super(event);
        this.beforeLayer = event.beforeLayer;
        this.beforeFromRowPositionRanges = event.beforeFromRowPositionRanges;
        this.beforeFromRowIndexes = IntLists.mutable.ofAll(event.beforeFromRowIndexes);
        this.beforeToRowPosition = event.beforeToRowPosition;
        this.beforeToRowIndex = event.beforeToRowIndex;
        this.reorderToTopEdge = event.reorderToTopEdge;
    }

    /**
     *
     * @return The indexes of the reordered rows.
     * @since 1.6
     */
    public Collection<Integer> getBeforeFromRowIndexes() {
        return ArrayUtil.asIntegerList(this.beforeFromRowIndexes.toSortedArray());
    }

    /**
     *
     * @return The indexes of the reordered rows.
     * @since 2.0
     */
    public int[] getBeforeFromRowIndexesArray() {
        return this.beforeFromRowIndexes.toSortedArray();
    }

    /**
     *
     * @return The position ranges of the reordered rows.
     */
    public Collection<Range> getBeforeFromRowPositionRanges() {
        return this.beforeFromRowPositionRanges;
    }

    /**
     *
     * @return The position of the row to which the reorder operation was
     *         performed.
     */
    public int getBeforeToRowPosition() {
        return this.beforeToRowPosition;
    }

    /**
     *
     * @return The index of the row to which the reorder operation was
     *         performed.
     * @since 1.6
     */
    public int getBeforeToRowIndex() {
        return this.beforeToRowIndex;
    }

    /**
     * Setter for the beforeToRowIndex that needs to be called used in case a
     * reorder operation was performed to a hidden row.
     *
     * @param beforeIndex
     *            The index of the row to which the reorder operation was
     *            performed.
     * @since 1.6
     */
    public void setBeforeToRowIndex(int beforeIndex) {
        this.beforeToRowIndex = beforeIndex;
    }

    /**
     *
     * @return <code>true</code> if the rows were reordered to the top edge of
     *         the toRowPosition, <code>false</code> if the reorder operation
     *         was performed on the bottom edge (e.g. at the end of a table)
     */
    public boolean isReorderToTopEdge() {
        return this.reorderToTopEdge;
    }

    @Override
    public Collection<StructuralDiff> getRowDiffs() {
        Collection<StructuralDiff> rowDiffs = new ArrayList<>();

        Collection<Range> beforeFromRowPositionRanges = getBeforeFromRowPositionRanges();

        final int beforeToRowPosition = (this.reorderToTopEdge)
                ? this.beforeToRowPosition
                : (this.beforeToRowPosition + 1);
        int afterAddRowPosition = beforeToRowPosition;
        for (Range beforeFromRowPositionRange : beforeFromRowPositionRanges) {
            if (beforeFromRowPositionRange.start < beforeToRowPosition) {
                afterAddRowPosition -=
                        Math.min(beforeFromRowPositionRange.end, beforeToRowPosition) - beforeFromRowPositionRange.start;
            } else {
                break;
            }
        }
        int cumulativeAddSize = 0;
        for (Range beforeFromRowPositionRange : beforeFromRowPositionRanges) {
            cumulativeAddSize += beforeFromRowPositionRange.size();
        }

        int offset = 0;
        for (Range beforeFromRowPositionRange : beforeFromRowPositionRanges) {
            int afterDeleteRowPosition = beforeFromRowPositionRange.start - offset;
            if (afterAddRowPosition < afterDeleteRowPosition) {
                afterDeleteRowPosition += cumulativeAddSize;
            }
            rowDiffs.add(new StructuralDiff(
                    DiffTypeEnum.DELETE,
                    beforeFromRowPositionRange,
                    new Range(afterDeleteRowPosition, afterDeleteRowPosition)));
            offset += beforeFromRowPositionRange.size();
        }
        Range beforeAddRange = new Range(beforeToRowPosition, beforeToRowPosition);
        offset = 0;
        for (Range beforeFromRowPositionRange : beforeFromRowPositionRanges) {
            int size = beforeFromRowPositionRange.size();
            rowDiffs.add(new StructuralDiff(
                    DiffTypeEnum.ADD,
                    beforeAddRange,
                    new Range(afterAddRowPosition + offset, afterAddRowPosition + offset + size)));
            offset += size;
        }

        return rowDiffs;
    }

    /**
     * This method is intended to be used for position conversion of the before
     * positions. The locally stored before positions are the positions of the
     * reordered row positions <b>before</b> the reorder is applied. Needed in
     * case the conversion needs to be done before other states in the given
     * layer are updated, and therefore the local before position cannot be
     * determined anymore.
     *
     * @param layer
     *            The layer that performed the conversion of the before
     *            positions.
     * @param fromRowPositionRanges
     *            The converted row position ranges that were reordered before
     *            the reorder happened.
     * @param toRowPosition
     *            The converted row position to which a reorder happened before
     *            the reordering was performed.
     *
     * @since 1.6
     */
    public void setConvertedBeforePositions(ILayer layer, Collection<Range> fromRowPositionRanges, int toRowPosition) {
        this.beforeLayer = layer;
        this.beforeFromRowPositionRanges = fromRowPositionRanges;
        this.beforeToRowPosition = toRowPosition;
    }

    @Override
    public boolean convertToLocal(ILayer targetLayer) {
        // if the conversion was done before, e.g. by an
        // AbstractRowHideShowLayer, we don't need to perform the conversion
        // again
        if (this.beforeLayer != targetLayer) {
            this.beforeFromRowPositionRanges =
                    targetLayer.underlyingToLocalRowPositions(getLayer(), this.beforeFromRowPositionRanges);
            this.beforeToRowPosition =
                    targetLayer.underlyingToLocalRowPosition(getLayer(), this.beforeToRowPosition);
            this.beforeLayer = targetLayer;
        }

        if (this.beforeToRowPosition >= 0) {
            return super.convertToLocal(targetLayer);
        } else {
            return false;
        }
    }

    @Override
    public RowReorderEvent cloneEvent() {
        return new RowReorderEvent(this);
    }

}
