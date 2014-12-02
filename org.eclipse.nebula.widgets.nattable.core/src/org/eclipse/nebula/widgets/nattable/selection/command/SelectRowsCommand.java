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
package org.eclipse.nebula.widgets.nattable.selection.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractMultiRowCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;

public class SelectRowsCommand extends AbstractMultiRowCommand {

    private ColumnPositionCoordinate columnPositionCoordinate;
    private final boolean withShiftMask;
    private final boolean withControlMask;
    private RowPositionCoordinate rowPositionCoordinateToMoveIntoViewport;

    public SelectRowsCommand(ILayer layer, int columnPosition, int rowPosition,
            boolean withShiftMask, boolean withControlMask) {
        this(layer, columnPosition, ArrayUtil.asIntArray(rowPosition),
                withShiftMask, withControlMask, rowPosition);
    }

    public SelectRowsCommand(ILayer layer, int columnPosition,
            int[] rowPositions, boolean withShiftMask, boolean withControlMask,
            int rowPositionToMoveIntoViewport) {
        super(layer, rowPositions);
        this.columnPositionCoordinate = new ColumnPositionCoordinate(layer,
                columnPosition);
        this.withControlMask = withControlMask;
        this.withShiftMask = withShiftMask;
        this.rowPositionCoordinateToMoveIntoViewport = new RowPositionCoordinate(
                layer, rowPositionToMoveIntoViewport);
    }

    protected SelectRowsCommand(SelectRowsCommand command) {
        super(command);
        this.columnPositionCoordinate = command.columnPositionCoordinate;
        this.withShiftMask = command.withShiftMask;
        this.withControlMask = command.withControlMask;
        this.rowPositionCoordinateToMoveIntoViewport = command.rowPositionCoordinateToMoveIntoViewport;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        ColumnPositionCoordinate targetColumnPositionCoordinate = LayerCommandUtil
                .convertColumnPositionToTargetContext(this.columnPositionCoordinate,
                        targetLayer);

        if (targetColumnPositionCoordinate != null
                && targetColumnPositionCoordinate.getColumnPosition() >= 0
                && super.convertToTargetLayer(targetLayer)) {
            this.columnPositionCoordinate = targetColumnPositionCoordinate;
            this.rowPositionCoordinateToMoveIntoViewport = LayerCommandUtil
                    .convertRowPositionToTargetContext(
                            this.rowPositionCoordinateToMoveIntoViewport,
                            targetLayer);
            return true;
        }
        return false;
    }

    public int getColumnPosition() {
        return this.columnPositionCoordinate.getColumnPosition();
    }

    public boolean isWithShiftMask() {
        return this.withShiftMask;
    }

    public boolean isWithControlMask() {
        return this.withControlMask;
    }

    public int getRowPositionToMoveIntoViewport() {
        if (this.rowPositionCoordinateToMoveIntoViewport != null) {
            return this.rowPositionCoordinateToMoveIntoViewport.getRowPosition();
        } else {
            return -1;
        }
    }

    @Override
    public SelectRowsCommand cloneCommand() {
        return new SelectRowsCommand(this);
    }
}
