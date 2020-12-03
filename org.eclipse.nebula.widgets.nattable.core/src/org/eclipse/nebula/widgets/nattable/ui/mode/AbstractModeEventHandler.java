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

    /**
     * Switch the mode in the underlying {@link ModeSupport}.
     *
     * @param mode
     *            The {@link Mode} to switch to.
     * @since 2.0
     */
    protected void switchMode(Mode mode) {
        this.modeSupport.switchMode(mode);
    }

    protected void switchMode(IModeEventHandler modeEventHandler) {
        this.modeSupport.switchMode(modeEventHandler);
    }

    @Override
    public void cleanup() {
        // not implemented
    }

    @Override
    public void keyPressed(KeyEvent event) {
        // not implemented
    }

    @Override
    public void keyReleased(KeyEvent event) {
        // not implemented
    }

    @Override
    public void mouseDoubleClick(MouseEvent event) {
        // not implemented
    }

    @Override
    public void mouseDown(MouseEvent event) {
        // not implemented
    }

    @Override
    public void mouseUp(MouseEvent event) {
        // not implemented
    }

    @Override
    public void mouseMove(MouseEvent event) {
        // not implemented
    }

    @Override
    public void mouseEnter(MouseEvent e) {
        // not implemented
    }

    @Override
    public void mouseExit(MouseEvent e) {
        // not implemented
    }

    @Override
    public void mouseHover(MouseEvent e) {
        // not implemented
    }

    @Override
    public void focusGained(FocusEvent event) {
        // not implemented
    }

    @Override
    public void focusLost(FocusEvent event) {
        switchMode(Mode.NORMAL_MODE);
    }
}
