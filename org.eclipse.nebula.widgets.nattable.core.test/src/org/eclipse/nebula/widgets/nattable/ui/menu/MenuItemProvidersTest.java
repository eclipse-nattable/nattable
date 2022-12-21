/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.ui.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;

public class MenuItemProvidersTest {

    @Test
    public void shouldGetDataFromParentForNestedMenus() {
        Display display = Display.getDefault();
        Shell shell = new Shell(display);

        Menu menu = new Menu(shell, SWT.POP_UP);
        menu.setData(MenuItemProviders.NAT_EVENT_DATA_KEY, new NatEventData(null, null, 5, 1, null));

        MenuItem item1 = new MenuItem(menu, SWT.PUSH);
        item1.setText("Push Item");

        MenuItem item2 = new MenuItem(menu, SWT.CASCADE);
        item2.setText("Cascade Item");

        Menu subMenu = new Menu(menu);
        item2.setMenu(subMenu);

        MenuItem subItem1 = new MenuItem(subMenu, SWT.PUSH);
        subItem1.setText("Subitem 1");

        MenuItem subItem2 = new MenuItem(subMenu, SWT.PUSH);
        subItem2.setText("Subitem 2");

        Event testEvent = new Event();
        testEvent.widget = shell;
        SelectionEvent selectionEvent = new SelectionEvent(testEvent);
        selectionEvent.widget = subItem2;

        NatEventData natEventData = MenuItemProviders.getNatEventData(selectionEvent);
        assertNotNull(natEventData);
        assertEquals(5, natEventData.getColumnPosition());
        assertEquals(1, natEventData.getRowPosition());
    }
}
