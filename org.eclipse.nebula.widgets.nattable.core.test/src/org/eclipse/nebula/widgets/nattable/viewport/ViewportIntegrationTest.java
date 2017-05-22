/*******************************************************************************
 * Copyright (c) 2017 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.viewport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

public class ViewportIntegrationTest {

    @Test
    public void shouldInitWithNoScroll() {
        List<String> contents = new ArrayList<>(Arrays.asList("one", "two", "three", "four", "five"));
        IDataProvider bodyDataProvider = new ListDataProvider<>(contents,
                new IColumnAccessor<String>() {

                    @Override
                    public Object getDataValue(String rowObject, int columnIndex) {
                        return rowObject;
                    }

                    @Override
                    public void setDataValue(String rowObject, int columnIndex,
                            Object newValue) {
                        // ignore
                    }

                    @Override
                    public int getColumnCount() {
                        return 1;
                    }
                });

        SelectionLayer selectionLayer = new SelectionLayer(new DataLayer(bodyDataProvider));
        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

        IDataProvider colDataProvider = new DummyColumnHeaderDataProvider(bodyDataProvider);
        ColumnHeaderLayer colHeader = new ColumnHeaderLayer(
                new DataLayer(colDataProvider), viewportLayer, selectionLayer);

        IDataProvider rowDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
        RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(
                new DataLayer(rowDataProvider), viewportLayer, selectionLayer);

        CornerLayer cornerLayer = new CornerLayer(
                new DataLayer(new DefaultCornerDataProvider(colDataProvider, rowDataProvider)),
                rowHeaderLayer, colHeader);

        GridLayer grid = new GridLayer(viewportLayer, colHeader, rowHeaderLayer, cornerLayer);
        // create the table with no scrollbars
        NatTable natTable = new NatTable(
                new Shell(),
                SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED,
                grid);
        // this caused a NPE for scrollbar initialization
        natTable.setSize(600, 600);
    }
}
