/*******************************************************************************
 * Copyright (c) 2013, 2024 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hover.config;

import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.hover.action.ClearHoverStylingAction;
import org.eclipse.nebula.widgets.nattable.hover.action.HoverStylingAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;

/**
 * UI bindings for applying and clearing styles when moving the mouse over
 * NatTable cells.
 * <p>
 * This configuration should be used if a table without headers is rendered. The
 * reason for this is that the client area is the whole available area in such a
 * case, so if the area is bigger than the rendered table, the hovering wouldn't
 * be removed if the mouse cursor is moved out of the cells but still in the
 * area.
 *
 * @see HoverLayer
 */
public class SimpleHoverStylingBindings extends AbstractUiBindingConfiguration {

    /**
     * The HoverLayer that is used to add hover styling.
     */
    private final HoverLayer layer;

    /**
     * @param layer
     *            The HoverLayer that is used to add hover styling.
     */
    public SimpleHoverStylingBindings(HoverLayer layer) {
        this.layer = layer;
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        // apply a hover styling on moving the mouse over a NatTable
        uiBindingRegistry.registerFirstMouseMoveBinding(
                (natTable, event, regionLabels) -> {

                    int width = SimpleHoverStylingBindings.this.layer.getPreferredWidth();
                    int height = SimpleHoverStylingBindings.this.layer.getPreferredHeight();

                    return ((event.x > 0 && event.x < width) && (event.y > 0 && event.y < height));
                },
                new HoverStylingAction(this.layer),
                new ClearHoverStylingAction());
    }

}
