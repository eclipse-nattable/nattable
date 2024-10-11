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
package org.eclipse.nebula.widgets.nattable.ui.action;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;

public class AggregateDragMode implements IDragMode, IDragModeWithKeySupport {

    private MouseEvent initialEvent;
    private MouseEvent currentEvent;

    private final Collection<IDragMode> dragModes = new LinkedHashSet<>();

    public AggregateDragMode() {
    }

    public AggregateDragMode(IDragMode... dragModes) {
        for (IDragMode dragMode : dragModes) {
            addDragMode(dragMode);
        }
    }

    public void addDragMode(IDragMode dragMode) {
        this.dragModes.add(dragMode);
    }

    public void removeDragMode(IDragMode dragMode) {
        this.dragModes.remove(dragMode);
    }

    @Override
    public void mouseDown(NatTable natTable, MouseEvent event) {
        this.initialEvent = event;
        this.currentEvent = this.initialEvent;

        for (IDragMode dragMode : this.dragModes) {
            dragMode.mouseDown(natTable, event);
        }

        natTable.forceFocus();
    }

    @Override
    public void mouseMove(NatTable natTable, MouseEvent event) {
        this.currentEvent = event;

        for (IDragMode dragMode : this.dragModes) {
            dragMode.mouseMove(natTable, event);
        }
    }

    @Override
    public void mouseUp(NatTable natTable, MouseEvent event) {
        for (IDragMode dragMode : this.dragModes) {
            dragMode.mouseUp(natTable, event);
        }
    }

    protected MouseEvent getInitialEvent() {
        return this.initialEvent;
    }

    protected MouseEvent getCurrentEvent() {
        return this.currentEvent;
    }

    @Override
    public void keyPressed(NatTable natTable, KeyEvent event) {
        for (IDragMode dragMode : this.dragModes) {
            if (dragMode instanceof IDragModeWithKeySupport)
                ((IDragModeWithKeySupport) dragMode).keyPressed(natTable, event);
        }
    }

    @Override
    public void keyReleased(NatTable natTable, KeyEvent event) {
        for (IDragMode dragMode : this.dragModes) {
            if (dragMode instanceof IDragModeWithKeySupport)
                ((IDragModeWithKeySupport) dragMode).keyReleased(natTable, event);
        }
    }

}
