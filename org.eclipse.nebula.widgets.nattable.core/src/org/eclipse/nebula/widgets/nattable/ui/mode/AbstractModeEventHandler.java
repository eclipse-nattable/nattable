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
package org.eclipse.nebula.widgets.nattable.ui.mode;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;

public class AbstractModeEventHandler implements IModeEventHandler {

    private ModeSupport modeSupport;

    protected final NatTable natTable;

    public AbstractModeEventHandler(ModeSupport modeSupport, NatTable natTable) {
        this.modeSupport = modeSupport;
        this.natTable = natTable;
    }

    protected ModeSupport getModeSupport() {
        return this.modeSupport;
    }

    protected void switchMode(String mode) {
        this.modeSupport.switchMode(mode);
    }

    protected void switchMode(IModeEventHandler modeEventHandler) {
        this.modeSupport.switchMode(modeEventHandler);
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void keyPressed(KeyEvent event) {
    }

    @Override
    public void keyReleased(KeyEvent event) {
    }

    @Override
    public void mouseDoubleClick(MouseEvent event) {
    }

    @Override
    public void mouseDown(MouseEvent event) {
    }

    @Override
    public void mouseUp(MouseEvent event) {
    }

    @Override
    public void mouseMove(MouseEvent event) {
    }

    @Override
    public void mouseEnter(MouseEvent e) {
    }

    @Override
    public void mouseExit(MouseEvent e) {
    }

    @Override
    public void mouseHover(MouseEvent e) {
    }

    @Override
    public void focusGained(FocusEvent event) {
    }

    @Override
    public void focusLost(FocusEvent event) {
        switchMode(Mode.NORMAL_MODE);
    }
}
