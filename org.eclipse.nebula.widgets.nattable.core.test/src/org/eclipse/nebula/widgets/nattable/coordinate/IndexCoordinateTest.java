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
package org.eclipse.nebula.widgets.nattable.coordinate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class IndexCoordinateTest {

    private IndexCoordinate i1;
    private IndexCoordinate i2;

    @Before
    public void setup() {
        this.i1 = new IndexCoordinate(1, 2);
        this.i2 = new IndexCoordinate(1, 2);
    }

    @Test
    public void testIdentity() {
        assertEquals(this.i1, this.i1);
    }

    @Test
    public void testEquals() {
        assertEquals(this.i1, this.i2);
    }

    @Test
    public void testIdentityHashCode() {
        assertEquals(this.i1.hashCode(), this.i1.hashCode());
    }

    @Test
    public void testHashCode() {
        assertEquals(this.i1.hashCode(), this.i2.hashCode());
    }

}
