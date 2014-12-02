/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        this.fromColumnPositionCoordinate = new ColumnPositionCoordinate(layer,
                fromColumnPosition);
    }

    protected ColumnReorderStartCommand(ColumnReorderStartCommand command) {
        this.fromColumnPositionCoordinate = command.fromColumnPositionCoordinate;
    }

    public int getFromColumnPosition() {
        return this.fromColumnPositionCoordinate.getColumnPosition();
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        ColumnPositionCoordinate targetFromColumnPositionCoordinate = LayerCommandUtil
                .convertColumnPositionToTargetContext(
                        this.fromColumnPositionCoordinate, targetLayer);
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
