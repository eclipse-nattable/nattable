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
package org.eclipse.nebula.widgets.nattable.grid.layer.event;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnVisualChangeEvent;

public class ColumnHeaderSelectionEvent extends ColumnVisualChangeEvent {

    public ColumnHeaderSelectionEvent(ILayer layer, int columnPosition) {
        this(layer, new Range(columnPosition, columnPosition + 1));
    }

    public ColumnHeaderSelectionEvent(ILayer layer, Range... columnPositionRanges) {
        this(layer, Arrays.asList(columnPositionRanges));
    }

    public ColumnHeaderSelectionEvent(ILayer layer, Collection<Range> columnPositionRanges) {
        super(layer, columnPositionRanges);
    }

    protected ColumnHeaderSelectionEvent(ColumnHeaderSelectionEvent event) {
        super(event);
    }

    @Override
    public ColumnHeaderSelectionEvent cloneEvent() {
        return new ColumnHeaderSelectionEvent(this);
    }

}
