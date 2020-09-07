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

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotEmpty;
import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotNull;

import java.text.NumberFormat;

import org.eclipse.nebula.widgets.nattable.Messages;

/**
 * Converts the display value to a double and vice versa.
 */
public abstract class NumericDisplayConverter extends DisplayConverter {

    protected NumberFormat nf = NumberFormat.getInstance();

    @Override
    public Object canonicalToDisplayValue(Object canonicalValue) {
        try {
            if (isNotNull(canonicalValue)) {
                if (this.nf != null) {
                    return this.nf.format(canonicalValue);
                }
                return canonicalValue.toString();
            }
            return null;
        } catch (Exception e) {
            return canonicalValue;
        }
    }

    @Override
    public Object displayToCanonicalValue(Object displayValue) {
        try {
            if (isNotNull(displayValue) && isNotEmpty(displayValue.toString())) {
                return convertToNumericValue(displayValue.toString().trim());
            }
            return null;
        } catch (Exception e) {
            throw new ConversionFailedException(Messages.getString("NumericDisplayConverter.failure", //$NON-NLS-1$
                    displayValue), e);
        }
    }

    protected abstract Object convertToNumericValue(String value);

    /**
     *
     * @return The {@link NumberFormat} that is used to format numeric values.
     */
    public NumberFormat getNumberFormat() {
        return this.nf;
    }

    /**
     *
     * @param nf
     *            The {@link NumberFormat} that should be used to format numeric
     *            values.
     */
    public void setNumberFormat(NumberFormat nf) {
        this.nf = nf;
    }

}
