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

import java.beans.PropertyChangeListener;

import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableModel;
import org.eclipse.nebula.widgets.nattable.extension.builder.model.TableRow;
import org.eclipse.nebula.widgets.nattable.extension.builder.util.ColumnAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregrateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

import ca.odell.glazedlists.EventList;

public class BodyLayerStack<T extends TableRow> extends AbstractLayerTransform {

	private final ColumnReorderLayer columnReorderLayer;
	private final ColumnHideShowLayer columnHideShowLayer;
	private final SelectionLayer selectionLayer;
	private final ViewportLayer viewportLayer;
	private final DataLayer bodyDataLayer;
	private final ListDataProvider<T> bodyDataProvider;
	private final GlazedListsEventLayer<T> glazedListsEventLayer;
	private AggregrateConfigLabelAccumulator aggregateLabelAccumulator;
	private ColumnGroupReorderLayer columnGroupReorderLayer;
	private ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer;
	private final FreezeLayer freezeLayer;

	public BodyLayerStack(TableModel tableModel, EventList<T> eventList) {

		IColumnAccessor<T> columnAccessor = new ColumnAccessor<T>(tableModel.columnProperties);
		bodyDataProvider = new ListDataProvider<T>(eventList, columnAccessor);

		bodyDataLayer = new DataLayer(bodyDataProvider, tableModel.tableStyle.defaultColumnWidth, tableModel.tableStyle.defaultRowHeight);

		// IUniqueIndexLayer summaryRowLayer = new SummaryRowLayer(bodyDataLayer, configRegistry);

		glazedListsEventLayer = new GlazedListsEventLayer<T>(bodyDataLayer,	eventList);

		if(tableModel.enableColumnGroups){
			columnReorderLayer = new ColumnReorderLayer(glazedListsEventLayer);
			columnGroupReorderLayer = new ColumnGroupReorderLayer(columnReorderLayer, tableModel.columnGroupModel);
			columnHideShowLayer = new ColumnHideShowLayer(columnGroupReorderLayer);
			columnGroupExpandCollapseLayer = new ColumnGroupExpandCollapseLayer(columnHideShowLayer, tableModel.columnGroupModel);
			selectionLayer = new SelectionLayer(columnGroupExpandCollapseLayer);
			viewportLayer = new ViewportLayer(selectionLayer);
		}else{
			columnReorderLayer = new ColumnReorderLayer(glazedListsEventLayer);
			columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
			selectionLayer = new SelectionLayer(columnHideShowLayer);
			viewportLayer = new ViewportLayer(selectionLayer);
		}

		freezeLayer = new FreezeLayer(selectionLayer);
	    final CompositeFreezeLayer compositeFreezeLayer = new CompositeFreezeLayer(freezeLayer, viewportLayer, selectionLayer);

		setUnderlyingLayer(compositeFreezeLayer);
		setupAggregateLabelAccumulator();
	}

	private void setupAggregateLabelAccumulator() {
		aggregateLabelAccumulator = new AggregrateConfigLabelAccumulator();
		getDataLayer().setConfigLabelAccumulator(aggregateLabelAccumulator);
	}

	public void addLabelAccumulator(IConfigLabelAccumulator accumulator) {
		aggregateLabelAccumulator.add(accumulator);
	}

	@Override
	public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
		super.setClientAreaProvider(clientAreaProvider);
	}

	public ColumnReorderLayer getColumnReorderLayer() {
		return columnReorderLayer;
	}

	public ColumnHideShowLayer getColumnHideShowLayer() {
		return columnHideShowLayer;
	}

	public SelectionLayer getSelectionLayer() {
		return selectionLayer;
	}

	public ViewportLayer getViewportLayer() {
		return viewportLayer;
	}

	public DataLayer getDataLayer() {
		return bodyDataLayer;
	}

	public ListDataProvider<T> getDataProvider() {
		return bodyDataProvider;
	}

	public PropertyChangeListener getGlazedListEventsLayer() {
		return glazedListsEventLayer;
	}

	public ColumnGroupReorderLayer getColumnGroupReorderLayer() {
		return columnGroupReorderLayer;
	}

	public ColumnGroupExpandCollapseLayer getColumnGroupExpandCollapseLayer() {
		return columnGroupExpandCollapseLayer;
	}

	public FreezeLayer getFreezeLayer() {
		return freezeLayer;
	}
}
