/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 459029
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.config;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.group.action.RowGroupExpandCollapseAction;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroupModel;
import org.eclipse.nebula.widgets.nattable.group.painter.RowGroupHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.selection.action.SelectRowGroupsAction;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

public class DefaultRowGroupHeaderLayerConfiguration<T> implements IConfiguration {

    public static final String GROUP_COLLAPSED_CONFIG_TYPE = "GROUP_COLLAPSED"; //$NON-NLS-1$
    public static final String GROUP_EXPANDED_CONFIG_TYPE = "GROUP_EXPANDED"; //$NON-NLS-1$

    @SuppressWarnings("unused")
    private final IRowGroupModel<T> rowGroupModel;

    public DefaultRowGroupHeaderLayerConfiguration() {
        this.rowGroupModel = null;
    }

    /**
     *
     * @param rowGroupModel
     *            The IRowGroupModel that is used by the RowGroupHeaderLayer.
     *
     * @deprecated use constructor without IRowGroupModel as it is not needed
     *             anymore
     */
    @Deprecated
    public DefaultRowGroupHeaderLayerConfiguration(final IRowGroupModel<T> rowGroupModel) {
        this.rowGroupModel = rowGroupModel;
    }

    @Override
    public void configureLayer(ILayer layer) {
        // No op
    }

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER,
                new BeveledBorderDecorator(new RowGroupHeaderTextPainter()),
                DisplayMode.NORMAL,
                GridRegion.ROW_GROUP_HEADER);
        // We are not setting a special configuration for rendering grid lines,
        // as this would override the column header configuration. This is
        // because the the column group header is part of the column header
        // region.
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {

        final IMouseAction action = new SelectRowGroupsAction();
        uiBindingRegistry.registerSingleClickBinding(
                MouseEventMatcher.rowGroupHeaderLeftClick(SWT.NONE), action);
        uiBindingRegistry.registerSingleClickBinding(
                MouseEventMatcher.rowGroupHeaderLeftClick(SWT.MOD1), action);
        uiBindingRegistry.registerSingleClickBinding(
                MouseEventMatcher.rowGroupHeaderLeftClick(SWT.MOD2), action);

        uiBindingRegistry.registerDoubleClickBinding(
                MouseEventMatcher.rowGroupHeaderLeftClick(SWT.NONE),
                new RowGroupExpandCollapseAction());
    }

}
