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
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.DefaultGlazedListsStaticFilterStrategy;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowHeaderComposite;
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
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.matchers.Matcher;

public class StaticFilterExampleGridLayer extends GridLayer {

	private final ListDataProvider<RowDataFixture> bodyDataProvider;
	private final DataLayer bodyDataLayer;

	public StaticFilterExampleGridLayer(IConfigRegistry configRegistry) {
		super(true);

		// Underlying data source
		EventList<RowDataFixture> eventList = GlazedLists.eventList(RowDataListFixture.getList());
		TransformedList<RowDataFixture, RowDataFixture> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);
		SortedList<RowDataFixture> sortedList = new SortedList<RowDataFixture>(rowObjectsGlazedList, null);
		FilterList<RowDataFixture> filterList = new FilterList<RowDataFixture>(sortedList);
		String[] propertyNames = RowDataListFixture.getPropertyNames();
		Map<String, String> propertyToLabelMap = RowDataListFixture.getPropertyToLabelMap();

		// Body layer
		IColumnPropertyAccessor<RowDataFixture> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<RowDataFixture>(propertyNames);
		
		bodyDataProvider = new ListDataProvider<RowDataFixture>(filterList, columnPropertyAccessor);
		//add a static filter that only shows RowDataFixtures with a rating other than "AAA"
//		bodyDataProvider = new AbstractFilterListDataProvider<RowDataFixture>(filterList, columnPropertyAccessor) {
//			@Override
//			protected boolean show(RowDataFixture object) {
//				return !(object.rating.equals("AAA"));
//			}
//		};
		
		bodyDataLayer = new DataLayer(bodyDataProvider);
		GlazedListsEventLayer<RowDataFixture> glazedListsEventLayer = new GlazedListsEventLayer<RowDataFixture>(bodyDataLayer, eventList);
		DefaultBodyLayerStack bodyLayer = new DefaultBodyLayerStack(glazedListsEventLayer);
		ColumnOverrideLabelAccumulator bodyLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		bodyDataLayer.setConfigLabelAccumulator(bodyLabelAccumulator);
		
		bodyLabelAccumulator.registerColumnOverrides(
		           RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.PRICING_TYPE_PROP_NAME),
		           "PRICING_TYPE_PROP_NAME");
		
		// Column header layer
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
		DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
		ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayer, bodyLayer.getSelectionLayer());
		
		SortHeaderLayer<RowDataFixture> sortHeaderLayer = new SortHeaderLayer<RowDataFixture>(
				columnHeaderLayer, 
				new GlazedListsSortModel<RowDataFixture>(
						sortedList, 
						columnPropertyAccessor,
						configRegistry, 
						columnHeaderDataLayer), false);
		sortHeaderLayer.addConfiguration(new SingleClickSortConfiguration());

		//	Note: The column header layer is wrapped in a filter row composite.
		//	This plugs in the filter row functionality
		
//		DefaultGlazedListsFilterStrategy<RowDataFixture> filterStrategy = 
//				new DefaultGlazedListsFilterStrategy<RowDataFixture>(autoFilterMatcherEditor, columnPropertyAccessor, configRegistry);
		DefaultGlazedListsStaticFilterStrategy<RowDataFixture> filterStrategy = 
				new DefaultGlazedListsStaticFilterStrategy<RowDataFixture>(filterList, columnPropertyAccessor, configRegistry);
		filterStrategy.addStaticFilter(new Matcher<RowDataFixture>() {
			
			@Override
			public boolean matches(RowDataFixture item) {
				return !(item.rating.equals("AAA"));
			}
		});
		
		FilterRowHeaderComposite<RowDataFixture> filterRowHeaderLayer =
			new FilterRowHeaderComposite<RowDataFixture>(filterStrategy, sortHeaderLayer, columnHeaderDataProvider, configRegistry
			);

		ColumnOverrideLabelAccumulator labelAccumulator = new ColumnOverrideLabelAccumulator(columnHeaderDataLayer);
		columnHeaderDataLayer.setConfigLabelAccumulator(labelAccumulator);

		// Register labels
		labelAccumulator.registerColumnOverrides(
           RowDataListFixture.getColumnIndexOfProperty(RowDataListFixture.RATING_PROP_NAME),
           "CUSTOM_COMPARATOR_LABEL");
		



		// Row header layer
		DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
		DefaultRowHeaderDataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayer, bodyLayer.getSelectionLayer());

		// Corner layer
		DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		CornerLayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, filterRowHeaderLayer);

		// Grid
		setBodyLayer(bodyLayer);
		// 	Note: Set the filter row as the column header
		setColumnHeaderLayer(filterRowHeaderLayer);
		setRowHeaderLayer(rowHeaderLayer);
		setCornerLayer(cornerLayer);
	}

	@Override
	public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
		super.setClientAreaProvider(clientAreaProvider);
	}
	
	public ListDataProvider<RowDataFixture> getBodyDataProvider() {
		return bodyDataProvider;
	}

	public DataLayer getBodyDataLayer() {
		return bodyDataLayer;
	}


}
