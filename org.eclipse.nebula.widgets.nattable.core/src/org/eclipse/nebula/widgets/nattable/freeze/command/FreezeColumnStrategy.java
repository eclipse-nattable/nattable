/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.freeze.command;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeHelper;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class FreezeColumnStrategy implements IFreezeCoordinatesProvider {

    private final FreezeLayer freezeLayer;
    private final ViewportLayer viewportLayer;

    private final int columnPosition;

    public FreezeColumnStrategy(FreezeLayer freezeLayer, int columnPosition) {
        this(freezeLayer, null, columnPosition);
    }

    /**
     *
     * @param freezeLayer
     *            The {@link FreezeLayer} for the {@link PositionCoordinate}.
     * @param viewportLayer
     *            The {@link ViewportLayer} needed to calculate the viewport
     *            relative position.
     * @param columnPosition
     *            The column position based on the CompositeFreezeLayer.
     * @since 1.5
     */
    public FreezeColumnStrategy(FreezeLayer freezeLayer, ViewportLayer viewportLayer, int columnPosition) {
        this.freezeLayer = freezeLayer;
        this.viewportLayer = viewportLayer;
        this.columnPosition = columnPosition;
    }

    @Override
    public PositionCoordinate getTopLeftPosition() {
        int columnPosition = 0;
        if (this.viewportLayer != null) {
            columnPosition = FreezeHelper.getTopLeftColumnPosition(this.viewportLayer);

            if (columnPosition > 0 && columnPosition >= this.columnPosition) {
                columnPosition = this.columnPosition;
            }
        }
        return new PositionCoordinate(this.freezeLayer, columnPosition, -1);
    }

    @Override
    public PositionCoordinate getBottomRightPosition() {
        return new PositionCoordinate(this.freezeLayer, this.columnPosition, -1);
    }

}
