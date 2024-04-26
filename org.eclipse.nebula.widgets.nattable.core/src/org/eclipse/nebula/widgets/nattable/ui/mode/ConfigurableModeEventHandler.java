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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.action.MouseMoveAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;

public class ConfigurableModeEventHandler extends AbstractModeEventHandler {

    public ConfigurableModeEventHandler(ModeSupport modeSupport, NatTable natTable) {
        super(modeSupport, natTable);
    }

    // Event handling /////////////////////////////////////////////////////////

    @Override
    public void keyPressed(KeyEvent event) {
        IKeyAction keyAction = getUiBindingRegistry().getKeyEventAction(event);
        if (keyAction != null) {
            this.natTable.forceFocus();
            keyAction.run(this.natTable, event);
        }
    }

    @Override
    public void mouseDown(MouseEvent event) {
        if (this.natTable.commitAndCloseActiveCellEditor()) {
            IMouseAction mouseDownAction = getUiBindingRegistry().getMouseDownAction(event);
            if (mouseDownAction != null) {
                event.data = NatEventData.createInstanceFromEvent(event);
                mouseDownAction.run(this.natTable, event);
            }

            IMouseAction singleClickAction = getUiBindingRegistry().getSingleClickAction(event);
            IMouseAction doubleClickAction = getUiBindingRegistry().getDoubleClickAction(event);
            IDragMode dragMode = getUiBindingRegistry().getDragMode(event);

            if (singleClickAction != null || doubleClickAction != null || dragMode != null) {
                switchMode(new MouseModeEventHandler(
                        getModeSupport(), this.natTable, event,
                        singleClickAction, doubleClickAction, dragMode));
            }
        }
    }

    private List<MouseMoveAction> currentActiveMoveActions = new ArrayList<>();

    @Override
    public synchronized void mouseMove(MouseEvent event) {
        if (event.x >= 0 && event.y >= 0) {
            event.data = NatEventData.createInstanceFromEvent(event);

            // check if current active move actions are still active
            for (Iterator<MouseMoveAction> it = this.currentActiveMoveActions.iterator(); it.hasNext();) {
                MouseMoveAction currentAction = it.next();
                LabelStack regionLabels = this.natTable.getRegionLabelsByXY(event.x, event.y);
                if (currentAction.mouseEventMatcher.matches(this.natTable, event, regionLabels) && currentAction.reexecuteEntryAction) {
                    currentAction.run(this.natTable, event);
                } else {
                    currentAction.runExit(this.natTable, event);
                    it.remove();
                }
            }

            IMouseAction mouseMoveAction = getUiBindingRegistry().getMouseMoveAction(event);

            if (mouseMoveAction != null) {
                if (mouseMoveAction instanceof MouseMoveAction && !this.currentActiveMoveActions.contains(mouseMoveAction)) {
                    mouseMoveAction.run(this.natTable, event);
                    this.currentActiveMoveActions.add((MouseMoveAction) mouseMoveAction);
                } else if (!(mouseMoveAction instanceof MouseMoveAction)) {
                    mouseMoveAction.run(this.natTable, event);
                }
            } else {
                this.natTable.setCursor(null);
            }
        }
    }

    @Override
    public synchronized void mouseHover(MouseEvent event) {
        if (event.x >= 0 && event.y >= 0) {
            IMouseAction mouseHoverAction = getUiBindingRegistry().getMouseHoverAction(event);
            if (mouseHoverAction != null) {
                event.data = NatEventData.createInstanceFromEvent(event);
                mouseHoverAction.run(this.natTable, event);
            }
        }
    }

    @Override
    public synchronized void mouseEnter(MouseEvent event) {
        if (event.x >= 0 && event.y >= 0) {
            IMouseAction mouseEnterAction = getUiBindingRegistry().getMouseEnterAction(event);
            if (mouseEnterAction != null) {
                event.data = NatEventData.createInstanceFromEvent(event);
                mouseEnterAction.run(this.natTable, event);
            } else {
                this.natTable.setCursor(null);
            }
        }
    }

    @Override
    public synchronized void mouseExit(MouseEvent event) {
        event.data = NatEventData.createInstanceFromEvent(event);

        // ensure that any current active move action is exited
        this.currentActiveMoveActions.forEach(action -> action.runExit(this.natTable, event));

        IMouseAction mouseExitAction = getUiBindingRegistry().getMouseExitAction(event);
        if (mouseExitAction != null) {
            mouseExitAction.run(this.natTable, event);
        } else {
            this.natTable.setCursor(null);
        }
    }

    private UiBindingRegistry getUiBindingRegistry() {
        return this.natTable.getUiBindingRegistry();
    }

}
