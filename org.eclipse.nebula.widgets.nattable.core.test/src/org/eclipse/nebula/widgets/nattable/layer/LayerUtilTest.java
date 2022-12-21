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
package org.eclipse.nebula.widgets.nattable.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LayerUtilTest {

    private ILayer layer;

    @BeforeEach
    public void setup() {
        this.layer = new DataLayerFixture();
    }

    // Column

    @Test
    public void testFindColumnPosition0() {
        assertEquals(0, LayerUtil.getColumnPositionByX(this.layer, 0));
    }

    @Test
    public void testFindColumnPositionAtEnd() {
        assertEquals(4, LayerUtil.getColumnPositionByX(this.layer, 464));
    }

    @Test
    public void testFindColumnPositionOffEnd() {
        assertEquals(-1, LayerUtil.getColumnPositionByX(this.layer, 465));
    }

    @Test
    public void testFindWeirdColumnPosition() {
        assertEquals(0, LayerUtil.getColumnPositionByX(this.layer, 145));
    }

    @Test
    public void testFindWeirdColumnPosition2() {
        assertEquals(2, LayerUtil.getColumnPositionByX(this.layer, 284));
        assertEquals(3, LayerUtil.getColumnPositionByX(this.layer, 285));
    }

    // Row

    @Test
    public void testFindRowPosition0() {
        assertEquals(0, LayerUtil.getRowPositionByY(this.layer, 0));
    }

    @Test
    public void testFindRowPositionAtEnd() {
        assertEquals(6, LayerUtil.getRowPositionByY(this.layer, 364));
    }

    @Test
    public void testFindRowPositionOffEnd() {
        assertEquals(-1, LayerUtil.getRowPositionByY(this.layer, 365));
    }

    @Test
    public void testFindWeirdRowPosition() {
        assertEquals(0, LayerUtil.getRowPositionByY(this.layer, 17));
    }

    @Test
    public void testFindWeirdRowPosition2() {
        assertEquals(1, LayerUtil.getRowPositionByY(this.layer, 42));
        assertEquals(5, LayerUtil.getRowPositionByY(this.layer, 241));
    }

}
