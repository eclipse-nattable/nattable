package org.eclipse.nebula.widgets.nattable.coordinate;

import static org.junit.Assert.*;

import org.junit.Test;

public class RangeTest {

    @Test
    public void testAdjacentRangeOverlap() {
        assertFalse(new Range(0, 1).overlap(new Range(1, 2)));
    }
    
    @Test
    public void testEmptyRangeOverlap() {
        assertFalse(new Range(0, 1).overlap(new Range(1, 1)));
        assertFalse(new Range(1, 1).overlap(new Range(1, 2)));
        assertFalse(new Range(1, 1).overlap(new Range(0, 2)));
    }

}
