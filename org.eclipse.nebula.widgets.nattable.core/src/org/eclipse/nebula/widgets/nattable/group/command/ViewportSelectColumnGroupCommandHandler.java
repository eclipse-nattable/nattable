/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRegionCommand;

/**
 * Command handler for the {@link ViewportSelectColumnGroupCommand}. Calculates
 * the column group based on the column position contained in the command and
 * triggers the selection via {@link SelectRegionCommand}.
 *
 * @since 1.6
 */
public class ViewportSelectColumnGroupCommandHandler extends AbstractLayerCommandHandler<ViewportSelectColumnGroupCommand> {

    private final AbstractLayer viewportLayer;
    private final ColumnGroupHeaderLayer columnGroupHeaderLayer;
    private final ColumnGroupGroupHeaderLayer columnGroupGroupHeaderLayer;

    public ViewportSelectColumnGroupCommandHandler(
            AbstractLayer viewportLayer,
            ColumnGroupHeaderLayer columnGroupHeaderLayer) {

        this(viewportLayer, columnGroupHeaderLayer, null);
    }

    public ViewportSelectColumnGroupCommandHandler(
            AbstractLayer viewportLayer,
            ColumnGroupHeaderLayer columnGroupHeaderLayer,
            ColumnGroupGroupHeaderLayer columnGroupGroupHeaderLayer) {

        if (viewportLayer == null || columnGroupHeaderLayer == null) {
            throw new IllegalArgumentException("viewportLayer and columnGroupHeaderLayer cannot be null!"); //$NON-NLS-1$
        }
        this.viewportLayer = viewportLayer;
        this.columnGroupHeaderLayer = columnGroupHeaderLayer;
        this.columnGroupGroupHeaderLayer = columnGroupGroupHeaderLayer;
    }

    @Override
    protected boolean doCommand(ViewportSelectColumnGroupCommand command) {
        int start = -1;
        int span = -1;
        // if a column group group is configured, we inspect the row position
        if (this.columnGroupGroupHeaderLayer != null && command.getNatTableRowPosition() == 0) {
            start = this.columnGroupGroupHeaderLayer.getStartPositionOfGroup(command.getColumnPosition());
            span = this.columnGroupGroupHeaderLayer.getColumnSpan(command.getColumnPosition());
        } else {
            start = this.columnGroupHeaderLayer.getStartPositionOfGroup(command.getColumnPosition());
            span = this.columnGroupHeaderLayer.getColumnSpan(command.getColumnPosition());
        }

        // the SelectRegionCommand needs to be executed on the underlying layer
        // this way the row range from 0 to MAX works, which otherwise breaks
        // when selecting a column group on scrolled state
        ILayer underlyingLayer = this.viewportLayer.getUnderlyingLayerByPosition(0, 0);
        ColumnPositionCoordinate underlyingStart = LayerCommandUtil.convertColumnPositionToTargetContext(
                new ColumnPositionCoordinate(this.viewportLayer, start), underlyingLayer);
        SelectRegionCommand regionCommand = new SelectRegionCommand(
                underlyingLayer,
                underlyingStart.getColumnPosition(),
                0,
                span,
                Integer.MAX_VALUE,
                command.isWithShiftMask(),
                command.isWithControlMask());

        // set the anchor row position to the first row in the viewport
        RowPositionCoordinate underlyingRow = LayerCommandUtil.convertRowPositionToTargetContext(
                new RowPositionCoordinate(this.viewportLayer, 0), underlyingLayer);
        regionCommand.setAnchorRowPosition(underlyingRow.rowPosition);
        underlyingLayer.doCommand(regionCommand);

        return true;
    }

    @Override
    public Class<ViewportSelectColumnGroupCommand> getCommandClass() {
        return ViewportSelectColumnGroupCommand.class;
    }

}