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
package org.eclipse.nebula.widgets.nattable.group.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.test.fixture.group.ColumnGroupModelFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Before;
import org.junit.Test;

public class ColumnHeaderReoderDragModeTest {

    private ColumnGroupModelFixture model;
    private ColumnHeaderReorderDragMode dragMode;
    private DataLayerFixture testLayer;

    @Before
    public void setup() {
        this.model = new ColumnGroupModelFixture();
        this.model.addColumnsIndexesToGroup("G4", 18, 19, 20);

        this.model.getColumnGroupByIndex(0).setUnbreakable(true);
        this.model.getColumnGroupByIndex(10).setUnbreakable(true);

        this.testLayer = new DataLayerFixture(20, 10, 100, 20);
        this.dragMode = new ColumnHeaderReorderDragMode(this.model);
    }

    /*
     * Test Setup
     *
     * 0 1 2 3 4 5 6 7 ... 10 11 12 ... 17 18 19 20
     * ------------------------------
     * --------------------------------------------------------- |<- G1 ->| |<--
     * G2 -->| |<--- G3 --->| |<--- G4 --->| |UnBreak.| |UnBreakable |
     */
    @Test
    public void basicReordering() throws Exception {
        assertTrue(this.dragMode.isValidTargetColumnPosition(this.testLayer, 6, 9));
    }

    @Test
    public void shouldNotAllowMovingIntoAnUnbreakableGroup() throws Exception {
        assertFalse(this.dragMode.isValidTargetColumnPosition(this.testLayer, 2, 10));
    }

    @Test
    public void shouldNotAllowMovingOutOfAnUnbreakableGroup() throws Exception {
        assertFalse(this.dragMode.isValidTargetColumnPosition(this.testLayer, 0, 7));
    }

    // Reordering among column group

    @Test
    public void shouldAllowReorderingWithinAnUnbreakableGroup()
            throws Exception {
        assertTrue(this.dragMode.isValidTargetColumnPosition(this.testLayer, 10, 11));
    }

    @Test
    public void shouldAllowReorderingWithinARegularGroup() throws Exception {
        assertTrue(this.dragMode.isValidTargetColumnPosition(this.testLayer, 18, 19));
    }

    // Moving between groups

    @Test
    public void shouldAllowMovingBetweenRegularGroups() throws Exception {
        assertTrue(this.dragMode.isValidTargetColumnPosition(this.testLayer, 3, 19));
    }

    @Test
    public void shouldNotAllowMovingBetweenTwoUnbreakableGroups()
            throws Exception {
        assertFalse(this.dragMode.isValidTargetColumnPosition(this.testLayer, 0, 11));
    }

    @Test
    public void shouldNotAllowMovingFromUnbreakableGroupToRegularGroup()
            throws Exception {
        assertFalse(this.dragMode.isValidTargetColumnPosition(this.testLayer, 0, 3));
    }

    @Test
    public void shouldNotAllowMovingFromRegularGroupToUnbreakableGroup()
            throws Exception {
        assertFalse(this.dragMode.isValidTargetColumnPosition(this.testLayer, 3, 11));
    }
}
