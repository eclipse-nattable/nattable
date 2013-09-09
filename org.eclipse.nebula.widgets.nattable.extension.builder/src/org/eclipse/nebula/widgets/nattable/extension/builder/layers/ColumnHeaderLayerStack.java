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
package org.eclipse.nebula.widgets.nattable.extension.builder.layers;

import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.builder.configuration.ColumnGroupConfiguration;
import org.eclipse.nebula.widgets.nattable.extension.builder.configuration.ColumnHeaderConfiguration;
import org.eclipse.nebula.widgets.nattable.extension.builder.configuration.SortConfiguration;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableModel;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableRow;
import org.eclipse.nebula.widgets.nattable.extension.builder.util.TableColumnUtils;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.DefaultGlazedListsFilterStrategy;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowHeaderComposite;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregrateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;

public class ColumnHeaderLayerStack<T extends TableRow> extends AbstractLayerTransform {

	private final ColumnHeaderLayer columnHeaderLayer;
	private ColumnGroupHeaderLayer columnGroupHeaderLayer;
	private SortHeaderLayer<T> sortableColumnHeaderLayer;
	private final IDataProvider columnHeaderDataProvider;
	private final DataLayer columnHeaderDataLayer;
	private AggregrateConfigLabelAccumulator aggregateLabelAccumulator;
	private FilterRowHeaderComposite<T> filterRowHeaderLayer;

	public ColumnHeaderLayerStack(SortedList<T> sortedList,
									FilterList<T> filterList,
									TableModel tableModel,
									BodyLayerStack<T> bodyLayer,
									IColumnPropertyAccessor<T> columnAccessor,
									IConfigRegistry configRegistry) {

		String[] propertyNames = TableColumnUtils.getPropertyNames(tableModel.columnProperties);
		Map<String, String> propertyToLabelMap = TableColumnUtils.getPropertyToLabelMap(tableModel.columnProperties);
		SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();

		columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);

		columnHeaderDataLayer = new DataLayer(columnHeaderDataProvider, tableModel.tableStyle.defaultColumnWidth, tableModel.tableStyle.columnHeaderHeight);

		columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayer, selectionLayer, false);

		GlazedListsSortModel<T> sortModel = new GlazedListsSortModel<T>(sortedList, columnAccessor, configRegistry, columnHeaderDataLayer);

		if(tableModel.enableColumnGroups) {
			columnGroupHeaderLayer = new ColumnGroupHeaderLayer(columnHeaderLayer, selectionLayer, tableModel.columnGroupModel, false);
			columnGroupHeaderLayer.setRowHeight(tableModel.tableStyle.columnGroupHeaderHeight);
			sortableColumnHeaderLayer = new SortHeaderLayer<T>(columnGroupHeaderLayer, sortModel, false);
		} else {
			sortableColumnHeaderLayer = new SortHeaderLayer<T>(columnHeaderLayer, sortModel, false);
		}

		if(tableModel.enableFilterRow){
			filterRowHeaderLayer =
				new FilterRowHeaderComposite<T>(
						new DefaultGlazedListsFilterStrategy<T>(
								filterList,
								columnAccessor,
								configRegistry
						),
						sortableColumnHeaderLayer, columnHeaderDataProvider, configRegistry
				);
			setUnderlyingLayer(filterRowHeaderLayer);
		} else {
			setUnderlyingLayer(sortableColumnHeaderLayer);
		}

		setupAggregateLabelAccumulator();

		// ** Configure **
		//	Sorting
		sortableColumnHeaderLayer.addConfiguration(new SortConfiguration());

		//	Column groups
		if(tableModel.enableColumnGroups){
			columnGroupHeaderLayer.addConfiguration(new ColumnGroupConfiguration(tableModel.columnGroupModel, tableModel));
		}

		columnHeaderLayer.addConfiguration(new ColumnHeaderConfiguration(tableModel.tableStyle));
	}

	private void setupAggregateLabelAccumulator() {
		aggregateLabelAccumulator = new AggregrateConfigLabelAccumulator();
		getDataLayer().setConfigLabelAccumulator(aggregateLabelAccumulator);
	}

	public void addLabelAccumulator(IConfigLabelAccumulator accumulator){
		aggregateLabelAccumulator.add(accumulator);
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

	public IDataProvider getDataProvider() {
		return columnHeaderDataProvider;
	}

	public DataLayer getDataLayer() {
		return columnHeaderDataLayer;
	}

	public SortHeaderLayer<T> getSortHeaderLayer() {
		return sortableColumnHeaderLayer;
	}

	public FilterRowHeaderComposite<T> getFilterRowHeaderLayer() {
		return filterRowHeaderLayer;
	}
}
