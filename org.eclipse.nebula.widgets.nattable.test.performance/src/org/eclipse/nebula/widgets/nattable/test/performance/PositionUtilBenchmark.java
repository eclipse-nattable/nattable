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

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.iterator.IntIterator;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;

public class PositionUtilBenchmark {

    private static final int ITERATIONS = 100;

    private int[] valuesPrimitive;

    private List<Integer> valuesWrapper;

    public static void main(String[] args) {
        PositionUtilBenchmark benchmark = new PositionUtilBenchmark();
        benchmark.startGrouping();

        System.out.println();

        benchmark.startBoxingUnboxing();

        System.out.println();
        benchmark.getPositionsPerformance();
    }

    PositionUtilBenchmark() {
        this.valuesPrimitive = new int[999_991];
        int index = 0;
        for (int i = 0; i < 1_000_000; i++) {
            if (i == 0 || i % 100_000 != 0) {
                this.valuesPrimitive[index] = i;
                index++;
            }
        }

        this.valuesWrapper = new ArrayList<>(999_991);
        for (int i = 0; i < 1_000_000; i++) {
            if (i == 0 || i % 100_000 != 0) {
                this.valuesWrapper.add(i);
            }
        }
    }

    @SuppressWarnings("unused")
    void startBoxingUnboxing() {

        System.out.println("start boxing/unboxing");
        System.out.println();

        List<Integer> boxed1 = getBoxedViaIteration();
        List<Integer> boxed2 = getBoxedViaStream();

        int[] unboxed1 = getUnBoxedViaIteration();
        int[] unboxed2 = getUnBoxedViaStream();
    }

