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
package org.eclipse.nebula.widgets.nattable.selection;

public class RowObjectIndexHolder<T> implements Comparable<RowObjectIndexHolder<T>> {

    private int index;
    private T row;

    public RowObjectIndexHolder(int index, T row) {
        this.index = index;
        this.row = row;
    }

    public Integer getIndex() {
        return this.index;
    }

    public T getRow() {
        return this.row;
    }

    @Override
    public int compareTo(RowObjectIndexHolder<T> o) {
        return getIndex().compareTo(o.getIndex());
    }

}
