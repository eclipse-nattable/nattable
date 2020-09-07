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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.factory.primitive.IntIntMaps;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;

public class CollectionsBenchmark {

    public static final int ITERATIONS = 100;

    MutableMap<String, MutableIntSet> hidden = Maps.mutable.empty();
    MutableIntIntMap map = IntIntMaps.mutable.empty();

    public static void main(String[] args) {

        CollectionsBenchmark benchmark = new CollectionsBenchmark();
        benchmark.startBenchmark();
    }

    CollectionsBenchmark() {
        this.hidden.put("Test1", IntSets.mutable.ofAll(IntStream.range(0, 100_000))); //$NON-NLS-1$
        this.hidden.put("Test2", IntSets.mutable.ofAll(IntStream.range(100_001, 200_000))); //$NON-NLS-1$
        this.hidden.put("Test3", IntSets.mutable.ofAll(IntStream.range(200_000, 300_000))); //$NON-NLS-1$
        this.hidden.put("Test4", IntSets.mutable.ofAll(IntStream.range(300_000, 400_000))); //$NON-NLS-1$
        this.hidden.put("Test5", IntSets.mutable.ofAll(IntStream.range(400_000, 500_000))); //$NON-NLS-1$

        for (int i = 0; i < 1_000_000; i++) {
            this.map.put(i, i * 2);
        }
    }

