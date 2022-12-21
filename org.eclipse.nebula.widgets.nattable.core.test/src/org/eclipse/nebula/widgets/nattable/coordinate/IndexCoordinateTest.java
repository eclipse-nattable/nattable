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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IndexCoordinateTest {

    private IndexCoordinate i1;
    private IndexCoordinate i2;

    @BeforeEach
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
