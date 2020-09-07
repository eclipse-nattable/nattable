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
import org.eclipse.nebula.widgets.nattable.group.action.RowGroupExpandCollapseAction;
import org.eclipse.nebula.widgets.nattable.group.action.ViewportSelectRowGroupAction;
import org.eclipse.nebula.widgets.nattable.group.painter.RowGroupHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.group.performance.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.action.RowGroupHeaderReorderDragMode;
import org.eclipse.nebula.widgets.nattable.group.performance.action.RowHeaderReorderDragMode;
import org.eclipse.nebula.widgets.nattable.painter.cell.VerticalTextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.BeveledBorderDecorator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.ui.action.AggregateDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.CellDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.NoOpMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

/**
 * The default configuration for the {@link RowGroupHeaderLayer}.
 *
 * @since 1.6
 */
public class DefaultRowGroupHeaderLayerConfiguration extends AbstractLayerConfiguration<RowGroupHeaderLayer> {

    private final boolean enableRowGroupSelectionBinding;
    private final boolean enableExpandCollapseBinding;

    private RowGroupHeaderLayer rowGroupHeaderLayer;

    /**
     * Creates the DefaultRowGroupHeaderLayerConfiguration with enabled
     * expand/collapse binding.
     *
     * @param enableGroupSelection
     *            <code>true</code> if single click selection bindings on the
     *            row group header should be enabled, <code>false</code> if no
     *            operations should be triggered on single click.
     */
    public DefaultRowGroupHeaderLayerConfiguration(boolean enableGroupSelection) {
        this(enableGroupSelection, true);
    }

    /**
     *
     * @param enableGroupSelection
     *            <code>true</code> if single click selection bindings on the
     *            row group header should be enabled, <code>false</code> if no
     *            operations should be triggered on single click.
     * @param enableExpandCollapse
     *            <code>true</code> if the binding should be registered to
     *            expand/collapse a group on double click, <code>false</code> if
     *            the binding should not be registered.
     */
    public DefaultRowGroupHeaderLayerConfiguration(boolean enableGroupSelection, boolean enableExpandCollapse) {
        this.enableRowGroupSelectionBinding = enableGroupSelection;
        this.enableExpandCollapseBinding = enableExpandCollapse;
    }

    @Override
    public void configureTypedLayer(RowGroupHeaderLayer layer) {
        this.rowGroupHeaderLayer = layer;
    }

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_PAINTER,
                new BeveledBorderDecorator(new RowGroupHeaderTextPainter(new VerticalTextPainter())),
                DisplayMode.NORMAL,
                GridRegion.ROW_GROUP_HEADER);
        // We are not setting a special configuration for rendering grid lines,
        // as this would override the column header configuration. This is
        // because the the row group header is part of the row header
        // region.

        // if the row group header value should be shown always, e.g. for
        // huge row groups, the consistent rendering is to show the group
        // name always top aligned
        if (this.rowGroupHeaderLayer.isShowAlwaysGroupNames()) {
            Style headerStyle = new Style();
            headerStyle.setAttributeValue(
                    CellStyleAttributes.VERTICAL_ALIGNMENT,
                    VerticalAlignmentEnum.TOP);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    headerStyle,
                    DisplayMode.NORMAL,
                    GridRegion.ROW_GROUP_HEADER);
        }
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        // Row Group Header is a part of the Group Header.
        // Register the 'row group header matcher' first so that it gets
        // picked up before the more general 'row header matcher'.
        uiBindingRegistry.registerMouseDragMode(
                MouseEventMatcher.rowGroupHeaderLeftClick(SWT.NONE),
                new AggregateDragMode(new CellDragMode(), new RowGroupHeaderReorderDragMode(this.rowGroupHeaderLayer)));

        uiBindingRegistry.registerMouseDragMode(
                MouseEventMatcher.rowHeaderLeftClick(SWT.NONE),
                new AggregateDragMode(new CellDragMode(), new RowHeaderReorderDragMode(this.rowGroupHeaderLayer)));

        // added NoOpMouseAction on single click because of Bug 428901
        uiBindingRegistry.registerMouseDownBinding(
                MouseEventMatcher.rowGroupHeaderLeftClick(SWT.NONE),
                new NoOpMouseAction());
        uiBindingRegistry.registerMouseDownBinding(
                MouseEventMatcher.rowGroupHeaderLeftClick(SWT.MOD1),
                new NoOpMouseAction());
        uiBindingRegistry.registerMouseDownBinding(
                MouseEventMatcher.rowGroupHeaderLeftClick(SWT.MOD2),
                new NoOpMouseAction());
        uiBindingRegistry.registerMouseDownBinding(
                MouseEventMatcher.rowGroupHeaderLeftClick(SWT.MOD1 | SWT.MOD2),
                new NoOpMouseAction());

        if (this.enableRowGroupSelectionBinding) {
            uiBindingRegistry.registerSingleClickBinding(
                    MouseEventMatcher.rowGroupHeaderLeftClick(SWT.NONE),
                    new ViewportSelectRowGroupAction(false, false));
            uiBindingRegistry.registerSingleClickBinding(
                    MouseEventMatcher.rowGroupHeaderLeftClick(SWT.MOD1),
                    new ViewportSelectRowGroupAction(false, true));
            uiBindingRegistry.registerSingleClickBinding(
                    MouseEventMatcher.rowGroupHeaderLeftClick(SWT.MOD2),
                    new ViewportSelectRowGroupAction(true, false));
            uiBindingRegistry.registerSingleClickBinding(
                    MouseEventMatcher.rowGroupHeaderLeftClick(SWT.MOD1 | SWT.MOD2),
                    new ViewportSelectRowGroupAction(true, true));
        }

        if (this.enableExpandCollapseBinding) {
            // if row group selection is enabled, the
            // RowGroupExpandCollapseAction is configured to be exclusive to
            // avoid that the selection is also triggered on expand/collapse.
            // Note that this is causing a delay of Display#getDoubleClickTime()
            // in performing the selection. In case expand/collapse is not
            // supported, you might want to override this configuration with a
            // non-exclusive RowGroupExpandCollapseAction.
            uiBindingRegistry.registerDoubleClickBinding(
                    MouseEventMatcher.rowGroupHeaderLeftClick(SWT.NONE),
                    new RowGroupExpandCollapseAction(this.enableRowGroupSelectionBinding));
        }
    }
}
