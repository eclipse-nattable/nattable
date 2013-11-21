/*******************************************************************************
 * Copyright (c) 2013 Stephan Wahlbrink and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 ******************************************************************************/

package org.eclipse.nebula.widgets.nattable.coordinate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * A special list for {@link Range ranges}, which sorts and merges automatically all added ranges.
 * <p>
 * Important Note: Added range objects may be changed if the list is modified. The range objects 
 * must not be changed outside of the list.</p>
 * <p>
 * The add and remove methods of RangeList guarantees that the ranges in a list object are never 
 * empty, do not intersect and touch and are always sorted.</p>
 * <p>
 * The class provides additionally direct {@link #values() access} to the single values described by
 * the ranges. 
 */
public final class RangeList extends ArrayList<Range> implements Set<Range> {
	
	
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Iterator which allows to iterate over the values of a collection with {@link Range} elements.
	 * 
	 * @see RangeList#valuesIterator()
	 */
	public static final class ValueIterator implements IValueIterator {
		
		
		private final Iterator<Range> rangeIter;
		
		private int nextValue;
		private int rangeEnd = -1;
		
		
		/**
		 * Creates a new iterator.
		 * 
		 * @param c the collection to iterate over
		 */
		public ValueIterator(/*@NonNull*/ final Collection<Range> c) {
			this.rangeIter = c.iterator();
		}
		
		
		@Override
		public boolean hasNext() {
			while (this.nextValue >= this.rangeEnd) {
				if (!this.rangeIter.hasNext()) {
					return false;
				}
				final Range range = this.rangeIter.next();
				this.nextValue = range.start;
				this.rangeEnd = range.end;
			}
			return true;
		}
		
		@Override
		public Integer next() {
			return Integer.valueOf(nextValue());
		}
		
		@Override
		public int nextValue() {
			while (this.nextValue >= this.rangeEnd) {
				final Range range = this.rangeIter.next();
				this.nextValue = range.start;
				this.rangeEnd = range.end;
			}
			return this.nextValue++;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	
	private final Values values = new Values();
	
	
	/**
	 * Creates a new empty list
	 */
	public RangeList() {
	}
	
	/**
	 * Creates a new list initially filled with the specified ranges.
	 * 
	 * @param initialRanges the ranges initially added to the list
	 */
	public RangeList(final Range... initialRanges) {
		this();
		
		for (int i = 0; i < initialRanges.length; i++) {
			add(initialRanges[i]);
		}
	}
	
	/**
	 * Creates a new list initially filled with the specified values.
	 * 
	 * @param initialValues the values initially added to the list
	 */
	public RangeList(final int... initialValues) {
		this();
		
		for (int i = 0; i < initialValues.length; i++) {
			this.values.add(initialValues[i]);
		}
	}
	
	
	private int indexOfStart(final int value) {
		int low = 0;
		int high = super.size() - 1;
		while (low <= high) {
			final int mid = (low + high) >>> 1;
			final int midValue = get(mid).start;
			if (value > midValue) {
				low = mid + 1;
			}
			else if (value < midValue) {
				high = mid - 1;
			}
			else {
				return mid;
			}
		}
		return -(low + 1);
	}
	
	
	@Override
	public boolean add(final Range range) {
		if (range.start == range.end) {
			return false;
		}
		int idx = indexOfStart(range.start);
		if (idx >= 0) { // range.start == range1.start
			final Range range1 = get(idx);
			if (range.end <= range1.end) {
				return false;
			}
			range1.end = range.end;
			checkMergeNext(range1, idx + 1);
			return true;
		}
		idx = -(idx + 1); // value > range1.start && value < range2.start
		if (idx > 0) {
			final Range range1 = get(idx - 1);
			if (range.end <= range1.end) {
				return false;
			}
			if (range.start <= range1.end) {
				range1.end = range.end;
				checkMergeNext(range1, idx);
				return true;
			}
		}
		if (idx < size()) {
			final Range range2 = get(idx);
			if (range.end >= range2.start) {
				range2.start = range.start;
				if (range.end > range2.end) {
					range2.end = range.end;
					checkMergeNext(range2, idx + 1);
				}
				return true;
			}
		}
		super.add(idx, range);
		return true;
	}
	
	@Override
	public void add(final int index, final Range element) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addAll(final Collection<? extends Range> c) {
		boolean changed = false;
		for (final Range range : c) {
			changed |= add(range);
		}
		return changed;
	}
	
	@Override
	public boolean addAll(final int index, final Collection<? extends Range> c) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Range set(final int index, final Range element) {
		throw new UnsupportedOperationException();
	}
	
	private void checkMergeNext(final Range range, final int nextIdx) {
		// range.start < range2.start
		while (nextIdx < size()) {
			final Range range2 = get(nextIdx);
			if (range.end < range2.start) {
				break;
			}
			remove(nextIdx);
			if (range.end <= range2.end) {
				range.end = range2.end;
				return;
			}
		}
	}
	
	public boolean remove(final Range range) {
		if (range.size() == 0) {
			return false;
		}
		int idx = indexOfStart(range.start);
		if (idx >= 0) { // range.start == range1.start
			final Range range1 = get(idx);
			if (range.end < range1.end) {
				range1.start = range.end;
				return true;
			}
			super.remove(idx);
			if (range.end == range1.end) {
				return true;
			}
			checkRemoveNext(range, idx);
			return true;
		}
		idx = -(idx + 1); // range.start > range1.start && range.start < range2.start
		if (idx > 0) {
			final Range range1 = get(idx - 1);
			if (range.start < range1.end) {
				if (range.end < range1.end) {
					super.add(idx++, new Range(range.end, range1.end));
				}
				range1.end = range.start;
				checkRemoveNext(range, idx);
				return true;
			}
		}
		return checkRemoveNext(range, idx);
	}
	
	@Override
	public boolean remove(final Object o) {
		if (o instanceof Range) {
			return remove((Range) o);
		}
		return false;
	}
	
	@Override
	public boolean removeAll(final Collection<?> c) {
		boolean changed = false;
		for (final Object o : c) {
			if (o instanceof Range) {
				changed |= remove((Range) o);
			}
		}
		return changed;
	}
	
	private boolean checkRemoveNext(final Range range, final int nextIdx) {
		boolean changed = false;
		// range.start < range2.start
		while (nextIdx < size()) {
			final Range range2 = get(nextIdx);
			if (range.end < range2.start) {
				break;
			}
			if (range.end < range2.end) {
				range2.start = range.end;
				return true;
			}
			remove(nextIdx);
			changed = true;
			if (range.end == range2.end) {
				return true;
			}
		}
		return changed;
	}
	
	
	/**
	 * Ordered set of single values described by the range list.
	 * 
	 * The class provides a similar interface like other Java collections to add, remove and
	 * check for containment and access of values. But the set works on primitive values and allows
	 * to support larger sizes in future.
	 */
	public final class Values implements /*OrderedSet<int>*/ Iterable<Integer> {
		
		
		public boolean isEmpty() {
			return RangeList.this.isEmpty();
		}
		
		public int size() {
			int count = 0;
			final int size = RangeList.super.size();
			for (int i = 0; i < size; i++) {
				count += RangeList.super.get(i).size();
			}
			return count;
		}
		
		public boolean contains(final int value) {
			int idx = indexOfStart(value);
			if (idx >= 0) { // value == range1.start
				return true;
			}
			idx = -(idx + 1); // value > range1.start && value < range2.start
			if (idx > 0) {
				final Range range1 = RangeList.super.get(idx - 1);
				return (value < range1.end);
			}
			return false;
		}
		
		@Override
		public IValueIterator iterator() {
			return new ValueIterator(RangeList.this);
		}
		
		public boolean add(final int value) {
			int idx = indexOfStart(value);
			if (idx >= 0) { // value == range1.start
				return false;
			}
			idx = -(idx + 1); // value > range1.start && value < range2.start
			if (idx > 0) {
				final Range range1 = RangeList.super.get(idx - 1);
				if (value < range1.end) {
					return false;
				}
				if (value == range1.end) {
					range1.end = value + 1;
					checkMergeNext(range1, idx);
					return true;
				}
			}
			if (idx < RangeList.super.size()) {
				final Range range2 = RangeList.super.get(idx);
				if (value == range2.start - 1) {
					range2.start = value;
					return true;
				}
			}
			RangeList.super.add(idx, new Range(value));
			return true;
		}
		
		public boolean remove(final int value) {
			int idx = indexOfStart(value);
			if (idx >= 0) { // value == range1.start
				final Range range1 = RangeList.super.get(idx);
				if (value == range1.end - 1) { // single value
					RangeList.super.remove(idx);
					return true;
				}
				range1.start++;
				return true;
			}
			idx = -(idx + 1); // value > range1.start && value < range2.start
			if (idx > 0) {
				final Range range1 = RangeList.super.get(idx - 1);
				if (value >= range1.end) {
					return false;
				}
				if (value == range1.end - 1) {
					range1.end--;
					return true;
				}
				RangeList.super.add(idx, new Range(value + 1, range1.end));
				range1.end = value;
				return true;
			}
			return false;
		}
		
		public void clear() {
			RangeList.this.clear();
		}
		
		public int first() {
			return RangeList.super.get(0).start;
		}
		
		public int last() {
			return RangeList.super.get(RangeList.super.size() - 1).end - 1;
		}
		
		public Range getRangeOf(final int value) {
			int idx = indexOfStart(value);
			if (idx >= 0) { // value == range1.start
				return RangeList.super.get(idx);
			}
			idx = -(idx + 1); // value > range1.start && value < range2.start
			if (idx > 0) {
				final Range range1 = RangeList.super.get(idx - 1);
				if (value < range1.end) {
					return range1;
				}
			}
			return null;
		}
		
		
		private List<Range> getRangeList() {
			return RangeList.this;
		}
		
		@Override
		public int hashCode() {
			return RangeList.this.hashCode() ^ 345;
		}
		
		@Override
		public boolean equals(final Object obj) {
			return ((this == obj
					|| (obj instanceof Values 
							&& RangeList.this.equals(((Values) obj).getRangeList())) ));
		}
		
	}
	
	/**
	 * Provides direct access to the single values of this list.
	 * 
	 * @return the values of the list
	 */
	public Values values() {
		return this.values;
	}
	
	
	@Deprecated // not recommend
	public static List<Integer> listValues(final Collection<Range> positions) {
		final ArrayList<Integer> list = new ArrayList<Integer>();
		for (final Iterator<Range> iter = positions.iterator(); iter.hasNext(); ) {
			final Range range = iter.next();
			final long sum = (long) list.size() + range.size();
			if (sum > 0xffffff) {
				throw new IndexOutOfBoundsException("" + sum); //$NON-NLS-1$ // TODO implement ranges
			}
			list.ensureCapacity((int) sum);
			for (int position = range.start; position < range.end; position++) {
				list.add(position);
			}
		}
		return list;
	}
	
}
