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
package org.eclipse.nebula.widgets.nattable.resize.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;

public class ColumnResizeCursorAction implements IMouseAction {

    private Cursor columnResizeCursor;

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        if (this.columnResizeCursor == null) {
            this.columnResizeCursor = new Cursor(Display.getDefault(), SWT.CURSOR_SIZEWE);

            natTable.addDisposeListener(e -> ColumnResizeCursorAction.this.columnResizeCursor.dispose());
        }

        natTable.setCursor(this.columnResizeCursor);
    }

}
