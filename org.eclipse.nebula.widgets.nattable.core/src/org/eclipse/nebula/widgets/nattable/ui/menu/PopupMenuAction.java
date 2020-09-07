/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - changed key for NatEventData
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.menu;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Menu;

/**
 * {@link IMouseAction} to open a {@link Menu}.
 */
public class PopupMenuAction implements IMouseAction {

    private Menu menu;
    private MenuManager menuManager;

    /**
     * Creates a {@link PopupMenuAction} that opens the given {@link Menu}.
     *
     * @param menu
     *            The {@link Menu} that should be opened by this action.
     */
    public PopupMenuAction(Menu menu) {
        if (menu == null) {
            throw new IllegalArgumentException("menu can not be null"); //$NON-NLS-1$
        }
        this.menu = menu;
    }

    /**
     * Creates a {@link PopupMenuAction} that creates and shows a {@link Menu}
     * that is created everytime by the given {@link MenuManager}
     *
     * @param menuManager
     *            The {@link MenuManager} that should be used to create the
     *            context menu.
     *
     * @since 1.6
     */
    public PopupMenuAction(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        if (this.menuManager != null) {
            if (this.menu != null) {
                this.menu.dispose();
            } else {
                natTable.addDisposeListener(e -> {
                    if (PopupMenuAction.this.menu != null
                            && !PopupMenuAction.this.menu.isDisposed()) {
                        PopupMenuAction.this.menu.dispose();
                    }
                });
            }
            this.menu = this.menuManager.createContextMenu(natTable);
        }
        this.menu.setData(MenuItemProviders.NAT_EVENT_DATA_KEY, event.data);
        this.menu.setVisible(true);
    }
}
