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
package org.eclipse.nebula.widgets.nattable.freeze.event;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;

public class FreezeEvent extends StructuralRefreshEvent {

    public FreezeEvent(ILayer layer) {
        super(layer);
    }

    protected FreezeEvent(FreezeEvent event) {
        super(event);
    }

    @Override
    public FreezeEvent cloneEvent() {
        return new FreezeEvent(this);
    }

    @Override
    public Collection<StructuralDiff> getColumnDiffs() {
        return null;
    }

    @Override
    public Collection<StructuralDiff> getRowDiffs() {
        return null;
    }

}
