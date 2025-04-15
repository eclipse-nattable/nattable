/*******************************************************************************
 * Copyright (c) 2012, 2025 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Roman Flueckiger <rflueckiger@inventage.com> - Bug 463130
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.mode;

import java.util.EnumMap;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.util.PlatformHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;

/**
 * Modal event handler for NatTable. This class acts as a proxy event listener.
 * It manages a set of IModeEventHandler instances which control the actual
 * event handling for a given mode. This allows the event handling behavior for
 * different modes to be grouped together and isolated from each other.
 */
public class ModeSupport implements KeyListener, MouseListener, MouseMoveListener, MouseTrackListener, FocusListener {

    private EnumMap<Mode, IModeEventHandler> modeEventHandlerMap = new EnumMap<>(Mode.class);

    private IModeEventHandler currentModeEventHandler;

    public ModeSupport(NatTable natTable) {
        natTable.addKeyListener(this);
        natTable.addMouseListener(this);
        natTable.addFocusListener(this);

        PlatformHelper.callSetter(natTable, "addMouseMoveListener", MouseMoveListener.class, this); //$NON-NLS-1$
        PlatformHelper.callSetter(natTable, "addMouseTrackListener", MouseTrackListener.class, this); //$NON-NLS-1$
    }

    /**
     * Register an event handler to handle events for a given mode.
     *
     * @param mode
     *            The mode.
     * @param modeEventHandler
     *            An IModeEventHandler instance that will handle events in the
     *            given mode.
     *
     * @see IModeEventHandler
     * @since 2.0
     */
    public void registerModeEventHandler(Mode mode, IModeEventHandler modeEventHandler) {
        this.modeEventHandlerMap.put(mode, modeEventHandler);
    }

    /**
     * Switch to the given mode.
     *
     * @param mode
     *            The target mode to switch to.
     * @since 2.0
     */
    public void switchMode(Mode mode) {
        if (this.currentModeEventHandler != null) {
            this.currentModeEventHandler.cleanup();
        }
        this.currentModeEventHandler = this.modeEventHandlerMap.get(mode);
    }

    public void switchMode(IModeEventHandler modeEventHandler) {
        if (this.currentModeEventHandler != null) {
            this.currentModeEventHandler.cleanup();
        }
        this.currentModeEventHandler = modeEventHandler;
    }

    @Override
    public void keyPressed(KeyEvent event) {
        this.currentModeEventHandler.keyPressed(event);
    }

    @Override
    public void keyReleased(KeyEvent event) {
        this.currentModeEventHandler.keyReleased(event);
    }

    @Override
    public void mouseDoubleClick(MouseEvent event) {
        modifyMouseEventForMac(event);
        this.currentModeEventHandler.mouseDoubleClick(event);
    }

    @Override
    public void mouseDown(MouseEvent event) {
        modifyMouseEventForMac(event);
        this.currentModeEventHandler.mouseDown(event);
    }

    @Override
    public void mouseUp(MouseEvent event) {
        modifyMouseEventForMac(event);
        this.currentModeEventHandler.mouseUp(event);
    }

    @Override
    public void mouseMove(MouseEvent event) {
        modifyMouseEventForMac(event);
        this.currentModeEventHandler.mouseMove(event);
    }

    @Override
    public void mouseEnter(MouseEvent e) {
        this.currentModeEventHandler.mouseEnter(e);
    }

    @Override
    public void mouseExit(MouseEvent e) {
        this.currentModeEventHandler.mouseExit(e);
    }

    @Override
    public void mouseHover(MouseEvent e) {
        this.currentModeEventHandler.mouseHover(e);
    }

    @Override
    public void focusGained(FocusEvent event) {
        this.currentModeEventHandler.focusGained(event);
    }

    @Override
    public void focusLost(FocusEvent event) {
        this.currentModeEventHandler.focusLost(event);
    }

    /**
     * Modifies the {@link MouseEvent} in case a CTRL + left click is performed.
     * This is necessary because on Mac that combination is used to trigger a
     * right click.
     *
     * @param event
     *            The {@link MouseEvent} to modify
     */
    private void modifyMouseEventForMac(MouseEvent event) {
        if (PlatformHelper.isMAC() && event.stateMask == SWT.MOD4 && event.button == 1) {
            event.stateMask = event.stateMask & ~SWT.MOD4;
            event.button = 3;
        }
    }

}
