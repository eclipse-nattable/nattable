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
 * {@link FunctionValue} that carries multiple {@link FunctionValue}s. Is used
 * to process range function arguments.
 *
 * @since 1.4
 */
public class MultipleValueFunctionValue implements FunctionValue {

    protected List<FunctionValue> values;

    public MultipleValueFunctionValue() {
        this(new ArrayList<FunctionValue>());
    }

    public MultipleValueFunctionValue(List<FunctionValue> values) {
        if (values == null) {
            throw new IllegalStateException("values can not be null"); //$NON-NLS-1$
        }
        this.values = values;
    }

    /**
     * Add a {@link FunctionValue} to the local list of {@link FunctionValue}s
     * this {@link MultipleValueFunctionValue} carries.
     * 
     * @param value
     *            The value to add.
     */
    public void addValue(FunctionValue value) {
        this.values.add(value);
    }

    @Override
    public List<FunctionValue> getValue() {
        return this.values;
    }

}
