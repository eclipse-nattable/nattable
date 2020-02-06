/*****************************************************************************
 * Copyright (c) 2020 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

        ArrayList<Integer> valuesCollection = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
            if (i == 0 || i % 100_000 != 0) {
                valuesCollection.add(i);
            }
        }
        boolean c1 = containsInCollection(valuesCollection, 450_000, false);

        int[] valuesArray = new int[999_991];
        int index = 0;
        for (int i = 0; i < 1_000_000; i++) {
            if (i == 0 || i % 100_000 != 0) {
                valuesArray[index] = i;
                index++;
            }
        }
        boolean c2 = containsInPrimitive(valuesArray, 450_000, false);

        MutableIntList valuesIntList = IntLists.mutable.of(Arrays.copyOf(valuesArray, valuesArray.length));
        boolean c3 = containsInMutableIntList(valuesIntList, 450_000, false);

        MutableIntSet valuesIntSet = IntSets.mutable.of(Arrays.copyOf(valuesArray, valuesArray.length));
        boolean c4 = containsInMutableIntSet(valuesIntSet, 450_000, false);

        if (!c1 || !c2 || !c3 || !c4) {
            System.err.println("value not contained");
        }

        System.out.println();
        flattenByIterationSet(this.hidden, false);
        flattenByIterationList(this.hidden, false);

        System.out.println();
        System.out.println("Remove Performance Tests"); //$NON-NLS-1$

        int[] toRemove = new int[100_000];
        for (int i = 0; i < 100_000; i++) {
            toRemove[i] = i + 200_000;
        }

        // This one is really slow, so we disable it here
        // List<Integer> dataList = IntStream.range(0,
        // 1_000_000).boxed().collect(Collectors.toList());
        // removeAllByIterationArrayList(dataList, toRemove, false);
        // removeAllArrayList(dataList, toRemove, false);

        MutableIntList dataIntList = IntLists.mutable.ofAll(IntStream.range(0, 1_000_000));
        // This one is really slow, so we disable it here
        // removeAllByIterationMutableIntList(dataIntList, toRemove, false);
        removeAllMutableIntList(dataIntList, toRemove, false);

        MutableIntSet dataIntSet = IntSets.mutable.ofAll(IntStream.range(0, 1_000_000));
        removeAllByIterationMutableIntSet(dataIntSet, toRemove, false);
        removeAllMutableIntSet(dataIntSet, toRemove, false);

        System.out.println();
        System.out.println("Sum and max Performance Tests"); //$NON-NLS-1$
        int[] result1 = sumAndMaxByIteration(this.map, false);
        int[] result2 = sumAndMax(this.map, false);

        if (!Arrays.equals(result1, result2)) {
            System.out.println("result is not equal");
        }
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

    public static boolean containsInCollection(List<Integer> values, int columnIndex, boolean rampUp) {
        int sum = 0;
        boolean result = false;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            long start = System.currentTimeMillis();

            result = values.contains(Integer.valueOf(columnIndex));

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("contains in collection\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
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
            System.out.println("contains in int[]\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return result;
    }

    private boolean containsInMutableIntList(MutableIntList values, int columnIndex, boolean rampUp) {
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

    private boolean containsInMutableIntSet(MutableIntSet values, int columnIndex, boolean rampUp) {
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

    public static void removeAllByIterationArrayList(List<Integer> values, int[] toRemove, boolean rampUp) {
        int sum = 0;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            // create a copy so we really remove everytime
            ArrayList<Integer> v = new ArrayList<Integer>(values);

            long start = System.currentTimeMillis();

            for (int r : toRemove) {
                v.remove(r);
            }

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("remove all by iteration ArrayList\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static void removeAllArrayList(List<Integer> values, int[] toRemove, boolean rampUp) {
        List<Integer> asIntegerList = ArrayUtil.asIntegerList(toRemove);
        int sum = 0;
        for (int j = 0; j < (rampUp ? 1 : ITERATIONS); j++) {
            // create a copy so we really remove everytime
            ArrayList<Integer> v = new ArrayList<Integer>(values);

            long start = System.currentTimeMillis();

            v.removeAll(asIntegerList);

            long end = System.currentTimeMillis();

            sum += (end - start);
        }

        if (!rampUp) {
            System.out.println("remove all ArrayList\t\t\t" + (sum / ITERATIONS) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
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
