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

public class ColumnReorderEndCommand implements ILayerCommand {

    private ColumnPositionCoordinate toColumnPositionCoordinate;
    private boolean reorderToLeftEdge;

    public ColumnReorderEndCommand(ILayer layer, int toColumnPosition) {
        if (toColumnPosition < layer.getColumnCount()) {
            this.reorderToLeftEdge = true;
        } else {
            this.reorderToLeftEdge = false;
            toColumnPosition--;
        }

        this.toColumnPositionCoordinate = new ColumnPositionCoordinate(layer,
                toColumnPosition);
    }

    protected ColumnReorderEndCommand(ColumnReorderEndCommand command) {
        this.toColumnPositionCoordinate = command.toColumnPositionCoordinate;
        this.reorderToLeftEdge = command.reorderToLeftEdge;
    }

    public int getToColumnPosition() {
        return this.toColumnPositionCoordinate.getColumnPosition();
    }

    public boolean isReorderToLeftEdge() {
        return this.reorderToLeftEdge;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        ColumnPositionCoordinate targetToColumnPositionCoordinate = LayerCommandUtil
                .convertColumnPositionToTargetContext(
                        this.toColumnPositionCoordinate, targetLayer);
        if (targetToColumnPositionCoordinate != null) {
            this.toColumnPositionCoordinate = targetToColumnPositionCoordinate;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ColumnReorderEndCommand cloneCommand() {
        return new ColumnReorderEndCommand(this);
    }

}
