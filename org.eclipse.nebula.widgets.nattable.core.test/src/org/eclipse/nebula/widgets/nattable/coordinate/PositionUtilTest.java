/*******************************************************************************
 * Copyright (c) 2012, 2018 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.coordinate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.junit.Test;

public class PositionUtilTest {

    @Test
    public void getGroupedByContiguous() {
        List<List<Integer>> groupedByContiguous =
                PositionUtil.getGroupedByContiguous(Arrays.asList(0, 1, 2, 4, 5));

        assertEquals(2, groupedByContiguous.size());

        assertEquals(0, groupedByContiguous.get(0).get(0).intValue());
        assertEquals(1, groupedByContiguous.get(0).get(1).intValue());
        assertEquals(2, groupedByContiguous.get(0).get(2).intValue());

        assertEquals(4, groupedByContiguous.get(1).get(0).intValue());
        assertEquals(5, groupedByContiguous.get(1).get(1).intValue());
    }

    @Test
    public void getGroupedByContiguous2() {
        List<List<Integer>> groupedByContiguous =
                PositionUtil.getGroupedByContiguous(Arrays.asList(0, 1, 2, 5, 7, 8, 10));

        assertEquals(4, groupedByContiguous.size());
        assertEquals(0, groupedByContiguous.get(0).get(0).intValue());
        assertEquals(1, groupedByContiguous.get(0).get(1).intValue());
        assertEquals(2, groupedByContiguous.get(0).get(2).intValue());

        assertEquals(5, groupedByContiguous.get(1).get(0).intValue());

        assertEquals(7, groupedByContiguous.get(2).get(0).intValue());
        assertEquals(8, groupedByContiguous.get(2).get(1).intValue());

        assertEquals(10, groupedByContiguous.get(3).get(0).intValue());
    }

    @Test
    public void groupByContinuousForEmptyCollection() {
        List<List<Integer>> groupedByContiguous =
                PositionUtil.getGroupedByContiguous(new ArrayList<Integer>());

        assertEquals(1, groupedByContiguous.size());
        assertTrue(ObjectUtils.isEmpty(groupedByContiguous.get(0)));
    }

    @Test
    public void getRanges() {
        List<Range> ranges = PositionUtil.getRanges(Arrays.asList(0, 1, 2, 5, 8, 9, 10));
        assertEquals(3, ranges.size());

        assertEquals(0, ranges.get(0).start);
        assertEquals(3, ranges.get(0).end);

        assertEquals(5, ranges.get(1).start);
        assertEquals(6, ranges.get(1).end);

        assertEquals(8, ranges.get(2).start);
        assertEquals(11, ranges.get(2).end);
    }

    @Test
    public void getRangesForAnEmptyCollection() {
        List<Range> ranges = PositionUtil.getRanges(new ArrayList<Integer>());
        assertEquals(0, ranges.size());
    }

    @Test
    public void getPositionsFromRanges() {
        Set<Range> ranges = new HashSet<>();
        ranges.add(new Range(0, 3));
        ranges.add(new Range(4, 7));

        int[] expected = new int[] { 0, 1, 2, 4, 5, 6 };
        int[] result = PositionUtil.getPositions(ranges);

        assertEquals(expected.length, result.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result[i]);
        }
    }

}
