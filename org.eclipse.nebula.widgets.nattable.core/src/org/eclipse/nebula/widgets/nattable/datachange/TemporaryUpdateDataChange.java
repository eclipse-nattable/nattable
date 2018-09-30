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
 * {@link DataChange} implementation that tracks {@link UpdateDataCommand}s for
 * temporary storage. Contains an {@link UpdateDataCommand} that is executed to
 * update the backing data on save.
 *
 * @since 1.6
 */
public class TemporaryUpdateDataChange extends UpdateDataChange {

    /**
     * Creates a {@link TemporaryUpdateDataChange} to track the data change
     * without updating the backing data directly.
     *
     * @param key
     *            The key under which the cell can be found that should be
     *            updated.
     * @param newValue
     *            The new value that should be set on save.
     * @param keyHandler
     *            The {@link CellKeyHandler} used to get indexes out of the key.
     */
    public TemporaryUpdateDataChange(Object key, Object newValue, CellKeyHandler<?> keyHandler) {
        super(key, newValue, keyHandler);
    }

    @Override
    public void discard(DataChangeLayer layer) {
        // do nothing as the data update was not performed on the backing data
        // this command simply needs to be discarded to remove the tracked data
        // change
    }

    @Override
    public void save(DataChangeLayer layer) {
        // execute the command to update the backing data
        layer.getUnderlyingLayerByPosition(0, 0).doCommand(getUpdateDataCommand(layer));
    }

}
