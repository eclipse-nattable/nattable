/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.action.ColumnGroupExpandCollapseAction;
import org.eclipse.nebula.widgets.nattable.group.action.ColumnGroupHeaderReorderDragMode;
import org.eclipse.nebula.widgets.nattable.group.action.ColumnHeaderReorderDragMode;
import org.eclipse.nebula.widgets.nattable.group.action.CreateColumnGroupAction;
import org.eclipse.nebula.widgets.nattable.group.action.UngroupColumnsAction;
import org.eclipse.nebula.widgets.nattable.group.action.ViewportSelectColumnGroupAction;
import org.eclipse.nebula.widgets.nattable.group.painter.ColumnGroupHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.action.AggregateDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.CellDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.NoOpMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

public class DefaultColumnGroupHeaderLayerConfiguration implements IConfiguration {

    public static final String GROUP_COLLAPSED_CONFIG_TYPE = "GROUP_COLLAPSED"; //$NON-NLS-1$
    public static final String GROUP_EXPANDED_CONFIG_TYPE = "GROUP_EXPANDED"; //$NON-NLS-1$

    private final ColumnGroupModel columnGroupModel;

    private final boolean enableColumnGroupSelectionHandling;

    /**
     * Creates a configuration without selection on single click bindings.
     *
     * @param columnGroupModel
     *            The {@link ColumnGroupModel} used to define the column groups.
     */
    public DefaultColumnGroupHeaderLayerConfiguration(ColumnGroupModel columnGroupModel) {
        this(columnGroupModel, false);
    }

    /**
     *
     * @param columnGroupModel
     *            The {@link ColumnGroupModel} used to define the column groups.
     * @param enableGroupSelection
     *            <code>true</code> if single click selection bindings on the
     *            column group header should be enabled, <code>false</code> if
     *            no operations should be triggered on single click.
     *
     * @since 1.6
     */
    public DefaultColumnGroupHeaderLayerConfiguration(ColumnGroupModel columnGroupModel, boolean enableGroupSelection) {
        this.columnGroupModel = columnGroupModel;
        this.enableColumnGroupSelectionHandling = enableGroupSelection;
    }

    @Override
    public void configureLayer(ILayer layer) {
        // No op
    }

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER,
                new BeveledBorderDecorator(new ColumnGroupHeaderTextPainter()),
                DisplayMode.NORMAL,
                GridRegion.COLUMN_GROUP_HEADER);
        // We are not setting a special configuration for rendering grid lines,
        // as this would override the column header configuration. This is
        // because the the column group header is part of the column header
        // region.
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        // Column Group Header is a part of the Group Header.
        // Register the 'column group header matcher' first so that it gets
        // picked up before the more general 'column header matcher'.
        uiBindingRegistry.registerMouseDragMode(
                MouseEventMatcher.columnGroupHeaderLeftClick(SWT.NONE),
                new AggregateDragMode(new CellDragMode(), new ColumnGroupHeaderReorderDragMode(this.columnGroupModel)));

        uiBindingRegistry.registerMouseDragMode(
                MouseEventMatcher.columnHeaderLeftClick(SWT.NONE),
                new AggregateDragMode(new CellDragMode(), new ColumnHeaderReorderDragMode(this.columnGroupModel)));

        // added NoOpMouseAction on single click because of Bug 428901
        if (!this.enableColumnGroupSelectionHandling) {
            uiBindingRegistry.registerFirstSingleClickBinding(
                    MouseEventMatcher.columnGroupHeaderLeftClick(SWT.NONE),
                    new NoOpMouseAction());
            uiBindingRegistry.registerFirstSingleClickBinding(
                    MouseEventMatcher.columnGroupHeaderLeftClick(SWT.MOD1),
                    new NoOpMouseAction());
            uiBindingRegistry.registerFirstSingleClickBinding(
                    MouseEventMatcher.columnGroupHeaderLeftClick(SWT.MOD2),
                    new NoOpMouseAction());
            uiBindingRegistry.registerFirstSingleClickBinding(
                    MouseEventMatcher.columnGroupHeaderLeftClick(SWT.MOD1 | SWT.MOD2),
                    new NoOpMouseAction());
        } else {
            uiBindingRegistry.registerFirstSingleClickBinding(
                    MouseEventMatcher.columnGroupHeaderLeftClick(SWT.NONE),
                    new ViewportSelectColumnGroupAction(false, false));
            uiBindingRegistry.registerFirstSingleClickBinding(
                    MouseEventMatcher.columnGroupHeaderLeftClick(SWT.MOD1),
                    new ViewportSelectColumnGroupAction(false, true));
            uiBindingRegistry.registerFirstSingleClickBinding(
                    MouseEventMatcher.columnGroupHeaderLeftClick(SWT.MOD2),
                    new ViewportSelectColumnGroupAction(true, false));
            uiBindingRegistry.registerFirstSingleClickBinding(
                    MouseEventMatcher.columnGroupHeaderLeftClick(SWT.MOD1 | SWT.MOD2),
                    new ViewportSelectColumnGroupAction(true, true));
        }

        uiBindingRegistry.registerDoubleClickBinding(
                MouseEventMatcher.columnGroupHeaderLeftClick(SWT.NONE),
                new ColumnGroupExpandCollapseAction());

        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(SWT.MOD1, 'g'),
                new CreateColumnGroupAction());
        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(SWT.MOD1, 'u'),
                new UngroupColumnsAction());
    }

}
