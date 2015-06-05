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

/**
 * Returns the average of a list of supplied numbers.
 * 
 * @since 1.4
 */
public class AverageFunction extends SumFunction {

    public AverageFunction() {
        super();
    }

    public AverageFunction(List<FunctionValue> values) {
        super(values);
    }

    @Override
    public BigDecimal getValue() {
        BigDecimal sum = super.getValue();
        if (!this.values.isEmpty()) {
            return sum.divide(new BigDecimal(this.values.size()));
        }
        return sum;
    }

}
