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
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 460052
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnsAndGroupsCommand;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.group.ColumnGroupModelFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseDataLayerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColumnGroupReorderLayerTest {

    private ColumnGroupReorderLayer layer;
    private ColumnGroupModelFixture modelFixture;
    public ColumnReorderLayer reorderLayer;

    @BeforeEach
    public void setUp() {
        this.modelFixture = new ColumnGroupModelFixture();
        this.reorderLayer = new ColumnReorderLayer(new BaseDataLayerFixture(24, 20));
        this.layer = new ColumnGroupReorderLayer(this.reorderLayer, this.modelFixture);
    }

    @Test
    public void shouldDragLeftAndMoveColumnIntoGroup() {
        // Drag left into group containing (0,1)
        ColumnReorderCommand command = new ColumnReorderCommand(this.layer, 5, 1);
        this.layer.doCommand(command);

        assertEquals(ColumnGroupModelFixture.TEST_GROUP_1,
                this.modelFixture.getColumnGroupByIndex(5).getName());
        assertEquals(ColumnGroupModelFixture.TEST_GROUP_1,
                this.modelFixture.getColumnGroupByIndex(1).getName());
        assertEquals(ColumnGroupModelFixture.TEST_GROUP_1,
                this.modelFixture.getColumnGroupByIndex(0).getName());

        assertEquals(3, getColumnIndexesInGroup(5).size());

        assertEquals(0, this.reorderLayer.getColumnIndexByPosition(0));
        assertEquals(5, this.reorderLayer.getColumnIndexByPosition(1));
        assertEquals(1, this.reorderLayer.getColumnIndexByPosition(2));
    }

    @Test
    public void shouldDragLeftAndRemoveColumnFromGroup() {
        // Drag left out of group
        ColumnReorderCommand command = new ColumnReorderCommand(this.layer, 3, 2);
        this.layer.doCommand(command);

        assertNull(this.modelFixture.getColumnGroupByIndex(3));
        assertNull(this.modelFixture.getColumnGroupByIndex(2));

        assertEquals(1, getColumnIndexesInGroup(4).size());
    }

    @Test
    public void shouldDragRightAndAddColumnToGroup() {
        final int fromColumnIndex = 2;
        final int toColumnIndex = 4;

        // Drag right into group
        ColumnReorderCommand command = new ColumnReorderCommand(this.layer,
                fromColumnIndex, toColumnIndex);
        this.layer.doCommand(command);

        assertEquals(ColumnGroupModelFixture.TEST_GROUP_2,
                this.modelFixture.getColumnGroupByIndex(fromColumnIndex).getName());
        assertEquals(ColumnGroupModelFixture.TEST_GROUP_2,
                this.modelFixture.getColumnGroupByIndex(toColumnIndex).getName());
        assertEquals(ColumnGroupModelFixture.TEST_GROUP_2,
                this.modelFixture.getColumnGroupByIndex(4).getName());
        assertEquals(3, getColumnIndexesInGroup(fromColumnIndex).size());
    }

    @Test
    public void shouldDragFirstCellRightAndAddColumnToGroup() {
        ColumnGroup columnGroup = this.modelFixture.getColumnGroupByIndex(0);
        columnGroup.removeColumn(0);
        assertEquals(1, getColumnIndexesInGroup(1).size());

        this.modelFixture.insertColumnIndexes(ColumnGroupModelFixture.TEST_GROUP_1, 2);
        assertEquals(2, getColumnIndexesInGroup(1).size());

        // Drag right into group
        ColumnReorderCommand command = new ColumnReorderCommand(this.layer, 0, 2);
        this.layer.doCommand(command);

        final int fromColumnIndex = 0;
        final int toColumnIndex = 2;

        assertEquals(ColumnGroupModelFixture.TEST_GROUP_1,
                this.modelFixture.getColumnGroupByIndex(fromColumnIndex).getName());
        assertEquals(ColumnGroupModelFixture.TEST_GROUP_1,
                this.modelFixture.getColumnGroupByIndex(toColumnIndex).getName());
        assertEquals(ColumnGroupModelFixture.TEST_GROUP_1,
                this.modelFixture.getColumnGroupByIndex(2).getName());
        assertEquals(3, getColumnIndexesInGroup(fromColumnIndex).size());
    }

    @Test
    public void shouldDragRightAndKeepColumnInGroup() {
        // Index in CG: 0,1
        // Drag 0 -> 1

        assertEquals(2, getColumnIndexesInGroup(0).size());

        // Drag right out of group
        ColumnReorderCommand command = new ColumnReorderCommand(this.layer, 0, 2);
        this.layer.doCommand(command);

        assertEquals(2, getColumnIndexesInGroup(1).size());
        assertNotNull(this.modelFixture.getColumnGroupByIndex(0));
        assertEquals(ColumnGroupModelFixture.TEST_GROUP_1,
                this.modelFixture.getColumnGroupByIndex(1).getName());
    }

    @Test
    public void shouldDragRightAndRemoveColumnFromGroup() {
        // Index in CG: 0,1
        // Drag 1 -> 1

        assertEquals(2, getColumnIndexesInGroup(0).size());

        // Drag right out of group
        ColumnReorderCommand command = new ColumnReorderCommand(this.layer, 1, 2);
        this.layer.doCommand(command);

        assertEquals(1, getColumnIndexesInGroup(0).size());
        assertNull(this.modelFixture.getColumnGroupByIndex(1));
    }

    @Test
    public void shouldDragRightAndNotRemoveFromAnUnbreakableGroup() {
        assertEquals(2, getColumnIndexesInGroup(0).size());

        setGroupUnBreakable(0);
        this.layer.doCommand(new ColumnReorderCommand(this.layer, 0, 1));

        assertEquals(2, getColumnIndexesInGroup(0).size());
    }

    @Test
    public void shouldRemoveFromOneGroupAndAddToAnother() {
        final int fromColumnIndex = 3;
        final int toColumnIndex = 1;

        assertEquals(ColumnGroupModelFixture.TEST_GROUP_2,
                this.modelFixture.getColumnGroupByIndex(3).getName());
        assertEquals(2, getColumnIndexesInGroup(3).size());
        assertEquals(ColumnGroupModelFixture.TEST_GROUP_1,
                this.modelFixture.getColumnGroupByIndex(1).getName());
        assertEquals(2, getColumnIndexesInGroup(1).size());

        // Swap members of column groups
        ColumnReorderCommand command =
                new ColumnReorderCommand(this.layer, fromColumnIndex, toColumnIndex);
        this.layer.doCommand(command);

        assertEquals(ColumnGroupModelFixture.TEST_GROUP_1,
                this.modelFixture.getColumnGroupByIndex(3).getName());
        assertEquals(3, getColumnIndexesInGroup(3).size());
        assertEquals(ColumnGroupModelFixture.TEST_GROUP_2,
                this.modelFixture.getColumnGroupByIndex(4).getName());
        assertEquals(1, getColumnIndexesInGroup(4).size());
    }

    @Test
    public void shouldNotMoveColumnBetweenGroupsIfEitherGroupIsUnbreakable() {
        setGroupUnBreakable(3);

        ColumnReorderCommand command = new ColumnReorderCommand(this.layer, 3, 1);
        this.layer.doCommand(command);

        this.modelFixture.assertUnchanged();
    }

    @Test
    public void shouldLeaveModelUnchangedOnDragLeftWithinSameGroup() {
        this.modelFixture.assertTestGroup3IsUnchanged();

        // Drag right and swap positions in group
        ColumnReorderCommand command = new ColumnReorderCommand(this.layer, 11, 10);
        this.layer.doCommand(command);

        // The group remains unchanged - order not tracked
        this.modelFixture.assertTestGroup3IsUnchanged();
    }

    @Test
    public void shouldLeaveModelUnchangedOnDragRightWithinSameGroup() {
        this.modelFixture.assertTestGroup3IsUnchanged();

        // Drag right and swap positions in group
        ColumnReorderCommand command = new ColumnReorderCommand(this.layer, 10, 12);
        this.layer.doCommand(command);

        this.modelFixture.assertTestGroup3IsUnchanged();
    }

    @Test
    public void shouldReorderEntireColumnGroup() {
        assertEquals(3, this.reorderLayer.getColumnIndexByPosition(3));
        assertEquals(4, this.reorderLayer.getColumnIndexByPosition(4));

        ReorderColumnGroupCommand reorderGroupCommand =
                new ReorderColumnGroupCommand(this.layer, 3, 13);
        this.layer.doCommand(reorderGroupCommand);

        assertEquals(3, this.reorderLayer.getColumnIndexByPosition(11));
        assertEquals(4, this.reorderLayer.getColumnIndexByPosition(12));
    }

    @Test
    public void shouldReorderEntireColumnGroupAndAddAColumnToCollapsedGroup() {
        collapse(3);
        this.layer.reorderColumnGroup(3, 13);

        assertEquals(3, this.reorderLayer.getColumnIndexByPosition(11));
        assertEquals(4, this.reorderLayer.getColumnIndexByPosition(12));

        this.layer.doCommand(new ColumnReorderCommand(this.layer, 16, 12));
        assertEquals(3, getColumnIndexesInGroup(3).size());
    }

    /*
     * .. 3 4 5 6 7 ... ------------------------------------- |<- G2 ->||<- G4
     * ->|
     */
    @Test
    public void adjacentColumnGroups() {
        this.modelFixture.addColumnsIndexesToGroup("G4", 5, 6);

        // Drag between the two groups
        ColumnReorderCommand command = new ColumnReorderCommand(this.layer, 2, 4);
        this.layer.doCommand(command);

        assertEquals(3, getColumnIndexesInGroup(3).size());
        assertTrue(getColumnIndexesInGroup(3).contains(Integer.valueOf(2)));
        assertTrue(getColumnIndexesInGroup(3).contains(Integer.valueOf(4)));
        assertTrue(getColumnIndexesInGroup(3).contains(Integer.valueOf(4)));

        assertEquals(2, getColumnIndexesInGroup(5).size());
        assertTrue(getColumnIndexesInGroup(5).contains(Integer.valueOf(5)));
        assertTrue(getColumnIndexesInGroup(5).contains(Integer.valueOf(6)));
    }

    /*
     * 0 1 2 3 4 5 6 7 ... 10 11 12
     * ------------------------------------------------------------------ |<- G1
     * ->| |<-- G2 -->| |<--- G3 --->|
     */
    @Test
    public void handleReorderColumnsAndGroupsCommand() {
        ReorderColumnsAndGroupsCommand command =
                new ReorderColumnsAndGroupsCommand(this.layer, Arrays.asList(3, 5, 6), 8);
        this.layer.doCommand(command);

        this.modelFixture.assertUnchanged();

        /*
         * See output for a better idea System.out.println("Index\tPosition");
         * for (int i = 0; i < reorderLayer.getColumnCount(); i++)
         * System.out.println(i + "\t" +
         * reorderLayer.getColumnIndexByPosition(i));
         */

        // Check new positions
        assertEquals(3, this.reorderLayer.getColumnIndexByPosition(4));
        assertEquals(4, this.reorderLayer.getColumnIndexByPosition(5));
        assertEquals(5, this.reorderLayer.getColumnIndexByPosition(6));
        assertEquals(6, this.reorderLayer.getColumnIndexByPosition(7));
    }

    @Test
    public void shouldAddToAGroupWhileReorderingMultipleColumns() {
        MultiColumnReorderCommand reorderCommand =
                new MultiColumnReorderCommand(this.layer, Arrays.asList(5, 6), 11);
        this.layer.doCommand(reorderCommand);

        assertEquals(5, getColumnIndexesInGroup(11).size());
        assertTrue(getColumnIndexesInGroup(11).contains(Integer.valueOf(10)));
        assertTrue(getColumnIndexesInGroup(11).contains(Integer.valueOf(11)));
        assertTrue(getColumnIndexesInGroup(11).contains(Integer.valueOf(12)));
        assertTrue(getColumnIndexesInGroup(11).contains(Integer.valueOf(5)));
        assertTrue(getColumnIndexesInGroup(11).contains(Integer.valueOf(6)));

        assertEquals(10, this.layer.getColumnIndexByPosition(8));
        assertEquals(5, this.layer.getColumnIndexByPosition(9));
        assertEquals(6, this.layer.getColumnIndexByPosition(10));
        assertEquals(11, this.layer.getColumnIndexByPosition(11));
        assertEquals(12, this.layer.getColumnIndexByPosition(12));
    }

    @Test
    public void shouldRemoveFromAGroupWhileReorderingMultipleColumns() {
        MultiColumnReorderCommand reorderCommand =
                new MultiColumnReorderCommand(this.layer, Arrays.asList(10, 11), 5);
        this.layer.doCommand(reorderCommand);

        assertEquals(1, getColumnIndexesInGroup(12).size());
        assertTrue(getColumnIndexesInGroup(12).contains(Integer.valueOf(12)));
    }

    @Test
    public void reorderMultipleColums() {
        // One of the 'from' column is already in the destination group
        MultiColumnReorderCommand reorderCommand =
                new MultiColumnReorderCommand(this.layer, Arrays.asList(9, 10), 11);
        this.layer.doCommand(reorderCommand);

        assertEquals(4, getColumnIndexesInGroup(12).size());
    }

    @Test
    public void shouldReorderToConsecutive() {
        // first ungroup group 2
        this.modelFixture.removeColumnGroup(this.modelFixture.getColumnGroupByIndex(3));

        assertEquals(0, this.layer.getColumnIndexByPosition(0));
        assertEquals(1, this.layer.getColumnIndexByPosition(1));
        assertEquals(2, this.layer.getColumnIndexByPosition(2));
        assertEquals(3, this.layer.getColumnIndexByPosition(3));

        // the first two columns are a group already, column 3 should be added
        // so we need to reorder to be consecutive
        MultiColumnReorderCommand reorderCommand =
                new MultiColumnReorderCommand(this.layer, Arrays.asList(0, 1, 3), 0);
        this.layer.doCommand(reorderCommand);

        // we only reordered, we did not change the group
        assertEquals(2, getColumnIndexesInGroup(0).size());

        assertEquals(0, this.layer.getColumnIndexByPosition(0));
        assertEquals(1, this.layer.getColumnIndexByPosition(1));
        assertEquals(3, this.layer.getColumnIndexByPosition(2));
        assertEquals(2, this.layer.getColumnIndexByPosition(3));
    }

    @Test
    public void shouldNotAllowMultiReorderIntoAnUnbreakableGroup() {
        setGroupUnBreakable(11);

        MultiColumnReorderCommand reorderCommand =
                new MultiColumnReorderCommand(this.layer, Arrays.asList(5, 6), 11);
        this.layer.doCommand(reorderCommand);

        this.modelFixture.assertUnchanged();
    }

    @Test
    public void shouldNotAllowRemovalFromAColumnGroupDuringMultiColumnReorder() {
        setGroupUnBreakable(4);

        MultiColumnReorderCommand reorderCommand =
                new MultiColumnReorderCommand(this.layer, Arrays.asList(4, 5), 6);
        this.layer.doCommand(reorderCommand);

        this.modelFixture.assertUnchanged();
    }

    @Test
    public void shouldMoveInAndOutGroupsByOneStepUp() {
        this.modelFixture.addColumnsIndexesToGroup("G4", 7, 8, 9);

        assertEquals("G3", this.modelFixture.getColumnGroupByIndex(10).getName());

        ColumnReorderCommand command = new ColumnReorderCommand(this.layer, 10, 9);
        this.layer.doCommand(command);

        assertNull(this.modelFixture.getColumnGroupByIndex(10));

        this.layer.doCommand(command);

        assertEquals("G4", this.modelFixture.getColumnGroupByIndex(10).getName());
    }

    @Test
    public void shouldMoveInAndOutGroupsByOneStepDown() {
        this.modelFixture.addColumnsIndexesToGroup("G4", 7, 8, 9);

        assertEquals("G4", this.modelFixture.getColumnGroupByIndex(9).getName());

        ColumnReorderCommand command = new ColumnReorderCommand(this.layer, 9, 11);
        this.layer.doCommand(command);

        assertNull(this.modelFixture.getColumnGroupByIndex(9));

        this.layer.doCommand(command);

        assertEquals("G3", this.modelFixture.getColumnGroupByIndex(9).getName());
    }

    private List<Integer> getColumnIndexesInGroup(int columnIndex) {
        return this.modelFixture.getColumnGroupByIndex(columnIndex).getMembers();
    }

    private void collapse(int columnIndex) {
        this.modelFixture.getColumnGroupByIndex(columnIndex).setCollapsed(true);
    }

    private void setGroupUnBreakable(int columnIndex) {
        this.modelFixture.getColumnGroupByIndex(columnIndex).setUnbreakable(true);
    }

}
