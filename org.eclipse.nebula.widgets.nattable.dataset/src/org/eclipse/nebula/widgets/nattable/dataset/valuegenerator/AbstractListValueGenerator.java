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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.eclipse.nebula.widgets.nattable.dataset.generator.IValueGenerator;

public abstract class AbstractListValueGenerator<T> implements IValueGenerator {

    private final List<T> listOfValues;

    public AbstractListValueGenerator(List<T> listOfValues) {
        this.listOfValues = Collections.unmodifiableList(listOfValues);
    }

    @SafeVarargs
    public AbstractListValueGenerator(T... values) {
        this(Arrays.asList(values));
    }

    @Override
    public Object newValue(Random random) {
        return this.listOfValues.get(random.nextInt(this.listOfValues.size()));
    }

    @SafeVarargs
    protected static <V> String[] toStringArray(V... values) {
        String[] retStrings = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            retStrings[i] = String.valueOf(values[i]);
        }
        return retStrings;
    }
}
