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
package org.eclipse.nebula.widgets.nattable.filterrow.config;

import java.util.Comparator;

import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;

/**
 * Attributes used to configure the filter row behavior.
 *
 * @see DefaultFilterRowConfiguration
 */
public interface FilterRowConfigAttributes {

    public static final ConfigAttribute<String> TEXT_DELIMITER = new ConfigAttribute<String>();
    public static final ConfigAttribute<TextMatchingMode> TEXT_MATCHING_MODE = new ConfigAttribute<TextMatchingMode>();

    /** Comparator to be used for threshold matching */
    public static final ConfigAttribute<Comparator<?>> FILTER_COMPARATOR = new ConfigAttribute<Comparator<?>>();

    /**
     * Display converter used to convert the string typed by the user to the
     * data type of the column or in case of combo boxes to convert the filter
     * object to string.
     */
    public static final ConfigAttribute<IDisplayConverter> FILTER_DISPLAY_CONVERTER = new ConfigAttribute<IDisplayConverter>();

    /**
     * Display converter that is used for text filter operations to convert the
     * body cell content to a string. Typically the same converter that is used
     * for rendering in the body.
     *
     * @since 2.0
     */
    public static final ConfigAttribute<IDisplayConverter> FILTER_CONTENT_DISPLAY_CONVERTER = new ConfigAttribute<IDisplayConverter>();
}
