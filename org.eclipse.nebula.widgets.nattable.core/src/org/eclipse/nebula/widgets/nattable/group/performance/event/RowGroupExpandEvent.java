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

import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowRowPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Specialization of the {@link ShowRowPositionsEvent}. Mainly used to inform
 * about showed rows but avoid handling it in the RowGroupHeaderLayer.
 *
 * @since 2.1
 */
public class RowGroupExpandEvent extends ShowRowPositionsEvent {

    /**
     * Constructor.
     *
     * @param layer
     *            The layer to which the given row positions match.
     * @param rowPositions
     *            The row positions that are made visible again.
     */
    public RowGroupExpandEvent(ILayer layer, Collection<Integer> rowPositions) {
        super(layer, rowPositions);
    }

    /**
     * Constructor.
     *
     * @param layer
     *            The layer to which the given row positions match.
     * @param rowPositions
     *            The row positions that are made visible again.
     */
    public RowGroupExpandEvent(ILayer layer, int... rowPositions) {
        super(layer, rowPositions);
    }

    /**
     * Clone constructor.
     *
     * @param event
     *            The {@link RowGroupExpandEvent} to clone.
     */
    protected RowGroupExpandEvent(RowGroupExpandEvent event) {
        super(event);
    }

}
