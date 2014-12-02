/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;

public class RowReorderStartCommandHandler extends
        AbstractLayerCommandHandler<RowReorderStartCommand> {

    private final RowReorderLayer rowReorderLayer;

    public RowReorderStartCommandHandler(RowReorderLayer rowReorderLayer) {
        this.rowReorderLayer = rowReorderLayer;
    }

    @Override
    public Class<RowReorderStartCommand> getCommandClass() {
        return RowReorderStartCommand.class;
    }

    @Override
    protected boolean doCommand(RowReorderStartCommand command) {
        int fromRowPosition = command.getFromRowPosition();

        this.rowReorderLayer.setReorderFromRowPosition(fromRowPosition);

        return true;
    }

}
