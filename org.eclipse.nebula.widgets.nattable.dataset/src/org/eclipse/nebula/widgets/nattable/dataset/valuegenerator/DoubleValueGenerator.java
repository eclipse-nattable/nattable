/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.dataset.valuegenerator;

import java.util.Random;

import org.eclipse.nebula.widgets.nattable.dataset.generator.IValueGenerator;

public class DoubleValueGenerator implements IValueGenerator {

    private int floor;

    private int range;

    /**
     * Generates random double values such that: (floor) &lt;= value &lt; (floor
     * + range)
     *
     * @param floor
     *            Minimum value to be returned by this value generator. May be
     *            positive or negative.
     * @param range
     *            Indicates size of the range of values to be returned by this
     *            generator. Must be &gt; 0.
     */
    public DoubleValueGenerator(int floor, int range) {
        if (range <= 0) {
            throw new IllegalArgumentException("Range must be > 0");
        }

        this.floor = floor;
        this.range = range;
    }

    @Override
    public Object newValue(Random random) {
        return new Double(this.floor + random.nextInt(this.range - 1)
                + random.nextDouble());
    }

}
