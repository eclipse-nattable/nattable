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
