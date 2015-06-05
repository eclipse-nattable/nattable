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
import java.util.List;

import org.eclipse.nebula.widgets.nattable.Messages;

/**
 * Returns the positive square root of a given number.
 *
 * @since 1.4
 */
public class SquareRootFunction extends AbstractMathSingleValueFunction {

    public SquareRootFunction() {
        super();
    }

    public SquareRootFunction(FunctionValue value) {
        super(value);
    }

    public SquareRootFunction(List<FunctionValue> values) {
        super(values);

        if (!this.values.isEmpty()) {
            BigDecimal converted = convertValue(this.values.get(0).getValue());
            if (converted.signum() < 0) {
                throw new FunctionException("#NUM!", Messages.getString("FormulaParser.error.negativeValue")); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    @Override
    public void addFunctionValue(FunctionValue value) {
        BigDecimal converted = convertValue(value.getValue());
        if (converted.signum() < 0) {
            throw new FunctionException("#NUM!", Messages.getString("FormulaParser.error.negativeValue")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        super.addFunctionValue(value);
    }

    @Override
    public BigDecimal getValue() {
        BigDecimal value = convertValue(getSingleValue().getValue());
        BigDecimal x = new BigDecimal(Math.sqrt(value.doubleValue()));
        return x.add(new BigDecimal(value.subtract(x.multiply(x)).doubleValue() / (x.doubleValue() * 2.0)));
    }

    @Override
    public String toString() {
        if (this.values.isEmpty()) {
            return "²"; //$NON-NLS-1$
        }
        else {
            return getSingleValue() + "²"; //$NON-NLS-1$
        }
    }
}
