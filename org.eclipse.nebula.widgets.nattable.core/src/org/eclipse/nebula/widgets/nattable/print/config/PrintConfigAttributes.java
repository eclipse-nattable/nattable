/*******************************************************************************
 * Copyright (c) 2016 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.print.config;

import org.eclipse.nebula.widgets.nattable.config.Direction;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.IStyle;

/**
 * Configuration attributes that are used to configure printing.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 *
 * @since 1.5
 */
public interface PrintConfigAttributes {

    /**
     * Configuration attribute to configure the scaling mode on printing.
     * <ul>
     * <li>{@link Direction#NONE} - no content related scaling, simple DPI
     * scaling (default)</li>
     * <li>{@link Direction#HORIZONTAL} - the content is scaled so that all
     * columns are printed on one page</li>
     * <li>{@link Direction#VERTICAL} - the content is scaled so that all rows
     * are printed on one page</li>
     * <li>{@link Direction#BOTH} - the content is scaled so that all columns
     * and rows are printed on one page</li>
     * </ul>
     */
    ConfigAttribute<Direction> FITTING_MODE = new ConfigAttribute<Direction>();

    /**
     * Configuration attribute to configure a scaling option in case
     * {@link #FITTING_MODE} is set. By default only downscaling is supported on
     * enabling the fit-to-page scaling. By setting {@link #STRETCH} to
     * <code>true</code> also upscaling will be performed to make the most out
     * of the available space.
     * <p>
     * <b>Note:</b> stretching is only supported for fitting mode
     * {@link Direction#HORIZONTAL}.
     * </p>
     */
    ConfigAttribute<Boolean> STRETCH = new ConfigAttribute<Boolean>();

    /**
     * Configuration attribute to configure the date format that is used for
     * rendering the print date in the footer region. If not specified the
     * default value <i>EEE, d MMM yyyy HH:mm a</i> will be used.
     */
    ConfigAttribute<String> DATE_FORMAT = new ConfigAttribute<String>();

    /**
     * Configuration attribute to configure the height of the footer. Needs to
     * be specified in printer DPI value. If not set the default value 300 will
     * be used.
     */
    ConfigAttribute<Integer> FOOTER_HEIGHT = new ConfigAttribute<Integer>();

    /**
     * Configuration attribute to configure the style that should be used to
     * print the footer. Currently only background color, foreground color and
     * font style attributes are supported.
     */
    ConfigAttribute<IStyle> FOOTER_STYLE = new ConfigAttribute<IStyle>();

    /**
     * Configuration attribute to configure the pattern for rendering the page
     * information in the footer. The pattern can include placeholders for the
     * current page and the total page count, where {0} = current page and {1} =
     * total page count, e.g. <i>Page {0}/{1}</i> to show <i>Page 1/6</i>
     */
    ConfigAttribute<String> FOOTER_PAGE_PATTERN = new ConfigAttribute<String>();
}
