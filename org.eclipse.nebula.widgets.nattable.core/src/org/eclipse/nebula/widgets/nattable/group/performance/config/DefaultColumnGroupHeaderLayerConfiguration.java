/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance.config;

import org.eclipse.nebula.widgets.nattable.config.AbstractLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.group.action.ColumnGroupExpandCollapseAction;
import org.eclipse.nebula.widgets.nattable.group.action.UngroupColumnsAction;
import org.eclipse.nebula.widgets.nattable.group.action.ViewportSelectColumnGroupAction;
import org.eclipse.nebula.widgets.nattable.group.painter.ColumnGroupHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.action.ColumnGroupHeaderReorderDragMode;
import org.eclipse.nebula.widgets.nattable.group.performance.action.ColumnHeaderReorderDragMode;
import org.eclipse.nebula.widgets.nattable.group.performance.action.CreateColumnGroupAction;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.action.AggregateDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.CellDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.NoOpMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

/**
 * The default configuration for the {@link ColumnGroupHeaderLayer}.
 *
 * @since 1.6
 */
public class DefaultColumnGroupHeaderLayerConfiguration extends AbstractLayerConfiguration<ColumnGroupHeaderLayer> {

    private final boolean enableColumnGroupSelectionBinding;
    private final boolean enableExpandCollapseBinding;

    private ColumnGroupHeaderLayer columnGroupHeaderLayer;

    /**
     * Creates the DefaultColumnGroupHeaderLayerConfiguration with enabled
     * expand/collapse binding.
     *
     * @param enableGroupSelection
     *            <code>true</code> if single click selection bindings on the
     *            column group header should be enabled, <code>false</code> if
     *            no operations should be triggered on single click.
     */
    public DefaultColumnGroupHeaderLayerConfiguration(boolean enableGroupSelection) {
        this(enableGroupSelection, true);
    }

    /**
     *
     * @param enableGroupSelection
     *            <code>true</code> if single click selection bindings on the
     *            column group header should be enabled, <code>false</code> if
     *            no operations should be triggered on single click.
     * @param enableExpandCollapse
     *            <code>true</code> if the binding should be registered to
     *            expand/collapse a group on double click, <code>false</code> if
     *            the binding should not be registered.
     */
    public DefaultColumnGroupHeaderLayerConfiguration(boolean enableGroupSelection, boolean enableExpandCollapse) {
        this.enableColumnGroupSelectionBinding = enableGroupSelection;
        this.enableExpandCollapseBinding = enableExpandCollapse;
    }

    @Override
    public void configureTypedLayer(ColumnGroupHeaderLayer layer) {
        this.columnGroupHeaderLayer = layer;
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

        // if the column group header value should be shown always, e.g. for
        // huge column groups, the consistent rendering is to show the group
        // name always left aligned
        if (this.columnGroupHeaderLayer.isShowAlwaysGroupNames()) {
            Style headerStyle = new Style();
            headerStyle.setAttributeValue(
                    CellStyleAttributes.HORIZONTAL_ALIGNMENT,
                    HorizontalAlignmentEnum.LEFT);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    headerStyle,
                    DisplayMode.NORMAL,
                    GridRegion.COLUMN_GROUP_HEADER);
        }
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        // Column Group Header is a part of the Group Header.
        // Register the 'column group header matcher' first so that it gets
        // picked up before the more general 'column header matcher'.
        uiBindingRegistry.registerMouseDragMode(
                MouseEventMatcher.columnGroupHeaderLeftClick(SWT.NONE),
                new AggregateDragMode(new CellDragMode(), new ColumnGroupHeaderReorderDragMode(this.columnGroupHeaderLayer)));

        uiBindingRegistry.registerMouseDragMode(
                MouseEventMatcher.columnHeaderLeftClick(SWT.NONE),
                new AggregateDragMode(new CellDragMode(), new ColumnHeaderReorderDragMode(this.columnGroupHeaderLayer)));

        // added NoOpMouseAction on single click because of Bug 428901
        if (!this.enableColumnGroupSelectionBinding) {
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

        if (this.enableExpandCollapseBinding) {
            // if column group selection is enabled, the
            // ColumnGroupExpandCollapseAction is configured to be exclusive to
            // avoid that the selection is also triggered on expand/collapse.
            // Note that this is causing a delay of Display#getDoubleClickTime()
            // in performing the selection. In case expand/collapse is not
            // supported, you might want to override this configuration with a
            // non-exclusive ColumnGroupExpandCollapseAction.
            uiBindingRegistry.registerDoubleClickBinding(
                    MouseEventMatcher.columnGroupHeaderLeftClick(SWT.NONE),
                    new ColumnGroupExpandCollapseAction(this.enableColumnGroupSelectionBinding));
        }

        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(SWT.MOD1, 'g'),
                new CreateColumnGroupAction());
        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(SWT.MOD1, 'u'),
                new UngroupColumnsAction());
    }

}
