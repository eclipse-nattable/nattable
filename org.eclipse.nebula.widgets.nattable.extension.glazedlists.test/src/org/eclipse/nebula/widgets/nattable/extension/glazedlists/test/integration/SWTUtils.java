/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.test.integration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;

public class SWTUtils {

    public static final int NO_CLICK = 0;
    public static final int LEFT_MOUSE_BUTTON = 1;
    public static final int RIGHT_MOUSE_BUTTON = 3;
    public static final int ONE_CLICK = 1;

    public static void leftClickOnCombo(int x, int y, int stateMask,
            Control control) {
        Event leftClickEvent = getLeftClickEvent(x, y, stateMask, control);
        control.notifyListeners(SWT.MouseDown, leftClickEvent);
        control.notifyListeners(SWT.MouseUp, leftClickEvent);
    }

    public static void selectInCombo(int x, int y, int stateMask,
            Control control) {
        Event leftClickEvent = getLeftClickEvent(x, y, stateMask, control);
        control.notifyListeners(SWT.Selection, leftClickEvent);
    }

    public static void leftClick(int x, int y, int stateMask, Widget nattable) {
        Event leftClickEvent = getLeftClickEvent(x, y, stateMask, nattable);

        nattable.notifyListeners(SWT.MouseDown, leftClickEvent);
        nattable.notifyListeners(SWT.MouseUp, leftClickEvent);
    }

    public static Event getLeftClickEvent(int x, int y, int stateMask,
            Widget nattable) {
        Event event = new Event();
        event.time = (int) System.currentTimeMillis();
        event.widget = nattable;
        event.display = nattable.getDisplay();
        event.x = x;
        event.y = y;
        event.button = LEFT_MOUSE_BUTTON;
        event.stateMask = stateMask;
        event.count = ONE_CLICK;
        return event;
    }

    public static void pressCharKey(char c, Widget natTable) {
        natTable.notifyListeners(SWT.KeyDown, keyEventWithChar(c));
    }

    public static void pressKey(int keyCode, Widget natTable) {
        natTable.notifyListeners(SWT.KeyDown, keyEvent(keyCode));
    }

    public static void pressKey(int keyCode, int stateMask, Widget natTable) {
        natTable.notifyListeners(SWT.KeyDown, keyEventWithModifier(keyCode, stateMask));
    }

    public static void pressKeyOnControl(int keyCode, Control control) {
        control.notifyListeners(SWT.KeyDown, keyEvent(keyCode));
    }

    public static void pressKeyOnControl(int keyCode, int stateMask, Control control) {
        control.notifyListeners(SWT.KeyDown, keyEventWithModifier(keyCode, stateMask));
    }

    public static Event keyEvent(int keyCode) {
        return keyEventWithModifier(keyCode, SWT.NONE);
    }

    public static Event keyEventWithModifier(int keyCode, int stateMask) {
        Event event = new Event();
        event.type = SWT.KeyDown;
        event.keyCode = keyCode;
        event.stateMask = stateMask;
        return event;
    }

    public static Event keyEventWithChar(char c) {
        Event event = new Event();
        event.type = SWT.KeyDown;
        event.character = c;
        event.stateMask = SWT.NONE;
        return event;
    }

    /**
     * Some of the tests do not run on Unix, due to issues with Xvfb. This check
     * helps skipping those tests.
     */
    public static boolean isRunningOnUnix() {
        return System.getProperty("os.name").equals("Linux");
    }

}
