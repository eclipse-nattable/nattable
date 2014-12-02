/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ColumnGroupModelTest {

    public static final String TEST_GROUP_NAME_3 = "testGroupName3";
    public static final String TEST_GROUP_NAME_2 = "testGroupName2";
    public static final String TEST_GROUP_NAME_1 = "testGroupName";
    private ColumnGroupModel model;

    @Before
    public void setup() {
        this.model = new ColumnGroupModel();
        this.model.addColumnsIndexesToGroup(TEST_GROUP_NAME_1, 0, 1);
        this.model.addColumnsIndexesToGroup(TEST_GROUP_NAME_2, 7, 8);
        this.model.addColumnsIndexesToGroup(TEST_GROUP_NAME_3, 12, 13);
    }

    @Test
    public void getColumnGroupForIndex() throws Exception {
        Assert.assertEquals(TEST_GROUP_NAME_1, this.model.getColumnGroupByIndex(1)
                .getName());
        Assert.assertEquals(TEST_GROUP_NAME_2, this.model.getColumnGroupByIndex(7)
                .getName());
        Assert.assertEquals(TEST_GROUP_NAME_3, this.model.getColumnGroupByIndex(13)
                .getName());
        Assert.assertNull(this.model.getColumnGroupByIndex(15));

        Assert.assertTrue(this.model.isPartOfAGroup(1));
        Assert.assertTrue(this.model.isPartOfAGroup(7));
        Assert.assertTrue(this.model.isPartOfAGroup(13));
        Assert.assertFalse(this.model.isPartOfAGroup(130));
    }

    @Test
    public void getColumnIndexesInGroup() throws Exception {
        List<Integer> columnIndexesInGroup = this.model.getColumnGroupByIndex(0)
                .getMembers();
        Assert.assertNotNull(columnIndexesInGroup);
        Assert.assertEquals(2, columnIndexesInGroup.size());
        Assert.assertEquals(0, columnIndexesInGroup.get(0).intValue());
        Assert.assertEquals(1, columnIndexesInGroup.get(1).intValue());
    }

    @Test
    public void noColumnGroup() throws Exception {
        ColumnGroup columnGroup = this.model.getColumnGroupByIndex(100);
        Assert.assertNull(columnGroup);
    }

    @Test
    public void isPartOfAGroup() throws Exception {
        Assert.assertTrue(this.model.isPartOfAGroup(7));
        Assert.assertFalse(this.model.isPartOfAGroup(70));
    }

    @Test
    public void collapse() throws Exception {
        collapse(0);

        Assert.assertTrue(isCollapsed(0));
        Assert.assertTrue(isCollapsed(1));

        Assert.assertFalse(isCollapsed(7));
    }

    @Test
    public void expand() throws Exception {
        collapse(7);
        Assert.assertTrue(isCollapsed(7));
        Assert.assertTrue(isCollapsed(8));

        expand(7);
        Assert.assertFalse(isCollapsed(7));
        Assert.assertFalse(isCollapsed(8));
    }

    @Test
    public void getCollapsedColumnCount() throws Exception {
        Assert.assertEquals(0, this.model.getCollapsedColumnCount());

        collapse(0);
        Assert.assertEquals(1, this.model.getCollapsedColumnCount());

        collapse(8);
        Assert.assertEquals(2, this.model.getCollapsedColumnCount());

        expand(8);
        Assert.assertEquals(1, this.model.getCollapsedColumnCount());
    }

    @Test
    public void removeColumnFromGroup() {
        ColumnGroup columnGroup = this.model.getColumnGroupByIndex(12);
        List<Integer> columnIndexesInGroup = columnGroup.getMembers();

        Assert.assertTrue(columnIndexesInGroup.contains(12));
        Assert.assertTrue(columnGroup.removeColumn(12));

        columnGroup = this.model.getColumnGroupByIndex(13);
        columnIndexesInGroup = columnGroup.getMembers();
        Assert.assertEquals(1, columnIndexesInGroup.size());
        Assert.assertTrue(columnGroup.removeColumn(13));

        Assert.assertFalse(this.model.isPartOfAGroup(12));
    }

    @Test
    public void shouldInsertAColumnIndexToAGroup() {
        List<Integer> columnIndexesInGroup = this.model.getColumnGroupByIndex(0)
                .getMembers();

        Assert.assertTrue(2 == columnIndexesInGroup.size());
        Assert.assertTrue(columnIndexesInGroup.contains(new Integer(0)));
        Assert.assertTrue(columnIndexesInGroup.contains(new Integer(1)));

        Assert.assertTrue(this.model.insertColumnIndexes(this.model
                .getColumnGroupByIndex(0).getName(), 4));
        columnIndexesInGroup = this.model.getColumnGroupByIndex(0).getMembers();

        Assert.assertEquals(3, columnIndexesInGroup.size());
        Assert.assertTrue(columnIndexesInGroup.contains(new Integer(0)));
        Assert.assertTrue(columnIndexesInGroup.contains(new Integer(1)));
        Assert.assertTrue(columnIndexesInGroup.contains(new Integer(4)));

        Assert.assertTrue(this.model.isPartOfAGroup(4));
    }

    @Test
    public void shouldNotInsertIntoAnUnbreakableGroup() throws Exception {
        this.model.getColumnGroupByIndex(0).setUnbreakable(true);

        Assert.assertFalse(this.model.insertColumnIndexes(this.model
                .getColumnGroupByIndex(0).getName(), 4));

        List<Integer> columnIndexesInGroup = this.model.getColumnGroupByIndex(0)
                .getMembers();
        Assert.assertEquals(2, columnIndexesInGroup.size());
        Assert.assertTrue(columnIndexesInGroup.contains(new Integer(0)));
        Assert.assertTrue(columnIndexesInGroup.contains(new Integer(1)));
    }

    @Test
    public void shouldFailWhenTryingToInsertSameColumnTwice() {
        this.model.insertColumnIndexes(this.model.getColumnGroupByIndex(0).getName(), 4);
        Assert.assertFalse(this.model.insertColumnIndexes(this.model
                .getColumnGroupByIndex(0).getName(), 4, 1, 0));
    }

    @Test
    public void shouldFindColumnGroupPositionForColumnIndex() {
        Assert.assertEquals(1, this.model.getColumnGroupPositionFromIndex(8));
        Assert.assertEquals(-1, this.model.getColumnGroupPositionFromIndex(11));
    }

    @Test
    public void toggleColumnGroup() throws Exception {
        Assert.assertFalse(isCollapsed(0));

        toggleColumnGroupExpandCollapse(0);
        Assert.assertTrue(isCollapsed(0));

        toggleColumnGroupExpandCollapse(0);
        Assert.assertFalse(isCollapsed(0));
    }

    @Test
    public void isCollapsedByName() throws Exception {
        Assert.assertFalse(this.model.getColumnGroupByName(TEST_GROUP_NAME_1)
                .isCollapsed());

        collapse(0);

        Assert.assertTrue(this.model.getColumnGroupByName(TEST_GROUP_NAME_1)
                .isCollapsed());
        Assert.assertFalse(this.model.getColumnGroupByName(TEST_GROUP_NAME_2)
                .isCollapsed());
    }

    @Test
    public void sizeOfGroup() throws Exception {
        assertEquals(2, this.model.getColumnGroupByIndex(0).getSize());
    }

    @Test
    public void markAsUnbreakable() throws Exception {
        Assert.assertFalse(this.model.isPartOfAnUnbreakableGroup(0));

        this.model.getColumnGroupByIndex(0).setUnbreakable(true);
        Assert.assertTrue(this.model.isPartOfAnUnbreakableGroup(0));
    }

    @Test
    public void shouldNotRemoveFromAnUnbreakableGroup() throws Exception {
        ColumnGroup columnGroup = this.model.getColumnGroupByIndex(7);
        columnGroup.setUnbreakable(true);
        Assert.assertFalse(columnGroup.removeColumn(7));
    }

    @Test
    public void getAllIndexesInGroups() throws Exception {
        List<Integer> indexes = this.model.getAllIndexesInGroups();
        Assert.assertEquals(6, indexes.size());
        Assert.assertTrue(indexes.contains(0));
        Assert.assertTrue(indexes.contains(1));
        Assert.assertTrue(indexes.contains(7));
        Assert.assertTrue(indexes.contains(8));
        Assert.assertTrue(indexes.contains(12));
        Assert.assertTrue(indexes.contains(13));
    }

    @Test
    public void sizeOfStaticColumns() throws Exception {
        this.model.addColumnsIndexesToGroup("TEST_GROUP_NAME_4", 14, 15, 16, 17);
        this.model.insertStaticColumnIndexes("TEST_GROUP_NAME_4", 15, 16);

        Assert.assertEquals(2, sizeOfStaticColumns(14));
        Assert.assertEquals(2, sizeOfStaticColumns(15));
        Assert.assertEquals(2, sizeOfStaticColumns(16));
        Assert.assertEquals(2, sizeOfStaticColumns(17));
    }

    @Test
    public void getStaticColumnIndexesInGroup() throws Exception {
        this.model.addColumnsIndexesToGroup("TEST_GROUP_NAME_4", 14, 15, 16, 17);
        this.model.insertStaticColumnIndexes("TEST_GROUP_NAME_4", 15, 16);

        Assert.assertEquals(2, this.model.getColumnGroupByIndex(14)
                .getStaticColumnIndexes().size());

        Assert.assertEquals(15, this.model.getColumnGroupByIndex(14)
                .getStaticColumnIndexes().get(0).intValue());
        Assert.assertEquals(16, this.model.getColumnGroupByIndex(14)
                .getStaticColumnIndexes().get(1).intValue());
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
