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
package org.eclipse.nebula.widgets.nattable.sort.event;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnVisualChangeEvent;

public class SortColumnEvent extends ColumnVisualChangeEvent {

    public SortColumnEvent(ILayer layer, int columnPosition) {
        super(layer, new Range(columnPosition, columnPosition + 1));
    }

    protected SortColumnEvent(SortColumnEvent event) {
        super(event);
    }

    @Override
    public SortColumnEvent cloneEvent() {
        return new SortColumnEvent(this);
    }

}
