/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractRowCommand;
import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class SelectRowGroupsCommand extends AbstractRowCommand {

    private ColumnPositionCoordinate columnPositionCoordinate;
    private final boolean withShiftMask;
    private final boolean withControlMask;
    private boolean moveAnchorToTopOfGroup = false;
    private int rowPositionToMoveIntoViewport = -1;

    public SelectRowGroupsCommand(ILayer layer, int columnPosition,
            int rowPosition, boolean withShiftMask, boolean withControlMask,
            boolean moveAnchortoTopOfGroup) {
        this(layer, columnPosition, rowPosition, withShiftMask,
                withControlMask, moveAnchortoTopOfGroup, -1);
    }

    public SelectRowGroupsCommand(ILayer layer, int columnPosition,
            int rowPosition, boolean withShiftMask, boolean withControlMask,
            boolean moveAnchortoTopOfGroup, int rowPositionToMoveIntoViewport) {
        this(layer, columnPosition, rowPosition, withShiftMask, withControlMask);
        this.moveAnchorToTopOfGroup = moveAnchortoTopOfGroup;
        this.rowPositionToMoveIntoViewport = rowPositionToMoveIntoViewport;
    }

    public SelectRowGroupsCommand(ILayer layer, int columnPosition,
            int rowPosition, boolean withShiftMask, boolean withControlMask) {
        super(layer, rowPosition);
        this.withShiftMask = withShiftMask;
        this.withControlMask = withControlMask;
        this.columnPositionCoordinate = new ColumnPositionCoordinate(layer,
                columnPosition);
    }

    protected SelectRowGroupsCommand(SelectRowGroupsCommand command) {
        super(command);
        this.columnPositionCoordinate = command.columnPositionCoordinate;
        this.withShiftMask = command.withShiftMask;
        this.withControlMask = command.withControlMask;
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

    @Override
    public SelectRowGroupsCommand cloneCommand() {
        return new SelectRowGroupsCommand(this);
    }

    public boolean isMoveAnchorToTopOfGroup() {
        return this.moveAnchorToTopOfGroup;
    }

    public int getRowPositionToMoveIntoViewport() {
        return this.rowPositionToMoveIntoViewport;
    }

}
