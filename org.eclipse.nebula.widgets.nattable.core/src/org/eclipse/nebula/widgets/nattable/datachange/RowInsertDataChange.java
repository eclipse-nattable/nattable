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

import org.eclipse.nebula.widgets.nattable.data.command.RowDeleteCommand;

/**
 * {@link DataChange} implementation to track row insert operations. Is used in
 * persistent mode and can only discard a row insert operation by deleting it
 * again via {@link RowDeleteCommand}.
 *
 * @since 1.6
 */
public class RowInsertDataChange implements DataChange {

    private Object key;
    @SuppressWarnings("rawtypes")
    private final CellKeyHandler keyHandler;

    /**
     * Create a {@link RowInsertDataChange} that is able to revert the performed
     * row insert operation.
     *
     * @param key
     *            The key under which the row can be found that should be
     *            deleted again.
     * @param keyHandler
     *            The {@link CellKeyHandler} used to get the row index out of
     *            the key.
     */
    public RowInsertDataChange(Object key, CellKeyHandler<?> keyHandler) {
        this.key = key;
        this.keyHandler = keyHandler;
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public void discard(DataChangeLayer layer) {
        int rowIndex = this.keyHandler.getRowIndex(this.key);
        layer.doCommand(new RowDeleteCommand(layer, rowIndex));
    }

    @Override
    public void save(DataChangeLayer layer) {
        // do nothing as we only support persistent mode
    }

    @Override
    public Object getKey() {
        return this.key;
    }

    @Override
    public void updateKey(Object key) {
        this.key = key;
    }

}
