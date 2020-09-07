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
package org.eclipse.nebula.widgets.nattable.search;

import java.util.Comparator;

/**
 * The comparator will base its comparison on the display value of a cell. The
 * display value is assumed to be a string.
 *
 */
public class CellValueAsStringComparator<T extends Comparable<String>> implements Comparator<T> {

    public CellValueAsStringComparator() {
    }

    @Override
    public int compare(T firstValue, T secondValue) {
        String firstCellValue = firstValue.toString();
        String secondCellValue = secondValue.toString();
        return firstCellValue.compareTo(secondCellValue);
    }
}
