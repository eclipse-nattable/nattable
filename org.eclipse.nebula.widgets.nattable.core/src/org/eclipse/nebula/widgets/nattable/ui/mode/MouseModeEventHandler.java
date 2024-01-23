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
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.mode;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.action.DragModeEventHandler;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseClickAction;
import org.eclipse.nebula.widgets.nattable.ui.util.MouseEventHelper;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;

public class MouseModeEventHandler extends AbstractModeEventHandler {

    private MouseEvent initialMouseDownEvent;

    private IMouseAction singleClickAction;

    private IMouseAction doubleClickAction;

    private boolean mouseDown;

    private IDragMode dragMode;

    private boolean doubleClick;

    private boolean skipProcessing = false;

    private Runnable delayedSingleClickRunnable = null;

    public MouseModeEventHandler(
            ModeSupport modeSupport, NatTable natTable, MouseEvent initialMouseDownEvent,
            IMouseAction singleClickAction, IMouseAction doubleClickAction, IDragMode dragMode) {

        super(modeSupport, natTable);

        this.mouseDown = true;

        this.initialMouseDownEvent = initialMouseDownEvent;

        this.singleClickAction = singleClickAction;
        this.doubleClickAction = doubleClickAction;
        this.dragMode = dragMode;
    }

    @Override
    public void mouseUp(final MouseEvent event) {
        this.mouseDown = false;
        // if the mouse is slightly moved between two clicks, it is not
        // recognized as double click by the system. But as we support a click
        // radius for high sensitive mouse movements, we check here if a double
        // click runnable is scheduled and perform a double click if the single
        // click is not executed yet. The click radius is handled in mouseMove()
        // where we execute the single click if the mouse moves out of the click
        // radius after mouseUp()
        if (this.delayedSingleClickRunnable == null) {
            this.doubleClick = false;
        } else {
            // avoid execution of single click action and trigger double click
            mouseDoubleClick(event);
            return;
        }

        if (this.singleClickAction != null) {
            // convert/validate/commit/close possible open editor needed in case
            // of conversion/validation errors to cancel any action
            if (this.natTable.commitAndCloseActiveCellEditor()) {
                if (this.doubleClickAction != null
                        && (isActionExclusive(this.singleClickAction) || isActionExclusive(this.doubleClickAction))) {
                    // If a doubleClick action is registered and either the
                    // single click or the double click action is exclusive,
                    // wait to see if this mouseUp is part of a doubleClick or
                    // not.
                    this.delayedSingleClickRunnable = () -> {
                        MouseModeEventHandler.this.delayedSingleClickRunnable = null;
                        if (!MouseModeEventHandler.this.doubleClick && !MouseModeEventHandler.this.skipProcessing) {
                            executeClickAction(MouseModeEventHandler.this.singleClickAction, event);
                        }
                    };
                    event.display.timerExec(event.display.getDoubleClickTime(), this.delayedSingleClickRunnable);
                } else {
                    executeClickAction(this.singleClickAction, event);
                }
            }
        } else if (this.doubleClickAction != null) {
            // If a doubleClick action is registered but no singleClick action,
            // wait to see if this mouseUp is part of a doubleClick or
            // not and switch mode back in case if not.
            this.delayedSingleClickRunnable = () -> {
                MouseModeEventHandler.this.delayedSingleClickRunnable = null;
                if (!MouseModeEventHandler.this.doubleClick) {
                    switchMode(Mode.NORMAL_MODE);
                }
            };
            event.display.timerExec(event.display.getDoubleClickTime(), this.delayedSingleClickRunnable);
        } else {
            // No single or double click action registered when mouseUp
            // detected. Switch back to normal mode.
            switchMode(Mode.NORMAL_MODE);
        }
    }

    @Override
    public void mouseDown(MouseEvent event) {
        // another mouse click was performed than initial
        // This handling is necessary to react correctly e.g. if an action is
        // registered for left double click and an action for right single
        // click. Performing a left and right click in short sequence, nothing
        // will happen without this handling. This is also true in case a click
        // is performed without modifier key pressed and performing a mouse
        // click with modifier key pressed shortly after.
        if ((event.button != this.initialMouseDownEvent.button)
                || (event.stateMask != this.initialMouseDownEvent.stateMask)) {
            // ensure the double click runnable is not executed and process
            // single click immediately
            this.skipProcessing = true;
            executeClickAction(this.singleClickAction, this.initialMouseDownEvent);

            // reset to the parent mode
            switchMode(Mode.NORMAL_MODE);
            // start the mouse event processing for the new button
            getModeSupport().mouseDown(event);
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        // ensure the double click runnable is not executed and process single
        // click immediately
        this.skipProcessing = true;
        executeClickAction(this.singleClickAction, this.initialMouseDownEvent);

        // reset to the parent mode
        switchMode(Mode.NORMAL_MODE);
        // start the key event processing
        getModeSupport().keyPressed(event);
    }

    @Override
    public void mouseDoubleClick(MouseEvent event) {
        // double click event is fired after second mouse up event, so it needs
        // to be set to true here
        // this way the exclusive single click action knows that it should not
        // execute as a double click was performed
        this.doubleClick = true;

        executeClickAction(this.doubleClickAction, event);
    }

    @Override
    public synchronized void mouseMove(MouseEvent event) {
        if (this.mouseDown && this.dragMode != null) {
            if (this.natTable.commitAndCloseActiveCellEditor()) {
                this.dragMode.mouseDown(this.natTable, this.initialMouseDownEvent);
                switchMode(new DragModeEventHandler(getModeSupport(), this.natTable, this.dragMode, this, this.initialMouseDownEvent));
            } else {
                switchMode(Mode.NORMAL_MODE);
            }
        } else if (!this.mouseDown
                && !MouseEventHelper.treatAsClick(this.initialMouseDownEvent, event)) {

            if (this.doubleClickAction != null) {
                // if mouseUp was called already and the mouse moves out of the
                // click radius, ensure the double click runnable is not
                // executed
                // and process single click immediately as it can not become a
                // double click
                this.skipProcessing = true;
                executeClickAction(this.singleClickAction, this.initialMouseDownEvent);
            }

            // No drag mode registered when mouseMove detected. Switch back
            // to normal mode.
            switchMode(Mode.NORMAL_MODE);
        }
    }

    /**
     * Executes the given IMouseAction and switches the DisplayMode back to
     * normal.
     *
     * @param action
     *            The IMouseAction that should be executed.
     * @param event
     *            The MouseEvent that triggers the action
     */
    private void executeClickAction(IMouseAction action, MouseEvent event) {
        // convert/validate/commit/close possible open editor
        // needed in case of conversion/validation errors to cancel any action
        if (this.natTable.commitAndCloseActiveCellEditor()) {
            if (action != null && event != null) {
                event.data = NatEventData.createInstanceFromEvent(event);
                action.run(this.natTable, event);
                // Single click action complete. Switch back to normal mode.
                switchMode(Mode.NORMAL_MODE);
            }
        }
    }

    /**
     * Checks whether the given IMouseAction should be performed exclusive or
     * not. If there is a single and a double click action configured, by
     * default both the single and the double click will be performed. This
     * behaviour can be modified if the given action is of type
     * IMouseClickAction and configured to be exclusive. In this case the single
     * or the double click action will be performed.
     *
     * @param action
     *            The IMouseAction to check
     * @return <code>true</code> if the given IMouseAction should be called
     *         exclusively, <code>false</code> if not.
     */
    private boolean isActionExclusive(IMouseAction action) {
        if (action instanceof IMouseClickAction) {
            return ((IMouseClickAction) action).isExclusive();
        }
        return false;
    }
}
