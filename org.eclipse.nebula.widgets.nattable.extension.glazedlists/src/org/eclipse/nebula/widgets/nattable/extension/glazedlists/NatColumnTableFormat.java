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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists;

import java.util.Comparator;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.NullComparator;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyResolver;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;

import ca.odell.glazedlists.gui.AdvancedTableFormat;

public class NatColumnTableFormat<R> implements AdvancedTableFormat<R> {

    private final IColumnPropertyResolver columnPropertyResolver;
    private IColumnAccessor<R> columnAccessor;
    private IConfigRegistry configRegistry;
    private final ILayer columnHeaderDataLayer;

    public NatColumnTableFormat(
            IColumnPropertyAccessor<R> columnPropertyAccessor,
            IConfigRegistry configRegistry, ILayer dataLayer) {
        this(columnPropertyAccessor, columnPropertyAccessor, configRegistry,
                dataLayer);
    }

    public NatColumnTableFormat(IColumnAccessor<R> columnAccessor,
            IColumnPropertyResolver columnPropertyResolver,
            IConfigRegistry configRegistry, ILayer columnHeaderDataLayer) {
        this.columnPropertyResolver = columnPropertyResolver;
        this.columnAccessor = columnAccessor;
        this.configRegistry = configRegistry;
        this.columnHeaderDataLayer = columnHeaderDataLayer;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return null;
    }

    @Override
    public Comparator<?> getColumnComparator(final int col) {
        ILayerCell cell = this.columnHeaderDataLayer.getCellByPosition(col, 0);
        if (cell == null) {
            return null;
        }
        Comparator<?> comparator = this.configRegistry.getConfigAttribute(
                SortConfigAttributes.SORT_COMPARATOR, cell.getDisplayMode(),
                cell.getConfigLabels().getLabels());

        return (comparator instanceof NullComparator) ? null : comparator;
    }

    @Override
    public String getColumnName(int col) {
        return this.columnPropertyResolver.getColumnProperty(col);
    }

    @Override
    public int getColumnCount() {
        return this.columnAccessor.getColumnCount();
    }

    @Override
    public Object getColumnValue(R rowObj, int col) {
        return this.columnAccessor.getDataValue(rowObj, col);
    }

}
