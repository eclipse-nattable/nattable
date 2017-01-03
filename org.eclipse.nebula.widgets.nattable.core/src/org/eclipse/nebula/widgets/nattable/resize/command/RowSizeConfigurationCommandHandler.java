/*****************************************************************************
 * Copyright (c) 2015, 2016 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * {@link ILayerCommandHandler} for the {@link RowSizeConfigurationCommand}.
 * Determines the row positions to resize based on the given label.
 *
 * @since 1.4
 */
public class RowSizeConfigurationCommandHandler implements ILayerCommandHandler<RowSizeConfigurationCommand> {

    private final DataLayer dataLayer;

    public RowSizeConfigurationCommandHandler(DataLayer dataLayer) {
        this.dataLayer = dataLayer;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, RowSizeConfigurationCommand command) {
        if (command.label == null) {
            if (command.newRowHeight == null) {
                this.dataLayer.setRowPercentageSizing(true);
            } else {
                this.dataLayer.setDefaultRowHeight(command.newRowHeight);
            }
        } else {
            // find row position
            for (int i = 0; i < this.dataLayer.getRowCount(); i++) {
                if (this.dataLayer.getConfigLabelsByPosition(0, i).hasLabel(command.label)) {
                    if (command.newRowHeight == null) {
                        this.dataLayer.setRowPercentageSizing(i, true);
                    } else {
                        if (command.percentageSizing) {
                            this.dataLayer.setRowHeightPercentageByPosition(i, command.newRowHeight);
                        } else {
                            this.dataLayer.setRowHeightByPosition(i, command.newRowHeight);
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public Class<RowSizeConfigurationCommand> getCommandClass() {
        return RowSizeConfigurationCommand.class;
    }

}
