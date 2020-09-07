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
package org.eclipse.nebula.widgets.nattable.freeze.event;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;

public class UnfreezeEvent extends StructuralRefreshEvent {

    public UnfreezeEvent(ILayer layer) {
        super(layer);
    }

    protected UnfreezeEvent(UnfreezeEvent event) {
        super(event);
    }

    @Override
    public UnfreezeEvent cloneEvent() {
        return new UnfreezeEvent(this);
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
