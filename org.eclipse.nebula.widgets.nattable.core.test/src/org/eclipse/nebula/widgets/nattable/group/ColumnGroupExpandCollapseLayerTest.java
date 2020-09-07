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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ColumnHideShowLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ColumnReorderLayerFixture;
import org.junit.Before;
import org.junit.Test;

public class ColumnGroupExpandCollapseLayerTest {

    private static final String TEST_GROUP_NAME = "G1";
    private ColumnGroupModel model;
    private ColumnGroupExpandCollapseLayer expandCollapseLayer;
    private ColumnHideShowLayerFixture underlyingLayer;

    @Before
    public void setup() {
        this.model = new ColumnGroupModel();

        // 10 Columns in total - column 10 hidden
        this.underlyingLayer = new ColumnHideShowLayerFixture(9);
        this.expandCollapseLayer = new ColumnGroupExpandCollapseLayer(
                this.underlyingLayer, this.model);

        this.model.addColumnsIndexesToGroup(TEST_GROUP_NAME, 2, 3, 4, 5);
    }

    @Test
    public void getColumnCountWhenColumnsAddedToTheGroup() throws Exception {
        assertEquals(9, this.expandCollapseLayer.getColumnCount());

        // Collapse and check count
        collapse(3);
        assertEquals(6, this.expandCollapseLayer.getColumnCount());

        // Expand and add a column
        expand(3);
        this.model.addColumnsIndexesToGroup(TEST_GROUP_NAME, 8);

        // Collapse again
        collapse(3);
        assertEquals(5, this.expandCollapseLayer.getColumnCount());
    }

    @Test
    public void getColumnCountWhenColumnsCollapsedAndHidden() throws Exception {
        // Columns 2 and 3 hidden
        this.underlyingLayer.hideColumnPositions(Arrays.asList(2, 3));
        assertEquals(7, this.expandCollapseLayer.getColumnCount());

        collapse(3);
        assertEquals(6, this.expandCollapseLayer.getColumnCount());

        expand(3);
        assertEquals(7, this.expandCollapseLayer.getColumnCount());
    }

    /*
     * Hide show layer 0 1 2(h) 3 4 5 6 7 8 9(h)
     * ------------------------------------------------- Expand/Collapse |<-----
     * CG1 ----->|
     */
    @Test
    public void isFirstVisibleWithFirstColumnHidden() throws Exception {
        this.underlyingLayer.hideColumnPositions(Arrays.asList(2));

        // assertFalse(expandCollapseLayer.isFirstVisibleColumnInGroup(2));
        assertTrue(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(3,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
        assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(4,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
        assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(5,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
    }

    /*
     * Hide show layer 0 1 2(h) 3(h) 4 5 6 7 8 9(h)
     * ------------------------------------------------- Expand/Collapse
     * |<------ CG1 ------->|
     */
    @Test
    public void isFirstVisibleWithFirstTwoColumnsHidden() throws Exception {
        this.underlyingLayer.hideColumnPositions(Arrays.asList(2, 3));

        assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(2,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
        assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(3,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
        assertTrue(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(4,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
        assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(5,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
    }

    /*
     * Hide show layer 0 1 2(h) 3(h) 4(h) 5(h) 6 7 8 9(h)
     * ------------------------------------------------- Expand/Collapse
     * |<--------- CG1 ------->|
     */
    @Test
    public void isFirstVisibleWithAllColumnsHidden() throws Exception {
        this.underlyingLayer.hideColumnPositions(Arrays.asList(2, 3, 4, 5));

        assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(2,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
        assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(3,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
        assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(4,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
        assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(5,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
    }

    @Test
    public void isFirstVisibleWithColumnsReordered() throws Exception {
        // Original indexes : 0 1 2 3 4
        // Indexes reordered : 4 1 0 2 3
        ColumnReorderLayer reorderLayer = new ColumnReorderLayerFixture();
        this.underlyingLayer = new ColumnHideShowLayerFixture(reorderLayer);
        this.expandCollapseLayer = new ColumnGroupExpandCollapseLayer(
                this.underlyingLayer, this.model);

        this.model.clear();
        this.model.addColumnsIndexesToGroup(TEST_GROUP_NAME, 0, 2, 3);

        // Hide index 0
        this.underlyingLayer.hideColumnPositions(Arrays.asList(2));

        assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(0,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
        assertTrue(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(2,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
        assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(3,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
    }

    /*
     * Hide show layer 0 1 2(h) 3(h) 4 5 6 7 8 9(h)
     * ------------------------------------------------- Expand/Collapse
     * |<------- CG1 ----->|
     */
    @Test
    public void isLastVisibleColumnIndexInGroup() throws Exception {
        this.underlyingLayer.hideColumnPositions(Arrays.asList(2, 3));

        assertFalse(ColumnGroupUtils.isLastVisibleColumnIndexInGroup(2,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
        assertFalse(ColumnGroupUtils.isLastVisibleColumnIndexInGroup(3,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
        assertFalse(ColumnGroupUtils.isLastVisibleColumnIndexInGroup(4,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
        assertTrue(ColumnGroupUtils.isLastVisibleColumnIndexInGroup(5,
                this.expandCollapseLayer, this.underlyingLayer, this.model));
    }

    @Test
    public void getVisibleColumnIndexesToTheRight() throws Exception {
        collapse(2);

        List<Integer> indexes = ColumnGroupUtils.getVisibleIndexesToTheRight(2,
                this.expandCollapseLayer, this.underlyingLayer, this.model);
        assertEquals(0, indexes.size());
    }

    /*
     * Hide show layer 0 1 2 3 4 5 6 7 8 9(h)
     * -------------------------------------- Expand/Collapse |<--- CG1 -->|
     */
    @Test
    public void getColumnIndexByPosition() throws Exception {
        assertEquals(2, this.expandCollapseLayer.getColumnIndexByPosition(2));
        assertEquals(3, this.expandCollapseLayer.getColumnIndexByPosition(3));
        assertEquals(4, this.expandCollapseLayer.getColumnIndexByPosition(4));
        assertEquals(5, this.expandCollapseLayer.getColumnIndexByPosition(5));
        assertEquals(9, this.expandCollapseLayer.getColumnCount());

        collapse(3);

        assertEquals(6, this.expandCollapseLayer.getColumnCount());

        assertEquals(2, this.expandCollapseLayer.getColumnIndexByPosition(2));
        assertEquals(6, this.expandCollapseLayer.getColumnIndexByPosition(3));
        assertEquals(7, this.expandCollapseLayer.getColumnIndexByPosition(4));
        assertEquals(8, this.expandCollapseLayer.getColumnIndexByPosition(5));

        assertEquals("[2, 0]", this.expandCollapseLayer.getDataValueByPosition(2, 0));
        assertEquals("[6, 0]", this.expandCollapseLayer.getDataValueByPosition(3, 0));
        assertEquals("[7, 0]", this.expandCollapseLayer.getDataValueByPosition(4, 0));
        assertEquals("[8, 0]", this.expandCollapseLayer.getDataValueByPosition(5, 0));
    }

    private void collapse(int columnIndex) {
        this.model.getColumnGroupByIndex(columnIndex).setCollapsed(true);
    }

    private void expand(int columnIndex) {
        this.model.getColumnGroupByIndex(columnIndex).setCollapsed(false);
    }

}
