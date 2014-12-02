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
package org.eclipse.nebula.widgets.nattable.search;

import java.util.Comparator;

/**
 * The comparator will base its comparison on the display value of a cell. The
 * display value is assumed to be a string.
 *
 */
public class CellValueAsStringComparator<T extends Comparable<String>>
        implements Comparator<T> {

    public CellValueAsStringComparator() {}

    @Override
    public int compare(T firstValue, T secondValue) {
        String firstCellValue = firstValue.toString();
        String secondCellValue = secondValue.toString();
        return firstCellValue.compareTo(secondCellValue);
    }
}
