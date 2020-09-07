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
