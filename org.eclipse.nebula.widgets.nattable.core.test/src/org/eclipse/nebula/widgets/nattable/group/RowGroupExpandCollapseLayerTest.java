/*******************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroup;
import org.eclipse.nebula.widgets.nattable.group.model.RowGroup;
import org.eclipse.nebula.widgets.nattable.group.model.RowGroupModel;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.junit.Before;
import org.junit.Test;

public class RowGroupExpandCollapseLayerTest {

    private RowGroupModel<Person> model;
    private RowHideShowLayer rowHideShowLayer;
    private RowGroupExpandCollapseLayer<Person> expandCollapseLayer;

    @Before
    public void setup() {
        String[] propertyNames = { "firstName", "lastName", "gender", "married", "birthday" };
        IRowDataProvider<Person> bodyDataProvider =
                new DefaultBodyDataProvider<>(PersonService.getFixedPersons(), propertyNames);
        DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);

        this.rowHideShowLayer = new RowHideShowLayer(bodyDataLayer);
        this.model = new RowGroupModel<>();
        this.model.setDataProvider(bodyDataProvider);
        this.expandCollapseLayer = new RowGroupExpandCollapseLayer<>(this.rowHideShowLayer, this.model);

        // Create a group of rows for the model.
        RowGroup<Person> rowGroup = new RowGroup<>(this.model, "Simpson", false);
        rowGroup.addMemberRow(bodyDataProvider.getRowObject(0));
        rowGroup.addStaticMemberRow(bodyDataProvider.getRowObject(1));
        rowGroup.addMemberRow(bodyDataProvider.getRowObject(2));
        rowGroup.addMemberRow(bodyDataProvider.getRowObject(3));
        rowGroup.addMemberRow(bodyDataProvider.getRowObject(4));
        rowGroup.addMemberRow(bodyDataProvider.getRowObject(5));
        rowGroup.addMemberRow(bodyDataProvider.getRowObject(6));
        rowGroup.addMemberRow(bodyDataProvider.getRowObject(7));
        rowGroup.addMemberRow(bodyDataProvider.getRowObject(8));
        rowGroup.addMemberRow(bodyDataProvider.getRowObject(9));
        this.model.addRowGroup(rowGroup);

        rowGroup = new RowGroup<>(this.model, "Flanders", false);
        rowGroup.addMemberRow(bodyDataProvider.getRowObject(10));
        rowGroup.addStaticMemberRow(bodyDataProvider.getRowObject(11));
        rowGroup.addMemberRow(bodyDataProvider.getRowObject(12));
        rowGroup.addMemberRow(bodyDataProvider.getRowObject(13));
        rowGroup.addMemberRow(bodyDataProvider.getRowObject(14));
        rowGroup.addMemberRow(bodyDataProvider.getRowObject(15));
        rowGroup.addMemberRow(bodyDataProvider.getRowObject(16));
        rowGroup.addMemberRow(bodyDataProvider.getRowObject(17));
        this.model.addRowGroup(rowGroup);
    }

    @Test
    public void getRowCountWhenColumnsAddedToTheGroup() throws Exception {
        assertEquals(18, this.expandCollapseLayer.getRowCount());

        // get Flanders row group
        IRowGroup<Person> group = this.model.getRowGroupForName("Flanders");

        // Collapse and check count
        group.collapse();
        assertEquals(11, this.expandCollapseLayer.getRowCount());

        // Expand and check count
        group.expand();

        // Collapse again
        assertEquals(18, this.expandCollapseLayer.getRowCount());
    }
    //
    // @Test
    // public void getRowCountWhenColumnsAddedToTheGroup() throws Exception {
    // assertEquals(18, this.expandCollapseLayer.getRowCount());
    //
    // // get Flanders row group
    // IRowGroup<Person> group = this.model.getRowGroupForName("Flanders");
    //
    // // Collapse and check count
    // group.collapse();
    // assertEquals(11, this.expandCollapseLayer.getRowCount());
    //
    // // Expand and add a column
    // expand(3);
    // this.model.addColumnsIndexesToGroup(TEST_GROUP_NAME, 8);
    //
    // // Collapse again
    // collapse(3);
    // assertEquals(5, this.expandCollapseLayer.getColumnCount());
    // }

    // @Test
    // public void getColumnCountWhenColumnsCollapsedAndHidden() throws
    // Exception {
    // // Columns 2 and 3 hidden
    // this.underlyingLayer.hideColumnPositions(Arrays.asList(2, 3));
    // assertEquals(7, this.expandCollapseLayer.getColumnCount());
    //
    // collapse(3);
    // assertEquals(6, this.expandCollapseLayer.getColumnCount());
    //
    // expand(3);
    // assertEquals(7, this.expandCollapseLayer.getColumnCount());
    // }
    //
    // /*
    // * Hide show layer 0 1 2(h) 3 4 5 6 7 8 9(h)
    // * ------------------------------------------------- Expand/Collapse
    // |<-----
    // * CG1 ----->|
    // */
    // @Test
    // public void isFirstVisibleWithFirstColumnHidden() throws Exception {
    // this.underlyingLayer.hideColumnPositions(Arrays.asList(2));
    //
    // // assertFalse(expandCollapseLayer.isFirstVisibleColumnInGroup(2));
    // assertTrue(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(3,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(4,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(5,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // }
    //
    // /*
    // * Hide show layer 0 1 2(h) 3(h) 4 5 6 7 8 9(h)
    // * ------------------------------------------------- Expand/Collapse
    // * |<------ CG1 ------->|
    // */
    // @Test
    // public void isFirstVisibleWithFirstTwoColumnsHidden() throws Exception {
    // this.underlyingLayer.hideColumnPositions(Arrays.asList(2, 3));
    //
    // assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(2,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(3,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // assertTrue(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(4,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(5,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // }
    //
    // /*
    // * Hide show layer 0 1 2(h) 3(h) 4(h) 5(h) 6 7 8 9(h)
    // * ------------------------------------------------- Expand/Collapse
    // * |<--------- CG1 ------->|
    // */
    // @Test
    // public void isFirstVisibleWithAllColumnsHidden() throws Exception {
    // this.underlyingLayer.hideColumnPositions(Arrays.asList(2, 3, 4, 5));
    //
    // assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(2,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(3,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(4,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(5,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // }
    //
    // @Test
    // public void isFirstVisibleWithColumnsReordered() throws Exception {
    // // Original indexes : 0 1 2 3 4
    // // Indexes reordered : 4 1 0 2 3
    // ColumnReorderLayer reorderLayer = new ColumnReorderLayerFixture();
    // this.underlyingLayer = new ColumnHideShowLayerFixture(reorderLayer);
    // this.expandCollapseLayer = new ColumnGroupExpandCollapseLayer(
    // this.underlyingLayer, this.model);
    //
    // this.model.clear();
    // this.model.addColumnsIndexesToGroup(TEST_GROUP_NAME, 0, 2, 3);
    //
    // // Hide index 0
    // this.underlyingLayer.hideColumnPositions(Arrays.asList(2));
    //
    // assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(0,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // assertTrue(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(2,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // assertFalse(ColumnGroupUtils.isFirstVisibleColumnIndexInGroup(3,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // }
    //
    // /*
    // * Hide show layer 0 1 2(h) 3(h) 4 5 6 7 8 9(h)
    // * ------------------------------------------------- Expand/Collapse
    // * |<------- CG1 ----->|
    // */
    // @Test
    // public void isLastVisibleColumnIndexInGroup() throws Exception {
    // this.underlyingLayer.hideColumnPositions(Arrays.asList(2, 3));
    //
    // assertFalse(ColumnGroupUtils.isLastVisibleColumnIndexInGroup(2,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // assertFalse(ColumnGroupUtils.isLastVisibleColumnIndexInGroup(3,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // assertFalse(ColumnGroupUtils.isLastVisibleColumnIndexInGroup(4,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // assertTrue(ColumnGroupUtils.isLastVisibleColumnIndexInGroup(5,
    // this.expandCollapseLayer, this.underlyingLayer, this.model));
    // }
    //
    // @Test
    // public void getVisibleColumnIndexesToTheRight() throws Exception {
    // collapse(2);
    //
    // List<Integer> indexes = ColumnGroupUtils.getVisibleIndexesToTheRight(2,
    // this.expandCollapseLayer, this.underlyingLayer, this.model);
    // assertEquals(0, indexes.size());
    // }
    //
    // /*
    // * Hide show layer 0 1 2 3 4 5 6 7 8 9(h)
    // * -------------------------------------- Expand/Collapse |<--- CG1 -->|
    // */
    // @Test
    // public void getColumnIndexByPosition() throws Exception {
    // assertEquals(2, this.expandCollapseLayer.getColumnIndexByPosition(2));
    // assertEquals(3, this.expandCollapseLayer.getColumnIndexByPosition(3));
    // assertEquals(4, this.expandCollapseLayer.getColumnIndexByPosition(4));
    // assertEquals(5, this.expandCollapseLayer.getColumnIndexByPosition(5));
    // assertEquals(9, this.expandCollapseLayer.getColumnCount());
    //
    // collapse(3);
    //
    // assertEquals(6, this.expandCollapseLayer.getColumnCount());
    //
    // assertEquals(2, this.expandCollapseLayer.getColumnIndexByPosition(2));
    // assertEquals(6, this.expandCollapseLayer.getColumnIndexByPosition(3));
    // assertEquals(7, this.expandCollapseLayer.getColumnIndexByPosition(4));
    // assertEquals(8, this.expandCollapseLayer.getColumnIndexByPosition(5));
    //
    // assertEquals("[2, 0]", this.expandCollapseLayer.getDataValueByPosition(2,
    // 0));
    // assertEquals("[6, 0]", this.expandCollapseLayer.getDataValueByPosition(3,
    // 0));
    // assertEquals("[7, 0]", this.expandCollapseLayer.getDataValueByPosition(4,
    // 0));
    // assertEquals("[8, 0]", this.expandCollapseLayer.getDataValueByPosition(5,
    // 0));
    // }

}