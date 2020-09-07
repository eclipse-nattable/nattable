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
package org.eclipse.nebula.widgets.nattable.examples.fixtures;

import java.util.Map;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

public class SelectionExampleGridLayer extends GridLayer {

    private final DataLayer bodyDataLayer;
    private final DataLayer columnHeaderDataLayer;
    private SelectionExampleBodyLayerStack bodyLayer;
    private ListDataProvider<RowDataFixture> bodyDataProvider;

    public SelectionExampleGridLayer() {
        super(true);
        EventList<RowDataFixture> eventList = GlazedLists.eventList(RowDataListFixture.getList());
        String[] propertyNames = RowDataListFixture.getPropertyNames();
        Map<String, String> propertyToLabelMap = RowDataListFixture.getPropertyToLabelMap();

        IColumnPropertyAccessor<RowDataFixture> columnPropertyAccessor =
                new ReflectiveColumnPropertyAccessor<>(propertyNames);
        this.bodyDataProvider =
                new ListDataProvider<>(eventList, columnPropertyAccessor);

        this.bodyDataLayer = new DataLayer(this.bodyDataProvider);
        this.bodyLayer = new SelectionExampleBodyLayerStack(this.bodyDataLayer);

        // Column header
        IDataProvider columnHeaderDataProvider =
                new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        this.columnHeaderDataLayer =
                new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ColumnHeaderLayer columnHeaderLayer =
                new ColumnHeaderLayer(this.columnHeaderDataLayer, this.bodyLayer, this.bodyLayer.getSelectionLayer());

        // Row header
        DefaultRowHeaderDataProvider rowHeaderDataProvider =
                new DefaultRowHeaderDataProvider(this.bodyDataProvider);
        DefaultRowHeaderDataLayer rowHeaderDataLayer =
                new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        RowHeaderLayer rowHeaderLayer =
                new RowHeaderLayer(rowHeaderDataLayer, this.bodyLayer, this.bodyLayer.getSelectionLayer());

        // Corner
        DefaultCornerDataProvider cornerDataProvider =
                new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer =
                new DataLayer(cornerDataProvider);
        CornerLayer cornerLayer =
                new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);

        // Grid
        setBodyLayer(this.bodyLayer);
        setColumnHeaderLayer(columnHeaderLayer);
        setRowHeaderLayer(rowHeaderLayer);
        setCornerLayer(cornerLayer);
    }

    public SelectionLayer getSelectionLayer() {
        return this.bodyLayer.getSelectionLayer();
    }

    public DataLayer getBodyDataLayer() {
        return this.bodyDataLayer;
    }

    public ListDataProvider<RowDataFixture> getBodyDataProvider() {
        return this.bodyDataProvider;
    }

    public DataLayer getColumnHeaderDataLayer() {
        return this.columnHeaderDataLayer;
    }

}
