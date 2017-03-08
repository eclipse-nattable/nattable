/*******************************************************************************
 * Copyright (c) 2017 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.viewport.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportDragCommand;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Abstract {@link IDragMode} implementation to support auto-scrolling on
 * dragging.
 *
 * @since 1.5
 */
public abstract class AutoScrollDragMode implements IDragMode {

    private final boolean horizontal;
    private final boolean vertical;

    private AutoScrollRunnable runnable;

    protected int horizontalBorderOffset = GUIHelper.convertHorizontalPixelToDpi(25);

    /**
     *
     * @param horizontal
     *            <code>true</code> to support horizontal auto-scrolling.
     * @param vertical
     *            <code>true</code> to support vertical auto-scrolling.
     */
    public AutoScrollDragMode(boolean horizontal, boolean vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    @Override
    public void mouseMove(NatTable natTable, MouseEvent event) {
        Rectangle clientArea = natTable.getClientAreaProvider().getClientArea();

        int x = event.x;
        int y = event.y;

        int horizontalDiff = 0;
        MoveDirectionEnum horizontal = MoveDirectionEnum.NONE;
        if (this.horizontal) {
            if (event.x < this.horizontalBorderOffset) {
                horizontal = MoveDirectionEnum.LEFT;
                x = Math.max(0, event.x);
                horizontalDiff = -event.x;
            } else if (event.x > (clientArea.width - this.horizontalBorderOffset)) {
                horizontal = MoveDirectionEnum.RIGHT;
                x = clientArea.width - 1;
                horizontalDiff = event.x - clientArea.width;
            }
        }

        int verticalDiff = 0;
        MoveDirectionEnum vertical = MoveDirectionEnum.NONE;
        if (this.vertical) {
            if (event.y < 0) {
                vertical = MoveDirectionEnum.UP;
                y = 0;
                verticalDiff = -event.y;
            } else if (event.y > clientArea.height) {
                vertical = MoveDirectionEnum.DOWN;
                y = clientArea.height - 1;
                verticalDiff = event.y - clientArea.height;
            }
        }

        if ((!MoveDirectionEnum.NONE.equals(horizontal)
                || !MoveDirectionEnum.NONE.equals(vertical)) && this.runnable == null) {
            this.runnable = new AutoScrollRunnable(natTable, x, y, horizontal, vertical);
            this.runnable.schedule();
        } else if (MoveDirectionEnum.NONE.equals(horizontal)
                && MoveDirectionEnum.NONE.equals(vertical)
                && this.runnable != null) {
            this.runnable.cancel();
            this.runnable = null;
        } else if (this.runnable != null) {
            this.runnable.calculateRepeatDelay(horizontalDiff, verticalDiff);
        }

        performDragAction(natTable, x, y, horizontal, vertical);
    }

    @Override
    public void mouseUp(NatTable natTable, MouseEvent event) {
        if (this.runnable != null) {
            this.runnable.cancel();
            this.runnable = null;
        }
    }

    /**
     *
     * @param natTable
     *            The NatTable instance the drag operation is currently
     *            performed on.
     * @param x
     *            The x coordinate of the mouse pointer on mouse move. Corrected
     *            to be inside the NatTable client area.
     * @param y
     *            The y coordinate of the mouse pointer on mouse move. Corrected
     *            to be inside the NatTable client area.
     * @param horizontal
     *            The horizontal direction where the auto-scroll should be
     *            performed to.
     * @param vertical
     *            The vertical direction where the auto-scroll should be
     *            performed to.
     */
    protected void performDragAction(
            NatTable natTable,
            int x, int y,
            MoveDirectionEnum horizontal, MoveDirectionEnum vertical) {
        // do nothing by default
    }

    /**
     * Runnable that continuously scrolls the viewport.
     */
    protected class AutoScrollRunnable implements Runnable {

        private final NatTable natTable;
        private final Display display;

        private final MoveDirectionEnum horizontal;
        private final MoveDirectionEnum vertical;

        private final int x, y;

        private int repeatDelay = 500;

        private boolean active = true;

        public AutoScrollRunnable(NatTable natTable,
                int x, int y,
                MoveDirectionEnum horizontal, MoveDirectionEnum vertical) {
            this.natTable = natTable;
            this.display = natTable.getDisplay();
            this.horizontal = horizontal;
            this.vertical = vertical;
            this.x = x;
            this.y = y;
        }

        /**
         * Schedule this runnable to start with a delay of 500 ms.
         */
        public void schedule() {
            if (this.display != null) {
                this.display.timerExec(500, this);
            }
        }

        /**
         * Calculates the delay of the repeated auto-scroll execution based on
         * the difference between the NatTable borders and the mouse cursor and
         * the move direction that is currently active.
         *
         * @param horizontalDiff
         *            The horizontal difference between the NatTable border and
         *            the mouse cursor.
         * @param verticalDiff
         *            The vertical difference between the NatTable border and
         *            the mouse cursor.
         */
        public void calculateRepeatDelay(int horizontalDiff, int verticalDiff) {
            if (!MoveDirectionEnum.NONE.equals(this.horizontal)) {
                int factor = horizontalDiff / 5;
                factor = Math.min(factor, 10);
                this.repeatDelay = 500 - (factor * 49);
            }

            if (!MoveDirectionEnum.NONE.equals(this.vertical)) {
                int factor = verticalDiff / 5;
                factor = Math.min(factor, 10);
                this.repeatDelay = 500 - (factor * 49);
            }
        }

        /**
         * Cancels the repeated execution.
         */
        public void cancel() {
            this.active = false;
        }

        @Override
        public void run() {
            if (this.active) {
                if (!MoveDirectionEnum.NONE.equals(this.horizontal)
                        || !MoveDirectionEnum.NONE.equals(this.vertical)) {
                    if (this.natTable.doCommand(new ViewportDragCommand(this.horizontal, this.vertical))) {
                        performDragAction(this.natTable, this.x, this.y, this.horizontal, this.vertical);
                    }
                }

                this.display.timerExec(this.repeatDelay, this);
            }
        }
    }
}
