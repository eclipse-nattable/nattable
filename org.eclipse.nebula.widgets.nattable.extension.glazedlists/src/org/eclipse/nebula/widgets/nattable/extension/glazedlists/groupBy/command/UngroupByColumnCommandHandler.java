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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByHeaderLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByModel;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;

/**
 * The {@link ILayerCommandHandler} for handling
 * {@link UngroupByColumnIndexCommand}s.
 */
public class UngroupByColumnCommandHandler extends AbstractLayerCommandHandler<UngroupByColumnIndexCommand> {

    private final GroupByHeaderLayer groupByHeaderLayer;

    /**
     * @param groupByHeaderLayer
     *            The {@link GroupByHeaderLayer} to which this command handler
     *            is registered to. Is needed to modify the {@link GroupByModel}
     *            and fire the update events.
     */
    public UngroupByColumnCommandHandler(GroupByHeaderLayer groupByHeaderLayer) {
        this.groupByHeaderLayer = groupByHeaderLayer;
    }

    @Override
    protected boolean doCommand(UngroupByColumnIndexCommand command) {
        int columnIndex = command.getGroupByColumnIndex();
        if (this.groupByHeaderLayer.getGroupByModel().removeGroupByColumnIndex(columnIndex)) {
            this.groupByHeaderLayer.fireLayerEvent(new VisualRefreshEvent(this.groupByHeaderLayer));
        }
        return true;
    }

    @Override
    public Class<UngroupByColumnIndexCommand> getCommandClass() {
        return UngroupByColumnIndexCommand.class;
    }

}
