/*******************************************************************************
 * Copyright (c) 2023 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.extension.nebula.richtext;

import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;

/**
 * {@link ConfigAttribute}s specifically for configuring the
 * {@link RichTextCellPainter}.
 *
 * @since 2.1
 */
public final class RichTextConfigAttributes {

    private RichTextConfigAttributes() {
        // private default constructor for constants class
    }

    /**
     * Attribute for configuring the markup IDisplayConverter that should be
     * used to convert the data in a cell for HTML rendering.
     *
     * @since 2.1
     */
    public static final ConfigAttribute<IDisplayConverter> MARKUP_DISPLAY_CONVERTER = new ConfigAttribute<>();

}
