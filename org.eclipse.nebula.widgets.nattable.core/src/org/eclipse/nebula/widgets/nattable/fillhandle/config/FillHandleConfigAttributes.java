/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
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
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 *
 * @since 1.4
 */
public interface FillHandleConfigAttributes {

    /**
     * ConfigAttribute to configure the line style used to render a special
     * border on dragging the fill handle.
     */
    ConfigAttribute<BorderStyle> FILL_HANDLE_REGION_BORDER_STYLE = new ConfigAttribute<BorderStyle>();

    /**
     * ConfigAttribute to configure the border style of the fill handle itself.
     */
    ConfigAttribute<BorderStyle> FILL_HANDLE_BORDER_STYLE = new ConfigAttribute<BorderStyle>();

    /**
     * ConfigAttribute to configure the color of the fill handle.
     */
    ConfigAttribute<Color> FILL_HANDLE_COLOR = new ConfigAttribute<Color>();

    /**
     * ConfigAttribute to configure the date field that should be incremented
     * when inserting a series via fill handle. Fields from the {@link Calendar}
     * class should be used for configuration.
     */
    ConfigAttribute<Integer> INCREMENT_DATE_FIELD = new ConfigAttribute<Integer>();

    /**
     * ConfigAttribute to configure the directions that are allowed for the fill
     * handle. If nothing is specified {@link Direction#BOTH} will be used
     * implicitly.
     */
    ConfigAttribute<Direction> ALLOWED_FILL_DIRECTION = new ConfigAttribute<Direction>();
}
