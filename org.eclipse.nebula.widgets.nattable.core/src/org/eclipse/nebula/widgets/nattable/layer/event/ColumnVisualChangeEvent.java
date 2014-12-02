/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
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
    public ColumnVisualChangeEvent(ILayer layer,
            Collection<Range> columnPositionRanges) {
        this.layer = layer;
        this.columnPositionRanges = columnPositionRanges;
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
    }

    @Override
    public ILayer getLayer() {
        return this.layer;
    }

    /**
     * @return The column position ranges for the columns that have changed.
     */
    public Collection<Range> getColumnPositionRanges() {
        return this.columnPositionRanges;
    }

    /**
     * Sets the column position ranges for the columns that have changed. Only
     * for internal use in cases where the constructor needs to calculate the
     * column position ranges within the child constructor.
     *
     * @param columnPositionRanges
     *            The column position ranges for the columns that have changed.
     */
    protected void setColumnPositionRanges(
            Collection<Range> columnPositionRanges) {
        this.columnPositionRanges = columnPositionRanges;
    }

    @Override
    public boolean convertToLocal(ILayer localLayer) {
        this.columnPositionRanges = localLayer.underlyingToLocalColumnPositions(
                this.layer, this.columnPositionRanges);
        this.layer = localLayer;

        return this.columnPositionRanges != null && this.columnPositionRanges.size() > 0;
    }

    @Override
    public Collection<Rectangle> getChangedPositionRectangles() {
        Collection<Rectangle> changedPositionRectangles = new ArrayList<Rectangle>();

        int rowCount = this.layer.getRowCount();
        for (Range range : this.columnPositionRanges) {
            changedPositionRectangles.add(new Rectangle(range.start, 0,
                    range.end - range.start, rowCount));
        }

        return changedPositionRectangles;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
