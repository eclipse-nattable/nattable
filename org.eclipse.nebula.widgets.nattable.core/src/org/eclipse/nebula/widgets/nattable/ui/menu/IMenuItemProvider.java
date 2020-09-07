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
package org.eclipse.nebula.widgets.nattable.ui.menu;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.widgets.Menu;

public interface IMenuItemProvider {

    /**
     * Add an item to the popup menu.
     *
     * @param natTable
     *            active table instance.
     * @param popupMenu
     *            the SWT {@link Menu} which popus up.
     */
    public void addMenuItem(final NatTable natTable, final Menu popupMenu);

}
