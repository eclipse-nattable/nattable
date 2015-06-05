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

import java.util.ArrayList;
import java.util.List;

/**
 * Subclasses are intended to process values that are set to it.
 * 
 * @since 1.4
 */
public abstract class AbstractFunction implements FunctionValue, OperatorFunctionValue {

    protected List<FunctionValue> values;

    public AbstractFunction() {
        this(new ArrayList<FunctionValue>());
    }

    public AbstractFunction(List<FunctionValue> values) {
        if (values == null) {
            throw new IllegalStateException("values can not be null"); //$NON-NLS-1$
        }
        this.values = values;
    }

    @Override
    public void addFunctionValue(FunctionValue value) {
        if (value != null) {
            if (value instanceof MultipleValueFunctionValue) {
                this.values.addAll(((MultipleValueFunctionValue) value).getValue());
            }
            else {
                this.values.add(value);
            }
        }
    }

    /**
     *
     * @return <code>true</code> if no values to process are added to this
     *         function, <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return this.values.isEmpty();
    }
}
