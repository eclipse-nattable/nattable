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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.test.integration;

import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
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
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.command.SortColumnCommand;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;

/**
 * This is a copy of the
 * org.eclipse.nebula.widgets.nattable.examples.fixtures.GlazedListsGridLayer It
 * has been copied here since glazed list tests bundle can't depend on the
 * examples bundle
 */
public class GlazedListsGridLayer<T> extends GridLayer {

    private ColumnOverrideLabelAccumulator columnLabelAccumulator;
    private DataLayer bodyDataLayer;
    private DataLayer columnHeaderDataLayer;
    private DefaultBodyLayerStack bodyLayerStack;
    private ListDataProvider<T> bodyDataProvider;
    private GlazedListsEventLayer<T> glazedListsEventLayer;

    public GlazedListsGridLayer(EventList<T> eventList, String[] propertyNames,
            Map<String, String> propertyToLabelMap,
            IConfigRegistry configRegistry) {
        this(eventList, propertyNames, propertyToLabelMap, configRegistry, true);
    }

    /**
     * The underlying {@link DataLayer} created is able to handle Events raised
     * by GlazedLists and fire corresponding NatTable events.
     *
     * The {@link SortHeaderLayer} triggers sorting on the the underlying
     * SortedList when a {@link SortColumnCommand} is received.
     */
    public GlazedListsGridLayer(EventList<T> eventList, String[] propertyNames,
            Map<String, String> propertyToLabelMap,
            IConfigRegistry configRegistry, boolean useDefaultConfiguration) {
        super(useDefaultConfiguration);

        // Body - with list event listener
        // NOTE: Remember to use the SortedList constructor with 'null' for the
        // Comparator
        SortedList<T> sortedList = new SortedList<>(eventList, null);
        IColumnPropertyAccessor<T> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<>(propertyNames);
        this.bodyDataProvider = new ListDataProvider<>(sortedList, columnPropertyAccessor);

        this.bodyDataLayer = new DataLayer(this.bodyDataProvider);
        this.glazedListsEventLayer = new GlazedListsEventLayer<>(this.bodyDataLayer, eventList);
        this.glazedListsEventLayer.setTestMode(true);
        this.bodyLayerStack = new DefaultBodyLayerStack(this.glazedListsEventLayer);

        // Sort Column header
        IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        this.columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(
                this.columnHeaderDataLayer, this.bodyLayerStack,
                this.bodyLayerStack.getSelectionLayer());

        // Auto configure off. Configurations have to applied manually.
        SortHeaderLayer<T> columnHeaderSortableLayer = new SortHeaderLayer<>(
                columnHeaderLayer, new GlazedListsSortModel<>(sortedList,
                        columnPropertyAccessor, configRegistry,
                        this.columnHeaderDataLayer),
                false);

        // Row header
        DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(this.bodyDataProvider);
        DefaultRowHeaderDataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer,
                this.bodyLayerStack, this.bodyLayerStack.getSelectionLayer());

        // Corner
        DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(
                columnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
        CornerLayer cornerLayer = new CornerLayer(cornerDataLayer,
                rowHeaderLayer, columnHeaderLayer);

        // Grid
        setBodyLayer(this.bodyLayerStack);
        setColumnHeaderLayer(columnHeaderSortableLayer);
        setRowHeaderLayer(rowHeaderLayer);
        setCornerLayer(cornerLayer);
    }

    public ColumnOverrideLabelAccumulator getColumnLabelAccumulator() {
        return this.columnLabelAccumulator;
    }

    @Override
    public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
        super.setClientAreaProvider(clientAreaProvider);
    }

    public DataLayer getBodyDataLayer() {
        return this.bodyDataLayer;
    }

    public ListDataProvider<T> getBodyDataProvider() {
        return this.bodyDataProvider;
    }

    public DataLayer getColumnHeaderDataLayer() {
        return this.columnHeaderDataLayer;
    }

    public DefaultBodyLayerStack getBodyLayerStack() {
        return this.bodyLayerStack;
    }

    public GlazedListsEventLayer<T> getGlazedListsEventLayer() {
        return this.glazedListsEventLayer;
    }
}
