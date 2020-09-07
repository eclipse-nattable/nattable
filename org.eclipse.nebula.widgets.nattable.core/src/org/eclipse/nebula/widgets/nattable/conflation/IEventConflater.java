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

import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;

/**
 * A Conflater queues events and periodically runs a task to handle those
 * Events. This prevents the table from being overwhelmed by ultra fast updates.
 */
public interface IEventConflater {

    public abstract void addEvent(ILayerEvent event);

    public abstract void clearQueue();

    /**
     * @return Number of events currently waiting to be handled
     */
    public abstract int getCount();

    public Runnable getConflaterTask();

}
