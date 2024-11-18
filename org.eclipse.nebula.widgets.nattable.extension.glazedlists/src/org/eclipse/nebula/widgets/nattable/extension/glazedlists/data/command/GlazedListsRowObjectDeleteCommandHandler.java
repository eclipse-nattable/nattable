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
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.command.RowObjectDeleteCommand;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsLockHelper;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowObjectDeleteEvent;

import ca.odell.glazedlists.EventList;

/**
 * Default command handler for the {@link RowObjectDeleteCommand}. Operates on a
 * {@link List} to remove row objects. Therefore this command handler should be
 * registered on the body DataLayer.
 *
 * <p>
 * This command handler fires a {@link RowObjectDeleteEvent} on completion that
 * carries the deleted object and the index it was stored before to be able to
 * revert the change correctly.
 * </p>
 *
 * @param <T>
 *            The type contained in the backing data list.
 *
 * @since 1.6
 */
@SuppressWarnings("rawtypes")
public class GlazedListsRowObjectDeleteCommandHandler<T> implements ILayerCommandHandler<RowObjectDeleteCommand> {

    protected EventList<T> bodyData;

    /**
     *
     * @param bodyData
     *            The backing data list on which the delete operation should be
     *            performed. Should be the same list that is used by the data
     *            provider.
     */
    public GlazedListsRowObjectDeleteCommandHandler(EventList<T> bodyData) {
        this.bodyData = bodyData;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean doCommand(ILayer targetLayer, RowObjectDeleteCommand command) {
        // first we need to determine the indexes so we are able to revert the
        // changes via DataChangeLayer in the correct order again
        int[] indexes = new int[command.getObjectsToDelete().size()];
        Map<Integer, T> deleted = new TreeMap<>();

        GlazedListsLockHelper.performWriteOperation(
                this.bodyData.getReadWriteLock(),
                () -> {
                    int idx = 0;
                    for (Object rowObject : command.getObjectsToDelete()) {
                        int index = this.bodyData.indexOf(rowObject);
                        deleted.put(index, (T) rowObject);
                        indexes[idx] = index;
                        idx++;
                    }
                    this.bodyData.removeAll(command.getObjectsToDelete());
                });

        // fire the event to refresh
        targetLayer.fireLayerEvent(new RowObjectDeleteEvent(targetLayer, deleted));
        return true;
    }

    @Override
    public Class<RowObjectDeleteCommand> getCommandClass() {
        return RowObjectDeleteCommand.class;
    }

}
