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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColumnGroupModelTest {

    public static final String TEST_GROUP_NAME_3 = "testGroupName3";
    public static final String TEST_GROUP_NAME_2 = "testGroupName2";
    public static final String TEST_GROUP_NAME_1 = "testGroupName";
    private ColumnGroupModel model;

    @BeforeEach
    public void setup() {
        this.model = new ColumnGroupModel();
        this.model.addColumnsIndexesToGroup(TEST_GROUP_NAME_1, 0, 1);
        this.model.addColumnsIndexesToGroup(TEST_GROUP_NAME_2, 7, 8);
        this.model.addColumnsIndexesToGroup(TEST_GROUP_NAME_3, 12, 13);
    }

    @Test
    public void getColumnGroupForIndex() throws Exception {
        assertEquals(TEST_GROUP_NAME_1, this.model.getColumnGroupByIndex(1).getName());
        assertEquals(TEST_GROUP_NAME_2, this.model.getColumnGroupByIndex(7).getName());
        assertEquals(TEST_GROUP_NAME_3, this.model.getColumnGroupByIndex(13).getName());
        assertNull(this.model.getColumnGroupByIndex(15));

        assertTrue(this.model.isPartOfAGroup(1));
        assertTrue(this.model.isPartOfAGroup(7));
        assertTrue(this.model.isPartOfAGroup(13));
        assertFalse(this.model.isPartOfAGroup(130));
    }

    @Test
    public void getColumnIndexesInGroup() throws Exception {
        List<Integer> columnIndexesInGroup = this.model.getColumnGroupByIndex(0).getMembers();
        assertNotNull(columnIndexesInGroup);
        assertEquals(2, columnIndexesInGroup.size());
        assertEquals(0, columnIndexesInGroup.get(0).intValue());
        assertEquals(1, columnIndexesInGroup.get(1).intValue());
    }

    @Test
    public void noColumnGroup() throws Exception {
        ColumnGroup columnGroup = this.model.getColumnGroupByIndex(100);
        assertNull(columnGroup);
    }

    @Test
    public void isPartOfAGroup() throws Exception {
        assertTrue(this.model.isPartOfAGroup(7));
        assertFalse(this.model.isPartOfAGroup(70));
    }

    @Test
    public void collapse() throws Exception {
        collapse(0);

        assertTrue(isCollapsed(0));
        assertTrue(isCollapsed(1));

        assertFalse(isCollapsed(7));
    }

    @Test
    public void expand() throws Exception {
        collapse(7);
        assertTrue(isCollapsed(7));
        assertTrue(isCollapsed(8));

        expand(7);
        assertFalse(isCollapsed(7));
        assertFalse(isCollapsed(8));
    }

    @Test
    public void getCollapsedColumnCount() throws Exception {
        assertEquals(0, this.model.getCollapsedColumnCount());

        collapse(0);
        assertEquals(1, this.model.getCollapsedColumnCount());

        collapse(8);
        assertEquals(2, this.model.getCollapsedColumnCount());

        expand(8);
        assertEquals(1, this.model.getCollapsedColumnCount());
    }

    @Test
    public void removeColumnFromGroup() {
        ColumnGroup columnGroup = this.model.getColumnGroupByIndex(12);
        List<Integer> columnIndexesInGroup = columnGroup.getMembers();

        assertTrue(columnIndexesInGroup.contains(12));
        assertTrue(columnGroup.removeColumn(12));

        columnGroup = this.model.getColumnGroupByIndex(13);
        columnIndexesInGroup = columnGroup.getMembers();
        assertEquals(1, columnIndexesInGroup.size());
        assertTrue(columnGroup.removeColumn(13));

        assertFalse(this.model.isPartOfAGroup(12));
    }

    @Test
    public void shouldInsertAColumnIndexToAGroup() {
        List<Integer> columnIndexesInGroup = this.model.getColumnGroupByIndex(0).getMembers();

        assertTrue(2 == columnIndexesInGroup.size());
        assertTrue(columnIndexesInGroup.contains(new Integer(0)));
        assertTrue(columnIndexesInGroup.contains(new Integer(1)));

        assertTrue(this.model.insertColumnIndexes(this.model.getColumnGroupByIndex(0).getName(), 4));
        columnIndexesInGroup = this.model.getColumnGroupByIndex(0).getMembers();

        assertEquals(3, columnIndexesInGroup.size());
        assertTrue(columnIndexesInGroup.contains(new Integer(0)));
        assertTrue(columnIndexesInGroup.contains(new Integer(1)));
        assertTrue(columnIndexesInGroup.contains(new Integer(4)));

        assertTrue(this.model.isPartOfAGroup(4));
    }

    @Test
    public void shouldNotInsertIntoAnUnbreakableGroup() throws Exception {
        this.model.getColumnGroupByIndex(0).setUnbreakable(true);

        assertFalse(this.model.insertColumnIndexes(this.model.getColumnGroupByIndex(0).getName(), 4));

        List<Integer> columnIndexesInGroup = this.model.getColumnGroupByIndex(0).getMembers();
        assertEquals(2, columnIndexesInGroup.size());
        assertTrue(columnIndexesInGroup.contains(new Integer(0)));
        assertTrue(columnIndexesInGroup.contains(new Integer(1)));
    }

    @Test
    public void shouldFailWhenTryingToInsertSameColumnTwice() {
        this.model.insertColumnIndexes(this.model.getColumnGroupByIndex(0).getName(), 4);
        assertFalse(this.model.insertColumnIndexes(this.model.getColumnGroupByIndex(0).getName(), 4, 1, 0));
    }

    @Test
    public void shouldFindColumnGroupPositionForColumnIndex() {
        assertEquals(1, this.model.getColumnGroupPositionFromIndex(8));
        assertEquals(-1, this.model.getColumnGroupPositionFromIndex(11));
    }

    @Test
    public void toggleColumnGroup() throws Exception {
        assertFalse(isCollapsed(0));

        toggleColumnGroupExpandCollapse(0);
        assertTrue(isCollapsed(0));

        toggleColumnGroupExpandCollapse(0);
        assertFalse(isCollapsed(0));
    }

    @Test
    public void isCollapsedByName() throws Exception {
        assertFalse(this.model.getColumnGroupByName(TEST_GROUP_NAME_1).isCollapsed());

        collapse(0);

        assertTrue(this.model.getColumnGroupByName(TEST_GROUP_NAME_1).isCollapsed());
        assertFalse(this.model.getColumnGroupByName(TEST_GROUP_NAME_2).isCollapsed());
    }

    @Test
    public void sizeOfGroup() throws Exception {
        assertEquals(2, this.model.getColumnGroupByIndex(0).getSize());
    }

    @Test
    public void markAsUnbreakable() throws Exception {
        assertFalse(this.model.isPartOfAnUnbreakableGroup(0));

        this.model.getColumnGroupByIndex(0).setUnbreakable(true);
        assertTrue(this.model.isPartOfAnUnbreakableGroup(0));
    }

    @Test
    public void shouldNotRemoveFromAnUnbreakableGroup() throws Exception {
        ColumnGroup columnGroup = this.model.getColumnGroupByIndex(7);
        columnGroup.setUnbreakable(true);
        assertFalse(columnGroup.removeColumn(7));
    }

    @Test
    public void getAllIndexesInGroups() throws Exception {
        List<Integer> indexes = this.model.getAllIndexesInGroups();
        assertEquals(6, indexes.size());
        assertTrue(indexes.contains(0));
        assertTrue(indexes.contains(1));
        assertTrue(indexes.contains(7));
        assertTrue(indexes.contains(8));
        assertTrue(indexes.contains(12));
        assertTrue(indexes.contains(13));
    }

    @Test
    public void sizeOfStaticColumns() throws Exception {
        this.model.addColumnsIndexesToGroup("TEST_GROUP_NAME_4", 14, 15, 16, 17);
        this.model.insertStaticColumnIndexes("TEST_GROUP_NAME_4", 15, 16);

        assertEquals(2, sizeOfStaticColumns(14));
        assertEquals(2, sizeOfStaticColumns(15));
        assertEquals(2, sizeOfStaticColumns(16));
        assertEquals(2, sizeOfStaticColumns(17));
    }

    @Test
    public void getStaticColumnIndexesInGroup() throws Exception {
        this.model.addColumnsIndexesToGroup("TEST_GROUP_NAME_4", 14, 15, 16, 17);
        this.model.insertStaticColumnIndexes("TEST_GROUP_NAME_4", 15, 16);

        assertEquals(2, this.model.getColumnGroupByIndex(14).getStaticColumnIndexes().size());

        assertEquals(15, this.model.getColumnGroupByIndex(14).getStaticColumnIndexes().get(0).intValue());
        assertEquals(16, this.model.getColumnGroupByIndex(14).getStaticColumnIndexes().get(1).intValue());
    }

    @Test
    public void testCollapseableColumnGroups() {
        assertTrue(this.model.getColumnGroupByIndex(1).isCollapseable());
        assertTrue(this.model.getColumnGroupByIndex(7).isCollapseable());
        assertTrue(this.model.getColumnGroupByIndex(13).isCollapseable());

        ColumnGroupModel nonExpandable = new ColumnGroupModel();
        nonExpandable.setDefaultCollapseable(false);
        nonExpandable.addColumnsIndexesToGroup(TEST_GROUP_NAME_1, 0, 1);
        nonExpandable.addColumnsIndexesToGroup(TEST_GROUP_NAME_2, 7, 8);
        nonExpandable.addColumnsIndexesToGroup(TEST_GROUP_NAME_3, 12, 13);

        assertFalse(nonExpandable.getColumnGroupByIndex(1).isCollapseable());
        assertFalse(nonExpandable.getColumnGroupByIndex(7).isCollapseable());
        assertFalse(nonExpandable.getColumnGroupByIndex(13).isCollapseable());
    }

    private void toggleColumnGroupExpandCollapse(int columnIndex) {
        this.model.getColumnGroupByIndex(columnIndex).toggleCollapsed();
    }

    private boolean isCollapsed(int columnIndex) {
        return this.model.getColumnGroupByIndex(columnIndex).isCollapsed();
    }

    private void collapse(int columnIndex) {
        this.model.getColumnGroupByIndex(columnIndex).setCollapsed(true);
    }

    private void expand(int columnIndex) {
        this.model.getColumnGroupByIndex(columnIndex).setCollapsed(false);
    }

    private int sizeOfStaticColumns(int columnIndex) {
        return this.model.getColumnGroupByIndex(columnIndex)
                .getStaticColumnIndexes().size();
    }

}
