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
package org.eclipse.nebula.widgets.nattable.datachange;

import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;

/**
 * {@link DataChange} implementation that tracks data updates. Contains an
 * {@link UpdateDataCommand} that is created as result of a DataUpdateEvent and
 * can be used to revert the data update.
 *
 * @since 1.6
 */
public abstract class UpdateDataChange implements DataChange {

    private Object key;
    private final Object value;
    @SuppressWarnings("rawtypes")
    private final CellKeyHandler keyHandler;

    /**
     * Create an {@link UpdateDataChange} that is able to revert the performed
     * data update.
     *
     * @param key
     *            The key under which the cell can be found that should be
     *            updated.
     * @param value
     *            The value that should be set on save or discard.
     * @param keyHandler
     *            The {@link CellKeyHandler} used to get indexes out of the key.
     */
    public UpdateDataChange(Object key, Object value, CellKeyHandler<?> keyHandler) {
        this.key = key;
        this.value = value;
        this.keyHandler = keyHandler;
    }

    @Override
    public Object getKey() {
        return this.key;
    }

    @Override
    public void updateKey(Object key) {
        this.key = key;
    }

    /**
     *
     * @return The value that should be set on save or discard.
     */
    public Object getValue() {
        return this.value;
    }

    @SuppressWarnings("unchecked")
    protected UpdateDataCommand getUpdateDataCommand(DataChangeLayer layer) {
        int columnIndex = this.keyHandler.getColumnIndex(this.key);
        int rowIndex = this.keyHandler.getRowIndex(this.key);
        return new UpdateDataCommand(layer, columnIndex, rowIndex, this.value);
    }

}