    List<Integer> getBoxedViaIteration() {
        int sum = 0;
        List<Integer> values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();

            values = ArrayUtil.asIntegerList(this.valuesPrimitive);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("boxing int[] to List<Integer> via for-loop\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    List<Integer> getBoxedViaStream() {
        int sum = 0;
        List<Integer> values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();

            values = Arrays.stream(this.valuesPrimitive).boxed().collect(Collectors.toList());

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("boxing int[] to List<Integer> via Stream\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    int[] getUnBoxedViaIteration() {
        int sum = 0;
        int[] values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();

            values = ArrayUtil.asIntArray(this.valuesWrapper);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("unboxing List<Integer> to int[] via for-loop\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    int[] getUnBoxedViaStream() {
        int sum = 0;
        int[] values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();

            values = this.valuesWrapper.stream().mapToInt(Integer::intValue).toArray();

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("unboxing List<Integer> to int[] via Stream\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    @SuppressWarnings("unused")
    void startGrouping() {

        System.out.println("start grouping");
        System.out.println();

        List<List<Integer>> groupedByContiguous = getGroupedByContiguousWrapperToWrapper(this.valuesWrapper);

        int[][] g1 = getGroupedByContiguousWithIntStream(this.valuesPrimitive);

        int[][] g2 = getGroupedByContiguousWithForLoop(this.valuesPrimitive);

        if (!equals(g1, g2)) {
            System.err.println("output not equal"); //$NON-NLS-1$
        }

        int[][] g21 = getGroupedByContiguousPositionUtil(this.valuesPrimitive);

        if (!equals(g2, g21)) {
            System.err.println("output not equal"); //$NON-NLS-1$
        }

        int[][] g3 = getGroupedByContiguousEclipseCollections(this.valuesPrimitive);

        if (!equals(g1, g3)) {
            System.err.println("output not equal between primitive and Eclipse Collections"); //$NON-NLS-1$
        }

        int[][] g4 = getGroupedByContiguousEclipseCollectionsWithCollector(this.valuesPrimitive);

        if (!equals(g1, g4)) {
            System.err.println("output not equal between Eclipse Collections"); //$NON-NLS-1$
        }

    }

    public static List<List<Integer>> getGroupedByContiguousWrapperToWrapper(Collection<Integer> numberCollection) {
        int sum = 0;
        ArrayList<List<Integer>> grouped = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();

            ArrayList<Integer> numbers = new ArrayList<>(numberCollection);
            Collections.sort(numbers);

            ArrayList<Integer> contiguous = new ArrayList<>();
            grouped = new ArrayList<>();

            for (int i = 0; i < numbers.size() - 1; i++) {
                if (numbers.get(i).intValue() + 1 != numbers.get(i + 1).intValue()) {
                    contiguous.add(numbers.get(i));
                    grouped.add(contiguous);
                    contiguous = new ArrayList<Integer>();
                } else {
                    contiguous.add(numbers.get(i));
                }
            }
            if (isNotEmpty(numbers)) {
                contiguous.add(numbers.get(numbers.size() - 1));
            }
            grouped.add(contiguous);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("grouping List<Integer> to List<List<Integer>>\t\t\t\t\t" + (sum / ITERATIONS) + " ms");

        return grouped;
    }

    public static int[][] getGroupedByContiguousWithIntStream(int... numbers) {
        int sum = 0;
        int[][] result = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();

            ArrayList<Range> ranges = Arrays.stream(numbers)
                    .sorted()
                    .collect(
                            ArrayList<Range>::new,
                            new RangeAccumulator(),
                            (g1, g2) -> {
                                g1.addAll(g2);
                            });

            result = ranges.stream()
                    .map(r -> IntStream.range(r.start, r.end).toArray())
                    .toArray(size -> new int[size][]);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("grouping int[] to int[][] via primitive streams, map via IntStream\t\t" + (sum / ITERATIONS) + " ms");

        return result;
    }

    public static int[][] getGroupedByContiguousWithForLoop(int... numbers) {
        int sum = 0;
        int[][] result = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();

            ArrayList<Range> ranges = Arrays.stream(numbers)
                    .sorted()
                    .collect(
                            ArrayList<Range>::new,
                            new RangeAccumulator(),
                            (g1, g2) -> {
                                g1.addAll(g2);
                            });

            result = ranges.stream()
                    .map(r -> {
                        int[] res = new int[r.end - r.start];
                        int i = 0;
                        for (int pos = r.start; pos < r.end; pos++) {
                            res[i] = pos;
                            i++;
                        }

                        return res;
                    })
                    .toArray(size -> new int[size][]);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("grouping int[] to int[][] via primitive streams, map via for-loop\t\t" + (sum / ITERATIONS) + " ms");

        return result;
    }

    public static int[][] getGroupedByContiguousPositionUtil(int... values) {
        int sum = 0;
        int[][] result = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();

            result = PositionUtil.getGroupedByContiguous(values);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("grouping int[] to int[][] via PositionUtil\t\t\t\t\t" + (sum / ITERATIONS) + " ms");

        return result;
    }

    public static int[][] getGroupedByContiguousEclipseCollections(int... numbers) {
        int sum = 0;
        int[][] result = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();

            MutableList<MutableIntList> out = Lists.mutable.empty();
            MutableIntList curList = IntLists.mutable.empty();

            // sort the numbers
            IntList iNumbers = IntLists.immutable.of(numbers).toSortedList();

            final IntIterator it = iNumbers.intIterator();
            int last = it.next();
            out.add(curList);
            curList.add(last);

            while (it.hasNext()) {
                int next = it.next();

                if (next == last + 1) {
                    curList.add(next);
                } else {
                    curList = IntLists.mutable.empty();
                    curList.add(next);
                    out.add(curList);
                }

                last = next;
            }

            result = out.collect(groupList -> groupList.toArray()).toArray(new int[0][0]);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("grouping int[] to int[][] via primitive Eclipse Collections\t\t\t" + (sum / ITERATIONS) + " ms");

        return result;
    }

    public static int[][] getGroupedByContiguousEclipseCollectionsWithCollector(int... numbers) {
        class ContiguousCollector {
            MutableList<MutableIntList> grouped = Lists.mutable.empty();
        }

        ObjIntConsumer<ContiguousCollector> accumulator = (collector, i) -> {
            MutableIntList lastGroup = collector.grouped.getLast();
            if (lastGroup == null) {
                lastGroup = IntLists.mutable.empty();
                collector.grouped.add(lastGroup);
            }
            if (!lastGroup.isEmpty()) {
                int last = lastGroup.getLast();
                if (i > (last + 1)) {
                    lastGroup = IntLists.mutable.empty();
                    collector.grouped.add(lastGroup);
                }
            }
            lastGroup.add(i);
        };

        int sum = 0;
        int[][] result = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();

            MutableList<MutableIntList> grouped = IntLists.immutable.of(numbers)
                    .primitiveStream()
                    .sorted()
                    .collect(
                            ContiguousCollector::new,
                            accumulator,
                            (g1, g2) -> {
                                g1.grouped.addAll(g2.grouped);
                            }).grouped;

            result = grouped.collect(groupList -> groupList.toArray()).toArray(new int[0][0]);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("grouping int[] to int[][] via primitive Eclipse Collections with Collector\t" + (sum / ITERATIONS) + " ms");

        return result;
    }

    public void getPositionsPerformance() {
        List<Range> values = Arrays.asList(
                new Range(0, 100_000),
                new Range(100_001, 200_000),
                new Range(200_001, 300_000),
                new Range(300_001, 400_000),
                new Range(400_001, 500_000),
                new Range(500_001, 600_000),
                new Range(600_001, 700_000),
                new Range(700_001, 800_000),
                new Range(800_001, 900_000),
                new Range(900_001, 1_000_000));

        int sum = 0;
        int[] groupedByContiguous = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();

            groupedByContiguous = PositionUtil.getPositions(values);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("PositionUtil.getPositions(Collection<Range>) " + (sum / ITERATIONS) + " ms , array length = " + groupedByContiguous.length);
    }

    private static class RangeAccumulator implements ObjIntConsumer<ArrayList<Range>> {

        @Override
        public void accept(ArrayList<Range> ranges, int i) {
            Range lastGroup;
            if (ranges.size() > 0) {
                lastGroup = ranges.get(ranges.size() - 1);
            } else {
                lastGroup = new Range(i, i + 1);
                ranges.add(lastGroup);
            }

            int last = lastGroup.end;
            if (i > last) {
                lastGroup = new Range(i, i + 1);
                ranges.add(lastGroup);
            } else {
                lastGroup.end = i + 1;
            }
        }

    }

    public static boolean equals(int[][] a, int[][] a2) {
        if (a == a2)
            return true;
        if (a == null || a2 == null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i = 0; i < length; i++) {
            int[] o1 = a[i];
            int[] o2 = a2[i];
            if (!(o1 == null ? o2 == null : Arrays.equals(o1, o2)))
                return false;
        }

        return true;
    }
}
