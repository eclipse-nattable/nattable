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
package org.eclipse.nebula.widgets.nattable.conflation;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;

public abstract class AbstractEventConflater implements IEventConflater {

    protected List<ILayerEvent> queue = new LinkedList<>();

    @Override
    public void addEvent(ILayerEvent event) {
        this.queue.add(event);
    }

    @Override
    public void clearQueue() {
        this.queue.clear();
    }

    @Override
    public int getCount() {
        return this.queue.size();
    }

    @Override
    public abstract Runnable getConflaterTask();
}
