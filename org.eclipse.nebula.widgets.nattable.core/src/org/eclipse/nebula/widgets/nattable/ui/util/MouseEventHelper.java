/*******************************************************************************
 * Copyright (c) 2015 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.util;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.events.MouseEvent;

/**
 * Helper class that provides methods for checking {@link MouseEvent}
 * correlations.
 *
 * @since 1.4
 */
public class MouseEventHelper {

    /**
     * Checks if the mouse down event and the mouse up event was triggered with
     * an accidental movement which causes a drag behavior. If the up event is
     * in an area of 5 pixels around the down event, we suspect it was a click
     * rather than a drag operation.
     *
     * @param downEvent
     *            The {@link MouseEvent} for mouse down.
     * @param upEvent
     *            The {@link MouseEvent} for mouse up.
     * @return <code>true</code> if the mouse down event and the mouse up event
     *         are too close to be a drag operation and should therefore be
     *         treated as a click, <code>false</code> if the {@link MouseEvent}
     *         properties are quite different so a user drag operation is
     *         assumed.
     */
    public static boolean treatAsClick(MouseEvent downEvent, MouseEvent upEvent) {

        if (((upEvent.x > (downEvent.x + 5)) || (upEvent.x < (downEvent.x - 5)))
                || ((upEvent.y > (downEvent.y + 5)) || (upEvent.y < (downEvent.y - 5)))) {
            return false;
        }

        return true;
    }

    /**
     * Checks if the mouse down event was processed on the same cell as the
     * mouse up event. Is used to handle small mouse movements when clicking as
     * a click and not as a drag operation.
     *
     * @param layer
     *            The layer needed to identify the column and row position
     *            according to the {@link MouseEvent} coordinates. Typically a
     *            NatTable instance.
     * @param downEvent
     *            The {@link MouseEvent} for mouse down.
     * @param upEvent
     *            The {@link MouseEvent} for mouse up.
     * @return <code>true</code> if the mouse up event was triggered on the same
     *         cell as the initial mouse down event.
     */
    public static boolean eventOnSameCell(ILayer layer, MouseEvent downEvent, MouseEvent upEvent) {
        int startCol = layer.getColumnPositionByX(downEvent.x);
        int startRow = layer.getRowPositionByY(downEvent.y);

        int col = layer.getColumnPositionByX(upEvent.x);
        int row = layer.getRowPositionByY(upEvent.y);

        return (startCol == col && startRow == row);
    }

}
