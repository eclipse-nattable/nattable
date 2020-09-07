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

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.DefaultGlazedListsFilterStrategy;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowHeaderComposite;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;

public class FullFeaturedColumnHeaderLayerStack<T> extends
        AbstractLayerTransform {

    private final ColumnHeaderLayer columnHeaderLayer;
    private final ColumnGroupHeaderLayer columnGroupHeaderLayer;
    private final SortHeaderLayer<T> sortableColumnHeaderLayer;
    private final IDataProvider columnHeaderDataProvider;
    private final DefaultColumnHeaderDataLayer columnHeaderDataLayer;

    public FullFeaturedColumnHeaderLayerStack(SortedList<T> sortedList,
            FilterList<T> filterList, String[] propertyNames,
            Map<String, String> propertyToLabelMap, ILayer bodyLayer,
            SelectionLayer selectionLayer, ColumnGroupModel columnGroupModel,
            IConfigRegistry configRegistry) {

        this.columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(
                propertyNames, propertyToLabelMap);

        this.columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(
                this.columnHeaderDataProvider);

        this.columnHeaderLayer = new ColumnHeaderLayer(this.columnHeaderDataLayer,
                bodyLayer, selectionLayer);

        final ReflectiveColumnPropertyAccessor<T> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<>(
                propertyNames);
        this.sortableColumnHeaderLayer = new SortHeaderLayer<>(this.columnHeaderLayer,
                new GlazedListsSortModel<>(sortedList, columnPropertyAccessor,
                        configRegistry, this.columnHeaderDataLayer));

        this.columnGroupHeaderLayer = new ColumnGroupHeaderLayer(
                this.sortableColumnHeaderLayer, selectionLayer, columnGroupModel);

        FilterRowHeaderComposite<T> composite = new FilterRowHeaderComposite<>(
                new DefaultGlazedListsFilterStrategy<>(filterList,
                        columnPropertyAccessor, configRegistry),
                this.columnGroupHeaderLayer, this.columnHeaderDataProvider,
                configRegistry);

        setUnderlyingLayer(composite);
    }

    @Override
    public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
        super.setClientAreaProvider(clientAreaProvider);
    }

    public ColumnGroupHeaderLayer getColumnGroupHeaderLayer() {
        return this.columnGroupHeaderLayer;
    }

    public ColumnHeaderLayer getColumnHeaderLayer() {
        return this.columnHeaderLayer;
    }

    public IDataProvider getColumnHeaderDataProvider() {
        return this.columnHeaderDataProvider;
    }

    public DefaultColumnHeaderDataLayer getColumnHeaderDataLayer() {
        return this.columnHeaderDataLayer;
    }
}
