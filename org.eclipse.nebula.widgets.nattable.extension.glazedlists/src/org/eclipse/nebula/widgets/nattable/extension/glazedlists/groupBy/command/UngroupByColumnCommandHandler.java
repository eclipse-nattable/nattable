/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public class UngroupByColumnCommandHandler extends
        AbstractLayerCommandHandler<UngroupByColumnIndexCommand> {

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
        if (this.groupByHeaderLayer.getGroupByModel().removeGroupByColumnIndex(
                columnIndex)) {
            this.groupByHeaderLayer.fireLayerEvent(new VisualRefreshEvent(
                    this.groupByHeaderLayer));
        }
        return true;
    }

    @Override
    public Class<UngroupByColumnIndexCommand> getCommandClass() {
        return UngroupByColumnIndexCommand.class;
    }

}
