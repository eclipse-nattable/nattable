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
package org.eclipse.nebula.widgets.nattable.freeze;

import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.swt.graphics.Color;

/**
 * Configuration attributes for the freeze separator visualization.
 *
 * @since 2.0
 */
public final class FreezeConfigAttributes {

    private FreezeConfigAttributes() {
        // private default constructor for constants class
    }

    /**
     * Configuration attribute to configure the color of the separator line.
     */
    public static final ConfigAttribute<Color> SEPARATOR_COLOR = new ConfigAttribute<>();

    /**
     * Configuration attribute to configure the width of the separator line.
     *
     * @since 1.6
     */
    public static final ConfigAttribute<Integer> SEPARATOR_WIDTH = new ConfigAttribute<>();

}
