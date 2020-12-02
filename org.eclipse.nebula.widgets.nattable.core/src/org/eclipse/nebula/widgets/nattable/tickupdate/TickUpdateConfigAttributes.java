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
package org.eclipse.nebula.widgets.nattable.tickupdate;

import org.eclipse.nebula.widgets.nattable.edit.gui.TickUpdateCellEditDialog;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;

/**
 * The configuration attributes for tick updates.
 */
public final class TickUpdateConfigAttributes {

    private TickUpdateConfigAttributes() {
        // private default constructor for helper class
    }

    /**
     * The configuration attribute for registering the
     * {@link ITickUpdateHandler} to use.
     */
    public static final ConfigAttribute<ITickUpdateHandler> UPDATE_HANDLER = new ConfigAttribute<>();

    /**
     * The configuration attribute to configure how the tick updates should be
     * usable in the {@link TickUpdateCellEditDialog}. If there is no value
     * registered for this attribute the dialog will be opened with this value
     * set to <code>false</code>.
     */
    public static final ConfigAttribute<Boolean> USE_ADJUST_BY = new ConfigAttribute<>();
}
