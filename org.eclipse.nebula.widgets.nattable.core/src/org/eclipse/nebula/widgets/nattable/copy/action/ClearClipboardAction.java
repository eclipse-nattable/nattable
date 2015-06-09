/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
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
package org.eclipse.nebula.widgets.nattable.copy.action;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.swt.events.KeyEvent;

/**
 * {@link IKeyAction} to clear the {@link InternalCellClipboard}.
 *
 * @since 1.4
 */
public class ClearClipboardAction implements IKeyAction {

    private InternalCellClipboard clipboard;

    /**
     * @param clipboard
     *            The clipboard that is used for internal copy/paste actions.
     */
    public ClearClipboardAction(InternalCellClipboard clipboard) {
        this.clipboard = clipboard;
    }

    @Override
    public void run(NatTable natTable, KeyEvent event) {
        this.clipboard.clear();
        natTable.fireLayerEvent(new VisualRefreshEvent(natTable));
    }

}
