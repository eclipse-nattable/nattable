/*******************************************************************************
 * Copyright (c) 2023 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.group.performance.event;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.hideshow.event.HideColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Specialization of the {@link HideColumnPositionsEvent}. Mainly used to inform
 * about hidden columns but avoid handling it in the ColumnGroupHeaderLayer.
 *
 * @since 2.1
 */
public class ColumnGroupCollapseEvent extends HideColumnPositionsEvent {

    /**
     * Creates a new ColumnGroupCollapseEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given column positions match.
     * @param columnPositions
     *            The positions of the columns that have changed.
     */
    public ColumnGroupCollapseEvent(ILayer layer, Collection<Integer> columnPositions) {
        super(layer, columnPositions);
    }

    /**
     * Creates a new ColumnGroupCollapseEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given column positions match.
     * @param columnPositions
     *            The positions of the columns that have changed.
     */
    public ColumnGroupCollapseEvent(ILayer layer, int... columnPositions) {
        super(layer, columnPositions);
    }

    /**
     * Creates a new ColumnGroupCollapseEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given column positions match.
     * @param columnPositions
     *            The positions of the columns that have changed.
     * @param columnIndexes
     *            The indexes of the columns that have changed.
     */
    public ColumnGroupCollapseEvent(ILayer layer, Collection<Integer> columnPositions, Collection<Integer> columnIndexes) {
        super(layer, columnPositions, columnIndexes);
    }

    /**
     * Creates a new ColumnGroupCollapseEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given column positions match.
     * @param columnPositions
     *            The positions of the columns that have changed.
     * @param columnIndexes
     *            The indexes of the columns that have changed.
     */
    public ColumnGroupCollapseEvent(ILayer layer, int[] columnPositions, int[] columnIndexes) {
        super(layer, columnPositions, columnIndexes);
    }

    /**
     * Clone constructor.
     *
     * @param event
     *            The event to clone.
     */
    protected ColumnGroupCollapseEvent(ColumnGroupCollapseEvent event) {
        super(event);
    }

    @Override
    public ColumnGroupCollapseEvent cloneEvent() {
        return new ColumnGroupCollapseEvent(this);
    }

}