    public void startBenchmark() {
        // rampUp to remove class loading from performance measure
        findByContains(this.hidden, 1, true);
        findByCollections(this.hidden, 1, true);
        flattenByIterationSet(this.hidden, true);
        flattenByIterationList(this.hidden, true);
        sumAndMaxByIteration(this.map, true);

        int[] valuesArray = createPrimitiveValuesArrayForEach();
        int[] valuesFromIntStream = createPrimitiveValuesArrayIntStream();

        if (!Arrays.equals(valuesArray, valuesFromIntStream)) {
            System.err.println("Primitive input not equal");
        }

        List<Integer> valuesList = createWrapperValuesForEach();
        List<Integer> wrapperFromStream = createWrapperValuesIntStream();

        if (!valuesList.equals(wrapperFromStream)) {
            System.err.println("Wrapper input not equal");
        }

        Set<Integer> valueHashSetWrapper = createWrapperValueHashSetForEach();
        Set<Integer> valueTreeSetWrapper = createWrapperValueTreeSetForEach();
        Set<Integer> wrapperHashSetFromStream = createWrapperValueHashSetIntStream();

        if (!valueHashSetWrapper.equals(wrapperHashSetFromStream) || !valueHashSetWrapper.equals(valueTreeSetWrapper)) {
            System.err.println("Wrapper input not equal");
        }

        System.out.println();

        createEclipseCollectionsListIteration();
        createEclipseCollectionsListOf(valuesArray);
        createEclipseCollectionsListIntegerCollection(valuesList);
        createEclipseCollectionsListIntStream();

        System.out.println();

        createEclipseCollectionsSetIteration();
        createEclipseCollectionsSetOf(valuesArray);
        createEclipseCollectionsSetIntegerCollection(valuesList);
        createEclipseCollectionsSetIntStream();

        System.out.println();
        System.out.println("Check value is contained");
        boolean c1 = containsInList(valuesList, 450_000, false);

        HashSet<Integer> valueSet = new HashSet<>(valuesList);
        boolean c2 = containsInSet(valueSet, 450_000, false);

        boolean c3 = containsInPrimitive(valuesArray, 450_000, false);
        boolean c4 = containsInPrimitiveIteration(valuesArray, 450_000, false);
        boolean c5 = containsInArrayBinarySearch(valuesArray, 450_000, false);

        MutableIntList valuesIntList = IntLists.mutable.of(Arrays.copyOf(valuesArray, valuesArray.length));
        boolean c6 = containsInMutableIntList(valuesIntList, 450_000, false);

        MutableIntSet valuesIntSet = IntSets.mutable.of(Arrays.copyOf(valuesArray, valuesArray.length));
        boolean c7 = containsInMutableIntSet(valuesIntSet, 450_000, false);

        if (!c1 || !c2 || !c3 || !c4 || !c5 || !c6 || !c7) {
            System.err.println("value not contained");
        }

        System.out.println();
        System.out.println("Check value is not contained");
        c1 = containsInList(valuesList, 2_000_000, false);
        c2 = containsInSet(valueSet, 2_000_000, false);
        c3 = containsInPrimitive(valuesArray, 2_000_000, false);
        c4 = containsInPrimitiveIteration(valuesArray, 2_000_000, false);
        c5 = containsInArrayBinarySearch(valuesArray, 2_000_000, false);
        c6 = containsInMutableIntList(valuesIntList, 2_000_000, false);
        c7 = containsInMutableIntSet(valuesIntSet, 2_000_000, false);

        if (c1 || c2 || c3 || c4 || c5 || c6 || c7) {
            System.err.println("value contained");
        }

        System.out.println();
        System.out.println("Check indexOf() 450_000");
        int i1 = indexOfInCollection(valuesList, 450_000, false);
        int i2 = indexOfInPrimitiveIteration(valuesArray, 450_000, false);
        int i3 = indexOfInMutableIntList(valuesIntList, 450_000, false);

        if ((i1 != i2) || (i2 != i3) || (i3 != 449_996)) {
            System.err.println("indexOf failed");
        }

        System.out.println();
        System.out.println("Check indexOf() 2_000_000");
        i1 = indexOfInCollection(valuesList, 2_000_000, false);
        i2 = indexOfInPrimitiveIteration(valuesArray, 2_000_000, false);
        i3 = indexOfInMutableIntList(valuesIntList, 2_000_000, false);

        if ((i1 != i2) || (i2 != i3) || (i3 != -1)) {
            System.err.println("indexOf failed");
        }

        System.out.println();
        System.out.println("Remove Performance Tests"); //$NON-NLS-1$

        int[] toRemove = new int[100_000];
        for (int i = 0; i < 100_000; i++) {
            toRemove[i] = i + 200_000;
        }

        // This one is really slow, so we disable it here
        int[] dataArray = IntStream.range(0, 1_000_000).unordered().toArray();
        // List<Integer> dataList = IntStream.range(0,
        // 1_000_000).unordered().boxed().collect(Collectors.toList());
        Set<Integer> dataSet = IntStream.range(0, 1_000_000).unordered().boxed().collect(Collectors.toSet());
        MutableIntList dataIntList = IntLists.mutable.ofAll(IntStream.range(0, 1_000_000).unordered());
        MutableIntSet dataIntSet = IntSets.mutable.ofAll(IntStream.range(0, 1_000_000).unordered());

        removeAllByStream(dataArray, toRemove, false);
        // removeAllByIterationList(dataList, toRemove, false);
        // removeAllList(dataList, toRemove, false);
        removeAllByIterationSet(dataSet, toRemove, false);
        removeAllSet(dataSet, toRemove, false);

        // This one is really slow, so we disable it here
        // removeAllByIterationMutableIntList(dataIntList, toRemove, false);
        removeAllMutableIntList(dataIntList, toRemove, false);

        removeAllByIterationMutableIntSet(dataIntSet, toRemove, false);
        removeAllMutableIntSet(dataIntSet, toRemove, false);

        System.out.println();
        boolean findByContains = findByContains(this.hidden, 100_000, false);
        boolean findByCollections = findByCollections(this.hidden, 100_000, false);

        if (findByContains || findByCollections) {
            System.err.println("non-existing value was found");
        }

        findByContains = findByContains(this.hidden, 450_000, false);
        findByCollections = findByCollections(this.hidden, 450_000, false);

        if (!findByContains || !findByCollections) {
            System.err.println("value was not found");
        }

        System.out.println();
        flattenByIterationSet(this.hidden, false);
        flattenByIterationList(this.hidden, false);

        System.out.println();
        System.out.println("Sum and max Performance Tests"); //$NON-NLS-1$
        int[] result1 = sumAndMaxByIteration(this.map, false);
        int[] result2 = sumAndMax(this.map, false);

        if (!Arrays.equals(result1, result2)) {
            System.out.println("result is not equal");
        }
    }

