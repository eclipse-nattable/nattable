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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class ArrayUtil {

    private ArrayUtil() {
        // private default constructor for helper class
    }

    public static final String[] STRING_TYPE_ARRAY = new String[] {};
    public static final int[] INT_TYPE_ARRAY = new int[] {};

    public static <T> List<T> asList(T[] array) {
        return new ArrayList<>(ArrayUtil.asCollection(array));
    }

    public static <T> Collection<T> asCollection(T[] array) {
        return Arrays.asList(array);
    }

    public static int[] asIntArray(int... ints) {
        return ints;
    }

    public static List<Integer> asIntegerList(int... ints) {
        return Arrays.stream(ints).boxed().collect(Collectors.toList());
    }

    public static boolean isEmpty(int[] array) {
        return (array == null) || (array.length == 0);
    }

    public static boolean isEmpty(String[] array) {
        return (array == null) || (array.length == 0);
    }

    public static boolean isNotEmpty(int[] array) {
        return !isEmpty(array);
    }

    public static boolean isNotEmpty(String[] array) {
        return !isEmpty(array);
    }

    /**
     * Transforms the given collection of <code>Integer</code>s to an array of
     * primitive <code>int</code> values.
     *
     * @param list
     *            The collection of <code>Integer</code>s to transform
     * @return The array representation of primitive <code>int</code> values.
     */
    public static int[] asIntArray(Collection<Integer> list) {
        int[] ints = new int[list.size()];
        int i = 0;
        for (int fromSet : list) {
            ints[i] = fromSet;
            i++;
        }
        return ints;
    }

    /**
     * Creates a new <code>int</code> array containing the elements between
     * start and end indices.
     * <p>
     * Similar to Apache Commons Lang <code>ArrayUtils.subarray()</code>
     * </p>
     *
     * @param array
     * @param startIndexInclusive
     * @param endIndexExclusive
     * @return
     */
    public static int[] subarray(int[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return new int[0];
        }
        if (startIndexInclusive < 0) {
            startIndexInclusive = 0;
        }
        if (endIndexExclusive > array.length) {
            endIndexExclusive = array.length;
        }
        int newSize = endIndexExclusive - startIndexInclusive;
        if (newSize <= 0) {
            return new int[0];
        }
        int[] subarray = new int[newSize];
        System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
        return subarray;
    }
}
