/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.sort.config;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.sort.action.SortColumnAction;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortableHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

public class DefaultSortConfiguration implements IConfiguration {

    /**
     * Config label that is added if a column is sorted.
     *
     * @since 1.4
     */
    public static final String SORT_CONFIG_TYPE = "SORT"; //$NON-NLS-1$
    /**
     * Config label that is added if a column is sorted descending.
     */
    public static final String SORT_DOWN_CONFIG_TYPE = "SORT_DOWN"; //$NON-NLS-1$
    /**
     * Config label that is added if a column is sorted ascending.
     */
    public static final String SORT_UP_CONFIG_TYPE = "SORT_UP"; //$NON-NLS-1$
    /**
     * The sort sequence can be appended to this base
     */
    public static final String SORT_SEQ_CONFIG_TYPE = "SORT_SEQ_"; //$NON-NLS-1$

    private ICellPainter cellPainter;

    public DefaultSortConfiguration() {
        this(new BeveledBorderDecorator(new SortableHeaderTextPainter()));
    }

    public DefaultSortConfiguration(ICellPainter cellPainter) {
        this.cellPainter = cellPainter;
    }

    @Override
    public void configureLayer(ILayer layer) {}

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(
                SortConfigAttributes.SORT_COMPARATOR,
                DefaultComparator.getInstance());

        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER,
                this.cellPainter,
                DisplayMode.NORMAL,
                SORT_DOWN_CONFIG_TYPE);
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER,
                this.cellPainter,
                DisplayMode.NORMAL,
                SORT_UP_CONFIG_TYPE);
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        uiBindingRegistry.registerSingleClickBinding(
                new MouseEventMatcher(SWT.MOD3, GridRegion.COLUMN_HEADER.toString(), 1),
                new SortColumnAction(false));

        uiBindingRegistry.registerSingleClickBinding(
                new MouseEventMatcher(SWT.MOD3 | SWT.MOD2, GridRegion.COLUMN_HEADER.toString(), 1),
                new SortColumnAction(true));
    }

}
