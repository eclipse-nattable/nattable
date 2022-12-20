/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hover.config;

import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.hover.action.ClearHoverStylingAction;
import org.eclipse.nebula.widgets.nattable.hover.action.HoverStylingByIndexAction;
import org.eclipse.nebula.widgets.nattable.resize.action.AutoResizeRowAction;
import org.eclipse.nebula.widgets.nattable.resize.action.RowResizeCursorAction;
import org.eclipse.nebula.widgets.nattable.resize.event.RowResizeEventMatcher;
import org.eclipse.nebula.widgets.nattable.resize.mode.RowResizeDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.NoOpMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

public class RowHeaderResizeHoverBindings extends AbstractUiBindingConfiguration {

    /**
     * The HoverLayer that is used to add hover styling.
     */
    private final HoverLayer layer;

    /**
     * @param layer
     *            The HoverLayer that is used to add hover styling.
     */
    public RowHeaderResizeHoverBindings(HoverLayer layer) {
        this.layer = layer;
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        // Mouse move - Show resize cursor
        uiBindingRegistry.registerFirstMouseMoveBinding(
                new RowResizeEventMatcher(SWT.NONE, 0),
                new RowResizeCursorAction());

        // apply a hover styling on moving the mouse over a NatTable and clear
        // the cursor
        uiBindingRegistry.registerMouseMoveBinding(
                new MouseEventMatcher(GridRegion.ROW_HEADER),
                new HoverStylingByIndexAction(this.layer));

        // clear any hover styling if the mouse is moved out of the region area
        // uiBindingRegistry.registerMouseMoveBinding(
        // new IMouseEventMatcher() {
        // @Override
        // public boolean matches(NatTable natTable, MouseEvent event,
        // LabelStack regionLabels) {
        // return (regionLabels != null &&
        // !regionLabels.hasLabel(GridRegion.ROW_HEADER));
        // }
        //
        // }, new ClearHoverStylingAction(layer));

        // clear any hover styling if the mouse is moved out of a NatTable
        // region
        uiBindingRegistry.registerMouseMoveBinding((natTable, event, regionLabels) -> (natTable != null && regionLabels == null), new ClearHoverStylingAction());

        // clear any hover styling if the mouse is moved out of the NatTable
        // area
        // always return true for the matcher because it is only asked in case
        // the mouse exits the NatTable client area, therefore further checks
        // are not necessary
        uiBindingRegistry.registerMouseExitBinding((natTable, event, regionLabels) -> true, new ClearHoverStylingAction());

        // Row resize
        uiBindingRegistry.registerFirstMouseDragMode(
                new RowResizeEventMatcher(SWT.NONE, 1),
                new RowResizeDragMode());

        uiBindingRegistry.registerDoubleClickBinding(
                new RowResizeEventMatcher(SWT.NONE, 1),
                new AutoResizeRowAction());
        uiBindingRegistry.registerSingleClickBinding(
                new RowResizeEventMatcher(SWT.NONE, 1),
                new NoOpMouseAction());
    }

}
