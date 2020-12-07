/*****************************************************************************
 * Copyright (c) 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.performance;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;

public class ListDataProviderBenchmark {

    static final int ITERATIONS = 100;

    static List<RowDataFixture> arrayList = new ArrayList<>(RowDataListFixture.getList(100_000));
    static List<RowDataFixture> mutableList = Lists.mutable.ofAll(arrayList);

    static ReflectiveColumnPropertyAccessor<RowDataFixture> columnPropertyAccessor =
            new ReflectiveColumnPropertyAccessor<>(RowDataListFixture.getPropertyNames());

    static ListDataProvider<RowDataFixture> arrayListAccessor =
            new ListDataProvider<>(arrayList, columnPropertyAccessor);

    static ListDataProvider<RowDataFixture> mutableListAccessor =
            new ListDataProvider<>(mutableList, columnPropertyAccessor);

    public static void main(String[] args) {
        startAccessColumnFirst();

    }

    static void startAccessColumnFirst() {
        System.out.println("Test access column first:");

        int rowStart = 0;
        int rowEnd = 1000;
        System.out.println("ListDataProvider access with ArrayList (" + rowStart + "/" + rowEnd + ")\t\t\t"
                + testAccessColumnFirst(arrayListAccessor, rowStart, rowEnd) + " ms");
        System.out.println("ListDataProvider access with MutableList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessColumnFirst(mutableListAccessor, rowStart, rowEnd) + " ms");
        rowStart = 1_000;
        rowEnd = 2_000;
        System.out.println("ListDataProvider access with ArrayList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessColumnFirst(arrayListAccessor, rowStart, rowEnd) + " ms");
        System.out.println("ListDataProvider access with MutableList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessColumnFirst(mutableListAccessor, rowStart, rowEnd) + " ms");
        rowStart = 10_000;
        rowEnd = 11_000;
        System.out.println("ListDataProvider access with ArrayList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessColumnFirst(arrayListAccessor, rowStart, rowEnd) + " ms");
        System.out.println("ListDataProvider access with MutableList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessColumnFirst(mutableListAccessor, rowStart, rowEnd) + " ms");
        rowStart = 99_000;
        rowEnd = 100_000;
        System.out.println("ListDataProvider access with ArrayList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessColumnFirst(arrayListAccessor, rowStart, rowEnd) + " ms");
        System.out.println("ListDataProvider access with MutableList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessColumnFirst(mutableListAccessor, rowStart, rowEnd) + " ms");
        rowStart = 0;
        rowEnd = 100_000;
        System.out.println("ListDataProvider access with ArrayList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessColumnFirst(arrayListAccessor, rowStart, rowEnd) + " ms");
        System.out.println("ListDataProvider access with MutableList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessColumnFirst(mutableListAccessor, rowStart, rowEnd) + " ms");

        System.out.println();
        System.out.println("Test access row first:");

        rowStart = 0;
        rowEnd = 1000;
        System.out.println("ListDataProvider access with ArrayList (" + rowStart + "/" + rowEnd + ")\t\t\t"
                + testAccessRowFirst(arrayListAccessor, rowStart, rowEnd) + " ms");
        System.out.println("ListDataProvider access with MutableList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessRowFirst(mutableListAccessor, rowStart, rowEnd) + " ms");
        rowStart = 1_000;
        rowEnd = 2_000;
        System.out.println("ListDataProvider access with ArrayList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessRowFirst(arrayListAccessor, rowStart, rowEnd) + " ms");
        System.out.println("ListDataProvider access with MutableList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessRowFirst(mutableListAccessor, rowStart, rowEnd) + " ms");
        rowStart = 10_000;
        rowEnd = 11_000;
        System.out.println("ListDataProvider access with ArrayList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessRowFirst(arrayListAccessor, rowStart, rowEnd) + " ms");
        System.out.println("ListDataProvider access with MutableList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessRowFirst(mutableListAccessor, rowStart, rowEnd) + " ms");
        rowStart = 99_000;
        rowEnd = 100_000;
        System.out.println("ListDataProvider access with ArrayList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessRowFirst(arrayListAccessor, rowStart, rowEnd) + " ms");
        System.out.println("ListDataProvider access with MutableList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessRowFirst(mutableListAccessor, rowStart, rowEnd) + " ms");
        rowStart = 0;
        rowEnd = 100_000;
        System.out.println("ListDataProvider access with ArrayList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessRowFirst(arrayListAccessor, rowStart, rowEnd) + " ms");
        System.out.println("ListDataProvider access with MutableList (" + rowStart + "/" + rowEnd + ")\t\t"
                + testAccessRowFirst(mutableListAccessor, rowStart, rowEnd) + " ms");
    }

    static int testAccessColumnFirst(IDataProvider dataProvider, int rowStart, int rowEnd) {

        int sum = 0;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();

            for (int columnIndex = 0; columnIndex < dataProvider.getColumnCount(); columnIndex++) {
                for (int rowIndex = rowStart; rowIndex < rowEnd; rowIndex++) {
                    dataProvider.getDataValue(columnIndex, rowIndex);
                }
            }

            long end = System.currentTimeMillis();

            sum += (end - start);
        }
        return (sum / ITERATIONS);
    }

    static int testAccessRowFirst(IDataProvider dataProvider, int rowStart, int rowEnd) {
        int sum = 0;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();

            for (int rowIndex = rowStart; rowIndex < rowEnd; rowIndex++) {
                for (int columnIndex = 0; columnIndex < dataProvider.getColumnCount(); columnIndex++) {
                    dataProvider.getDataValue(columnIndex, rowIndex);
                }
            }

            long end = System.currentTimeMillis();

            sum += (end - start);
        }
        return (sum / ITERATIONS);
    }
}
