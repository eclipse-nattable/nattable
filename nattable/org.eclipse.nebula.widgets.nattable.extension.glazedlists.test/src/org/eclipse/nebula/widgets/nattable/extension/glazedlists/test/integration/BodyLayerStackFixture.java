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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.test.integration;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

import ca.odell.glazedlists.EventList;

public class BodyLayerStackFixture<T> extends AbstractLayerTransform {

	private final ColumnReorderLayer columnReorderLayer;
	private final ColumnGroupReorderLayer columnGroupReorderLayer;
	private final ColumnHideShowLayer columnHideShowLayer;
	private final ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer;
	private final SelectionLayer selectionLayer;
	private final ViewportLayer viewportLayer;
	private final DataLayer bodyDataLayer;
	private final ListDataProvider<T> bodyDataProvider;
	private final GlazedListsEventLayer<T> glazedListsEventLayer;

	public BodyLayerStackFixture(EventList<T> eventList,
							IColumnPropertyAccessor<T> columnPropertyAccessor,
							IConfigRegistry configRegistry) {

		bodyDataProvider = new ListDataProvider<T>(eventList, columnPropertyAccessor);

		bodyDataLayer = new DataLayer(bodyDataProvider);

		glazedListsEventLayer = new GlazedListsEventLayer<T>(bodyDataLayer, eventList);
		glazedListsEventLayer.setTestMode(true);

		ColumnGroupModel columnGroupModel = new ColumnGroupModel();
		columnReorderLayer = new ColumnReorderLayer(glazedListsEventLayer);
		columnGroupReorderLayer = new ColumnGroupReorderLayer(columnReorderLayer, columnGroupModel);
		columnHideShowLayer = new ColumnHideShowLayer(columnGroupReorderLayer);
		columnGroupExpandCollapseLayer = new ColumnGroupExpandCollapseLayer(columnHideShowLayer, columnGroupModel);
		selectionLayer = new SelectionLayer(columnGroupExpandCollapseLayer);
		viewportLayer = new ViewportLayer(selectionLayer);

		setUnderlyingLayer(viewportLayer);
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

	public DataLayer getBodyDataLayer() {
		return bodyDataLayer;
	}

	public ListDataProvider<T> getBodyDataProvider() {
		return bodyDataProvider;
	}

	public ColumnGroupExpandCollapseLayer getColumnGroupExpandCollapseLayer() {
		return columnGroupExpandCollapseLayer;
	}

	public GlazedListsEventLayer<T> getGlazedListEventsLayer() {
		return glazedListsEventLayer;
	}
}
