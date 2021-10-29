/*******************************************************************************
 * Copyright (c) 2012, 2021 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.test.fixture;

import static org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum.ASC;
import static org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum.DESC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;

@SuppressWarnings("rawtypes")
public class SortModelFixture implements ISortModel {

    ArrayList<Integer> sortedColumnIndexes;
    ArrayList<Integer> sortOrder;
    ArrayList<SortDirectionEnum> sortDirection;
    HashMap<Integer, List<Comparator>> columnComparators = new HashMap<>();

    public SortModelFixture() {
        this(Arrays.asList(0, 5, 6, 3), Arrays.asList(6, 5, 3, 0), Arrays.asList(ASC, DESC, ASC, DESC));
    }

    public SortModelFixture(
            List<Integer> sortedColumnIndexes, List<Integer> sortOrder, List<SortDirectionEnum> sortDirection) {
        this.sortedColumnIndexes = new ArrayList<>(sortedColumnIndexes);
        this.sortOrder = new ArrayList<>(sortOrder);
        this.sortDirection = new ArrayList<>(sortDirection);
    }

    public static SortModelFixture getEmptyModel() {
        return new SortModelFixture(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public List<Integer> getSortedColumnIndexes() {
        return this.sortedColumnIndexes;
    }

    @Override
    public boolean isColumnIndexSorted(int columnIndex) {
        return this.sortedColumnIndexes.contains(columnIndex);
    }

    @Override
    public int getSortOrder(int columnIndex) {
        if (this.sortedColumnIndexes.contains(columnIndex)) {
            return this.sortOrder.indexOf(columnIndex);
        }
        return -1;
    }

    @Override
    public SortDirectionEnum getSortDirection(int columnIndex) {
        if (this.sortedColumnIndexes.contains(columnIndex)) {
            return this.sortDirection.get(this.sortOrder.indexOf(columnIndex));
        }
        return SortDirectionEnum.NONE;
    }

    @Override
    public List<Comparator> getComparatorsForColumnIndex(int columnIndex) {
        return this.columnComparators.get(columnIndex);
    }

    @Override
    public Comparator<?> getColumnComparator(int columnIndex) {
        return null;
    }

    @Override
    public void sort(int columnIndex, SortDirectionEnum direction, boolean accumulate) {
        this.sortedColumnIndexes.add(columnIndex);
        this.sortOrder.add(columnIndex);
        this.sortDirection.add(direction);
    }

    @Override
    public void clear() {
        this.sortedColumnIndexes.clear();
        this.sortOrder.clear();
        this.sortDirection.clear();
    }

}
