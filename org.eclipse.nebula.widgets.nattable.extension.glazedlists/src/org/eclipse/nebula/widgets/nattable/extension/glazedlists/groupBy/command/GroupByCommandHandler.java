/*******************************************************************************
 * Copyright (c) 2025 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByHeaderLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.GroupByModel;
import org.eclipse.nebula.widgets.nattable.groupby.GroupByCommand;
import org.eclipse.nebula.widgets.nattable.groupby.GroupByCommand.GroupByAction;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;

/**
 * The {@link ILayerCommandHandler} for handling {@link GroupByCommand}s.
 *
 * @since 2.6
 */
public class GroupByCommandHandler extends AbstractLayerCommandHandler<GroupByCommand> {

    private final GroupByHeaderLayer groupByHeaderLayer;

    /**
     * @param groupByHeaderLayer
     *            The {@link GroupByHeaderLayer} to which this command handler
     *            is registered to. Is needed to modify the {@link GroupByModel}
     *            and fire the update events.
     */
    public GroupByCommandHandler(GroupByHeaderLayer groupByHeaderLayer) {
        this.groupByHeaderLayer = groupByHeaderLayer;
    }

    @Override
    protected boolean doCommand(GroupByCommand command) {
        GroupByAction action = command.getAction();
        int[] columnIndexes = command.getIndexes();
        boolean fireEvent = false;
        switch (action) {
            case ADD:
                fireEvent = this.groupByHeaderLayer.getGroupByModel().addAllGroupByColumnIndexes(columnIndexes);
                break;
            case CLEAR:
                this.groupByHeaderLayer.getGroupByModel().clearGroupByColumnIndexes();
                fireEvent = true;
                break;
            case REMOVE:
                fireEvent = this.groupByHeaderLayer.getGroupByModel().removeAllGroupByColumnIndexes(columnIndexes);
                break;
            case SET:
                this.groupByHeaderLayer.getGroupByModel().setGroupByColumnIndexes(columnIndexes);
                fireEvent = true;
                break;
        }

        if (fireEvent) {
            this.groupByHeaderLayer.fireLayerEvent(new VisualRefreshEvent(this.groupByHeaderLayer));
        }

        return true;
    }

    @Override
    public Class<GroupByCommand> getCommandClass() {
        return GroupByCommand.class;
    }

}