    private int[] createPrimitiveValuesArrayForEach() {
        int sum = 0;
        int[] values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();
            values = new int[999_991];
            int index = 0;
            for (int i = 0; i < 1_000_000; i++) {
                if (i == 0 || i % 100_000 != 0) {
                    values[index] = i;
                    index++;
                }
            }
            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("collecting int[] via for-loop\t\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    private int[] createPrimitiveValuesArrayIntStream() {
        int sum = 0;
        int[] values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();
            values = IntStream.range(0, 1_000_000)
                    .filter(i -> i == 0 || i % 100_000 != 0)
                    .toArray();
            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("collecting int[] via IntStream\t\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    private List<Integer> createWrapperValuesForEach() {
        int sum = 0;
        ArrayList<Integer> values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();
            values = new ArrayList<>(999_991);
            for (int i = 0; i < 1_000_000; i++) {
                if (i == 0 || i % 100_000 != 0) {
                    values.add(i);
                }
            }
            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("collecting List<Integer> via for-loop\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    private List<Integer> createWrapperValuesIntStream() {
        int sum = 0;
        List<Integer> values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();

            values = IntStream.range(0, 1_000_000)
                    .filter(i -> (i == 0 || i % 100_000 != 0))
                    .boxed()
                    .collect(Collectors.toList());

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("collecting List<Integer> via IntStream\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    private Set<Integer> createWrapperValueHashSetForEach() {
        int sum = 0;
        HashSet<Integer> values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();
            values = new HashSet<>(999_991);
            for (int i = 0; i < 1_000_000; i++) {
                if (i == 0 || i % 100_000 != 0) {
                    values.add(i);
                }
            }
            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("collecting HashSet<Integer> via for-loop\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    private Set<Integer> createWrapperValueTreeSetForEach() {
        int sum = 0;
        TreeSet<Integer> values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();
            values = new TreeSet<>();
            for (int i = 0; i < 1_000_000; i++) {
                if (i == 0 || i % 100_000 != 0) {
                    values.add(i);
                }
            }
            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("collecting TreeSet<Integer> via for-loop\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    private Set<Integer> createWrapperValueHashSetIntStream() {
        int sum = 0;
        Set<Integer> values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();

            values = IntStream.range(0, 1_000_000)
                    .filter(i -> (i == 0 || i % 100_000 != 0))
                    .boxed()
                    .collect(Collectors.toSet());

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("collecting Set<Integer> via IntStream\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    private MutableIntList createEclipseCollectionsListIteration() {
        int sum = 0;
        MutableIntList values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();
            values = IntLists.mutable.withInitialCapacity(999_991);
            for (int i = 0; i < 1_000_000; i++) {
                if (i == 0 || i % 100_000 != 0) {
                    values.add(i);
                }
            }
            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("creating MutableIntList via iteration\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    private MutableIntList createEclipseCollectionsListOf(int[] inputArray) {
        int sum = 0;
        MutableIntList values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();
            values = IntLists.mutable.of(inputArray);
            // values = IntLists.mutable.of(inputArray).distinct().sortThis();
            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("creating MutableIntList of int[]\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    private MutableIntList createEclipseCollectionsListIntegerCollection(List<Integer> inputCollection) {
        int sum = 0;
        MutableIntList values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();
            values = IntLists.mutable.ofAll(inputCollection);
            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("creating MutableIntList via Integer collection\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    private MutableIntList createEclipseCollectionsListIntStream() {
        int sum = 0;
        MutableIntList values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();
            values = IntLists.mutable.ofAll(IntStream.range(0, 1_000_000).filter(i -> i == 0 || i % 100_000 != 0));
            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("creating MutableIntList via IntStream\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    private MutableIntSet createEclipseCollectionsSetIteration() {
        int sum = 0;
        MutableIntSet values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();
            values = IntSets.mutable.withInitialCapacity(999_991);
            for (int i = 0; i < 1_000_000; i++) {
                if (i == 0 || i % 100_000 != 0) {
                    values.add(i);
                }
            }
            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("creating MutableIntSet via iteration\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    private MutableIntSet createEclipseCollectionsSetOf(int[] inputArray) {
        int sum = 0;
        MutableIntSet values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();
            values = IntSets.mutable.of(inputArray);
            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("creating MutableIntSet of int[]\t\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    private MutableIntSet createEclipseCollectionsSetIntegerCollection(List<Integer> inputCollection) {
        int sum = 0;
        MutableIntSet values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();
            values = IntSets.mutable.ofAll(inputCollection);
            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("creating MutableIntSet of Integer collection\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    private MutableIntSet createEclipseCollectionsSetIntStream() {
        int sum = 0;
        MutableIntSet values = null;
        for (int j = 0; j < ITERATIONS; j++) {
            long start = System.currentTimeMillis();
            values = IntSets.mutable.ofAll(IntStream.range(0, 1_000_000).filter(i -> (i == 0 || i % 100_000 != 0)));
            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        System.out.println("creating MutableIntSet via IntStream\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        return values;
    }

    public static boolean findByContains(MutableMap<String, MutableIntSet> hidden, int columnIndex, boolean rampUp) {
        int sum = 0;
        boolean result = false;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            long start = System.currentTimeMillis();

            for (MutableIntSet indexes : hidden.values()) {
                if (indexes.contains(columnIndex)) {
                    result = true;
                    break;
                }
            }
            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("find via value iteration\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    public static boolean findByCollections(MutableMap<String, MutableIntSet> hidden, int columnIndex, boolean rampUp) {
        int sum = 0;
        boolean result = false;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            long start = System.currentTimeMillis();

            MutableIntSet detect = hidden.detect(indexes -> indexes.contains(columnIndex));
            result = detect != null;
            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("find via detect()\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    public static boolean containsInList(List<Integer> values, int columnIndex, boolean rampUp) {
        int sum = 0;
        boolean result = false;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            long start = System.currentTimeMillis();

            result = values.contains(Integer.valueOf(columnIndex));

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("contains in List\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    public static boolean containsInSet(Set<Integer> values, int columnIndex, boolean rampUp) {
        int sum = 0;
        boolean result = false;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            long start = System.currentTimeMillis();

            result = values.contains(Integer.valueOf(columnIndex));

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("contains in Set\t\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    public static boolean containsInPrimitive(int[] values, int columnIndex, boolean rampUp) {
        int sum = 0;
        boolean result = false;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            long start = System.currentTimeMillis();

            result = Arrays.stream(values).anyMatch(x -> x == columnIndex);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("contains in int[] stream\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    public static boolean containsInArrayBinarySearch(int[] values, int columnIndex, boolean rampUp) {
        int sum = 0;
        boolean result = false;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            long start = System.currentTimeMillis();

            result = Arrays.binarySearch(values, columnIndex) >= 0;

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("contains in int[] binary search\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    public static boolean containsInPrimitiveIteration(int[] values, int columnIndex, boolean rampUp) {
        int sum = 0;
        boolean result = false;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            long start = System.currentTimeMillis();

            for (int i : values) {
                if (i == columnIndex) {
                    result = true;
                    break;
                }
            }

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("contains in int[] iteration\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    public boolean containsInMutableIntList(MutableIntList values, int columnIndex, boolean rampUp) {
        int sum = 0;
        boolean result = false;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            long start = System.currentTimeMillis();

            result = values.contains(columnIndex);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("contains in MutableIntList\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    public boolean containsInMutableIntSet(MutableIntSet values, int columnIndex, boolean rampUp) {
        int sum = 0;
        boolean result = false;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            long start = System.currentTimeMillis();

            result = values.contains(columnIndex);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("contains in MutableIntSet\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    public static int indexOfInCollection(List<Integer> values, int columnIndex, boolean rampUp) {
        int sum = 0;
        int result = -1;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            long start = System.currentTimeMillis();

            result = values.indexOf(Integer.valueOf(columnIndex));

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("indexOf in collection\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    public static int indexOfInPrimitiveIteration(int[] values, int columnIndex, boolean rampUp) {
        int sum = 0;
        int result = -1;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            long start = System.currentTimeMillis();

            for (int index = 0; index < values.length; index++) {
                int i = values[index];
                if (i == columnIndex) {
                    result = index;
                    break;
                }
            }

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("indexOf in int[] iteration\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    public int indexOfInMutableIntList(MutableIntList values, int columnIndex, boolean rampUp) {
        int sum = 0;
        int result = -1;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            long start = System.currentTimeMillis();

            result = values.indexOf(columnIndex);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("indexOf in MutableIntList\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    public static int[] flattenByIterationSet(MutableMap<String, MutableIntSet> hidden, boolean rampUp) {
        int sum = 0;
        int[] result = null;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            long start = System.currentTimeMillis();

            MutableIntSet hiddenColumnIndexes = IntSets.mutable.empty();
            for (MutableIntSet indexes : hidden.values()) {
                hiddenColumnIndexes.addAll(indexes);
            }
            result = hiddenColumnIndexes.toSortedArray();

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("flatten by iteration MutableIntSet\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    public static int[] flattenByIterationList(MutableMap<String, MutableIntSet> hidden, boolean rampUp) {
        int sum = 0;
        int[] result = null;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            long start = System.currentTimeMillis();

            MutableIntList hiddenColumnIndexes = IntLists.mutable.empty();
            for (MutableIntSet indexes : hidden.values()) {
                hiddenColumnIndexes.addAll(indexes);
            }
            result = hiddenColumnIndexes.distinct().toSortedArray();

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("flatten by iteration MutableIntList\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    public static void removeAllByStream(int[] values, int[] toRemove, boolean rampUp) {
        int sum = 0;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            // create a copy so we really remove everytime
            long start = System.currentTimeMillis();

            int[] result = Arrays.stream(values).filter(v -> (Arrays.binarySearch(toRemove, v) < 0)).toArray();

            long end = System.currentTimeMillis();

            sum += (end - start);

            if (result.length != values.length - toRemove.length) {
                System.err.println("remove failed");
            }
        }

        if (!rampUp) {
            System.out.println("remove all by primitive stream\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static void removeAllByIterationList(List<Integer> values, int[] toRemove, boolean rampUp) {
        int sum = 0;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            // create a copy so we really remove everytime
            ArrayList<Integer> v = new ArrayList<>(values);

            long start = System.currentTimeMillis();

            for (Integer r : toRemove) {
                v.remove(r);
            }

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("remove all by iteration List\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static void removeAllList(List<Integer> values, int[] toRemove, boolean rampUp) {
        List<Integer> asIntegerList = ArrayUtil.asIntegerList(toRemove);
        int sum = 0;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            // create a copy so we really remove everytime
            ArrayList<Integer> v = new ArrayList<>(values);

            long start = System.currentTimeMillis();

            v.removeAll(asIntegerList);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("remove all List\t\t\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static void removeAllByIterationSet(Set<Integer> values, int[] toRemove, boolean rampUp) {
        int sum = 0;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            // create a copy so we really remove everytime
            HashSet<Integer> v = new HashSet<>(values);

            long start = System.currentTimeMillis();

            for (Integer r : toRemove) {
                v.remove(r);
            }

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("remove all by iteration Set\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static void removeAllSet(Set<Integer> values, int[] toRemove, boolean rampUp) {
        List<Integer> asIntegerList = ArrayUtil.asIntegerList(toRemove);
        int sum = 0;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            // create a copy so we really remove everytime
            HashSet<Integer> v = new HashSet<>(values);

            long start = System.currentTimeMillis();

            v.removeAll(asIntegerList);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("remove all Set\t\t\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static void removeAllByIterationMutableIntList(MutableIntList values, int[] toRemove, boolean rampUp) {
        int sum = 0;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            // create a copy so we really remove everytime
            MutableIntList v = values.toList();

            long start = System.currentTimeMillis();

            for (int r : toRemove) {
                v.remove(r);
            }

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("remove all by iteration MutableIntList\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static void removeAllMutableIntList(MutableIntList values, int[] toRemove, boolean rampUp) {
        int sum = 0;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            // create a copy so we really remove everytime
            MutableIntList v = values.toList();

            long start = System.currentTimeMillis();

            v.removeAll(toRemove);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("remove all MutableIntList\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static void removeAllByIterationMutableIntSet(MutableIntSet values, int[] toRemove, boolean rampUp) {
        int sum = 0;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            // create a copy so we really remove everytime
            MutableIntSet v = values.toSet();

            long start = System.currentTimeMillis();

            for (int r : toRemove) {
                v.remove(r);
            }

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("remove all by iteration MutableIntSet\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static void removeAllMutableIntSet(MutableIntSet values, int[] toRemove, boolean rampUp) {
        int sum = 0;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            // create a copy so we really remove everytime
            MutableIntSet v = values.toSet();

            long start = System.currentTimeMillis();

            v.removeAll(toRemove);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("remove all MutableIntSet\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static int[] sumAndMaxByIteration(MutableIntIntMap map, boolean rampUp) {
        int sum = 0;
        int[] result = new int[2];
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            long start = System.currentTimeMillis();

            int valueSum = 0;
            int lastPos = -1;
            for (IntIntPair entry : map.keyValuesView().toSortedList()) {
                valueSum += entry.getTwo();
                lastPos = Math.max(lastPos, entry.getOne());
            }

            result[0] = valueSum;
            result[1] = lastPos;

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("sum and max by iteration\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    public static int[] sumAndMax(MutableIntIntMap map, boolean rampUp) {
        int sum = 0;
        int[] result = new int[2];
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            long start = System.currentTimeMillis();

            result[0] = (int) map.values().sum();
            result[1] = map.keySet().max();

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("sum and max\t\t\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }
}
