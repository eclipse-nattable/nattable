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
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;

/**
 * Command handler for handling {@link StructuralRefreshCommand}s. Simply fires
 * a {@link StructuralRefreshEvent}.
 *
 * Needed to be able to refresh all layers by simply calling a command on the
 * NatTable instance itself (Remember that events are fired bottom up the layer
 * stack while commands are propagated top down).
 *
 * To refresh all layers by calling a {@link StructuralRefreshCommand} on the
 * NatTable instance, the {@link StructuralRefreshCommandHandler} should be
 * registered against the DataLayer.
 */
public class StructuralRefreshCommandHandler implements ILayerCommandHandler<StructuralRefreshCommand> {

    @Override
    public Class<StructuralRefreshCommand> getCommandClass() {
        return StructuralRefreshCommand.class;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, StructuralRefreshCommand command) {
        targetLayer.fireLayerEvent(new StructuralRefreshEvent(targetLayer));
        return false;
    }

}
