/*******************************************************************************
 * Copyright (c) 2017, 2020 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.datachange.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.datachange.DataChangeLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * {@link ILayerCommandHandler} to handle the {@link SaveDataChangesCommand} on
 * the {@link DataChangeLayer}.
 *
 * @since 1.6
 */
public class SaveDataChangesCommandHandler implements ILayerCommandHandler<SaveDataChangesCommand> {

    private final DataChangeLayer dataChangeLayer;

    /**
     *
     * @param dataChangeLayer
     *            The {@link DataChangeLayer} to which this command handler
     *            should be registered to.
     */
    public SaveDataChangesCommandHandler(DataChangeLayer dataChangeLayer) {
        this.dataChangeLayer = dataChangeLayer;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, SaveDataChangesCommand command) {
        this.dataChangeLayer.saveDataChanges();
        return true;
    }

    @Override
    public Class<SaveDataChangesCommand> getCommandClass() {
        return SaveDataChangesCommand.class;
    }

}
