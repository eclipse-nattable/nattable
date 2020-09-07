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
package org.eclipse.nebula.widgets.nattable.data.convert;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * Converts the display value to a {@link BigDecimal} and vice versa.
 */
public class DefaultBigDecimalDisplayConverter extends DecimalNumericDisplayConverter {

    public DefaultBigDecimalDisplayConverter() {
        this.nf.setMinimumFractionDigits(0);
        ((DecimalFormat) this.nf).setParseBigDecimal(true);
    }

    @Override
    protected Object convertToNumericValue(String value) {
        try {
            return this.nf.parse(value);
        } catch (ParseException e) {
            throw new NumberFormatException(e.getLocalizedMessage());
        }
    }
}
