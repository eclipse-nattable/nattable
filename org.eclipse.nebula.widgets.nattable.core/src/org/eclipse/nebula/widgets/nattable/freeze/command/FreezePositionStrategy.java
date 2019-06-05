/*******************************************************************************
 * Copyright (c) 2012, 2019 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.freeze.command;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeHelper;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class FreezePositionStrategy implements IFreezeCoordinatesProvider {

    private final FreezeLayer freezeLayer;
    private final ViewportLayer viewportLayer;

    private final int columnPosition;
    private final int rowPosition;

    public FreezePositionStrategy(FreezeLayer freezeLayer, int columnPosition, int rowPosition) {
        this(freezeLayer, null, columnPosition, rowPosition);
    }

    /**
     * @param freezeLayer
     *            The {@link FreezeLayer} for the {@link PositionCoordinate}.
     * @param viewportLayer
     *            The {@link ViewportLayer} needed to calculate the viewport
     *            relative position.
     * @param columnPosition
     *            The column position based on the CompositeFreezeLayer.
     * @param rowPosition
     *            The row position based on the CompositeFreezeLayer.
     * @since 1.5
     */
    public FreezePositionStrategy(FreezeLayer freezeLayer, ViewportLayer viewportLayer, int columnPosition, int rowPosition) {
        this(freezeLayer, viewportLayer, columnPosition, rowPosition, false);
    }

    /**
     * @param freezeLayer
     *            The {@link FreezeLayer} for the {@link PositionCoordinate}.
     * @param viewportLayer
     *            The {@link ViewportLayer} needed to calculate the viewport
     *            relative position.
     * @param columnPosition
     *            The column position based on the CompositeFreezeLayer.
     * @param rowPosition
     *            The row position based on the CompositeFreezeLayer.
     * @param include
     *            Whether the last selected cell should be included in the
     *            freeze region or not. Include means the freeze borders will be
     *            to the right and bottom, while exclude means the freeze
     *            borders are to the left and top. Default is
     *            <code>false</code>.
     * @since 1.6
     */
    public FreezePositionStrategy(FreezeLayer freezeLayer, ViewportLayer viewportLayer, int columnPosition, int rowPosition, boolean include) {
        this.freezeLayer = freezeLayer;
        this.viewportLayer = viewportLayer;
        this.columnPosition = !include ? columnPosition : columnPosition + 1;
        this.rowPosition = !include ? rowPosition : rowPosition + 1;
    }

    @Override
    public PositionCoordinate getTopLeftPosition() {
        int columnPosition = 0;
        int rowPosition = 0;
        if (this.viewportLayer != null) {
            columnPosition = FreezeHelper.getTopLeftColumnPosition(this.viewportLayer);
            if (columnPosition > 0 && columnPosition >= this.columnPosition) {
                columnPosition = this.columnPosition;
            }
            rowPosition = this.viewportLayer.getScrollableLayer().getRowPositionByY(this.viewportLayer.getOrigin().getY());
            if (rowPosition > 0 && rowPosition >= this.rowPosition) {
                rowPosition = this.rowPosition;
            }
        }
        return new PositionCoordinate(this.freezeLayer, columnPosition, rowPosition);
    }

    @Override
    public PositionCoordinate getBottomRightPosition() {
        return new PositionCoordinate(this.freezeLayer, this.columnPosition - 1, this.rowPosition - 1);
    }

}
