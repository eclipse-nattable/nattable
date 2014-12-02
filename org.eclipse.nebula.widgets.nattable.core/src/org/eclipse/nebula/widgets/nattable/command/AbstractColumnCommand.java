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
package org.eclipse.nebula.widgets.nattable.command;

import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public abstract class AbstractColumnCommand implements ILayerCommand {

    private ColumnPositionCoordinate columnPositionCoordinate;

    protected AbstractColumnCommand(ILayer layer, int columnPosition) {
        this.columnPositionCoordinate = new ColumnPositionCoordinate(layer,
                columnPosition);
    }

    protected AbstractColumnCommand(AbstractColumnCommand command) {
        this.columnPositionCoordinate = command.columnPositionCoordinate;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        ColumnPositionCoordinate targetColumnPositionCoordinate = LayerCommandUtil
                .convertColumnPositionToTargetContext(this.columnPositionCoordinate,
                        targetLayer);
        if (targetColumnPositionCoordinate != null) {
            this.columnPositionCoordinate = targetColumnPositionCoordinate;
            return true;
        } else {
            return false;
        }
    }

    public ILayer getLayer() {
        return this.columnPositionCoordinate.getLayer();
    }

    public int getColumnPosition() {
        return this.columnPositionCoordinate.getColumnPosition();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + " columnPosition=" + this.columnPositionCoordinate.getColumnPosition(); //$NON-NLS-1$
    }

}
