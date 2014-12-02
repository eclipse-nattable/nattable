/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
