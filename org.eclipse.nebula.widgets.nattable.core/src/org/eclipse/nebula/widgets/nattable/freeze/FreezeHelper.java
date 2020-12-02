/*******************************************************************************
 * Copyright (c) 2012, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.freeze;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeColumnCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezePositionCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeRowCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeSelectionCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.UnFreezeGridCommand;
import org.eclipse.nebula.widgets.nattable.freeze.event.FreezeEvent;
import org.eclipse.nebula.widgets.nattable.freeze.event.UnfreezeEvent;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

/**
 * Helper class to deal with freeze and unfreeze of a NatTable.
 *
 * <p>
 * This class is intended for internal use only. Consider using the appropriate
 * commands instead of using this helper directly.
 *
 * @see FreezeColumnCommand
 * @see FreezeRowCommand
 * @see FreezePositionCommand
 * @see FreezeSelectionCommand
 * @see UnFreezeGridCommand
 */
public final class FreezeHelper {

    private FreezeHelper() {
        // private default constructor for helper class
    }

    /**
     * Freezes the grid at the specified position. This method is for internal
     * use. Consider using the appropriate commands on the NatTable instead to
     * freeze the grid programmatically.
     *
     * @param freezeLayer
     *            The FreezeLayer of the grid to perform the freeze action.
     * @param viewportLayer
     *            The ViewportLayer of the grid to perform the freeze action.
     * @param topLeftPosition
     *            The top left position of the freeze area
     * @param bottomRightPosition
     *            The bottom right position of the freeze area
     *
     * @see FreezeColumnCommand
     * @see FreezeRowCommand
     * @see FreezePositionCommand
     * @see FreezeSelectionCommand
     */
    public static void freeze(
            FreezeLayer freezeLayer,
            ViewportLayer viewportLayer,
            PositionCoordinate topLeftPosition,
            PositionCoordinate bottomRightPosition) {

        if (freezeLayer == null || viewportLayer == null) {
            throw new IllegalArgumentException("freezeLayer and viewportLayer can not be null!"); //$NON-NLS-1$
        }

        if (topLeftPosition != null && bottomRightPosition != null) {
            freezeLayer.setTopLeftPosition(
                    topLeftPosition.columnPosition,
                    topLeftPosition.rowPosition);
            freezeLayer.setBottomRightPosition(
                    bottomRightPosition.columnPosition,
                    bottomRightPosition.rowPosition);

            IUniqueIndexLayer scrollableLayer = viewportLayer.getScrollableLayer();
            int originX = (bottomRightPosition.columnPosition == scrollableLayer.getColumnCount() - 1)
                    ? scrollableLayer.getWidth()
                    : scrollableLayer.getStartXOfColumnPosition(bottomRightPosition.columnPosition + 1);
            int originY = (bottomRightPosition.rowPosition == scrollableLayer.getRowCount() - 1)
                    ? scrollableLayer.getHeight()
                    : scrollableLayer.getStartYOfRowPosition(bottomRightPosition.rowPosition + 1);
            viewportLayer.setMinimumOrigin(originX, originY);
            viewportLayer.setOriginX(0);
            viewportLayer.setOriginY(0);
            viewportLayer.fireLayerEvent(new FreezeEvent(viewportLayer));
        }
    }

    /**
     * Unfreezes the grid at the specified position. This method is for internal
     * use. Consider using the appropriate command on the NatTable instead to
     * unfreeze the grid programmatically.
     *
     * @param freezeLayer
     *            The FreezeLayer of the grid to perform the freeze action.
     * @param viewportLayer
     *            The ViewportLayer of the grid to perform the freeze action.
     *
     * @see UnFreezeGridCommand
     */
    public static void unfreeze(FreezeLayer freezeLayer, ViewportLayer viewportLayer) {
        if (freezeLayer == null || viewportLayer == null) {
            throw new IllegalArgumentException("freezeLayer and viewportLayer can not be null!"); //$NON-NLS-1$
        }

        resetViewport(freezeLayer, viewportLayer);

        freezeLayer.setTopLeftPosition(-1, -1);
        freezeLayer.setBottomRightPosition(-1, -1);

        viewportLayer.fireLayerEvent(new UnfreezeEvent(viewportLayer));
    }

    /**
     * Helper method to reset the origin coordinates of the viewport. Is needed
     * to perform an unfreeze or to override a current frozen state.
     *
     * @param freezeLayer
     *            The FreezeLayer of the grid to perform the freeze action.
     * @param viewportLayer
     *            The ViewportLayer of the grid to perform the freeze action.
     */
    public static void resetViewport(FreezeLayer freezeLayer, ViewportLayer viewportLayer) {
        PositionCoordinate topLeftPosition = freezeLayer.getTopLeftPosition();
        viewportLayer.resetOrigin(
                viewportLayer.getScrollableLayer().getStartXOfColumnPosition(Math.max(0, topLeftPosition.columnPosition)),
                viewportLayer.getScrollableLayer().getStartYOfRowPosition(Math.max(0, topLeftPosition.rowPosition)));
    }

    /**
     * Helper method to retrieve the top left column position on freezing a
     * column. Will get the column position on the underlying layer of the
     * ViewportLayer based on the origin x coordinate. For the
     * ResizeColumnHideShowLayer a special handling is performed to get the left
     * most column position in case columns to the left are hidden.
     *
     * @param viewportLayer
     *            The ViewportLayer needed for calculating the top left column
     *            position.
     * @return The column position that should be used for the top left
     *         coordinate.
     *
     * @since 1.6
     */
    public static int getTopLeftColumnPosition(ViewportLayer viewportLayer) {
        int columnPosition = viewportLayer.getScrollableLayer().getColumnPositionByX(viewportLayer.getOrigin().getX());

        // special handling for ResizeColumnHideShowLayer
        // hidden columns have a width of 0, therefore we need to check if there
        // are hidden columns to the left by checking the start x position of
        // adjacent columns to the left
        int startX = viewportLayer.getScrollableLayer().getStartXOfColumnPosition(columnPosition);
        int originStartX = startX;
        while (columnPosition > 0) {
            startX = viewportLayer.getScrollableLayer().getStartXOfColumnPosition(columnPosition - 1);
            if (startX == originStartX) {
                columnPosition--;
            } else {
                break;
            }
        }
        return columnPosition;
    }
}
