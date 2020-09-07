/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.formula.function;

import java.math.BigDecimal;

/**
 * {@link FunctionValue} that carries a {@link BigDecimal} for calculations.
 *
 * @since 1.4
 */
public class BigDecimalFunctionValue implements FunctionValue {

    private final BigDecimal value;

    public BigDecimalFunctionValue(long value) {
        this.value = new BigDecimal(value);
    }

    public BigDecimalFunctionValue(double value) {
        this.value = BigDecimal.valueOf(value);
    }

    public BigDecimalFunctionValue(String value) {
        this.value = new BigDecimal(value.trim());
    }

    public BigDecimalFunctionValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public BigDecimal getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value.toString();
    }
}
