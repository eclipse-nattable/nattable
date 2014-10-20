/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * Represents an Range of numbers. Example a Range of selected rows: 1 - 100
 * Ranges are inclusive of their start value and not inclusive of their end
 * value, i.e. start &lt;= x &lt; end
 */
public class Range {

    public int start = 0;
    public int end = 0;

    public Range(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int size() {
        return this.end - this.start;
    }

    /**
     * @return TRUE if the range contains the given row position
     */
    public boolean contains(int position) {
        return position >= this.start && position < this.end;
    }

    public boolean overlap(Range range) {
        return (this.start < this.end) && // this is a non-empty range
                (range.start < range.end) && // range parameter is non-empty
                (this.contains(range.start) || this.contains(range.end - 1)
                        || range.contains(this.start) || range.contains(this.end - 1));
    }

    public Set<Integer> getMembers() {
        Set<Integer> members = new HashSet<Integer>();
        for (int i = this.start; i < this.end; i++) {
            members.add(Integer.valueOf(i));
        }
        return members;
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

    public static void sortByStart(List<Range> ranges) {
        Collections.sort(ranges, new Comparator<Range>() {
            @Override
            public int compare(Range range1, Range range2) {
                return Integer.valueOf(range1.start).compareTo(
                        Integer.valueOf(range2.start));
            }
        });
    }

}
