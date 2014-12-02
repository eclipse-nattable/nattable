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
 * Specialization of the RowVisualChangeEvent. The only difference is the
 * handling of this type of event in the NatTable event handling. While the
 * RowVisualChangeEvent causes a whole redraw operation of the visible part
 * (which is necessary to update everything if a data value has change, for
 * example important for conditional styling), this event only forces to redraw
 * the specified row itself.
 *
 * @author Dirk Fauth
 *
 */
public class RowVisualUpdateEvent extends RowVisualChangeEvent {

    /**
     * Create a new RowVisualUpdateEvent based on the given information.
     *
     * @param layer
     *            The layer to which the given row position belongs.
     * @param rowPosition
     *            The row position of the row that needs to be redrawn.
     */
    public RowVisualUpdateEvent(ILayer layer, int rowPosition) {
        super(layer, rowPosition);
    }

    /**
     * Create a new RowVisualUpdateEvent based on the given information.
     *
     * @param layer
     *            The layer to which the given column and row position belong.
     * @param rowPositions
     *            The row positions of the rows that need to be redrawn.
     */
    public RowVisualUpdateEvent(IUniqueIndexLayer layer, int[] rowPositions) {
        super(layer, PositionUtil.getRanges(rowPositions));
    }

    /**
     * Create a new RowVisualUpdateEvent based on the given information.
     *
     * @param layer
     *            The layer to which the given row positions belong.
     * @param rowPositions
     *            The row positions of the rows that need to be redrawn.
     */
    public RowVisualUpdateEvent(IUniqueIndexLayer layer,
            Collection<Integer> rowPositions) {
        super(layer, PositionUtil.getRanges(rowPositions));
    }

    /**
     * Create a new RowVisualUpdateEvent out of the given event. Used internally
     * for cloning purposes.
     *
     * @param event
     *            The event to create the clone from.
     */
    protected RowVisualUpdateEvent(RowVisualUpdateEvent event) {
        super(event);
    }

    @Override
    public RowVisualUpdateEvent cloneEvent() {
        return new RowVisualUpdateEvent(this);
    }

}
