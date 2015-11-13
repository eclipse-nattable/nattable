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
package org.eclipse.nebula.widgets.nattable.data.convert;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * Converts the display value to a {@link BigDecimal} and vice versa.
 */
public class DefaultBigDecimalDisplayConverter extends
        DecimalNumericDisplayConverter {

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
