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
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;

import ca.odell.glazedlists.SortedList;

/**
 * Column header layer stack, with a {@link SortHeaderLayer}.
 * 	Utilizes {@link GlazedListsSortModel} for sorting
 */
public class GlazedListsColumnHeaderLayerStack<T> extends AbstractLayerTransform {
	private IDataProvider dataProvider;
	private DefaultColumnHeaderDataLayer dataLayer;
	private ColumnHeaderLayer columnHeaderLayer;

	public GlazedListsColumnHeaderLayerStack(String[] propertyNames, 
												Map<String, String> propertyToLabelMap, 
												SortedList<T> sortedList,
												IColumnPropertyAccessor<T> columnPropertyAccessor, 
												IConfigRegistry configRegistry,
												DefaultBodyLayerStack bodyLayerStack) {

		this(new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap),
				sortedList,
				columnPropertyAccessor, 
				configRegistry,
				bodyLayerStack);
	}
	
	public GlazedListsColumnHeaderLayerStack(IDataProvider dataProvider, 
			SortedList<T> sortedList,
			IColumnPropertyAccessor<T> columnPropertyAccessor, 
			IConfigRegistry configRegistry,
			DefaultBodyLayerStack bodyLayerStack) {
		
		this.dataProvider = dataProvider;
		dataLayer = new DefaultColumnHeaderDataLayer(dataProvider);
		columnHeaderLayer = new ColumnHeaderLayer(dataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());

		SortHeaderLayer<T> sortHeaderLayer = new SortHeaderLayer<T>(
												columnHeaderLayer, 
												new GlazedListsSortModel<T>(
														sortedList, 
														columnPropertyAccessor,
														configRegistry, 
														dataLayer), 
												false);

		setUnderlyingLayer(sortHeaderLayer);
	}
	
	@Override
	public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
		super.setClientAreaProvider(clientAreaProvider);
	}

	public DataLayer getDataLayer() {
		return dataLayer;
	}

	public IDataProvider getDataProvider() {
		return dataProvider;
	}

	public ColumnHeaderLayer getColumnHeaderLayer() {
		return columnHeaderLayer;
	}
}
