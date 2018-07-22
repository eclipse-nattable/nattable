/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
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
package org.eclipse.nebula.widgets.nattable.hideshow.indicator;

import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.swt.graphics.Color;

/**
 * Configuration attributes for the hide indicator rendering.
 *
 * @see HideIndicatorOverlayPainter
 *
 * @since 1.6
 */
public final class HideIndicatorConfigAttributes {

    /**
     * Configuration attribute for configuring the line width of the hide
     * indicator.
     */
    public static final ConfigAttribute<Integer> HIDE_INDICATOR_LINE_WIDTH = new ConfigAttribute<Integer>();

    /**
     * Configuration attribute for configuring the color of the hide indicator.
     */
    public static final ConfigAttribute<Color> HIDE_INDICATOR_COLOR = new ConfigAttribute<Color>();

    private HideIndicatorConfigAttributes() {
        // empty constructor for constants class
    }
}
