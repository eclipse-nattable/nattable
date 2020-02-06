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

/**
 * Structural change event to indicate that rows are made visible again.
 */
public class ShowRowPositionsEvent extends RowStructuralChangeEvent {

    /**
     * Constructor.
     *
     * @param layer
     *            The layer to which the given row positions match.
     * @param rowPositions
     *            The row positions that are made visible again.
     * @deprecated Use {@link #ShowRowPositionsEvent(ILayer, int...)} with
     *             primitive types to avoid autoboxing.
     */
    @Deprecated
    public ShowRowPositionsEvent(ILayer layer, Collection<Integer> rowPositions) {
        super(layer, PositionUtil.getRanges(rowPositions));
    }

    /**
     * Constructor.
     *
     * @param layer
     *            The layer to which the given row positions match.
     * @param rowPositions
     *            The row positions that are made visible again.
     * @since 2.0
     */
    public ShowRowPositionsEvent(ILayer layer, int... rowPositions) {
        super(layer, PositionUtil.getRanges(rowPositions));
    }

    /**
     * Clone constructor.
     *
     * @param event
     *            The {@link ShowRowPositionsEvent} to clone.
     */
    protected ShowRowPositionsEvent(ShowRowPositionsEvent event) {
        super(event);
    }

    @Override
    public Collection<StructuralDiff> getRowDiffs() {
        Collection<StructuralDiff> rowDiffs =
                new ArrayList<>(getRowPositionRanges().size());

        int offset = 0;
        for (Range range : getRowPositionRanges()) {
            rowDiffs.add(new StructuralDiff(
                    DiffTypeEnum.ADD,
                    new Range(range.start - offset, range.start - offset),
                    range));
            offset += range.size();
        }

        return rowDiffs;
    }

    @Override
    public ShowRowPositionsEvent cloneEvent() {
        return new ShowRowPositionsEvent(this);
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        super.convertToLocal(localLayer);
        return true;
    }
}
