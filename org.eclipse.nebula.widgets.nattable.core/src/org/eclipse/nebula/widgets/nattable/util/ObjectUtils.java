/*******************************************************************************
 * Copyright (c) 2012, 2024 Original authors and others.
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

public final class ObjectUtils {

    private ObjectUtils() {
        // private default constructor for helper class
    }

    /**
     * Transfers the given iterator to an unmodifiable collection.
     *
     * @param <T>
     *            The type of the objects contained in the iterator.
     * @param iterator
     *            The iterator to transfer.
     * @return Contents of the iterator as an unmodifiable Collection.
     */
    public static <T> Collection<T> asCollection(Iterator<T> iterator) {
        Collection<T> collection = new ArrayList<>();
        return addToCollection(iterator, collection);
    }

    public static <T> List<T> asList(Collection<T> collection) {
        return new ArrayList<>(collection);
    }

    public static int[] asIntArray(Collection<Integer> collection) {
        int[] copy = new int[collection.size()];

        int index = 0;
        for (Integer value : collection) {
            copy[index] = value.intValue();
            index++;
        }

        return copy;
    }

    /**
     * Transfers the given iterator into an unmodifiable ordered collection
     * based on the given comparator.
     *
     * @param <T>
     *            The type of the objects contained in the iterator.
     * @param iterator
     *            The iterator to transfer.
     * @param comparator
     *            The comparator to order the collection.
     * @return Contents of the iterator as an unmodifiable ordered Collection.
     */
    public static <T> Collection<T> asOrderedCollection(Iterator<T> iterator, Comparator<T> comparator) {
        Collection<T> collection = new TreeSet<>(comparator);
        return addToCollection(iterator, collection);
    }

    private static <T> Collection<T> addToCollection(Iterator<T> iterator, Collection<T> collection) {
        while (iterator.hasNext()) {
            T object = iterator.next();
            collection.add(object);
        }
        return Collections.unmodifiableCollection(collection);
    }

    public static <T> String toString(Collection<T> collection) {
        if (collection == null) {
            return "NULL"; //$NON-NLS-1$
        }
        String out = "[ "; //$NON-NLS-1$
        int count = 1;
        for (T object : collection) {
            if (object == null) {
                continue;
            }
            out = out + object.toString();
            if (collection.size() != count) {
                out = out + ";\n"; //$NON-NLS-1$
            }
            count++;
        }
        out = out + " ]"; //$NON-NLS-1$
        return out;
    }

    public static <T> String toString(T[] array) {
        return toString(Arrays.asList(array));
    }

    /**
     *
     * @param <T>
     *            The type of objects in the collection.
     * @param collection
     *            The collection to check.
     * @return <code>true</code> if the given collection is <code>null</code> or
     *         empty.
     */
    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     *
     * @param string
     *            The string to check.
     * @return <code>true</code> if the given string is <code>null</code> or
     *         empty.
     */
    public static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    /**
     *
     * @param string
     *            The string to check.
     * @return <code>true</code> if the given string is not <code>null</code>
     *         and not empty.
     */
    public static boolean isNotEmpty(String string) {
        return string != null && string.length() > 0;
    }

    /**
     *
     * @param <T>
     *            The type of objects in the collection.
     * @param collection
     *            The collection to check.
     * @return <code>true</code> if the given collection is not
     *         <code>null</code> and not empty.
     */
    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }

    /**
     *
     * @param object
     *            The object to check.
     * @return <code>true</code> if the given object reference is
     *         <code>null</code>.
     */
    public static boolean isNull(Object object) {
        return object == null;
    }

    /**
     *
     * @param object
     *            The object to check.
     * @return <code>true</code> if the given object reference is not
     *         <code>null</code>.
     */
    public static boolean isNotNull(Object object) {
        return object != null;
    }

    private static final Random RANDOM = new Random();

    /**
     * @return a random Date
     */
    public static Date getRandomDate() {
        return new Date(RANDOM.nextLong());
    }

    /**
     * @return 4 digit random Integer number
     */
    public static int getRandomNumber() {
        return RANDOM.nextInt(10000);
    }

    /**
     *
     * @param max
     *            the upper bound (exclusive)
     * @return random Integer number between 0 and parameter max
     */
    public static int getRandomNumber(int max) {
        return RANDOM.nextInt(max);
    }

    public static <T> T getLastElement(List<T> list) {
        return list.get(list.size() - 1);
    }

    public static <T> T getFirstElement(List<T> list) {
        return list.get(0);
    }

    /**
     * Checks if the passed collections are equal or not. Checks for content
     * equality and does not take the order of values in the collection into
     * account.
     *
     * @param c1
     *            The first collection.
     * @param c2
     *            The second collection.
     * @return <code>true</code> if the passed collections contain the same
     *         values even in different order, <code>false</code> if the
     *         collections do not contain the same values.
     *
     * @since 2.1
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static boolean collectionsEqual(Collection c1, Collection c2) {
        if ((c1 != null && c2 != null)) {

            // as we check for content equality, double values in the
            // collections do not count, and the performance increases using a
            // HashSet
            HashSet hc1 = new HashSet(c1);
            HashSet hc2 = new HashSet(c2);

            if (hc1.size() == hc2.size()) {

                if (!hc1.equals(hc2)) {
                    // as equality for collections take into account the order
                    // and the elements we perform an additional check if the
                    // same items regardless the order are contained in both
                    // lists
                    for (Object f1 : hc1) {
                        if (!hc2.contains(f1)) {
                            return false;
                        }
                    }
                    // as lists can contain the same element twice, we also
                    // perform a counter check
                    for (Object f2 : hc2) {
                        if (!hc1.contains(f2)) {
                            return false;
                        }
                    }
                }

                return true;
            }
        }

        return false;
    }
}
