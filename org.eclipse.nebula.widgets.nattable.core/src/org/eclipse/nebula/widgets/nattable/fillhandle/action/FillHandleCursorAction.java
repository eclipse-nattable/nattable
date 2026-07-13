/*****************************************************************************
 * Copyright (c) 2015, 2026 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *      Loris Securo <lorissek@gmail.com> - Bug 499508
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.fillhandle.action;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.fillhandle.config.FillHandleConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.widgets.Display;

/**
 * Action to enable a custom cursor (small cross) on NatTable. Used when moving
 * over the fill handle of a current selection.
 *
 * @see FillHandleConfiguration
 *
 * @since 1.4
 */
public class FillHandleCursorAction implements IMouseAction {

    private Cursor fillHandleCursor;

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        if (this.fillHandleCursor == null) {
            ImageDescriptor imageDescriptor = GUIHelper.getImageDescriptor("fill_handle"); //$NON-NLS-1$
            ImageDataProvider imageDataProvider = zoom -> {
                return imageDescriptor.getImageData(zoom);
            };
            this.fillHandleCursor = new Cursor(Display.getDefault(), imageDataProvider, 7, 7);

            natTable.addDisposeListener(e -> FillHandleCursorAction.this.fillHandleCursor.dispose());
        }

        natTable.setCursor(this.fillHandleCursor);
    }

}
