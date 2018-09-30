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
public class PersistenceUpdateDataChange extends UpdateDataChange {

    /**
     * Create an {@link PersistenceUpdateDataChange} that is able to revert the
     * performed data update.
     *
     * @param key
     *            The key under which the cell can be found that should be
     *            updated.
     * @param oldValue
     *            The old value that should be re-applied on discard.
     * @param keyHandler
     *            The {@link CellKeyHandler} used to get indexes out of the key.
     */
    public PersistenceUpdateDataChange(Object key, Object oldValue, CellKeyHandler<?> keyHandler) {
        super(key, oldValue, keyHandler);
    }

    @Override
    public void discard(DataChangeLayer layer) {
        // since we contain the UpdateDataCommand that can be executed to revert
        // a data change, the command is executed on the underlying layer here.
        layer.getUnderlyingLayerByPosition(0, 0).doCommand(getUpdateDataCommand(layer));
    }

    @Override
    public void save(DataChangeLayer layer) {
        // do nothing as the data update was already performed on the backing
        // data
    }

}
