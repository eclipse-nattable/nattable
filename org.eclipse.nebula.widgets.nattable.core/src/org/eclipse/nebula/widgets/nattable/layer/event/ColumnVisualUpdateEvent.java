/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.event;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;

/**
 * Specialization of the ColumnVisualChangeEvent. The only difference is the
 * handling of this type of event in the NatTable event handling. While the
 * ColumnVisualChangeEvent causes a whole redraw operation of the visible part
 * (which is necessary to update everything if a data value has change, for
 * example important for conditional styling), this event only forces to redraw
 * the specified column itself.
 */
public class ColumnVisualUpdateEvent extends ColumnVisualChangeEvent {

    /**
     * Create a new ColumnVisualUpdateEvent based on the given information.
     *
     * @param layer
     *            The layer to which the given column position belongs.
     * @param columnPosition
     *            The column position of the column that needs to be redrawn.
     */
    public ColumnVisualUpdateEvent(ILayer layer, int columnPosition) {
        super(layer, columnPosition);
    }

    /**
     * Create a new ColumnVisualUpdateEvent based on the given information.
     *
     * @param layer
     *            The layer to which the given column positions belong.
     * @param columnPositions
     *            The column positions of the columns that need to be redrawn.
     */
    public ColumnVisualUpdateEvent(IUniqueIndexLayer layer, int[] columnPositions) {
        super(layer, PositionUtil.getRanges(columnPositions));
    }

    /**
     * Create a new ColumnVisualUpdateEvent based on the given information.
     *
     * @param layer
     *            The layer to which the given column positions belong.
     * @param columnPositions
     *            The column position of the columns that need to be redrawn.
     */
    public ColumnVisualUpdateEvent(IUniqueIndexLayer layer, Collection<Integer> columnPositions) {
        super(layer, PositionUtil.getRanges(columnPositions));
    }

    /**
     * Create a new ColumnVisualUpdateEvent out of the given event. Used
     * internally for cloning purposes.
     *
     * @param event
     *            The event to create the clone from.
     */
    protected ColumnVisualUpdateEvent(ColumnVisualUpdateEvent event) {
        super(event);
    }

    @Override
    public ColumnVisualUpdateEvent cloneEvent() {
        return new ColumnVisualUpdateEvent(this);
    }

}
