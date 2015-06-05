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
 * Returns the remainder from a division between two supplied numbers.
 *
 * @since 1.4
 */
public class ModFunction extends AbstractMathFunction {

    public ModFunction() {
        super();
    }

    public ModFunction(List<FunctionValue> values) {
        super(values);

        if (values.size() > 2) {
            throw new FunctionException("#N/A", //$NON-NLS-1$
            Messages.getString("FormulaParser.error.wrongNumberOfArguments", new Object[] { 2, values.size() })); //$NON-NLS-1$
        }
    }

    @Override
    public BigDecimal getValue() {
        if (this.values.size() != 2) {
            throw new FunctionException("#N/A", //$NON-NLS-1$
            Messages.getString("FormulaParser.error.wrongNumberOfArguments", new Object[] { 2, this.values.size() })); //$NON-NLS-1$
        }

        BigDecimal number = convertValue(this.values.get(0).getValue());
        BigDecimal divisor = convertValue(this.values.get(1).getValue());
        if (BigDecimal.ZERO.equals(divisor)) {
            throw new FunctionException("#DIV/0!", Messages.getString("FormulaParser.error.divisionByZero")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return number.remainder(divisor);
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "%"; //$NON-NLS-1$
        }
        else if (this.values.size() == 1) {
            return this.values.get(0) + " %"; //$NON-NLS-1$
        }
        else {
            return this.values.get(0) + " % " + this.values.get(1); //$NON-NLS-1$
        }
    }
}
