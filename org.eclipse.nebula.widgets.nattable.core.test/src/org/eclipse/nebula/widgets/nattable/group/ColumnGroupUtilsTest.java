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
package org.eclipse.nebula.widgets.nattable.group;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.nebula.widgets.nattable.layer.stack.ColumnGroupBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.group.ColumnGroupModelFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColumnGroupUtilsTest {

    private ColumnGroupModel model;
    private ColumnGroupBodyLayerStack bodyStack;

    /*
     * Test fixture
     *
     * 0 1 2 3 4 5 6 ... 8 9 10 11 12
     * ------------------------------------------------------------------ |<- G1
     * ->| |<-- G2 -->| |<- G4 ->|<--- G3 --->|
     */
    @BeforeEach
    public void setup() {
        this.model = new ColumnGroupModelFixture();
        this.model.addColumnsIndexesToGroup("G4", 8, 9);

        this.bodyStack = new ColumnGroupBodyLayerStack(new DataLayerFixture(20, 10,
                10, 20), this.model);

        new NatTableFixture(this.bodyStack); // Inits client area
    }

    @Test
    public void isRightEdgeOfAColumnGroup() throws Exception {
        assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(this.bodyStack, 0, 0,
                this.model));

        // 1
        assertTrue(ColumnGroupUtils.isRightEdgeOfAColumnGroup(this.bodyStack, 1, 1,
                this.model));

        assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(this.bodyStack, 2, 2,
                this.model));
        assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(this.bodyStack, 3, 3,
                this.model));

        // 4
        assertTrue(ColumnGroupUtils.isRightEdgeOfAColumnGroup(this.bodyStack, 4, 4,
                this.model));

        assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(this.bodyStack, 5, 5,
                this.model));
        assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(this.bodyStack, 6, 6,
                this.model));
        assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(this.bodyStack, 7, 7,
                this.model));
        assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(this.bodyStack, 8, 8,
                this.model));

        // 9
        assertTrue(ColumnGroupUtils.isRightEdgeOfAColumnGroup(this.bodyStack, 9, 9,
                this.model));

        assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(this.bodyStack, 10,
                10, this.model));
        assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(this.bodyStack, 11,
                11, this.model));

        // 12
        assertTrue(ColumnGroupUtils.isRightEdgeOfAColumnGroup(this.bodyStack, 12,
                12, this.model));

        assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(this.bodyStack, 13,
                13, this.model));
        assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(this.bodyStack, 14,
                14, this.model));
    }

    @Test
    public void isLeftEdgeOfAColumnGroup() throws Exception {
        // 0
        assertTrue(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(this.bodyStack, 0, 0,
                this.model));

        assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(this.bodyStack, 1, 1,
                this.model));
        assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(this.bodyStack, 2, 2,
                this.model));

        // 3
        assertTrue(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(this.bodyStack, 3, 3,
                this.model));

        assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(this.bodyStack, 4, 4,
                this.model));
        assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(this.bodyStack, 5, 5,
                this.model));
        assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(this.bodyStack, 6, 6,
                this.model));
        assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(this.bodyStack, 7, 7,
                this.model));

        // 8
        assertTrue(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(this.bodyStack, 8, 8,
                this.model));

        assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(this.bodyStack, 9, 9,
                this.model));

        // 10
        assertTrue(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(this.bodyStack, 10, 10,
                this.model));

        assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(this.bodyStack, 11,
                11, this.model));
        assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(this.bodyStack, 12,
                12, this.model));
        assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(this.bodyStack, 13,
                13, this.model));
        assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(this.bodyStack, 14,
                14, this.model));
    }
}
