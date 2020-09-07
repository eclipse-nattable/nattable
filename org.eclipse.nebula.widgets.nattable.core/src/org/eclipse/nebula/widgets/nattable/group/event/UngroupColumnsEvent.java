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
package org.eclipse.nebula.widgets.nattable.group.event;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;

public class UngroupColumnsEvent extends VisualRefreshEvent {

    public UngroupColumnsEvent(ILayer layer) {
        super(layer);
    }

    protected UngroupColumnsEvent(UngroupColumnsEvent event) {
        super(event);
    }

    @Override
    public UngroupColumnsEvent cloneEvent() {
        return new UngroupColumnsEvent(this);
    }

}
