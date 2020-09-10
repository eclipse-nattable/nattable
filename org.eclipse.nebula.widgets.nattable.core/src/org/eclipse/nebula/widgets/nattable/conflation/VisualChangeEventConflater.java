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
 *     Roman Flueckiger <roman.flueckiger@mac.com> - Bug 454440
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.conflation;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IVisualChangeEvent;

/**
 * Gathers all the VisualChangeEvents. When it's run, it refreshes/repaints the
 * table.
 */
public class VisualChangeEventConflater extends AbstractEventConflater {

    private final NatTable natTable;

    public VisualChangeEventConflater(NatTable ownerLayer) {
        this.natTable = ownerLayer;
    }

    @Override
    public void addEvent(ILayerEvent event) {
        if (event instanceof IVisualChangeEvent) {
            super.addEvent(event);
        }
    }

    @Override
    public Runnable getConflaterTask() {
        return () -> {
            if (VisualChangeEventConflater.this.queue.size() > 0) {
                clearQueue();

                VisualChangeEventConflater.this.natTable.getDisplay().asyncExec(VisualChangeEventConflater.this.natTable::updateResize);
            }
        };
    }

}
