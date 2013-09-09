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

import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * Represents an Range of numbers.
 * Example a Range of selected rows: 1 - 100
 * Ranges are inclusive of their start value and not inclusive of their end value, i.e. start &lt;= x &lt; end
 */
public class Range {

	public int start = 0;
	public int end = 0;


	/**
	 * Creates a new range with the specified start and end values.
	 */
	public Range(final int start, final int end) {
		if (start > end) {
			throw new IllegalArgumentException("start: " + start + " > end: " + end); //$NON-NLS-1$ //$NON-NLS-2$
		}
		this.start = start;
		this.end = end;
	}
	
	/**
	 * Creates a new range which contains the specified single value.
	 */
	public Range(final int value) {
		this.start = value;
		this.end = value + 1;
	}
	
	
	public int size() {
		return end - start;
	}
	
	/**
	 * @return TRUE if the range contains the given row position
	 */
	public boolean contains(int position) {
		return position >= start && position < end;
	}

	public boolean overlap(Range range) {
		return
				(start < end) &&  // this is a non-empty range
				(range.start < range.end) &&  // range parameter is non-empty
				(this.contains(range.start) || this.contains(range.end - 1) || range.contains(start) || range.contains(end - 1));
	}

	public Set<Integer> getMembers() {
		Set<Integer> members = new HashSet<Integer>();
		for (int i = start; i < end; i++) {
			members.add(Integer.valueOf(i));
		}
		return members;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Range)) {
			return false;
		}
		final Range other = (Range) obj;
		return (this.start == other.start && this.end == other.end);
	}
	
	@Override
	public String toString() {
		return "Range [" + start + ", " + end + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}


	public static void sortByStart(List<Range> ranges) {
		Collections.sort(ranges, new Comparator<Range>() {
			public int compare(Range range1, Range range2) {
				return Integer.valueOf(range1.start).compareTo(
						Integer.valueOf(range2.start));
			}
		});
	}

}
