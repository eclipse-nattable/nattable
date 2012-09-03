/*******************************************************************************
 * Copyright (c) 2012 Edwin Park and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Edwin Park - initial API and implementation
 ******************************************************************************/
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
