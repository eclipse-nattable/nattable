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
package org.eclipse.nebula.widgets.nattable.fillhandle.config;

import java.util.Calendar;

import org.eclipse.nebula.widgets.nattable.config.Direction;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.swt.graphics.Color;

/**
 * This interface contains {@link ConfigAttribute}s that can be used to
 * configure the fill handle behavior.
 *
 * @since 1.4
 */
public final class FillHandleConfigAttributes {

    private FillHandleConfigAttributes() {
        // private default constructor for constants class
    }

    /**
     * ConfigAttribute to configure the line style used to render a special
     * border on dragging the fill handle.
     */
    public static final ConfigAttribute<BorderStyle> FILL_HANDLE_REGION_BORDER_STYLE = new ConfigAttribute<>();

    /**
     * ConfigAttribute to configure the border style of the fill handle itself.
     */
    public static final ConfigAttribute<BorderStyle> FILL_HANDLE_BORDER_STYLE = new ConfigAttribute<>();

    /**
     * ConfigAttribute to configure the color of the fill handle.
     */
    public static final ConfigAttribute<Color> FILL_HANDLE_COLOR = new ConfigAttribute<>();

    /**
     * ConfigAttribute to configure the date field that should be incremented
     * when inserting a series via fill handle. Fields from the {@link Calendar}
     * class should be used for configuration.
     */
    public static final ConfigAttribute<Integer> INCREMENT_DATE_FIELD = new ConfigAttribute<>();

    /**
     * ConfigAttribute to configure the directions that are allowed for the fill
     * handle. If nothing is specified {@link Direction#BOTH} will be used
     * implicitly.
     */
    public static final ConfigAttribute<Direction> ALLOWED_FILL_DIRECTION = new ConfigAttribute<>();
}
