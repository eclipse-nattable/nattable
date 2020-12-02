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
package org.eclipse.nebula.widgets.nattable.columnRename.event;

import java.util.Arrays;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;

public class RenameColumnHeaderEvent extends ColumnVisualChangeEvent {

    /**
     *
     * @param layer
     *            The layer to which the column position matches.
     * @param columnPosition
     *            The renamed column position.
     * @since 1.4
     */
    public RenameColumnHeaderEvent(ILayer layer, int columnPosition) {
        super(layer, PositionUtil.getRanges(Arrays.asList(columnPosition)));
    }

    public RenameColumnHeaderEvent(ColumnHeaderLayer layer, int columnPosition) {
        super(layer, PositionUtil.getRanges(Arrays.asList(columnPosition)));
    }

    // Copy constructor
    protected RenameColumnHeaderEvent(RenameColumnHeaderEvent event) {
        super(event);
    }

    @Override
    public ILayerEvent cloneEvent() {
        return new RenameColumnHeaderEvent(this);
    }
}
