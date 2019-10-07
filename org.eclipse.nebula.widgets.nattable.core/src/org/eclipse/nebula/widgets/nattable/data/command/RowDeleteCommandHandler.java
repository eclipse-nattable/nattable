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
package org.eclipse.nebula.widgets.nattable.data.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowObjectDeleteEvent;

/**
 * Default command handler for the {@link RowDeleteCommand}. Operates on a
 * {@link List} to remove row objects by index. Therefore this command handler
 * should be registered on the body DataLayer.
 *
 * <p>
 * This command handler fires a {@link RowObjectDeleteEvent} on completion that
 * also carries the deleted object.
 * </p>
 *
 * @param <T>
 *            The type contained in the backing data list.
 *
 * @since 1.6
 */
public class RowDeleteCommandHandler<T> implements ILayerCommandHandler<RowDeleteCommand> {

    private List<T> bodyData;

    /**
     *
     * @param bodyData
     *            The backing data list on which the delete operation should be
     *            performed. Should be the same list that is used by the data
     *            provider.
     */
    public RowDeleteCommandHandler(List<T> bodyData) {
        this.bodyData = bodyData;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, RowDeleteCommand command) {
        // convert the transported position to the target layer
        if (command.convertToTargetLayer(targetLayer)) {
            int[] positions = command.getRowPositions().stream()
                    .sorted()
                    .mapToInt(i -> i)
                    .toArray();

            Map<Integer, T> deleted = new HashMap<Integer, T>();
            for (int i = positions.length - 1; i >= 0; i--) {
                // remove the element
                int pos = positions[i];
                deleted.put(pos, this.bodyData.remove(pos));
            }
            // fire the event to refresh
            targetLayer.fireLayerEvent(new RowObjectDeleteEvent(targetLayer, deleted));
            return true;
        }
        return false;
    }

    @Override
    public Class<RowDeleteCommand> getCommandClass() {
        return RowDeleteCommand.class;
    }

}
