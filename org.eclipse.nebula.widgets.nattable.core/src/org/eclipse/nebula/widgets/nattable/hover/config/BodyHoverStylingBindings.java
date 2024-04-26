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
 * NatTable cells. Is registered together with the HoverLayer that is used to
 * add the hover styling functionality.
 *
 * @see HoverLayer
 */
public class BodyHoverStylingBindings extends AbstractUiBindingConfiguration {

    /**
     * The HoverLayer that is used to add hover styling.
     */
    private final HoverLayer layer;

    /**
     * @param layer
     *            The HoverLayer that is used to add hover styling.
     */
    public BodyHoverStylingBindings(HoverLayer layer) {
        this.layer = layer;
    }

    @Override
    public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        // apply a hover styling on moving the mouse over a NatTable
        uiBindingRegistry.registerFirstMouseMoveBinding(
                (natTable, event, regionLabels) -> BodyHoverStylingBindings.this.layer.getClientAreaProvider().getClientArea()
                        .contains(event.x, event.y),
                new HoverStylingAction(this.layer),
                new ClearHoverStylingAction());
    }

}
