/*******************************************************************************
 * Copyright (c) 2012, 2014 Original authors and others.
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

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Menu;

public class PopupMenuAction implements IMouseAction {

    private Menu menu;

    public PopupMenuAction(Menu menu) {
        this.menu = menu;
    }

    @Override
    public void run(NatTable natTable, MouseEvent event) {
        this.menu.setData(MenuItemProviders.NAT_EVENT_DATA_KEY, event.data);
        this.menu.setVisible(true);
    }
}
