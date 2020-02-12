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
package org.eclipse.nebula.widgets.nattable.hideshow.event;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;

public class HideRowPositionsEvent extends RowStructuralChangeEvent {

    /**
     * Creates a new HideRowPositionsEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given row positions match.
     * @param rowPositions
     *            The positions of the rows that have changed.
     */
    public HideRowPositionsEvent(ILayer layer, Collection<Integer> rowPositions) {
        super(layer, PositionUtil.getRanges(rowPositions));
    }

    /**
     * Creates a new HideRowPositionsEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given row positions match.
     * @param rowPositions
     *            The positions of the rows that have changed.
     * @since 2.0
     */
    public HideRowPositionsEvent(ILayer layer, int... rowPositions) {
        super(layer, PositionUtil.getRanges(rowPositions));
    }

    /**
     * Creates a new HideRowPositionsEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given row positions match.
     * @param rowPositions
     *            The positions of the rows that have changed.
     * @param rowIndexes
     *            The indexes of the rows that have changed.
     *
     * @since 1.6
     */
    public HideRowPositionsEvent(ILayer layer, Collection<Integer> rowPositions, Collection<Integer> rowIndexes) {
        super(layer, PositionUtil.getRanges(rowPositions), rowIndexes);
    }

    /**
     * Creates a new HideRowPositionsEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given row positions match.
     * @param rowPositions
     *            The positions of the rows that have changed.
     * @param rowIndexes
     *            The indexes of the rows that have changed.
     *
     * @since 2.0
     */
    public HideRowPositionsEvent(ILayer layer, int[] rowPositions, int[] rowIndexes) {
        super(layer, PositionUtil.getRanges(rowPositions), rowIndexes);
    }

    /**
     * Clone constructor.
     *
     * @param event
     *            The event to clone.
     */
    protected HideRowPositionsEvent(HideRowPositionsEvent event) {
        super(event);
    }

    @Override
    public HideRowPositionsEvent cloneEvent() {
        return new HideRowPositionsEvent(this);
    }

    @Override
    public Collection<StructuralDiff> getRowDiffs() {
        Collection<StructuralDiff> rowDiffs =
                new ArrayList<>(getRowPositionRanges().size());

        for (Range range : getRowPositionRanges()) {
            StructuralDiff diff = new StructuralDiff(
                    DiffTypeEnum.DELETE,
                    range,
                    new Range(range.start, range.start));
            rowDiffs.add(diff);
        }

        return rowDiffs;
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        super.convertToLocal(localLayer);
        return true;
    }

}
