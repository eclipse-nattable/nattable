/*******************************************************************************
 * Copyright (c) 2014, 2015 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.data.convert;

import java.text.NumberFormat;

/**
 * Abstract base class for decimal converters.
 */
public abstract class DecimalNumericDisplayConverter extends NumericDisplayConverter {

    /**
     * Creates a converter that uses {@link NumberFormat}.
     */
    public DecimalNumericDisplayConverter() {
        this(true);
    }

    /**
     * Creates a converter and allows to specify whether {@link NumberFormat}
     * should be used or not.
     *
     * @param useNumberFormat
     *            <code>true</code> if a {@link NumberFormat} should be used,
     *            <code>false</code> if not.
     * 
     * @since 1.4
     */
    public DecimalNumericDisplayConverter(boolean useNumberFormat) {
        if (useNumberFormat) {
            this.nf.setMinimumFractionDigits(1);
            this.nf.setMaximumFractionDigits(2);
        } else {
            this.nf = null;
        }
    }

    /**
     * Sets the minimum number of digits allowed in the fraction portion of a
     * number.
     *
     * @param newValue
     *            newValue the minimum number of fraction digits to be shown; if
     *            less than zero, then zero is used.
     *
     * @see NumberFormat#setMinimumFractionDigits(int)
     */
    public void setMinimumFractionDigits(int newValue) {
        if (this.nf != null) {
            this.nf.setMinimumFractionDigits(newValue);
        }
    }

    /**
     * Sets the maximum number of digits allowed in the fraction portion of a
     * number.
     *
     * @param newValue
     *            newValue the maximum number of fraction digits to be shown; if
     *            less than zero, then zero is used.
     *
     * @see NumberFormat#setMaximumFractionDigits(int)
     */
    public void setMaximumFractionDigits(int newValue) {
        if (this.nf != null) {
            this.nf.setMaximumFractionDigits(newValue);
        }
    }
}
