/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *		Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
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
        this.value = new BigDecimal(value);
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
