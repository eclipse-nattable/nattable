/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
     * data type of the column
     */
    public static final ConfigAttribute<IDisplayConverter> FILTER_DISPLAY_CONVERTER = new ConfigAttribute<IDisplayConverter>();

}
