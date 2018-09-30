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

import org.eclipse.nebula.widgets.nattable.data.command.RowInsertCommand;

/**
 * {@link DataChange} implementation to track row delete operations. Is used in
 * persistent mode and can only discard a row delete operation by adding it
 * again via {@link RowInsertCommand}.
 *
 * @since 1.6
 */
public class RowDeleteDataChange implements DataChange {

    private int rowIndex;
    private final Object value;

    /**
     * Create a {@link RowDeleteDataChange} that is able to revert the performed
     * row delete operation.
     *
     * @param rowIndex
     *            The row index at which the row should be inserted again.
     * @param value
     *            The row object that should be inserted again.
     */
    public RowDeleteDataChange(int rowIndex, Object value) {
        this.rowIndex = rowIndex;
        this.value = value;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void discard(DataChangeLayer layer) {
        layer.doCommand(new RowInsertCommand(layer, this.rowIndex, this.value));
    }

    @Override
    public void save(DataChangeLayer layer) {
        // do nothing as we only support persistent mode
    }

    @Override
    public Object getKey() {
        return this.rowIndex;
    }

    @Override
    public void updateKey(Object key) {
        if (key instanceof Number) {
            this.rowIndex = ((Number) key).intValue();
        }
    }

    /**
     *
     * @return The row object that was deleted.
     */
    protected Object getValue() {
        return this.value;
    }
}
