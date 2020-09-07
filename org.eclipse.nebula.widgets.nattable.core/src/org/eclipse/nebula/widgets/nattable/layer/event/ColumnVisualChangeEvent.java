/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.graphics.Rectangle;

/**
 * An event that indicates a visible change to one ore more columns in the
 * layer.
 */
public abstract class ColumnVisualChangeEvent implements IVisualChangeEvent {

    /**
     * The ILayer to which the given column positions match
     */
    private ILayer layer;
    /**
     * The column position ranges for the columns that have changed. They are
     * related to the set ILayer.
     */
    private Collection<Range> columnPositionRanges;
    /**
     * The indexes of the columns that have changed.
     */
    private int[] columnIndexes;

    /**
     * Creates a new ColumnVisualChangeEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given column position matches.
     * @param columnPosition
     *            The column position of the column that has changed.
     */
    public ColumnVisualChangeEvent(ILayer layer, int columnPosition) {
        this(layer, new Range(columnPosition, columnPosition + 1));
    }

    /**
     * Creates a new ColumnVisualChangeEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given column positions match.
     * @param columnPositionRanges
     *            The column position ranges for the columns that have changed.
     */
    public ColumnVisualChangeEvent(ILayer layer, Range... columnPositionRanges) {
        this(layer, Arrays.asList(columnPositionRanges));
    }

    /**
     * Creates a new ColumnVisualChangeEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given column positions match.
     * @param columnPositionRanges
     *            The column position ranges for the columns that have changed.
     */
    public ColumnVisualChangeEvent(ILayer layer, Collection<Range> columnPositionRanges) {
        this.layer = layer;
        this.columnPositionRanges = columnPositionRanges;
    }

    /**
     * Creates a new ColumnVisualChangeEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given column positions match.
     * @param columnPositionRanges
     *            The column position ranges for the columns that have changed.
     * @param columnIndexes
     *            The indexes of the columns that have changed.
     * @since 1.6
     */
    public ColumnVisualChangeEvent(ILayer layer, Collection<Range> columnPositionRanges, Collection<Integer> columnIndexes) {
        this.layer = layer;
        this.columnPositionRanges = columnPositionRanges;
        this.columnIndexes = columnIndexes.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Creates a new ColumnVisualChangeEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given column positions match.
     * @param columnPositionRanges
     *            The column position ranges for the columns that have changed.
     * @param columnIndexes
     *            The indexes of the columns that have changed.
     * @since 2.0
     */
    public ColumnVisualChangeEvent(ILayer layer, Collection<Range> columnPositionRanges, int... columnIndexes) {
        this.layer = layer;
        this.columnPositionRanges = columnPositionRanges;
        this.columnIndexes = columnIndexes;
    }

    /**
     * Creates a new ColumnVisualChangeEvent based on the given instance. Mainly
     * needed for cloning.
     *
     * @param event
     *            The ColumnVisualChangeEvent out of which the new instance
     *            should be created.
     */
    protected ColumnVisualChangeEvent(ColumnVisualChangeEvent event) {
        this.layer = event.layer;
        this.columnPositionRanges = event.columnPositionRanges;
        this.columnIndexes = event.columnIndexes;
    }

    @Override
    public ILayer getLayer() {
        return this.layer;
    }

    /**
     * @return The column position ranges for the columns that have changed.
     */
    public Collection<Range> getColumnPositionRanges() {
        return this.columnPositionRanges != null ? this.columnPositionRanges : new ArrayList<Range>(0);
    }

    /**
     * Sets the column position ranges for the columns that have changed. Only
     * for internal use in cases where the constructor needs to calculate the
     * column position ranges within the child constructor.
     *
     * @param columnPositionRanges
     *            The column position ranges for the columns that have changed.
     */
    protected void setColumnPositionRanges(Collection<Range> columnPositionRanges) {
        this.columnPositionRanges = columnPositionRanges;
    }

    /**
     *
     * @return The indexes of the columns that have changed.
     * @since 2.0
     */
    public int[] getColumnIndexes() {
        if (this.columnIndexes == null) {
            int[] positions = PositionUtil.getPositions(this.columnPositionRanges);
            this.columnIndexes = new int[positions.length];
            for (int i = 0; i < positions.length; i++) {
                int pos = positions[i];
                this.columnIndexes[i] = this.layer.getColumnIndexByPosition(pos);
            }
        }
        return this.columnIndexes;
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        if (this.columnPositionRanges != null) {
            this.columnPositionRanges = localLayer.underlyingToLocalColumnPositions(this.layer, this.columnPositionRanges);
        }

        this.layer = localLayer;

        return this.columnPositionRanges != null && this.columnPositionRanges.size() > 0;
    }

    @Override
    public Collection<Rectangle> getChangedPositionRectangles() {
        if (this.columnPositionRanges == null) {
            return new ArrayList<>(0);
        }

        int rowCount = this.layer.getRowCount();
        return this.columnPositionRanges.stream()
                .map(range -> new Rectangle(range.start, 0, range.end - range.start, rowCount))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
