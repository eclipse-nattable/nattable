/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
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
