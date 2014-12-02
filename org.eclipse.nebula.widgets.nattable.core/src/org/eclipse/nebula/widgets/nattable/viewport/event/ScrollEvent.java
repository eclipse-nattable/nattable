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
package org.eclipse.nebula.widgets.nattable.viewport.event;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class ScrollEvent extends StructuralRefreshEvent {

    public ScrollEvent(ViewportLayer viewportLayer) {
        super(viewportLayer);
    }

    protected ScrollEvent(ScrollEvent event) {
        super(event);
    }

    @Override
    public ScrollEvent cloneEvent() {
        return new ScrollEvent(this);
    }

    @Override
    public Collection<StructuralDiff> getColumnDiffs() {
        // TODO this is bogus - should have a horiz/vert scroll event instead
        // that are multi col/row structural changes
        return null;
    }

    @Override
    public Collection<StructuralDiff> getRowDiffs() {
        // TODO this is bogus - should have a horiz/vert scroll event instead
        // that are multi col/row structural changes
        return null;
    }

}
