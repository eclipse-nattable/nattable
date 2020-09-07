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
import java.util.List;

/**
 * Negates the given value.
 *
 * @since 1.4
 */
public class NegateFunction extends AbstractMathSingleValueFunction {

    public NegateFunction() {
        super();
    }

    public NegateFunction(FunctionValue value) {
        super(value);
    }

    public NegateFunction(List<FunctionValue> values) {
        super(values);
    }

    @Override
    public BigDecimal getValue() {
        return convertValue(getSingleValue().getValue()).negate();
    }

    @Override
    public String toString() {
        if (this.values.isEmpty()) {
            return "-"; //$NON-NLS-1$
        } else {
            return "- " + getSingleValue(); //$NON-NLS-1$
        }
    }
}
