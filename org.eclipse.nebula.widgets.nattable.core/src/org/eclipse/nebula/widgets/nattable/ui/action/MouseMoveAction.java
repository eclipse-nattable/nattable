/*******************************************************************************
 * Copyright (c) 2024 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.ui.matcher.IMouseEventMatcher;
import org.eclipse.swt.events.MouseEvent;

/**
 * {@link IMouseAction} that is used to configure mouse move bindings. Contains
 * a {@link IMouseAction} that is executed on entering the
 * {@link IMouseEventMatcher} and a {@link IMouseAction} that is executed once
 * the {@link IMouseEventMatcher} is exited.
 */
public class MouseMoveAction implements IMouseAction {

    public final IMouseEventMatcher mouseEventMatcher;
    public final IMouseAction entryAction;
    public final IMouseAction exitAction;
    public final boolean reexecuteEntryAction;

    /**
     *
     * @param mouseEventMatcher
     *            The {@link IMouseEventMatcher} to determine the enter and exit
     *            condition.
     * @param entryAction
     *            The {@link IMouseAction} that should be executed on enter.
     * @param exitAction
     *            The {@link IMouseAction} that should be executed on exit.
     * @param reexecuteEntryAction
     *            <code>true</code> if the entry action should be executed
     *            everytime as long as the {@link IMouseEventMatcher} matches,
     *            <code>false</code> if the entry action should only be executed
     *            on enter.
     */
    public MouseMoveAction(IMouseEventMatcher mouseEventMatcher, IMouseAction entryAction, IMouseAction exitAction, boolean reexecuteEntryAction) {
        this.mouseEventMatcher = mouseEventMatcher;
        this.reexecuteEntryAction = reexecuteEntryAction;
        this.entryAction = entryAction;
        this.exitAction = exitAction;
    }

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        this.entryAction.run(natTable, event);
    }

    public void runExit(NatTable natTable, MouseEvent event) {
        this.exitAction.run(natTable, event);
    }

}
