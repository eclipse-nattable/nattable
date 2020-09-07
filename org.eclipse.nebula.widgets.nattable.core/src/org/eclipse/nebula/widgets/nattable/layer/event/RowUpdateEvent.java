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
package org.eclipse.nebula.widgets.nattable.layer.event;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class RowUpdateEvent extends RowVisualChangeEvent {

    public RowUpdateEvent(ILayer layer, int rowPosition) {
        this(layer, new Range(rowPosition, rowPosition + 1));
    }

    public RowUpdateEvent(ILayer layer, Range rowPositionRange) {
        super(layer, rowPositionRange);
    }

    public RowUpdateEvent(RowUpdateEvent event) {
        super(event);
    }

    @Override
    public RowUpdateEvent cloneEvent() {
        return new RowUpdateEvent(this);
    }

}
