/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.datachange;

import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.edit.event.DataUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;

/**
 * {@link DataChangeHandler} to handle {@link UpdateDataCommand}s for data
 * changes that that should be stored temporarily and not directly performed on
 * the backing data. Creates {@link UpdateDataChange}s to track the
 * {@link UpdateDataCommand}s so they can be executed on the backing data on
 * save.
 *
 * @since 1.6
 */
public class TemporaryUpdateDataChangeHandler extends UpdateDataChangeHandler<TemporaryUpdateDataChange> implements TemporaryDataProvider, ILayerCommandHandler<UpdateDataCommand> {

    /**
     * Creates an {@link TemporaryUpdateDataChangeHandler} to handle
     * {@link DataUpdateEvent}s to be able to track and revert data changes.
     *
     * @param layer
     *            The {@link DataChangeLayer} this handler should be assigned
     *            to.
     * @param keyHandler
     *            The {@link CellKeyHandler} that is used to store data changes
     *            for a specific key.
     */
    public TemporaryUpdateDataChangeHandler(DataChangeLayer layer, CellKeyHandler<?> keyHandler) {
        super(layer, keyHandler, new ConcurrentHashMap<>());
    }

    @Override
    public boolean tracksDataChange(int columnPosition, int rowPosition) {
        Object key = this.keyHandler.getKey(columnPosition, rowPosition);
        return (key != null && this.dataChanges.containsKey(key));
    }

    @Override
    public Object getDataValueByPosition(int columnPosition, int rowPosition) {
        Object key = this.keyHandler.getKey(columnPosition, rowPosition);
        return this.dataChanges.get(key).getValue();
    }

    @Override
    public boolean doCommand(ILayer targetLayer, UpdateDataCommand command) {
        if (command.convertToTargetLayer(this.layer)) {
            // we handle the command and avoid that it is handled by the
            // underlying layers, but we only really handle it if the handling
            // is enabled.
            if (this.handleDataUpdate) {
                UpdateDataCommand updateCommand = command;
                int columnPosition = updateCommand.getColumnPosition();
                int rowPosition = updateCommand.getRowPosition();
                Object key = this.keyHandler.getKey(columnPosition, rowPosition);
                if (key != null) {
                    Object currentValue = this.layer.getDataValueByPosition(columnPosition, rowPosition);
                    if ((currentValue == null && updateCommand.getNewValue() != null)
                            || (updateCommand.getNewValue() == null && currentValue != null)
                            || (currentValue != null && updateCommand.getNewValue() != null && !currentValue.equals(updateCommand.getNewValue()))) {

                        // store the change in the DataChangeLayer
                        TemporaryUpdateDataChange change = new TemporaryUpdateDataChange(key, command.getNewValue(), this.keyHandler);
                        this.layer.addDataChange(change);

                        // update the local storage of tracked changes
                        Object underlyingDataValue = this.layer.getUnderlyingLayerByPosition(0, 0).getDataValueByPosition(columnPosition, rowPosition);
                        if ((updateCommand.getNewValue() == null && underlyingDataValue == null)
                                || (updateCommand.getNewValue() != null && updateCommand.getNewValue().equals(underlyingDataValue))) {
                            // the value was changed back to the original value
                            // in the underlying layer simply remove the local
                            // storage to not showing the cell as dirty
                            this.dataChanges.remove(key);
                            // rebuild the position tracking in the layer
                            rebuildPositionCollections();
                        } else {
                            // update the position tracking
                            this.changedColumns.add(columnPosition);
                            this.changedRows.add(rowPosition);

                            // store the change locally
                            this.dataChanges.put(key, change);
                        }
                        this.layer.fireLayerEvent(new CellVisualChangeEvent(this.layer, columnPosition, rowPosition));
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Class<UpdateDataCommand> getCommandClass() {
        return UpdateDataCommand.class;
    }

}
