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

public class FullFeaturedColumnHeaderLayerStack<T> extends AbstractLayerTransform {

	private final ColumnHeaderLayer columnHeaderLayer;
	private final ColumnGroupHeaderLayer columnGroupHeaderLayer;
	private final SortHeaderLayer<T> sortableColumnHeaderLayer;
	private final IDataProvider columnHeaderDataProvider;
	private final DefaultColumnHeaderDataLayer columnHeaderDataLayer;

	public FullFeaturedColumnHeaderLayerStack(SortedList<T> sortedList,
												FilterList<T> filterList,
												String[] propertyNames,
												Map<String, String> propertyToLabelMap,
												ILayer bodyLayer,
												SelectionLayer selectionLayer,
												ColumnGroupModel columnGroupModel,
												IConfigRegistry configRegistry) {

		columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);

		columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);

		columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayer, selectionLayer);
		
		final ReflectiveColumnPropertyAccessor<T> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<T>(propertyNames);
		sortableColumnHeaderLayer = new SortHeaderLayer<T>(
				columnHeaderLayer,
				new GlazedListsSortModel<T>(
						sortedList,
						columnPropertyAccessor,
						configRegistry,
						columnHeaderDataLayer
						)
				);

		columnGroupHeaderLayer = new ColumnGroupHeaderLayer(sortableColumnHeaderLayer, selectionLayer, columnGroupModel);

		FilterRowHeaderComposite<T> composite =
			new FilterRowHeaderComposite<T>(
					new DefaultGlazedListsFilterStrategy<T>(
							filterList,
							columnPropertyAccessor,
							configRegistry
					),
					columnGroupHeaderLayer, columnHeaderDataProvider, configRegistry);

		setUnderlyingLayer(composite);
	}

	@Override
	public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
		super.setClientAreaProvider(clientAreaProvider);
	}

	public ColumnGroupHeaderLayer getColumnGroupHeaderLayer() {
		return columnGroupHeaderLayer;
	}

	public ColumnHeaderLayer getColumnHeaderLayer() {
		return columnHeaderLayer;
	}

	public IDataProvider getColumnHeaderDataProvider() {
		return columnHeaderDataProvider;
	}

	public DefaultColumnHeaderDataLayer getColumnHeaderDataLayer() {
		return columnHeaderDataLayer;
	}
}
