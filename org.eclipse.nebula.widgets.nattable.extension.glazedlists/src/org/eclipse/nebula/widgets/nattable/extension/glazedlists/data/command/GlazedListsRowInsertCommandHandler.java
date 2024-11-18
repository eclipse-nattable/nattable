/*******************************************************************************
 * Copyright (c) 2019, 2024 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.data.command;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.command.RowInsertCommand;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsLockHelper;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowInsertEvent;

import ca.odell.glazedlists.EventList;

/**
 * Default command handler for the {@link RowInsertCommand}. Operates on a
 * {@link List} to add row objects by index. Therefore this command handler
 * should be registered on the body DataLayer.
 *
 * @param <T>
 *            The type contained in the backing data list.
 * @since 1.6
 */
@SuppressWarnings("rawtypes")
public class GlazedListsRowInsertCommandHandler<T> implements ILayerCommandHandler<RowInsertCommand> {

    protected EventList<T> bodyData;

    /**
     *
     * @param bodyData
     *            The backing data list on which the delete operation should be
     *            performed. Should be the same list that is used by the data
     *            provider.
     */
    public GlazedListsRowInsertCommandHandler(EventList<T> bodyData) {
        this.bodyData = bodyData;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean doCommand(ILayer targetLayer, RowInsertCommand command) {
        // convert the transported position to the target layer
        if (command.convertToTargetLayer(targetLayer)) {
            GlazedListsLockHelper.performWriteOperation(
                    this.bodyData.getReadWriteLock(),
                    () -> {

                        // add the elements
                        if (command.getRowIndex() < 0 || command.getRowIndex() >= this.bodyData.size()) {
                            this.bodyData.addAll(command.getObjects());
                            // fire the event to refresh
                            targetLayer.fireLayerEvent(new RowInsertEvent(
                                    targetLayer,
                                    new Range(this.bodyData.size() - command.getObjects().size(), this.bodyData.size())));
                        } else {
                            this.bodyData.addAll(command.getRowIndex(), command.getObjects());
                            // fire the event to refresh
                            targetLayer.fireLayerEvent(new RowInsertEvent(
                                    targetLayer,
                                    new Range(command.getRowIndex(), command.getRowIndex() + command.getObjects().size())));
                        }
                    });
            return true;
        }
        return false;
    }

    @Override
    public Class<RowInsertCommand> getCommandClass() {
        return RowInsertCommand.class;
    }

}
