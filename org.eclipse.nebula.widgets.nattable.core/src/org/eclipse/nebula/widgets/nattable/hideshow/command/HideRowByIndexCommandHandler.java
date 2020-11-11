/*******************************************************************************
 * Copyright (c) 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.IRowHideShowLayer;

/**
 * {@link ILayerCommandHandler} for the {@link HideRowByIndexCommand}. Hides
 * rows identified by index via {@link IRowHideShowLayer}.
 *
 * @since 2.0
 */
public class HideRowByIndexCommandHandler extends AbstractLayerCommandHandler<HideRowByIndexCommand> {

    private final IRowHideShowLayer rowHideShowLayer;

    /**
     *
     * @param rowHideShowLayer
     *            The {@link IRowHideShowLayer} on which this command handler
     *            should operate.
     */
    public HideRowByIndexCommandHandler(IRowHideShowLayer rowHideShowLayer) {
        this.rowHideShowLayer = rowHideShowLayer;
    }

    @Override
    public Class<HideRowByIndexCommand> getCommandClass() {
        return HideRowByIndexCommand.class;
    }

    @Override
    protected boolean doCommand(HideRowByIndexCommand command) {
        this.rowHideShowLayer.hideRowIndexes(command.getRowIndexes());
        return true;
    }

}
