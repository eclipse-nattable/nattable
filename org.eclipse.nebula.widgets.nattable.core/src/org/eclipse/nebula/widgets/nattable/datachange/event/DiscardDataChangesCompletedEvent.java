/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.datachange.event;

import org.eclipse.nebula.widgets.nattable.datachange.DataChangeLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;

/**
 * {@link VisualRefreshEvent} that is fired after
 * {@link DataChangeLayer#discardDataChanges()} is done.
 *
 * @since 1.6
 */
public class DiscardDataChangesCompletedEvent extends VisualRefreshEvent {

    /**
     *
     * @param layer
     *            The layer to refresh.
     */
    public DiscardDataChangesCompletedEvent(ILayer layer) {
        super(layer);
    }

    /**
     * Clone constructor.
     *
     * @param event
     *            The event to clone.
     */
    protected DiscardDataChangesCompletedEvent(DiscardDataChangesCompletedEvent event) {
        super(event);
    }

}
