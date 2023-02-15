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

import org.eclipse.nebula.widgets.nattable.hideshow.event.ShowColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;

/**
 * Specialization of the {@link ShowColumnPositionsEvent}. Mainly used to inform
 * about showed columns but avoid handling it in the ColumnGroupHeaderLayer.
 *
 * @since 2.1
 */
public class ColumnGroupExpandEvent extends ShowColumnPositionsEvent {

    /**
     * Constructor.
     *
     * @param layer
     *            The layer to which the given column positions match.
     * @param columnPositions
     *            The column positions that are made visible again.
     */
    public ColumnGroupExpandEvent(IUniqueIndexLayer layer, Collection<Integer> columnPositions) {
        super(layer, columnPositions);
    }

    /**
     * Constructor.
     *
     * @param layer
     *            The layer to which the given column positions match.
     * @param columnPositions
     *            The column positions that are made visible again.
     */
    public ColumnGroupExpandEvent(ILayer layer, int... columnPositions) {
        super(layer, columnPositions);
    }

    /**
     * Clone constructor.
     *
     * @param event
     *            The {@link ColumnGroupExpandEvent} to clone.
     */
    public ColumnGroupExpandEvent(ShowColumnPositionsEvent event) {
        super(event);
    }

}
