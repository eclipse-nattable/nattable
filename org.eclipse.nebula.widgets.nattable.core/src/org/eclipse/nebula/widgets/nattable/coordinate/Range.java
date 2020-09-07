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
package org.eclipse.nebula.widgets.nattable.coordinate;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a consecutive range of numbers, e.g. a range of selected rows: 1 -
 * 100. Ranges are inclusive of their start value and exclusive of their end
 * value, i.e. start &lt;= x &lt; end
 */
public class Range {

    public int start = 0;
    public int end = 0;

    /**
     * Create a new {@link Range}.
     *
     * @param start
     *            The start position inclusive.
     * @param end
     *            The end position exclusive.
     */
    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     *
     * @return The size of this range.
     */
    public int size() {
        return this.end - this.start;
    }

    /**
     * Check if the given position is contained in this {@link Range}.
     *
     * @return <code>true</code> if the range contains the given position.
     */
    public boolean contains(int position) {
        return position >= this.start && position < this.end;
    }

    /**
     * Check if the given {@link Range} overlaps this {@link Range}.
     *
     * @param range
     *            The {@link Range} to check.
     * @return <code>true</code> if the given {@link Range} contains positions
     *         that are also contained in this {@link Range}.
     */
    public boolean overlap(Range range) {
        return (this.start < this.end) && // this is a non-empty range
                (range.start < range.end) && // range parameter is non-empty
                (this.contains(range.start) || this.contains(range.end - 1)
                        || range.contains(this.start) || range.contains(this.end - 1));
    }

    /**
     *
     * @return The values represented by this {@link Range}.
     */
    public Set<Integer> getMembers() {
        Set<Integer> members = new HashSet<Integer>();
        for (int i = this.start; i < this.end; i++) {
            members.add(Integer.valueOf(i));
        }
        return members;
    }

    /**
     *
     * @return The values represented by this {@link Range}.
     * @since 2.0
     */
    public int[] getMembersArray() {
        // iterating this way is faster than using IntStream.range()
        int[] result = new int[this.end - this.start];
        int i = 0;
        for (int pos = this.start; pos < this.end; pos++) {
            result[i] = pos;
            i++;
        }

        return result;
    }

    @Override
    public String toString() {
        return "Range[" + this.start + "," + this.end + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Range other = (Range) obj;
        if (this.end != other.end)
            return false;
        if (this.start != other.start)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.end;
        result = prime * result + this.start;
        return result;
    }

    /**
     * Helper method to sort a list of {@link Range} objects by their start
     * position.
     *
     * @param ranges
     *            The {@link Range} list to sort.
     */
    public static void sortByStart(List<Range> ranges) {
        Collections.sort(ranges, Comparator.comparing(r -> r.start));
    }

}
