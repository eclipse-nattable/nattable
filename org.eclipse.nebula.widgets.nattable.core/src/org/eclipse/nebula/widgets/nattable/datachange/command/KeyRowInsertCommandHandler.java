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
package org.eclipse.nebula.widgets.nattable.datachange.command;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.command.RowInsertCommand;
import org.eclipse.nebula.widgets.nattable.datachange.CellKeyHandler;
import org.eclipse.nebula.widgets.nattable.datachange.event.KeyRowInsertEvent;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Default command handler for the {@link RowInsertCommand}. Operates on a
 * {@link List} to add row objects by index. Therefore this command handler
 * should be registered on the body DataLayer.
 * <p>
 * Note: This implementation uses a {@link CellKeyHandler} to create and fire
 * {@link KeyRowInsertEvent}s, which additionally transport the key under which
 * the inserted row can be identified.
 * </p>
 *
 * @param <T>
 *            The type contained in the backing data list.
 * @since 1.6
 */
@SuppressWarnings("rawtypes")
public class KeyRowInsertCommandHandler<T> implements ILayerCommandHandler<RowInsertCommand> {

    private List<T> bodyData;
    private final CellKeyHandler<?> keyHandler;

    /**
     *
     * @param bodyData
     *            The backing data list on which the delete operation should be
     *            performed. Should be the same list that is used by the data
     *            provider.
     * @param keyHandler
     *            The {@link CellKeyHandler} that is used to generate the key
     *            for the dataChanges which is later used to identify the row
     *            again.
     */
    public KeyRowInsertCommandHandler(List<T> bodyData, CellKeyHandler<?> keyHandler) {
        this.bodyData = bodyData;
        this.keyHandler = keyHandler;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean doCommand(ILayer targetLayer, RowInsertCommand command) {
        // convert the transported position to the target layer
        if (command.convertToTargetLayer(targetLayer)) {
            // add the elements
            if (command.getRowPosition() < 0 || command.getRowPosition() > this.bodyData.size()) {
                int start = this.bodyData.size();

                this.bodyData.addAll(command.getObjects());

                List<Object> keys = new ArrayList<Object>();
                for (int i = 0; i < command.getObjects().size(); i++) {
                    keys.add(this.keyHandler.getKey(-1, start + i));
                }

                // fire the event to refresh
                targetLayer.fireLayerEvent(new KeyRowInsertEvent(
                        targetLayer,
                        new Range(start, start + command.getObjects().size()),
                        keys,
                        this.keyHandler));
            } else {
                this.bodyData.addAll(command.getRowPosition(), command.getObjects());

                List<Object> keys = new ArrayList<Object>();
                for (int i = 0; i < command.getObjects().size(); i++) {
                    keys.add(this.keyHandler.getKey(-1, command.getRowPosition() + i));
                }

                // fire the event to refresh
                targetLayer.fireLayerEvent(new KeyRowInsertEvent(
                        targetLayer,
                        new Range(command.getRowPosition(), command.getRowPosition() + command.getObjects().size()),
                        keys,
                        this.keyHandler));
            }
            return true;
        }
        return false;
    }

    @Override
    public Class<RowInsertCommand> getCommandClass() {
        return RowInsertCommand.class;
    }

}
