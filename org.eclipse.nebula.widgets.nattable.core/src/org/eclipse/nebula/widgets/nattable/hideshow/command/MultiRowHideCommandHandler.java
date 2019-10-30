/*******************************************************************************
 * Copyright (c) 2012, 2019 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.IRowHideShowLayer;

public class MultiRowHideCommandHandler extends
        AbstractLayerCommandHandler<MultiRowHideCommand> {

    private final IRowHideShowLayer rowHideShowLayer;

    /**
     *
     * @param rowHideShowLayer
     *            The {@link IRowHideShowLayer} to which this command handler
     *            should be registered.
     * @since 2.0
     */
    public MultiRowHideCommandHandler(IRowHideShowLayer rowHideShowLayer) {
        this.rowHideShowLayer = rowHideShowLayer;
    }

    @Override
    public Class<MultiRowHideCommand> getCommandClass() {
        return MultiRowHideCommand.class;
    }

    @Override
    protected boolean doCommand(MultiRowHideCommand command) {
        this.rowHideShowLayer.hideRowPositions(command.getRowPositions());
        return true;
    }

}
