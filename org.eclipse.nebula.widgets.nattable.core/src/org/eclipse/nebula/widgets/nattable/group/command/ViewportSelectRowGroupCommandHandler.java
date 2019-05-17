/*******************************************************************************
 * Copyright (c) 2019 Dirk Fauth.
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
 * Command handler for the {@link ViewportSelectRowGroupCommand}. Calculates the
 * row group based on the row position contained in the command and triggers the
 * selection via {@link SelectRegionCommand}.
 *
 * @since 1.6
 */
public class ViewportSelectRowGroupCommandHandler extends AbstractLayerCommandHandler<ViewportSelectRowGroupCommand> {

    private final AbstractLayer viewportLayer;

    public ViewportSelectRowGroupCommandHandler(AbstractLayer viewportLayer) {
        if (viewportLayer == null) {
            throw new IllegalArgumentException("viewportLayer cannot be null!"); //$NON-NLS-1$
        }
        this.viewportLayer = viewportLayer;
    }

    @Override
    protected boolean doCommand(ViewportSelectRowGroupCommand command) {
        ILayer underlyingLayer = this.viewportLayer.getUnderlyingLayerByPosition(0, 0);
        RowPositionCoordinate underlyingStart =
                LayerCommandUtil.convertRowPositionToTargetContext(
                        new RowPositionCoordinate(command.getLayer(), command.getOriginRowPosition()),
                        underlyingLayer);
        int span = command.getRowSpan();

        if (underlyingStart == null) {
            // check the diff between origin and clicked position to modify the
            // span
            span += command.getOriginRowPosition();
            underlyingStart =
                    LayerCommandUtil.convertRowPositionToTargetContext(
                            new RowPositionCoordinate(command.getLayer(), 0),
                            underlyingLayer);
        }

        if (underlyingStart != null) {
            SelectRegionCommand regionCommand = new SelectRegionCommand(
                    underlyingLayer,
                    0,
                    underlyingStart.getRowPosition(),
                    Integer.MAX_VALUE,
                    span,
                    command.isWithShiftMask(),
                    command.isWithControlMask());

            // set the anchor column position to the first column and first row
            // in the viewport
            ColumnPositionCoordinate underlyingColumn = LayerCommandUtil.convertColumnPositionToTargetContext(
                    new ColumnPositionCoordinate(this.viewportLayer, 0), underlyingLayer);
            regionCommand.setAnchorColumnPosition(underlyingColumn.columnPosition);
            if (command.getOriginRowPosition() <= 0) {
                RowPositionCoordinate underlyingRow = LayerCommandUtil.convertRowPositionToTargetContext(
                        new RowPositionCoordinate(this.viewportLayer, 0), underlyingLayer);
                regionCommand.setAnchorRowPosition(underlyingRow.rowPosition);
            }
            underlyingLayer.doCommand(regionCommand);
        }

        return true;
    }

    @Override
    public Class<ViewportSelectRowGroupCommand> getCommandClass() {
        return ViewportSelectRowGroupCommand.class;
    }

}