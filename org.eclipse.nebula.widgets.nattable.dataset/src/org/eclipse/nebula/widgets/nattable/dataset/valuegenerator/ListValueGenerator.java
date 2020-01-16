/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.dataset.valuegenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.eclipse.nebula.widgets.nattable.dataset.generator.IValueGenerator;

public class ListValueGenerator<T> implements IValueGenerator {

    private final List<T> values;
    private final int nullLoadFactor;

    @SafeVarargs
    public ListValueGenerator(int nullLoadFactor, T... values) {
        this.nullLoadFactor = nullLoadFactor;
        this.values = Arrays.asList(values);
    }

    @Override
    public Object newValue(Random random) {
        int choice = random.nextInt(this.values.size() + this.nullLoadFactor);
        return choice >= this.values.size() ? null : this.values.get(choice);
    }

}
