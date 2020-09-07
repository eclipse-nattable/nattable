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

/**
 * Implementations of this interface specify a value of a function. This can be
 * a parameter, a function or an operator.
 *
 * @since 1.4
 */
public interface FunctionValue {

    /**
     * @return The value this {@link FunctionValue} carries or calculates based
     *         on its function.
     */
    Object getValue();
}
