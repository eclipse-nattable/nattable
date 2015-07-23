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
package org.eclipse.nebula.widgets.nattable.sort.config;

import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.sort.action.SortColumnAction;
import org.eclipse.nebula.widgets.nattable.sort.event.ColumnHeaderClickEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

/**
 * Modifies the default sort configuration to sort on a <i>single left</i> click
 * on the column header.
 */
public class SingleClickSortConfiguration extends DefaultSortConfiguration {

    public SingleClickSortConfiguration() {
        super();
    }

    public SingleClickSortConfiguration(ICellPainter cellPainter) {
        super(cellPainter);
    }

    /**
     * Remove the original key bindings and implement new ones.
     */
    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        // Register new bindings
        uiBindingRegistry.registerFirstSingleClickBinding(
                new ColumnHeaderClickEventMatcher(SWT.NONE, 1),
                new SortColumnAction(false));

        uiBindingRegistry.registerSingleClickBinding(
                MouseEventMatcher.columnHeaderLeftClick(SWT.MOD3),
                new SortColumnAction(true));
    }

}
