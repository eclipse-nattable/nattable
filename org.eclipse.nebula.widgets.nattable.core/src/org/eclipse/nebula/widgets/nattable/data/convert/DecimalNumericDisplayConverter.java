/*******************************************************************************
 * Copyright (c) 2014 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.data.convert;

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotNull;

import java.text.NumberFormat;

/**
 * @author Dirk Fauth
 *
 */
public abstract class DecimalNumericDisplayConverter extends
        NumericDisplayConverter {

    public DecimalNumericDisplayConverter() {
        this.nf.setMinimumFractionDigits(1);
        this.nf.setMaximumFractionDigits(2);
    }

    @Override
    public Object canonicalToDisplayValue(Object canonicalValue) {
        try {
            if (isNotNull(canonicalValue)) {
                return nf.format(canonicalValue);
            }
            return null;
        } catch (Exception e) {
            return canonicalValue;
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
        this.nf.setMinimumFractionDigits(newValue);
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
        this.nf.setMaximumFractionDigits(newValue);
    }
}
