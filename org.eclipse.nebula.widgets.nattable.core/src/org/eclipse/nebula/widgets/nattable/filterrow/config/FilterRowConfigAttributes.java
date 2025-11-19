/*******************************************************************************
 * Copyright (c) 2012, 2025 Original authors and others.
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
import java.util.List;
import java.util.function.Function;

import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.filterrow.TextMatchingMode;
import org.eclipse.nebula.widgets.nattable.filterrow.combobox.FilterRowCategoryValueMapper;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;

/**
 * Attributes used to configure the filter row behavior.
 *
 * @see DefaultFilterRowConfiguration
 */
public final class FilterRowConfigAttributes {

    private FilterRowConfigAttributes() {
        // private default constructor for constants class
    }

    public static final ConfigAttribute<String> TEXT_DELIMITER = new ConfigAttribute<>();
    public static final ConfigAttribute<TextMatchingMode> TEXT_MATCHING_MODE = new ConfigAttribute<>();

    /** Comparator to be used for threshold matching */
    public static final ConfigAttribute<Comparator<?>> FILTER_COMPARATOR = new ConfigAttribute<>();

    /**
     * Display converter used to convert the string typed by the user to the
     * data type of the column or in case of combo boxes to convert the filter
     * object to string.
     */
    public static final ConfigAttribute<IDisplayConverter> FILTER_DISPLAY_CONVERTER = new ConfigAttribute<>();

    /**
     * Display converter that is used for text filter operations to convert the
     * body cell content to a string. Typically the same converter that is used
     * for rendering in the body.
     *
     * @since 2.0
     */
    public static final ConfigAttribute<IDisplayConverter> FILTER_CONTENT_DISPLAY_CONVERTER = new ConfigAttribute<>();

    /**
     * Flag to configure whether collection values in the cell should be
     * flattened when using the combobox filter row.
     *
     * @since 2.7
     */
    public static final ConfigAttribute<Boolean> FLATTEN_COLLECTION_VALUES = new ConfigAttribute<>();

    /**
     * {@link Function} that maps a {@link String} that contains a collection of
     * values to a {@link List} of trimmed {@link String}s.
     *
     * @since 2.7
     */
    public static final ConfigAttribute<Function<? super Object, ? extends Object>> LIST_VALUE_MAP_FUNCTION = new ConfigAttribute<>();

    /**
     * {@link FilterRowCategoryValueMapper} that is used to map values in a
     * filter collection to a category. If a
     * {@link FilterRowCategoryValueMapper} is set,
     * {@link #FLATTEN_COLLECTION_VALUES} is automatically treated as
     * <code>true</code>.
     *
     * @see #USE_CATEGORIES_ONLY
     *
     * @since 2.7
     */
    public static final ConfigAttribute<FilterRowCategoryValueMapper<?>> CATEGORY_VALUE_MAPPER = new ConfigAttribute<>();

    /**
     * Flag to configure whether values in the filter combobox should be
     * replaced by the categories mapped via {@link #CATEGORY_VALUE_MAPPER} or
     * if the categories should be added to the available values. If not set it
     * will be treated as <code>false</code>.
     *
     * @see CATEGORY_VALUE_MAPPER
     *
     * @since 2.7
     */
    public static final ConfigAttribute<Boolean> USE_CATEGORIES_ONLY = new ConfigAttribute<>();
}
