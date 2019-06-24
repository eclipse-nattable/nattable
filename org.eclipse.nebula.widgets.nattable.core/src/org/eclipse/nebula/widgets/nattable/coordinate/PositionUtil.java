/*******************************************************************************
 * Copyright (c) 2012, 2019 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.coordinate;

import static org.eclipse.nebula.widgets.nattable.util.ObjectUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;

public class PositionUtil {

    /**
     * Finds contiguous numbers in a group of numbers.
     */
    public static List<List<Integer>> getGroupedByContiguous(Collection<Integer> numberCollection) {
        List<Integer> numbers = new ArrayList<Integer>(numberCollection);
        Collections.sort(numbers);

        List<Integer> contiguous = new ArrayList<Integer>();
        List<List<Integer>> grouped = new ArrayList<List<Integer>>();

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
        return grouped;
    }

    /**
     * Creates {@link Range}s out of list of numbers. The contiguous numbers are
     * grouped together in Ranges.
     * <p>
     * Example: 0, 1, 2, 4, 5, 6 will return [[Range(0 - 3)][Range(4 - 7)]]
     * </p>
     * <p>
     * The last number in the Range is not inclusive.
     * </p>
     *
     * @param numbers
     *            The numbers to create the Range collection.
     * @return List of Ranges for the given Collection of numbers.
     */
    public static List<Range> getRanges(Collection<Integer> numbers) {
        List<Range> ranges = null;

        if (isNotEmpty(numbers)) {
            List<List<Integer>> grouped = getGroupedByContiguous(numbers);
            ranges = new ArrayList<Range>(grouped.size());
            for (List<Integer> number : grouped) {
                int start = number.get(0);
                int end = number.get(number.size() - 1) + 1;

                ranges.add(new Range(start, end));
            }
        }

        return ranges != null ? ranges : new ArrayList<Range>();
    }

    /**
     * Creates {@link Range}s out of list of numbers. The contiguous numbers are
     * grouped together in Ranges.
     *
     * <p>
     * Example: 0, 1, 2, 4, 5, 6 will return [[Range(0 - 3)][Range(4 - 7)]]
     * </p>
     * <p>
     * The last number in the Range is not inclusive.
     * </p>
     *
     * @param numbers
     *            The numbers to create the Range collection.
     * @return List of Ranges for the given Collection of numbers.
     */
    public static List<Range> getRanges(int... numbers) {
        List<Integer> numberCollection = new ArrayList<Integer>(numbers.length);

        for (int number : numbers) {
            numberCollection.add(number);
        }

        return getRanges(numberCollection);
    }

    /**
     * Creates an array of positions from the given set of {@link Range}s.
     *
     * <p>
     * Example: [[Range(0 - 3)][Range(4 - 7)]] will return [0, 1, 2, 4, 5, 6].
     * </p>
     * <p>
     * The last number in the Range is not inclusive.
     * </p>
     *
     * @param ranges
     *            a set of ranges to retrieve positions
     * @return an array of positions retrieved from ranges
     *
     * @since 1.6
     */
    public static int[] getPositions(Collection<Range> ranges) {
        if ((ranges == null) || (ranges.size() == 0)) {
            return new int[0];
        }
        // TODO change to Java 8 streams
        // return ranges
        // .stream()
        // .flatMapToInt(r -> IntStream.range(r.start, r.end))
        // .sorted()
        // .toArray();

        Set<Integer> positions = new HashSet<Integer>();
        for (Range r : ranges) {
            for (int i = r.start; i < r.end; i++) {
                positions.add(i);
            }
        }
        int[] result = new int[positions.size()];
        int index = 0;
        for (int pos : positions) {
            result[index] = pos;
            index++;
        }
        Arrays.sort(result);
        return result;
    }

    /**
     * Creates an array of positions from the given set of {@link Range}s.
     *
     * <p>
     * Example: [[Range(0 - 3)][Range(4 - 7)]] will return [0, 1, 2, 4, 5, 6].
     * </p>
     * <p>
     * The last number in the Range is not inclusive.
     * </p>
     *
     * @param ranges
     *            a set of ranges to retrieve positions
     * @return an array of positions retrieved from ranges
     *
     * @since 1.6
     */
    public static int[] getPositions(Range... ranges) {
        return getPositions(Arrays.asList(ranges));
    }

    /**
     * Join a set of ranges if they describe a consecutive range when combined.
     *
     * @param ranges
     *            Collection of {@link Range}s that should be joined.
     * @return The joined {@link Range} or <code>null</code> if {@link Range}s
     *         do not describe a consecutive {@link Range} when combined.
     *
     * @since 1.6
     */
    public static Range joinConsecutiveRanges(Collection<Range> ranges) {
        if (ranges == null || ranges.isEmpty()) {
            return null;
        }

        // put to list
        List<Range> sortedRanges = new ArrayList<Range>(ranges);

        // sort by 1) start, 2) end position
        Collections.sort(sortedRanges, new Comparator<Range>() {

            @Override
            public int compare(Range o1, Range o2) {
                if (o1.start == o2.start) {
                    return Integer.compare(o1.end, o2.end);
                } else {
                    return Integer.compare(o1.start, o2.start);
                }
            }
        });

        int start = sortedRanges.get(0).start;
        int end = sortedRanges.get(0).end;
        for (int i = 1; i < sortedRanges.size(); i++) {
            Range range = sortedRanges.get(i);
            if (range.start > end) {
                return null;
            }
            end = Integer.max(end, range.end);
        }

        return new Range(start, end);
    }

    /**
     * Takes a collection of {@link Range}s and merges them to get
     * {@link Range}s without overlapping. If there are no gaps between the
     * {@link Range}s a single Range will be the result, otherwise multiple
     * {@link Range}s will be in the resulting collection.
     *
     * @param ranges
     *            The {@link Range}s to merge.
     * @return Collection of {@link Range}s without overlapping.
     *
     * @since 1.6
     */
    public static List<Range> mergeRanges(Collection<Range> ranges) {
        Set<Integer> numbers = new TreeSet<Integer>();
        for (Range range : ranges) {
            for (int number = range.start; number < range.end; number++) {
                numbers.add(number);
            }
        }

        return getRanges(numbers);
    }

    /**
     * Calculates the horizontal move direction based on the from and to column
     * position.
     *
     * @param fromPosition
     *            The position from which a move is triggered.
     * @param toPosition
     *            The position to which a move is triggered.
     * @return The direction of the triggered move operation.
     *
     * @see MoveDirectionEnum#LEFT
     * @see MoveDirectionEnum#RIGHT
     * @see MoveDirectionEnum#NONE
     *
     * @since 1.6
     */
    public static MoveDirectionEnum getHorizontalMoveDirection(int fromPosition, int toPosition) {
        if (fromPosition > toPosition) {
            return MoveDirectionEnum.LEFT;
        } else if (fromPosition < toPosition) {
            return MoveDirectionEnum.RIGHT;
        } else {
            return MoveDirectionEnum.NONE;
        }
    }

    /**
     * Calculates the vertical move direction based on the from and to row
     * position.
     *
     * @param fromRowPosition
     *            The row position from which a move is triggered.
     * @param toRowPosition
     *            The row position to which a move is triggered.
     * @return The direction of the triggered move operation.
     *
     * @see MoveDirectionEnum#UP
     * @see MoveDirectionEnum#DOWN
     * @see MoveDirectionEnum#NONE
     *
     * @since 1.6
     */
    public static MoveDirectionEnum getVerticalMoveDirection(int fromRowPosition, int toRowPosition) {
        if (fromRowPosition > toRowPosition) {
            return MoveDirectionEnum.UP;
        } else if (fromRowPosition < toRowPosition) {
            return MoveDirectionEnum.DOWN;
        } else {
            return MoveDirectionEnum.NONE;
        }
    }

}
