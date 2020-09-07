/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.util;

/**
 * Interface that is used by the CalculatedValueCache to calculate values in a
 * background process. In a modern world this would be better implemented as a
 * function/lambda, but as we need to work with Java 6, there are no lambdas yet
 * and so we need to work with anonymous inner classes.
 *
 * @see CalculatedValueCache
 */
public interface ICalculator {

    /**
     * Will execute the calculation of a value.
     *
     * @return The value that is calculated in a background process.
     */
    Object executeCalculation();
}