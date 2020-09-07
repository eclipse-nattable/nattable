/*******************************************************************************
 * Copyright (c) 2017, 2020 Dirk Fauth and others.
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
    public Point getKeyWithColumnUpdate(Point oldKey, int columnIndex) {
        return new Point(columnIndex, oldKey.y);
    }

    @Override
    public Point getKeyWithRowUpdate(Point oldKey, int rowIndex) {
        return new Point(oldKey.x, rowIndex);
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
