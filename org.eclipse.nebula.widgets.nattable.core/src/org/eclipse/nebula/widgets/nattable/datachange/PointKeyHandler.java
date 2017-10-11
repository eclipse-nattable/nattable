/*******************************************************************************
 * Copyright (c) 2017 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.datachange;

import org.eclipse.swt.graphics.Point;

/**
 * Implementation of the {@link CellKeyHandler} that handles {@link Point}s as
 * keys.
 *
 * @since 1.6
 */
public class PointKeyHandler implements CellKeyHandler<Point> {

    @Override
    public Point getKey(int columnIndex, int rowIndex) {
        return new Point(columnIndex, rowIndex);
    }

    @Override
    public int getColumnIndex(Point key) {
        return key.x;
    }

    @Override
    public int getRowIndex(Point key) {
        return key.y;
    }

    @Override
    public boolean updateOnHorizontalStructuralChange() {
        // return true because the column is identified by index
        return true;
    }

    @Override
    public boolean updateOnVerticalStructuralChange() {
        // return true because the row is identified by index
        return true;
    }

}
