/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.junit.Test;

public class RangeTest {

	@Test
	public void range() throws Exception {
		//1 cell
		Range range = new Range(2, 3);

		assertTrue(range.contains(2));
		assertFalse(range.contains(3));
	}

	@Test
	public void testEquality() throws Exception {
		assertTrue(new Range(3,10).equals(new Range(3,10)));
		assertFalse(new Range(3,10).equals(new Range(3,11)));
	}

	@Test
	public void sortByStart() throws Exception {
		List<Range> ranges = new ArrayList<Range>();
		ranges.add(new Range(3, 5));
		ranges.add(new Range(3, 7));
		ranges.add(new Range(20, 25));
		ranges.add(new Range(2, 16));

		Range.sortByStart(ranges);

		assertTrue(ranges.get(0).start == 2);
		assertTrue(ranges.get(1).start == 3);
		assertTrue(ranges.get(2).start == 3);
		assertTrue(ranges.get(3).start == 20);
	}

	@SuppressWarnings("boxing")
	@Test
	public void getMembers() throws Exception {
		Set<Integer> members = new Range(3, 10).getMembers();

		assertEquals(7, members.size());
		HashSet<Integer> expectedMembes = new HashSet<Integer>(Arrays.asList(3, 4, 5, 6, 7, 8, 9));
		assertEquals(expectedMembes, members);
	}
}
