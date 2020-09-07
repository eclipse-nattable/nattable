/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * {@link DataChangeLayer#saveDataChanges()} is done.
 *
 * @since 1.6
 */
public class SaveDataChangesCompletedEvent extends VisualRefreshEvent {

    /**
     *
     * @param layer
     *            The layer to refresh.
     */
    public SaveDataChangesCompletedEvent(ILayer layer) {
        super(layer);
    }

    /**
     * Clone constructor.
     *
     * @param event
     *            The event to clone.
     */
    protected SaveDataChangesCompletedEvent(SaveDataChangesCompletedEvent event) {
        super(event);
    }

}
