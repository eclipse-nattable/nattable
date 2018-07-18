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

    public ViewportSelectColumnGroupCommandHandler(AbstractLayer viewportLayer) {
        if (viewportLayer == null) {
            throw new IllegalArgumentException("viewportLayer cannot be null!"); //$NON-NLS-1$
        }
        this.viewportLayer = viewportLayer;
    }

    @Override
    protected boolean doCommand(ViewportSelectColumnGroupCommand command) {
        ILayer underlyingLayer = this.viewportLayer.getUnderlyingLayerByPosition(0, 0);
        ColumnPositionCoordinate underlyingStart =
                LayerCommandUtil.convertColumnPositionToTargetContext(
                        new ColumnPositionCoordinate(command.getLayer(), command.getOriginColumnPosition()),
                        underlyingLayer);
        int span = command.getColumnSpan();

        if (underlyingStart == null) {
            // check the diff between origin and clicked position to modify the
            // span
            span += command.getOriginColumnPosition();
            underlyingStart =
                    LayerCommandUtil.convertColumnPositionToTargetContext(
                            new ColumnPositionCoordinate(command.getLayer(), 0),
                            underlyingLayer);
        }

        if (underlyingStart != null) {
            SelectRegionCommand regionCommand = new SelectRegionCommand(
                    underlyingLayer,
                    underlyingStart.getColumnPosition(),
                    0,
                    span,
                    Integer.MAX_VALUE,
                    command.isWithShiftMask(),
                    command.isWithControlMask());

            // set the anchor row position to the first column and first row in
            // the viewport
            RowPositionCoordinate underlyingRow = LayerCommandUtil.convertRowPositionToTargetContext(
                    new RowPositionCoordinate(this.viewportLayer, 0), underlyingLayer);
            regionCommand.setAnchorRowPosition(underlyingRow.rowPosition);
            if (command.getOriginColumnPosition() <= 0) {
                ColumnPositionCoordinate underlyingColumn = LayerCommandUtil.convertColumnPositionToTargetContext(
                        new ColumnPositionCoordinate(this.viewportLayer, 0), underlyingLayer);
                regionCommand.setAnchorColumnPosition(underlyingColumn.columnPosition);
            }
            underlyingLayer.doCommand(regionCommand);
        }

        return true;
    }

    @Override
    public Class<ViewportSelectColumnGroupCommand> getCommandClass() {
        return ViewportSelectColumnGroupCommand.class;
    }

}