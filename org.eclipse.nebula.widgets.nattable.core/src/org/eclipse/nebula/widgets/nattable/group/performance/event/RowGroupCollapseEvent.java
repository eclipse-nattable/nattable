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

import org.eclipse.nebula.widgets.nattable.hideshow.event.HideRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Specialization of the {@link HideRowPositionsEvent}. Mainly used to inform
 * about hidden rows but avoid handling it in the RowGroupHeaderLayer.
 *
 * @since 2.1
 */
public class RowGroupCollapseEvent extends HideRowPositionsEvent {

    /**
     * Creates a new RowGroupCollapseEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given row positions match.
     * @param rowPositions
     *            The positions of the rows that have changed.
     */
    public RowGroupCollapseEvent(ILayer layer, Collection<Integer> rowPositions) {
        super(layer, rowPositions);
    }

    /**
     * Creates a new RowGroupCollapseEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given row positions match.
     * @param rowPositions
     *            The positions of the rows that have changed.
     */
    public RowGroupCollapseEvent(ILayer layer, int... rowPositions) {
        super(layer, rowPositions);
    }

    /**
     * Creates a new RowGroupCollapseEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given row positions match.
     * @param rowPositions
     *            The positions of the rows that have changed.
     * @param rowIndexes
     *            The indexes of the rows that have changed.
     */
    public RowGroupCollapseEvent(ILayer layer, Collection<Integer> rowPositions, Collection<Integer> rowIndexes) {
        super(layer, rowPositions, rowIndexes);
    }

    /**
     * Creates a new RowGroupCollapseEvent based on the given information.
     *
     * @param layer
     *            The ILayer to which the given row positions match.
     * @param rowPositions
     *            The positions of the rows that have changed.
     * @param rowIndexes
     *            The indexes of the rows that have changed.
     */
    public RowGroupCollapseEvent(ILayer layer, int[] rowPositions, int[] rowIndexes) {
        super(layer, rowPositions, rowIndexes);
    }

    /**
     * Clone constructor.
     *
     * @param event
     *            The event to clone.
     */
    protected RowGroupCollapseEvent(RowGroupCollapseEvent event) {
        super(event);
    }

}
