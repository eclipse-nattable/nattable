/*****************************************************************************
 * Copyright (c) 2016, 2020 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;

/**
 * Action to enable a custom cursor (resize) on NatTable. Used when moving over
 * the border of a resizable composite.
 *
 * @since 1.4
 */
public class HorizontalResizeCursorAction implements IMouseAction {

    private Cursor resizeCursor;

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        if (this.resizeCursor == null) {
            this.resizeCursor = new Cursor(Display.getDefault(), GUIHelper.getDisplayImage("horizontal_resize").getImageData(), 15, 15); //$NON-NLS-1$

            natTable.addDisposeListener(e -> HorizontalResizeCursorAction.this.resizeCursor.dispose());
        }

        natTable.setCursor(this.resizeCursor);
    }

}
