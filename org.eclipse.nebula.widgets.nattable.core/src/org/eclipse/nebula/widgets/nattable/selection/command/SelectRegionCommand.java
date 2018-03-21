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
package org.eclipse.nebula.widgets.nattable.selection.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.LayerCommandUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.ColumnPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.coordinate.RowPositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Command to trigger the selection of multiple consecutive cells.
 *
 * @since 1.6
 */
public class SelectRegionCommand implements ILayerCommand {

    private ILayer sourceLayer;
    private Rectangle region;
    private boolean shiftMask;
    private boolean controlMask;

    private int anchorRowPosition = -1;
    private int anchorColumnPosition = -1;

    /**
     * Create a SelectionRegionCommand with the given parameters.
     *
     * @param layer
     *            The {@link ILayer} to which the column and row position
     *            matches.
     * @param startColumnPosition
     *            The column position that is the start of the region to select.
     * @param startRowPosition
     *            The row position that is the start of the region to select.
     * @param regionWidth
     *            The number of columns that should be included in the
     *            selection.
     * @param regionHeight
     *            The number of rows that should be included in the selection.
     * @param shiftMask
     *            <code>true</code> if the selection should be performed as if
     *            the shift key was pressed.
     * @param controlMask
     *            <code>true</code> if the selection should be performed as if
     *            the control key was pressed.
     */
    public SelectRegionCommand(ILayer layer,
            int startColumnPosition, int startRowPosition,
            int regionWidth, int regionHeight,
            boolean shiftMask, boolean controlMask) {

        this(layer, new Rectangle(startColumnPosition, startRowPosition, regionWidth, regionHeight), shiftMask, controlMask);
    }

    /**
     * Create a SelectionRegionCommand with the given parameters.
     *
     * @param layer
     *            The {@link ILayer} to which the column and row position in the
     *            rectangle matches.
     * @param region
     *            The region that should be selected.
     * @param shiftMask
     *            <code>true</code> if the selection should be performed as if
     *            the shift key was pressed.
     * @param controlMask
     *            <code>true</code> if the selection should be performed as if
     *            the control key was pressed.
     */
    public SelectRegionCommand(ILayer layer, Rectangle region, boolean shiftMask, boolean controlMask) {
        this.sourceLayer = layer;
        this.region = region;
        this.shiftMask = shiftMask;
        this.controlMask = controlMask;
    }

    /**
     * Constructor that is used to clone a command.
     *
     * @param command
     *            The command that should be cloned.
     */
    protected SelectRegionCommand(SelectRegionCommand command) {
        this.sourceLayer = command.sourceLayer;
        this.region = command.region;
        this.shiftMask = command.shiftMask;
        this.controlMask = command.controlMask;
        this.anchorColumnPosition = command.anchorColumnPosition;
        this.anchorRowPosition = command.anchorRowPosition;
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        PositionCoordinate sourceCoordinate = new PositionCoordinate(this.sourceLayer, this.region.x, this.region.y);
        PositionCoordinate targetCoordinate = LayerCommandUtil.convertPositionToTargetContext(sourceCoordinate, targetLayer);
        if (targetCoordinate != null) {
            this.sourceLayer = targetCoordinate.getLayer();
            this.region.x = targetCoordinate.columnPosition;
            this.region.y = targetCoordinate.rowPosition;

            if (this.anchorColumnPosition >= 0) {
                ColumnPositionCoordinate sourceColumn = new ColumnPositionCoordinate(this.sourceLayer, this.anchorColumnPosition);
                ColumnPositionCoordinate targetColumn = LayerCommandUtil.convertColumnPositionToTargetContext(sourceColumn, targetLayer);
                this.anchorColumnPosition = targetColumn.getColumnPosition();
            }

            if (this.anchorRowPosition >= 0) {
                RowPositionCoordinate sourceRow = new RowPositionCoordinate(this.sourceLayer, this.anchorRowPosition);
                RowPositionCoordinate targetRow = LayerCommandUtil.convertRowPositionToTargetContext(sourceRow, targetLayer);
                this.anchorRowPosition = targetRow.getRowPosition();
            }

            return true;
        }
        return false;
    }

    @Override
    public ILayerCommand cloneCommand() {
        return new SelectRegionCommand(this);
    }

    /**
     *
     * @return The region that should be selected.
     */
    public Rectangle getRegion() {
        return this.region;
    }

    /**
     *
     * @return <code>true</code> if the selection should be performed as if the
     *         shift key was pressed.
     */
    public boolean isShiftMask() {
        return this.shiftMask;
    }

    /**
     *
     * @return <code>true</code> if the selection should be performed as if the
     *         control key was pressed.
     */
    public boolean isControlMask() {
        return this.controlMask;
    }

    /**
     *
     * @return The row position to which the selection anchor should be set to
     *         or -1 if the anchor should be set to the region start y.
     */
    public int getAnchorRowPosition() {
        return this.anchorRowPosition;
    }

    /**
     * Specifying an anchor row position will force to move the selection anchor
     * to the given row position. Without setting a value for the anchor row
     * position, the selection anchor will be placed according to the region
     * start.
     *
     * @param anchorRowPosition
     *            The row position to which the selection anchor should be set
     *            to.
     */
    public void setAnchorRowPosition(int anchorRowPosition) {
        this.anchorRowPosition = anchorRowPosition;
    }

    /**
     *
     * @return The column position to which the selection anchor should be set
     *         to or -1 if the anchor should be set to the region start x.
     */
    public int getAnchorColumnPosition() {
        return this.anchorColumnPosition;
    }

    /**
     * Specifying an anchor column position will force to move the selection
     * anchor to the given column position. Without setting a value for the
     * anchor column position, the selection anchor will be placed according to
     * the region start.
     *
     * @param anchorColumnPosition
     *            The column position to which the selection anchor should be
     *            set to.
     */
    public void setAnchorColumnPosition(int anchorColumnPosition) {
        this.anchorColumnPosition = anchorColumnPosition;
    }

}
