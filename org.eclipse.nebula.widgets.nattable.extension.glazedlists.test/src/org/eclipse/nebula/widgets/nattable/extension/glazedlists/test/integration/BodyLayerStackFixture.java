/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
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

        this.bodyDataProvider = new ListDataProvider<>(eventList, columnPropertyAccessor);

        this.bodyDataLayer = new DataLayer(this.bodyDataProvider);

        this.glazedListsEventLayer = new GlazedListsEventLayer<>(this.bodyDataLayer, eventList);
        this.glazedListsEventLayer.setTestMode(true);

        ColumnGroupModel columnGroupModel = new ColumnGroupModel();
        this.columnReorderLayer = new ColumnReorderLayer(this.glazedListsEventLayer);
        this.columnGroupReorderLayer = new ColumnGroupReorderLayer(
                this.columnReorderLayer, columnGroupModel);
        this.columnHideShowLayer = new ColumnHideShowLayer(this.columnGroupReorderLayer);
        this.columnGroupExpandCollapseLayer = new ColumnGroupExpandCollapseLayer(
                this.columnHideShowLayer, columnGroupModel);
        this.selectionLayer = new SelectionLayer(this.columnGroupExpandCollapseLayer);
        this.viewportLayer = new ViewportLayer(this.selectionLayer);

        setUnderlyingLayer(this.viewportLayer);
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

    public DataLayer getBodyDataLayer() {
        return this.bodyDataLayer;
    }

    public ListDataProvider<T> getBodyDataProvider() {
        return this.bodyDataProvider;
    }

    public ColumnGroupExpandCollapseLayer getColumnGroupExpandCollapseLayer() {
        return this.columnGroupExpandCollapseLayer;
    }

    public GlazedListsEventLayer<T> getGlazedListEventsLayer() {
        return this.glazedListsEventLayer;
    }
}
