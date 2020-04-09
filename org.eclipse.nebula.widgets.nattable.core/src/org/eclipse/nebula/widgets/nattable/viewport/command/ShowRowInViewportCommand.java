/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.viewport.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;

/**
 * Command to show a row position in the viewport.
 */
public class ShowRowInViewportCommand extends AbstractContextFreeCommand {

    private final int rowPosition;

    /**
     * @param rowPosition
     *            The row position in the underlying layer of the ViewportLayer
     *            that should be shown in the viewport.
     *
     * @since 1.6
     */
    public ShowRowInViewportCommand(int rowPosition) {
        this.rowPosition = rowPosition;
    }

    /**
     * @return The row position in the layer below the ViewportLayer to be
     *         shown.
     */
    public int getRowPosition() {
        return this.rowPosition;
    }

}
