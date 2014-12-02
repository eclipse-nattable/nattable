/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/

package org.eclipse.nebula.widgets.nattable.layer;

import org.eclipse.nebula.widgets.nattable.data.ISpanningDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.DataCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.swt.graphics.Rectangle;

public class SpanningDataLayer extends DataLayer {

    public SpanningDataLayer(ISpanningDataProvider dataProvider) {
        super(dataProvider);
    }

    public SpanningDataLayer(ISpanningDataProvider dataProvider,
            int defaultColumnWidth, int defaultRowHeight) {
        super(dataProvider, defaultColumnWidth, defaultRowHeight);
    }

    protected SpanningDataLayer() {
        super();
    }

    protected SpanningDataLayer(int defaultColumnWidth, int defaultRowHeight) {
        super(defaultColumnWidth, defaultRowHeight);
    }

    @Override
    public ISpanningDataProvider getDataProvider() {
        return (ISpanningDataProvider) super.getDataProvider();
    }

    @Override
    public ILayerCell getCellByPosition(int columnPosition, int rowPosition) {
        if (columnPosition < 0 || columnPosition >= getColumnCount()
                || rowPosition < 0 || rowPosition >= getRowCount()) {
            return null;
        }

        DataCell dataCell = getDataProvider().getCellByPosition(columnPosition,
                rowPosition);

        return new LayerCell(this, columnPosition, rowPosition, dataCell);
    }

    @Override
    public Rectangle getBoundsByPosition(int columnPosition, int rowPosition) {
        ILayerCell cell = getCellByPosition(columnPosition, rowPosition);
        return super.getBoundsByPosition(cell.getOriginColumnPosition(),
                cell.getOriginRowPosition());
    }

    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        ILayerCell cell = getCellByPosition(columnIndex, rowIndex);
        for (int i = 0; i < cell.getColumnSpan(); i++) {
            for (int j = 0; j < cell.getRowSpan(); j++) {
                super.setDataValue(cell.getOriginColumnPosition() + i,
                        cell.getOriginRowPosition() + j, newValue);
            }
        }
    }
}
