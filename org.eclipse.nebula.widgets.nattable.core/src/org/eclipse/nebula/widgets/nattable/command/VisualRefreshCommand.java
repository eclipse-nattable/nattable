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
package org.eclipse.nebula.widgets.nattable.command;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;

/**
 * Command that triggers a {@link VisualRefreshEvent}. To support refreshing of
 * every layer in a NatTable the {@link VisualRefreshCommandHandler} should be
 * registered against the DataLayer.
 */
public class VisualRefreshCommand implements ILayerCommand {

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        // no need for a check as the command simply triggers the firing of a
        // VisualRefreshEvent
        return true;
    }

    @Override
    public ILayerCommand cloneCommand() {
        // as the command doesn't have a state, the clone is simply a new
        // instance
        return new VisualRefreshCommand();
    }

}
