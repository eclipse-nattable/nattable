/*******************************************************************************
 * Copyright (c) 2014, 2020 Roman Flueckiger.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Roman Flueckiger <roman.flueckiger@mac.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.ui.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.ui.NatEventData;

/**
 * A helper class for managing {@link IMenuItemState}s in the
 * {@link PopupMenuBuilder}.
 */
public class MenuItemStateMap {

    /**
     * All currently registered states.
     */
    protected Map<String, List<IMenuItemState>> states;

    /**
     * Checks if all the registered states for the given id are active.
     *
     * @param id
     *            the id identifying the menu item to be checked for activeness.
     * @param natEventData
     *            NatTable location information where the popup menu was
     *            requested.
     * @return <code>true</code> if ALL states registered for the given id are
     *         active OR no states exist. Otherwise <code>false</code>.
     */
    protected boolean isActive(String id, NatEventData natEventData) {
        if (this.states != null) {
            List<IMenuItemState> states = this.states.get(id);
            if (states != null) {
                for (IMenuItemState state : states) {
                    if (!state.isActive(natEventData)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Add an additional state for the given id.
     *
     * @param id
     *            the id identifying the menu item to be associated with the
     *            given state.
     * @param state
     *            the state that will be called to check for activeness.
     */
    public void addMenuItemState(String id, IMenuItemState state) {
        if (this.states == null) {
            this.states = new HashMap<String, List<IMenuItemState>>();
        }

        List<IMenuItemState> states = this.states.get(id);
        if (states == null) {
            states = new ArrayList<IMenuItemState>();
            this.states.put(id, states);
        }

        states.add(state);
    }

}
