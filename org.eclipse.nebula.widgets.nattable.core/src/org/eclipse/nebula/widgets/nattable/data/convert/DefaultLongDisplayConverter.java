/*******************************************************************************
 * Copyright (c) 2012, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.data.convert;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Converts the display value to a long and vice versa.
 */
public class DefaultLongDisplayConverter extends NumericDisplayConverter {

    /**
     * Creates a converter that uses {@link NumberFormat}.
     */
    public DefaultLongDisplayConverter() {
        this(true);
    }

    /**
     * Creates a converter and allows to specify whether {@link NumberFormat}
     * should be used or not.
     *
     * @param useNumberFormat
     *            <code>true</code> if a {@link NumberFormat} should be used,
     *            <code>false</code> if not.
     * @since 1.4
     */
    public DefaultLongDisplayConverter(boolean useNumberFormat) {
        if (!useNumberFormat) {
            this.nf = null;
        }
    }

    @Override
    protected Object convertToNumericValue(String value) {
        if (this.nf != null) {
            try {
                return this.nf.parse(value).longValue();
            } catch (ParseException e) {
                throw new NumberFormatException(e.getLocalizedMessage());
            }
        }
        return Long.valueOf(value);
    }

}
