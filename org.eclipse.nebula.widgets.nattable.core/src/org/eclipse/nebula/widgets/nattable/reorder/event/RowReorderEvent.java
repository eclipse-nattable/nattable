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
package org.eclipse.nebula.widgets.nattable.reorder.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;

/**
 * Event indicating that one or multiple rows are moved to a new position.
 */
public class RowReorderEvent extends RowStructuralChangeEvent {

    private Collection<Range> beforeFromRowPositionRanges;

    private int beforeToRowPosition;
    private boolean reorderToTopEdge;

    /**
     * @param layer
     * @param beforeFromRowPosition
     * @param beforeToRowPosition
     * @param reorderToTopEdge
     */
    public RowReorderEvent(ILayer layer, int beforeFromRowPosition,
            int beforeToRowPosition, boolean reorderToTopEdge) {

        this(layer, Arrays.asList(new Integer[] { Integer
                .valueOf(beforeFromRowPosition) }), beforeToRowPosition,
                reorderToTopEdge);
    }

    /**
     * @param layer
     * @param beforeFromRowPositions
     * @param beforeToRowPosition
     * @param reorderToTopEdge
     */
    public RowReorderEvent(ILayer layer, List<Integer> beforeFromRowPositions,
            int beforeToRowPosition, boolean reorderToTopEdge) {
        super(layer);
        this.beforeFromRowPositionRanges = PositionUtil
                .getRanges(beforeFromRowPositions);
        this.reorderToTopEdge = reorderToTopEdge;
        this.beforeToRowPosition = beforeToRowPosition;

        List<Integer> allRowPositions = new ArrayList<Integer>(
                beforeFromRowPositions);
        allRowPositions.add(Integer.valueOf(beforeToRowPosition));
        setRowPositionRanges(PositionUtil.getRanges(allRowPositions));
    }

    /**
     * Constructor for internal use to clone this event.
     *
     * @param event
     *            The event out of which the new one should be created
     */
    public RowReorderEvent(RowReorderEvent event) {
        super(event);
        this.beforeFromRowPositionRanges = event.beforeFromRowPositionRanges;
        this.beforeToRowPosition = event.beforeToRowPosition;
        this.reorderToTopEdge = event.reorderToTopEdge;
    }

    public Collection<Range> getBeforeFromRowPositionRanges() {
        return this.beforeFromRowPositionRanges;
    }

    public int getBeforeToRowPosition() {
        return this.beforeToRowPosition;
    }

    public boolean isReorderToTopEdge() {
        return this.reorderToTopEdge;
    }

    @Override
    public Collection<StructuralDiff> getRowDiffs() {
        Collection<StructuralDiff> rowDiffs = new ArrayList<StructuralDiff>();

        Collection<Range> beforeFromRowPositionRanges = getBeforeFromRowPositionRanges();

        final int beforeToRowPosition = (this.reorderToTopEdge) ? this.beforeToRowPosition
                : (this.beforeToRowPosition + 1);
        int afterAddRowPosition = beforeToRowPosition;
        for (Range beforeFromRowPositionRange : beforeFromRowPositionRanges) {
            if (beforeFromRowPositionRange.start < beforeToRowPosition) {
                afterAddRowPosition -= Math.min(beforeFromRowPositionRange.end,
                        beforeToRowPosition) - beforeFromRowPositionRange.start;
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
            int afterDeleteRowPosition = beforeFromRowPositionRange.start
                    - offset;
            if (afterAddRowPosition < afterDeleteRowPosition) {
                afterDeleteRowPosition += cumulativeAddSize;
            }
            rowDiffs.add(new StructuralDiff(DiffTypeEnum.DELETE,
                    beforeFromRowPositionRange, new Range(
                            afterDeleteRowPosition, afterDeleteRowPosition)));
            offset += beforeFromRowPositionRange.size();
        }
        Range beforeAddRange = new Range(beforeToRowPosition,
                beforeToRowPosition);
        offset = 0;
        for (Range beforeFromRowPositionRange : beforeFromRowPositionRanges) {
            int size = beforeFromRowPositionRange.size();
            rowDiffs.add(new StructuralDiff(DiffTypeEnum.ADD, beforeAddRange,
                    new Range(afterAddRowPosition + offset, afterAddRowPosition
                            + offset + size)));
            offset += size;
        }

        return rowDiffs;
    }

    @Override
    public boolean convertToLocal(ILayer targetLayer) {
        this.beforeFromRowPositionRanges = targetLayer
                .underlyingToLocalRowPositions(getLayer(),
                        this.beforeFromRowPositionRanges);
        this.beforeToRowPosition = targetLayer.underlyingToLocalRowPosition(
                getLayer(), this.beforeToRowPosition);

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
