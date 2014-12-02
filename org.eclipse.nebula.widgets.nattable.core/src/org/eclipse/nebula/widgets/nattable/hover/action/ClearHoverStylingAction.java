/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.hover.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.hover.HoverLayer;
import org.eclipse.nebula.widgets.nattable.hover.command.ClearHoverStylingCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.ClearCursorAction;
import org.eclipse.swt.events.MouseEvent;

/**
 * IMouseAction that will clear a hover styling that is currently applied in a
 * HoverLayer.
 * <p>
 * Will also clear any set cursor by default.
 *
 * @author Dirk Fauth
 *
 * @see HoverLayer
 * @see ClearHoverStylingCommand
 */
public class ClearHoverStylingAction extends ClearCursorAction {

    /**
     * The HoverLayer whose hover styling should not be cleared.
     */
    private HoverLayer hoverLayer;

    /**
     * Create a ClearHoverStylingAction that will trigger clearing the hover
     * styling in every HoverLayer that exists in the layer composition.
     */
    public ClearHoverStylingAction() {}

    /**
     * Create a ClearHoverStylingAction that will trigger clearing the hover
     * styling in every HoverLayer that exists in the layer composition, except
     * the given HoverLayer.
     *
     * @param hoverLayer
     *            The HoverLayer whose hover styling should not be cleared.
     */
    public ClearHoverStylingAction(HoverLayer hoverLayer) {
        this.hoverLayer = hoverLayer;
    }

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        super.run(natTable, event);

        natTable.doCommand(new ClearHoverStylingCommand(this.hoverLayer));
    }
}
