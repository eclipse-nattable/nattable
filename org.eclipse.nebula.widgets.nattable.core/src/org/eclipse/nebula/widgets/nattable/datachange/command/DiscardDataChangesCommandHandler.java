/*******************************************************************************
 * Copyright (c) 2017 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.datachange.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.datachange.DataChangeLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * {@link ILayerCommandHandler} to handle the {@link DiscardDataChangesCommand}
 * on the {@link DataChangeLayer}.
 *
 * @since 1.6
 */
public class DiscardDataChangesCommandHandler implements ILayerCommandHandler<DiscardDataChangesCommand> {

    private final DataChangeLayer dataChangeLayer;

    /**
     *
     * @param dataChangeLayer
     *            The {@link DataChangeLayer} to which this command handler
     *            should be registered to.
     */
    public DiscardDataChangesCommandHandler(DataChangeLayer dataChangeLayer) {
        this.dataChangeLayer = dataChangeLayer;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, DiscardDataChangesCommand command) {
        this.dataChangeLayer.discardDataChanges();
        return true;
    }

    @Override
    public Class<DiscardDataChangesCommand> getCommandClass() {
        return DiscardDataChangesCommand.class;
    }

}
