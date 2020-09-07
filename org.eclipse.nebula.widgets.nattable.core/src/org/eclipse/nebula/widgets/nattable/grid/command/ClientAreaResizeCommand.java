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
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.grid.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Scrollable;

/**
 * Command that gives the layers access to ClientArea and the Scrollable
 */
public class ClientAreaResizeCommand extends AbstractContextFreeCommand {

    /**
     * The {@link Scrollable}, normally the NatTable itself.
     */
    private Scrollable scrollable;

    /**
     * This is the area within the client area that is used for percentage
     * calculation. Without using a GridLayer, this will be the client area of
     * the scrollable. On using a GridLayer this value will be overriden with
     * the body region area.
     */
    private Rectangle calcArea;

    public ClientAreaResizeCommand(Scrollable scrollable) {
        this.scrollable = scrollable;
    }

    public Scrollable getScrollable() {
        return this.scrollable;
    }

    public Rectangle getCalcArea() {
        if (this.calcArea == null) {
            return this.scrollable.getClientArea();
        }
        return this.calcArea;
    }

    public void setCalcArea(Rectangle calcArea) {
        this.calcArea = calcArea;
    }

}
