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
 * Command to show a column position in the viewport.
 */
public class ShowColumnInViewportCommand extends AbstractContextFreeCommand {

    private final int columnPosition;

    /**
     * @param columnPosition
     *            The column position in the underlying layer of the
     *            ViewportLayer that should be shown in the viewport.
     *
     * @since 1.6
     */
    public ShowColumnInViewportCommand(int columnPosition) {
        this.columnPosition = columnPosition;
    }

    /**
     * @return The column position in the layer below the ViewportLayer to be
     *         shown.
     */
    public int getColumnPosition() {
        return this.columnPosition;
    }

}
