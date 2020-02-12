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
package org.eclipse.nebula.widgets.nattable.layer.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.graphics.Rectangle;

/**
 * An event that indicates a visible change to one ore more rows in the layer.
 */
public abstract class RowVisualChangeEvent implements IVisualChangeEvent {

    /**
     * The ILayer to which the given row positions match
     */
    private ILayer layer;
    /**
     * The row position ranges for the rows that have changed. They are related
     * to the set ILayer.
     */
    private Collection<Range> rowPositionRanges;
    /**
     * The indexes of the rows that have changed.
     */
    private int[] rowIndexes;

    /**
     * Creates a new RowVisualChangeEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given row position matches.
     * @param rowPosition
     *            The row position of the row that has changed.
     */
    public RowVisualChangeEvent(ILayer layer, int rowPosition) {
        this(layer, new Range(rowPosition, rowPosition + 1));
    }

    /**
     * Creates a new RowVisualChangeEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given row positions match.
     * @param rowPositionRanges
     *            The row position ranges for the rows that have changed.
     */
    public RowVisualChangeEvent(ILayer layer, Range... rowPositionRanges) {
        this(layer, Arrays.asList(rowPositionRanges));
    }

    /**
     * Creates a new RowVisualChangeEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given row positions match.
     * @param rowPositionRanges
     *            The row position ranges for the rows that have changed.
     */
    public RowVisualChangeEvent(ILayer layer, Collection<Range> rowPositionRanges) {
        this.layer = layer;
        this.rowPositionRanges = rowPositionRanges;
    }

    /**
     * Creates a new RowVisualChangeEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given column positions match.
     * @param rowPositionRanges
     *            The row position ranges for the rows that have changed.
     * @param rowIndexes
     *            The indexes of the rows that have changed.
     * @since 1.6
     */
    public RowVisualChangeEvent(ILayer layer, Collection<Range> rowPositionRanges, Collection<Integer> rowIndexes) {
        this.layer = layer;
        this.rowPositionRanges = rowPositionRanges;
        this.rowIndexes = rowIndexes.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Creates a new RowVisualChangeEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given column positions match.
     * @param rowPositionRanges
     *            The row position ranges for the rows that have changed.
     * @param rowIndexes
     *            The indexes of the rows that have changed.
     * @since 2.0
     */
    public RowVisualChangeEvent(ILayer layer, Collection<Range> rowPositionRanges, int... rowIndexes) {
        this.layer = layer;
        this.rowPositionRanges = rowPositionRanges;
        this.rowIndexes = rowIndexes;
    }

    /**
     * Creates a new RowVisualChangeEvent based on the given instance. Mainly
     * needed for cloning.
     *
     * @param event
     *            The RowVisualChangeEvent out of which the new instance should
     *            be created.
     */
    protected RowVisualChangeEvent(RowVisualChangeEvent event) {
        this.layer = event.layer;
        this.rowPositionRanges = event.rowPositionRanges;
    }

    @Override
    public ILayer getLayer() {
        return this.layer;
    }

    /**
     * @return The row position ranges for the rows that have changed.
     */
    public Collection<Range> getRowPositionRanges() {
        return this.rowPositionRanges;
    }

    /**
     * Sets the row position ranges for the rows that have changed. Only for
     * internal use in cases where the constructor needs to calculate the row
     * position ranges within the child constructor.
     *
     * @param rowPositionRanges
     *            The row position ranges for the rows that have changed.
     */
    protected void setRowPositionRanges(Collection<Range> rowPositionRanges) {
        this.rowPositionRanges = rowPositionRanges;
    }

    /**
     *
     * @return The indexes of the rows that have changed.
     * @since 2.0
     */
    public int[] getRowIndexes() {
        if (this.rowIndexes == null) {
            int[] positions = PositionUtil.getPositions(this.rowPositionRanges);
            this.rowIndexes = new int[positions.length];
            for (int i = 0; i < positions.length; i++) {
                int pos = positions[i];
                this.rowIndexes[i] = this.layer.getRowIndexByPosition(pos);
            }
        }
        return this.rowIndexes;
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        this.rowPositionRanges =
                localLayer.underlyingToLocalRowPositions(this.layer, this.rowPositionRanges);
        this.layer = localLayer;

        return this.rowPositionRanges != null && this.rowPositionRanges.size() > 0;
    }

    @Override
    public Collection<Rectangle> getChangedPositionRectangles() {
        Collection<Rectangle> changedPositionRectangles =
                new ArrayList<>(this.rowPositionRanges.size());

        int columnCount = this.layer.getColumnCount();
        for (Range range : this.rowPositionRanges) {
            changedPositionRectangles.add(
                    new Rectangle(0, range.start, columnCount, range.end - range.start));
        }

        return changedPositionRectangles;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
