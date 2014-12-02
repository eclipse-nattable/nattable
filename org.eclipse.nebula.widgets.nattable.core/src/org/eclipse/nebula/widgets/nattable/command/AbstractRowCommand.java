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

import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public abstract class AbstractRowCommand implements ILayerCommand {

    private RowPositionCoordinate rowPositionCoordinate;

    protected AbstractRowCommand(ILayer layer, int rowPosition) {
        this.rowPositionCoordinate = new RowPositionCoordinate(layer, rowPosition);
    }

    protected AbstractRowCommand(AbstractRowCommand command) {
        this.rowPositionCoordinate = command.rowPositionCoordinate;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        RowPositionCoordinate targetRowPositionCoordinate = LayerCommandUtil
                .convertRowPositionToTargetContext(this.rowPositionCoordinate,
                        targetLayer);
        if (targetRowPositionCoordinate != null) {
            this.rowPositionCoordinate = targetRowPositionCoordinate;
            return true;
        } else {
            return false;
        }
    }

    public int getRowPosition() {
        return this.rowPositionCoordinate.getRowPosition();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + " rowPosition=" + this.rowPositionCoordinate.getRowPosition(); //$NON-NLS-1$
    }

}
