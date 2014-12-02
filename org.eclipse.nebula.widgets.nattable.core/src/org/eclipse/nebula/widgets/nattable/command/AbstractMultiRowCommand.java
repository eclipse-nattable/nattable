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

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public abstract class AbstractMultiRowCommand implements ILayerCommand {

    private Collection<RowPositionCoordinate> rowPositionCoordinates;

    protected AbstractMultiRowCommand(ILayer layer, int... rowPositions) {
        setRowPositions(layer, rowPositions);
    }

    protected AbstractMultiRowCommand(AbstractMultiRowCommand command) {
        this.rowPositionCoordinates = new HashSet<RowPositionCoordinate>(
                command.rowPositionCoordinates);
    }

    public Collection<Integer> getRowPositions() {
        Collection<Integer> rowPositions = new HashSet<Integer>();
        for (RowPositionCoordinate rowPositionCoordinate : this.rowPositionCoordinates) {
            rowPositions
                    .add(Integer.valueOf(rowPositionCoordinate.rowPosition));
        }
        return rowPositions;
    }

    protected final void setRowPositions(ILayer layer, int... rowPositions) {
        this.rowPositionCoordinates = new HashSet<RowPositionCoordinate>();
        for (int rowPosition : rowPositions) {
            this.rowPositionCoordinates.add(new RowPositionCoordinate(layer,
                    rowPosition));
        }
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        Collection<RowPositionCoordinate> convertedRowPositionCoordinates = new HashSet<RowPositionCoordinate>();
        for (RowPositionCoordinate rowPositionCoordinate : this.rowPositionCoordinates) {
            RowPositionCoordinate convertedRowPositionCoordinate = LayerCommandUtil
                    .convertRowPositionToTargetContext(rowPositionCoordinate,
                            targetLayer);
            if (convertedRowPositionCoordinate != null) {
                convertedRowPositionCoordinates
                        .add(convertedRowPositionCoordinate);
            }
        }

        if (convertedRowPositionCoordinates.size() > 0) {
            this.rowPositionCoordinates = convertedRowPositionCoordinates;
            return true;
        } else {
            return false;
        }
    }

}
