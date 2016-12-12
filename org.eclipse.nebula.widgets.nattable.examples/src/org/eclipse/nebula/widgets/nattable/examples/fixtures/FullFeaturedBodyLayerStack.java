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
            IRowIdAccessor<T> rowIdAccessor, String[] propertyNames,
            IConfigRegistry configRegistry, ColumnGroupModel columnGroupModel) {
        this(eventList, rowIdAccessor, propertyNames, configRegistry,
                columnGroupModel, true);
    }

    public FullFeaturedBodyLayerStack(EventList<T> eventList,
            IRowIdAccessor<T> rowIdAccessor, String[] propertyNames,
            IConfigRegistry configRegistry, ColumnGroupModel columnGroupModel,
            boolean useDefaultConfiguration) {

        IColumnPropertyAccessor<T> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<>(
                propertyNames);
        this.bodyDataProvider = new ListDataProvider<>(eventList,
                columnPropertyAccessor);
        this.bodyDataLayer = new DataLayer(this.bodyDataProvider);
        this.glazedListsEventLayer = new GlazedListsEventLayer<>(this.bodyDataLayer,
                eventList);
        this.blinkingLayer = new BlinkLayer<>(this.glazedListsEventLayer,
                this.bodyDataProvider, rowIdAccessor, columnPropertyAccessor,
                configRegistry);
        SummaryRowLayer summaryRowLayer = new SummaryRowLayer(this.blinkingLayer,
                configRegistry);

        this.columnReorderLayer = new ColumnReorderLayer(summaryRowLayer);
        this.columnGroupReorderLayer = new ColumnGroupReorderLayer(
                this.columnReorderLayer, columnGroupModel);
        this.columnHideShowLayer = new ColumnHideShowLayer(this.columnGroupReorderLayer);
        this.columnGroupExpandCollapseLayer = new ColumnGroupExpandCollapseLayer(
                this.columnHideShowLayer, columnGroupModel);
        this.selectionLayer = new SelectionLayer(this.columnGroupExpandCollapseLayer);
        this.viewportLayer = new ViewportLayer(this.selectionLayer);
        this.freezeLayer = new FreezeLayer(this.selectionLayer);
        this.compositeFreezeLayer = new CompositeFreezeLayer(this.freezeLayer,
                this.viewportLayer, this.selectionLayer);

        setUnderlyingLayer(this.compositeFreezeLayer);

        if (useDefaultConfiguration) {
            addConfiguration(new ColumnStyleChooserConfiguration(this,
                    this.selectionLayer));
        }

    }

    @Override
    public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
        super.setClientAreaProvider(clientAreaProvider);
    }

    public ColumnReorderLayer getColumnReorderLayer() {
        return this.columnReorderLayer;
    }

    public ColumnHideShowLayer getColumnHideShowLayer() {
        return this.columnHideShowLayer;
    }

    public SelectionLayer getSelectionLayer() {
        return this.selectionLayer;
    }

    public ViewportLayer getViewportLayer() {
        return this.viewportLayer;
    }

    public BlinkLayer<T> getBlinkingLayer() {
        return this.blinkingLayer;
    }

    public DataLayer getBodyDataLayer() {
        return this.bodyDataLayer;
    }

    public ListDataProvider<T> getBodyDataProvider() {
        return this.bodyDataProvider;
    }

    public ColumnGroupExpandCollapseLayer getColumnGroupExpandCollapseLayer() {
        return this.columnGroupExpandCollapseLayer;
    }

    public PropertyChangeListener getGlazedListEventsLayer() {
        return this.glazedListsEventLayer;
    }
}
