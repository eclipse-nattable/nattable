/*******************************************************************************
 * Copyright (c) 2014 Roman Flueckiger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Roman Flueckiger <roman.flueckiger@mac.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.menu;

import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.swt.widgets.MenuItem;

/**
 * <p>
 * Implementors represent a dynamic state of a {@link MenuItem} provided by a
 * {@link IMenuItemProvider}.
 * </p>
 * <p>
 * See also:<br>
 * {@link PopupMenuBuilder#withEnabledState(String, IMenuItemState)}<br>
 * {@link PopupMenuBuilder#withVisibleState(String, IMenuItemState)}
 * </p>
 */
public interface IMenuItemState {

    /**
     * Return whether this state is currently active or not. Active means, that
     * the menu item property this state is associated with will apply. E.g. if
     * the state is used in conjunction with 'enablement', active means the menu
     * item is to be enabled. This method is called every time the popup menu is
     * shown.
     *
     * @param natEventData
     *            NatTable location information where the popup menu was
     *            requested.
     * @return <code>true</code> if the state is active.
     */
    boolean isActive(NatEventData natEventData);

}
