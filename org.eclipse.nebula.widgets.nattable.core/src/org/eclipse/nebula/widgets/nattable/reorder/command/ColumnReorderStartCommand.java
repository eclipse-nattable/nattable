/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.reorder.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class ColumnReorderStartCommand implements ILayerCommand {

    private ColumnPositionCoordinate fromColumnPositionCoordinate;

    public ColumnReorderStartCommand(ILayer layer, int fromColumnPosition) {
        this.fromColumnPositionCoordinate = new ColumnPositionCoordinate(layer, fromColumnPosition);
    }

    protected ColumnReorderStartCommand(ColumnReorderStartCommand command) {
        this.fromColumnPositionCoordinate = command.fromColumnPositionCoordinate;
    }

    public int getFromColumnPosition() {
        return this.fromColumnPositionCoordinate.getColumnPosition();
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        ColumnPositionCoordinate targetFromColumnPositionCoordinate =
                LayerCommandUtil.convertColumnPositionToTargetContext(this.fromColumnPositionCoordinate, targetLayer);
        if (targetFromColumnPositionCoordinate != null) {
            this.fromColumnPositionCoordinate = targetFromColumnPositionCoordinate;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ColumnReorderStartCommand cloneCommand() {
        return new ColumnReorderStartCommand(this);
    }

}
