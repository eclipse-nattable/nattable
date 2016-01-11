/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.mode;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.ui.util.MouseEventHelper;
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
    public void cleanup() {}

    @Override
    public void keyPressed(KeyEvent event) {}

    @Override
    public void keyReleased(KeyEvent event) {}

    @Override
    public void mouseDoubleClick(MouseEvent event) {}

    @Override
    public void mouseDown(MouseEvent event) {}

    @Override
    public void mouseUp(MouseEvent event) {}

    @Override
    public void mouseMove(MouseEvent event) {}

    @Override
    public void mouseEnter(MouseEvent e) {}

    @Override
    public void mouseExit(MouseEvent e) {}

    @Override
    public void mouseHover(MouseEvent e) {}

    @Override
    public void focusGained(FocusEvent event) {}

    @Override
    public void focusLost(FocusEvent event) {
        switchMode(Mode.NORMAL_MODE);
    }

    /**
     * Checks if the mouse down event was processed on the same cell as the
     * mouse up event. Is used to handle small mouse movements when clicking as
     * a click and not as a drag operation.
     *
     * @param downEvent
     *            The MouseEvent for mouse down.
     * @param upEvent
     *            The MouseEvent for mouse up.
     * @return <code>true</code> if the mouse up event was triggered on the same
     *         cell as the initial mouse down event.
     *
     * @deprecated Use
     *             {@link MouseEventHelper#eventOnSameCell(org.eclipse.nebula.widgets.nattable.layer.ILayer, MouseEvent, MouseEvent)}
     *             instead
     */
    @Deprecated
    protected boolean eventOnSameCell(MouseEvent downEvent, MouseEvent upEvent) {
        int startCol = this.natTable.getColumnPositionByX(downEvent.x);
        int startRow = this.natTable.getRowPositionByY(downEvent.y);

        int col = this.natTable.getColumnPositionByX(upEvent.x);
        int row = this.natTable.getRowPositionByY(upEvent.y);

        return (startCol == col && startRow == row);
    }
}
