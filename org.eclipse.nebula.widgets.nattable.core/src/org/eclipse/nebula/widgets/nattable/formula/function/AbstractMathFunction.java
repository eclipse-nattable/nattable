/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.formula.function;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.Messages;

/**
 * Subclass of this class are intended to perform mathematical operations on the
 * values set to it. It supports conversion of values to {@link BigDecimal} and
 * ensures that only valid {@link FunctionValue}s are accepted.
 *
 * @since 1.4
 */
public abstract class AbstractMathFunction extends AbstractFunction {

    public AbstractMathFunction() {
        super();
    }

    public AbstractMathFunction(List<FunctionValue> values) {
        super(values);
        validateMethodParameter(values);
    }

    @Override
    public void addFunctionValue(FunctionValue value) {
        if (value instanceof StringFunctionValue) {
            throw new FunctionException("#VALUE!", //$NON-NLS-1$
                    Messages.getString("FormulaParser.error.invalidNumberValue", value.getValue())); //$NON-NLS-1$
        }

        if (value instanceof MultipleValueFunctionValue) {
            List<FunctionValue> multi = ((MultipleValueFunctionValue) value).getValue();
            validateMethodParameter(multi);
        }
        super.addFunctionValue(value);
    }

    /**
     * Converts a given value to a {@link BigDecimal}.
     *
     * @param value
     *            The object to convert.
     * @return The {@link BigDecimal} representation of the given object.
     * @throws NumberFormatException
     *             is the given value can not be converted to a
     *             {@link BigDecimal}
     */
    protected BigDecimal convertValue(Object value) {
        if (value instanceof FunctionValue) {
            value = ((FunctionValue) value).getValue();
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            return new BigDecimal(((Number) value).doubleValue());
        }
        return new BigDecimal(value.toString());
    }

    /**
     * Performs a type check for the given list of {@link FunctionValue}s and
     * throws a {@link FunctionException} in case a {@link StringFunctionValue}
     * is contained.
     *
     * @param values
     *            The list of {@link FunctionValue} that should be checked.
     *
     * @throws FunctionException
     *             if a {@link StringFunctionValue} is detected.
     */
    protected void validateMethodParameter(List<FunctionValue> values) {
        for (FunctionValue value : values) {
            if (value instanceof StringFunctionValue) {
                throw new FunctionException("#VALUE!", //$NON-NLS-1$
                        Messages.getString("FormulaParser.error.invalidNumberValue", value.getValue())); //$NON-NLS-1$
            }
        }
    }
}
