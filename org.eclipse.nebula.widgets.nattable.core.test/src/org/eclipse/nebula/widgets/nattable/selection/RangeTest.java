/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
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
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.junit.Test;

public class RangeTest {

    @Test
    public void shouldContainStartPosition() {
        // 1 cell
        Range range = new Range(2, 3);
        assertTrue(range.contains(2));
    }

    @Test
    public void shouldNotContainEndPosition() {
        // 1 cell
        Range range = new Range(2, 3);
        assertFalse(range.contains(3));
    }

    @Test
    public void shouldContainPosition() {
        // 1 cell
        Range range = new Range(2, 4);
        assertTrue(range.contains(3));
    }

    @Test
    public void shouldIdentifyEquality() {
        assertTrue(new Range(3, 10).equals(new Range(3, 10)));
    }

    @Test
    public void shouldIdentifyNonEquality() {
        assertFalse(new Range(3, 10).equals(new Range(3, 11)));
    }

    @Test
    public void shouldSortRangesByStart() {
        ArrayList<Range> ranges = new ArrayList<>();
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

    @Test
    public void shouldReturnMemberCollection() {
        Set<Integer> members = new Range(3, 10).getMembers();

        assertEquals(7, members.size());
        HashSet<Integer> expectedMembes = new HashSet<>(Arrays.asList(3, 4, 5, 6, 7, 8, 9));
        assertEquals(expectedMembes, members);
    }

    @Test
    public void shouldReturnMemberArray() {
        int[] members = new Range(3, 10).getMembersArray();

        assertEquals(7, members.length);
        int[] expectedMembes = new int[] { 3, 4, 5, 6, 7, 8, 9 };
        assertTrue("Expected member array is not the same as the returned member array", Arrays.equals(expectedMembes, members));
    }
}
