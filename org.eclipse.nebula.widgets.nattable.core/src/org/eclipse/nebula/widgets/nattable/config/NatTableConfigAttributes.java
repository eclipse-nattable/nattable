/*******************************************************************************
 * Copyright (c) 2020 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.config;

import org.eclipse.nebula.widgets.nattable.layer.IDpiConverter;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;

/**
 * Configuration attributes to configure top level NatTable configurations, e.g.
 * dpi converter for scaling.
 *
 * @since 2.0
 */
public final class NatTableConfigAttributes {

    /**
     * Configuration attribute for registering an {@link IDpiConverter} to
     * convert dimensions horizontally.
     */
    public static final ConfigAttribute<IDpiConverter> HORIZONTAL_DPI_CONVERTER = new ConfigAttribute<IDpiConverter>();

    /**
     * Configuration attribute for registering an {@link IDpiConverter} to
     * convert dimensions vertically.
     */
    public static final ConfigAttribute<IDpiConverter> VERTICAL_DPI_CONVERTER = new ConfigAttribute<IDpiConverter>();

    /**
     * Configuration attribute for registering the font scaling factor in case
     * scaling is active.
     */
    public static final ConfigAttribute<Float> FONT_SCALING_FACTOR = new ConfigAttribute<Float>();

    private NatTableConfigAttributes() {
        // empty default constructor
    }
}
