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
package org.eclipse.nebula.widgets.nattable.conflation;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;

public abstract class AbstractEventConflater implements IEventConflater {

    protected List<ILayerEvent> queue = new LinkedList<ILayerEvent>();

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
