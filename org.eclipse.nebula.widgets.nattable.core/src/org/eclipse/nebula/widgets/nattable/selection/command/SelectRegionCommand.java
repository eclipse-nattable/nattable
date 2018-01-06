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
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
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
    }

    @Override
    public boolean convertToTargetLayer(ILayer targetLayer) {
        PositionCoordinate sourceCoordinate = new PositionCoordinate(this.sourceLayer, this.region.x, this.region.y);
        PositionCoordinate targetCoordinate = LayerCommandUtil.convertPositionToTargetContext(sourceCoordinate, targetLayer);
        if (targetCoordinate != null) {
            this.region.x = targetCoordinate.columnPosition;
            this.region.y = targetCoordinate.rowPosition;
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

}
