/*******************************************************************************
 * Copyright (c) 2012, 2024 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 453882
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.action;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.ui.mode.AbstractModeEventHandler;
import org.eclipse.nebula.widgets.nattable.ui.mode.Mode;
import org.eclipse.nebula.widgets.nattable.ui.mode.ModeSupport;
import org.eclipse.nebula.widgets.nattable.ui.mode.MouseModeEventHandler;
import org.eclipse.nebula.widgets.nattable.ui.util.MouseEventHelper;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;

public class DragModeEventHandler extends AbstractModeEventHandler {

    private final IDragMode dragMode;

    private final MouseModeEventHandler parentModeEventHandler;
    private final MouseEvent mouseDownEvent;

    private boolean realDrag = false;

    private AtomicBoolean mouseUpHandled = new AtomicBoolean(false);

    public DragModeEventHandler(
            ModeSupport modeSupport,
            NatTable natTable,
            IDragMode dragMode,
            MouseModeEventHandler parentModeEventHandler,
            MouseEvent mouseDownEvent) {

        super(modeSupport, natTable);

        this.dragMode = dragMode;
        this.parentModeEventHandler = parentModeEventHandler;
        this.mouseDownEvent = mouseDownEvent;
    }

    @Override
    public void mouseMove(MouseEvent event) {
        this.dragMode.mouseMove(this.natTable, event);

        if (!this.realDrag
                && !MouseEventHelper.treatAsClick(this.mouseDownEvent, event)) {
            this.realDrag = true;
        }
    }

    @Override
    public void mouseUp(MouseEvent event) {
        // avoid to handle mouseUp twice because of focusLost
        if (this.mouseUpHandled.compareAndSet(false, true)) {
            // mouse up needs to be handled to ensure that allocated resources
            // are cleaned up correctly, e.g. overlay painters in reorder or
            // resize drag modes.
            this.dragMode.mouseUp(this.natTable, event);

            // Bug 379884
            // check if the drag operation started and ended within the same
            // cell and the mouse was not moved out of the click area. In that
            // case the registered click operation is executed also.
            if (!this.realDrag
                    && MouseEventHelper.eventOnSameCell(this.natTable, this.mouseDownEvent, event)) {
                // switching back to the parent mode to correctly handle
                // possible double clicks
                this.parentModeEventHandler.mouseUp(event);
                switchMode(this.parentModeEventHandler);
            } else {
                switchMode(Mode.NORMAL_MODE);
            }
        }
    }

    @Override
    public void focusLost(FocusEvent event) {
        // Bug 453882
        // NatTable lost the focus while a drag operation was active
        // so we simple skip the drag operation by calling mouseUp using the
        // initial mouseDown event
        mouseUp(this.mouseDownEvent);
    }
}
