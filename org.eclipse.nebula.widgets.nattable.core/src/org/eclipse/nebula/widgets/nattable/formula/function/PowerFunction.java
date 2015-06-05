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
import java.util.Iterator;
import java.util.List;

/**
 * Returns the result of a given number raised to a supplied power.
 *
 * @since 1.4
 */
public class PowerFunction extends AbstractMathFunction {

    public PowerFunction() {
        super();
    }

    public PowerFunction(List<FunctionValue> values) {
        super(values);
    }

    @Override
    public BigDecimal getValue() {
        if (this.values.isEmpty()) {
            return new BigDecimal(0);
        }

        BigDecimal result = null;
        for (FunctionValue value : this.values) {
            if (result == null) {
                result = convertValue(value.getValue());
            }
            else {
                result = result.pow(convertValue(value.getValue()).intValue());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "^"; //$NON-NLS-1$
        }
        else if (this.values.size() == 1) {
            return "^ " + this.values.get(0); //$NON-NLS-1$
        }
        else {
            StringBuilder builder = new StringBuilder();
            for (Iterator<FunctionValue> it = this.values.iterator(); it.hasNext();) {
                FunctionValue v = it.next();
                builder.append(v);
                if (it.hasNext()) {
                    builder.append(" ^ "); //$NON-NLS-1$
                }
            }
            return builder.toString();
        }
    }
}
