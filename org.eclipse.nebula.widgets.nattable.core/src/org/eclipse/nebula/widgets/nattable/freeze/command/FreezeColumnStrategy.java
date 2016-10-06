/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
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
            columnPosition = this.viewportLayer.getScrollableLayer().getColumnPositionByX(this.viewportLayer.getOrigin().getX());
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
