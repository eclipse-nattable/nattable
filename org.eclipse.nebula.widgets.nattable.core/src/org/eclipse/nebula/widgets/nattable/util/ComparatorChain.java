/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.util;

import java.util.Comparator;
import java.util.List;

public class ComparatorChain<T> implements Comparator<T> {

    private final List<Comparator<T>> comparators;

    public ComparatorChain(List<Comparator<T>> comparators) {
        this.comparators = comparators;
    }

    @Override
    public int compare(T arg0, T arg1) {
        for (int i = 0; i < this.comparators.size(); i++) {
            int compareResult = this.comparators.get(i).compare(arg0, arg1);
            if (compareResult != 0) {
                return compareResult;
            }
        }
        return 0;
    }

}
