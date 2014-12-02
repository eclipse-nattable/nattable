/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.grid.data;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.swt.graphics.Point;

public class DummyBodyDataProvider implements IDataProvider {

    private final int columnCount;

    private final int rowCount;

    private Map<Point, Object> values = new HashMap<Point, Object>();

    public DummyBodyDataProvider(int columnCount, int rowCount) {
        this.columnCount = columnCount;
        this.rowCount = rowCount;
    }

    @Override
    public int getColumnCount() {
        return this.columnCount;
    }

    @Override
    public int getRowCount() {
        return this.rowCount;
    }

    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        Point point = new Point(columnIndex, rowIndex);
        if (this.values.containsKey(point)) {
            return this.values.get(point);
        } else {
            return "Col: " + (columnIndex + 1) + ", Row: " + (rowIndex + 1); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        this.values.put(new Point(columnIndex, rowIndex), newValue);
    }

}
