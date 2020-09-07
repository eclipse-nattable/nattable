/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;

public class RowReorderStartCommandHandler extends AbstractLayerCommandHandler<RowReorderStartCommand> {

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
