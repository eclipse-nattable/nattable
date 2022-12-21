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

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PositionCoordinateTest {

    private PositionCoordinate p1;
    private PositionCoordinate p2;

    @BeforeEach
    public void setup() {
        ILayer layer = new DataLayerFixture();
        this.p1 = new PositionCoordinate(layer, 1, 2);
        this.p2 = new PositionCoordinate(layer, 1, 2);
    }

    @Test
    public void testIdentity() {
        assertEquals(this.p1, this.p1);
    }

    @Test
    public void testEquals() {
        assertEquals(this.p1, this.p2);
    }

    @Test
    public void testIdentityHashCode() {
        assertEquals(this.p1.hashCode(), this.p1.hashCode());
    }

    @Test
    public void testHashCode() {
        assertEquals(this.p1.hashCode(), this.p2.hashCode());
    }

}
