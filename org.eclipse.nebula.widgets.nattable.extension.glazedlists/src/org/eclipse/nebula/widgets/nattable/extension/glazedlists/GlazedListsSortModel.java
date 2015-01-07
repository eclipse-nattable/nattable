/*******************************************************************************
 * Copyright (c) 2012, 2013, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 444839
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists;

import java.util.Comparator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyResolver;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;

import ca.odell.glazedlists.SortedList;

public class GlazedListsSortModel<T> implements ISortModel, ILayerListener {

    private NatTableComparatorChooser<T> comparatorChooser;
    private NatColumnTableFormat<T> tableFormat;
    protected final SortedList<T> sortedList;
    protected final IColumnAccessor<T> columnAccessor;
    protected final IColumnPropertyResolver columnPropertyResolver;
    protected final IConfigRegistry configRegistry;
    protected final ILayer columnHeaderDataLayer;

    public GlazedListsSortModel(SortedList<T> sortedList,
            IColumnPropertyAccessor<T> columnPropertyAccessor,
            IConfigRegistry configRegistry,
            ILayer dataLayer) {

        this(sortedList,
                columnPropertyAccessor,
                columnPropertyAccessor,
                configRegistry,
                dataLayer);
    }

    public GlazedListsSortModel(SortedList<T> sortedList,
            IColumnAccessor<T> columnAccessor,
            IColumnPropertyResolver columnPropertyResolver,
            IConfigRegistry configRegistry,
            ILayer dataLayer) {
        this.sortedList = sortedList;
        this.columnAccessor = columnAccessor;
        this.columnPropertyResolver = columnPropertyResolver;
        this.configRegistry = configRegistry;
        this.columnHeaderDataLayer = dataLayer;

        this.columnHeaderDataLayer.addLayerListener(this);
    }

    protected NatTableComparatorChooser<T> getComparatorChooser() {
        if (this.comparatorChooser == null) {
            this.tableFormat = new NatColumnTableFormat<T>(
                    this.columnAccessor,
                    this.columnPropertyResolver,
                    this.configRegistry,
                    this.columnHeaderDataLayer);
            this.comparatorChooser = new NatTableComparatorChooser<T>(
                    this.sortedList,
                    this.tableFormat);
        }

        return this.comparatorChooser;
    }

    @Override
    public List<Integer> getSortedColumnIndexes() {
        return getComparatorChooser().getSortingColumns();
    }

    @Override
    public int getSortOrder(int columnIndex) {
        return getComparatorChooser().getClickSequence(columnIndex);
    }

    @Override
    public SortDirectionEnum getSortDirection(int columnIndex) {
        return getComparatorChooser().getSortDirectionForColumnIndex(columnIndex);
    }

    @Override
    public boolean isColumnIndexSorted(int columnIndex) {
        return getComparatorChooser().isColumnIndexSorted(columnIndex);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<Comparator> getComparatorsForColumnIndex(int columnIndex) {
        return getComparatorChooser().getComparatorsForColumn(columnIndex);
    }

    @Override
    public Comparator<?> getColumnComparator(int columnIndex) {
        return this.tableFormat.getColumnComparator(columnIndex);
    }

    @Override
    public void sort(int columnIndex, SortDirectionEnum sortDirection, boolean accumulate) {
        getComparatorChooser().sort(columnIndex, sortDirection, accumulate);
    }

    @Override
    public void clear() {
        getComparatorChooser().clearComparator();
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        if (event instanceof StructuralRefreshEvent
                && ((StructuralRefreshEvent) event).isHorizontalStructureChanged()) {
            String test = getComparatorChooser().toString();
            this.comparatorChooser = null;
            getComparatorChooser().fromString(test);
        }
    }
}
