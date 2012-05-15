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
package org.eclipse.nebula.widgets.nattable.examples.fixtures;

import java.beans.PropertyChangeListener;

import org.eclipse.nebula.widgets.nattable.blink.BlinkLayer;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.config.ColumnStyleChooserConfiguration;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowLayer;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

import ca.odell.glazedlists.EventList;

public class FullFeaturedBodyLayerStack<T> extends AbstractLayerTransform {

	private ColumnReorderLayer columnReorderLayer;
	private ColumnGroupReorderLayer columnGroupReorderLayer;
	private ColumnHideShowLayer columnHideShowLayer;
	private ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer;
	private final SelectionLayer selectionLayer;
	private final ViewportLayer viewportLayer;
	private BlinkLayer<T> blinkingLayer;
	private DataLayer bodyDataLayer;
	private FreezeLayer freezeLayer;
	private CompositeFreezeLayer compositeFreezeLayer;
	private ListDataProvider<T> bodyDataProvider;
	private GlazedListsEventLayer<T> glazedListsEventLayer;

	public FullFeaturedBodyLayerStack(EventList<T> eventList,
			IRowIdAccessor<T> rowIdAccessor,
			String[] propertyNames,
			IConfigRegistry configRegistry,
			ColumnGroupModel columnGroupModel) {
		this(eventList, rowIdAccessor, propertyNames, configRegistry, columnGroupModel, true);
	}

	public FullFeaturedBodyLayerStack(EventList<T> eventList,
			IRowIdAccessor<T> rowIdAccessor,
			String[] propertyNames,
			IConfigRegistry configRegistry,
			ColumnGroupModel columnGroupModel,
			boolean useDefaultConfiguration) {

		IColumnPropertyAccessor<T> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<T>(propertyNames);
		bodyDataProvider = new GlazedListsDataProvider<T>(eventList, columnPropertyAccessor);
		bodyDataLayer = new DataLayer(bodyDataProvider);
		glazedListsEventLayer = new GlazedListsEventLayer<T>(bodyDataLayer, eventList);
		blinkingLayer = new BlinkLayer<T>(glazedListsEventLayer, bodyDataProvider, rowIdAccessor, columnPropertyAccessor, configRegistry);
		SummaryRowLayer summaryRowLayer = new SummaryRowLayer(blinkingLayer, configRegistry);

		columnReorderLayer = new ColumnReorderLayer(summaryRowLayer);
		columnGroupReorderLayer = new ColumnGroupReorderLayer(columnReorderLayer, columnGroupModel);
		columnHideShowLayer = new ColumnHideShowLayer(columnGroupReorderLayer);
		columnGroupExpandCollapseLayer = new ColumnGroupExpandCollapseLayer(columnHideShowLayer, columnGroupModel);
		selectionLayer = new SelectionLayer(columnGroupExpandCollapseLayer);
		viewportLayer = new ViewportLayer(selectionLayer);
		freezeLayer = new FreezeLayer(selectionLayer);
	    compositeFreezeLayer = new CompositeFreezeLayer(freezeLayer, viewportLayer, selectionLayer);

		setUnderlyingLayer(compositeFreezeLayer);

		if (useDefaultConfiguration) {
			addConfiguration(new ColumnStyleChooserConfiguration(this, selectionLayer));
		}
		
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

	public BlinkLayer<T> getBlinkingLayer() {
		return blinkingLayer;
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

	public PropertyChangeListener getGlazedListEventsLayer() {
		return glazedListsEventLayer;
	}
}
