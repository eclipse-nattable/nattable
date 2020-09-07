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
package org.eclipse.nebula.widgets.nattable.ui.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.events.MouseEvent;

/**
 * Implementing this interface will create an action that translates an SWT
 * MouseEvent into a command that should be executed in the NatTable.
 * <p>
 * This concept allows to catch general MouseEvents on the NatTable control
 * itself, and translate it into commands that correspond to a cell which is
 * determined via x/y coordinates.
 */
public interface IMouseAction {

    /**
     * Translates the SWT MouseEvent to a NatTable command and executes that
     * command accordingly.
     *
     * @param natTable
     *            The NatTable instance on which the MouseEvent was fired and on
     *            which the command should be executed.
     * @param event
     *            The received MouseEvent.
     */
    void run(NatTable natTable, MouseEvent event);
}
