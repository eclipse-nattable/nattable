/*******************************************************************************
 * Copyright (c) 2019, 2020 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group.performance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.junit.Before;
import org.junit.Test;

public class GroupModelTest {

    public static final String TEST_GROUP_NAME_1 = "testGroupName";
    public static final String TEST_GROUP_NAME_2 = "testGroupName2";
    public static final String TEST_GROUP_NAME_3 = "testGroupName3";

    private GroupModel model;

    @Before
    public void setup() {
        this.model = new GroupModel();
        this.model.addGroup(TEST_GROUP_NAME_1, 0, 4);
        this.model.addGroup(TEST_GROUP_NAME_2, 5, 3);
        this.model.addGroup(TEST_GROUP_NAME_3, 12, 2);
    }

    @Test
    public void shouldGetGroupByPosition() {
        assertEquals(TEST_GROUP_NAME_1, this.model.getGroupByPosition(1).getName());
        assertEquals(TEST_GROUP_NAME_2, this.model.getGroupByPosition(5).getName());
        assertEquals(TEST_GROUP_NAME_3, this.model.getGroupByPosition(13).getName());
        assertNull(this.model.getGroupByPosition(15));
    }

    @Test
    public void shouldGetGroupByName() {
        assertEquals(TEST_GROUP_NAME_1, this.model.getGroupByName(TEST_GROUP_NAME_1).getName());
        assertEquals(TEST_GROUP_NAME_2, this.model.getGroupByName(TEST_GROUP_NAME_2).getName());
        assertEquals(TEST_GROUP_NAME_3, this.model.getGroupByName(TEST_GROUP_NAME_3).getName());
        assertNull(this.model.getGroupByPosition(15));
    }

    @Test
    public void shouldIdentifyGroupByPosition() {
        assertTrue(this.model.isPartOfAGroup(1));
        assertTrue(this.model.isPartOfAGroup(7));
        assertTrue(this.model.isPartOfAGroup(13));
        assertFalse(this.model.isPartOfAGroup(130));
    }

    @Test
    public void shouldCreateGroupsDefaultCollapseable() {
        assertTrue(this.model.getGroupByPosition(1).isCollapseable());
        assertTrue(this.model.getGroupByPosition(5).isCollapseable());
        assertTrue(this.model.getGroupByPosition(13).isCollapseable());
    }

    @Test
    public void shouldCreateGroupsDefaultNotCollapseable() {
        GroupModel temp = new GroupModel();
        temp.setDefaultCollapseable(false);

        temp.addGroup(TEST_GROUP_NAME_1, 0, 4);
        temp.addGroup(TEST_GROUP_NAME_2, 5, 3);
        temp.addGroup(TEST_GROUP_NAME_3, 12, 2);

        assertFalse(temp.getGroupByPosition(1).isCollapseable());
        assertFalse(temp.getGroupByPosition(5).isCollapseable());
        assertFalse(temp.getGroupByPosition(13).isCollapseable());
    }

    @Test
    public void shouldExpandCollapseGroup() {
        assertFalse(this.model.getGroupByPosition(0).isCollapsed());
        assertFalse(this.model.getGroupByPosition(5).isCollapsed());
        assertFalse(this.model.getGroupByPosition(13).isCollapsed());

        this.model.getGroupByPosition(0).setCollapsed(true);

        assertTrue(this.model.getGroupByPosition(0).isCollapsed());
        assertFalse(this.model.getGroupByPosition(5).isCollapsed());
        assertFalse(this.model.getGroupByPosition(13).isCollapsed());

        this.model.getGroupByPosition(0).setCollapsed(false);

        assertFalse(this.model.getGroupByPosition(0).isCollapsed());
        assertFalse(this.model.getGroupByPosition(5).isCollapsed());
        assertFalse(this.model.getGroupByPosition(13).isCollapsed());
    }

    @Test
    public void shouldNotCollapseNonCollapseableGroup() {
        Group group = this.model.getGroupByPosition(5);
        assertTrue(group.isCollapseable());
        assertFalse(group.isCollapsed());
        assertTrue(this.model.isPartOfACollapseableGroup(6));

        this.model.setGroupCollapseable(5, false);
        group.setCollapsed(true);

        assertFalse(this.model.isPartOfACollapseableGroup(6));

        assertFalse(group.isCollapsed());
    }

    @Test
    public void shouldExpandCollapsedGroupOnSettingNonCollapseable() {
        Group group = this.model.getGroupByPosition(5);
        group.setCollapsed(true);

        group.setCollapseable(false);

        assertFalse(group.isCollapsed());
    }

    @Test
    public void shouldToggleCollapsedState() {
        Group group = this.model.getGroupByPosition(5);
        assertFalse(group.isCollapsed());
        group.toggleCollapsed();
        assertTrue(group.isCollapsed());
        group.toggleCollapsed();
        assertFalse(group.isCollapsed());
    }

    @Test
    public void shouldCreateGroupsDefaultBreakable() {
        assertFalse(this.model.getGroupByPosition(1).isUnbreakable());
        assertFalse(this.model.getGroupByPosition(5).isUnbreakable());
        assertFalse(this.model.getGroupByPosition(13).isUnbreakable());
    }

    @Test
    public void shouldCreateGroupsDefaultUnbreakable() {
        GroupModel temp = new GroupModel();
        temp.setDefaultUnbreakable(true);

        temp.addGroup(TEST_GROUP_NAME_1, 0, 4);
        temp.addGroup(TEST_GROUP_NAME_2, 5, 3);
        temp.addGroup(TEST_GROUP_NAME_3, 12, 2);

        assertTrue(temp.getGroupByPosition(1).isUnbreakable());
        assertTrue(temp.getGroupByPosition(5).isUnbreakable());
        assertTrue(temp.getGroupByPosition(13).isUnbreakable());
    }

    @Test
    public void shouldFillInternalMemberIndexes() {
        Group group1 = this.model.getGroupByPosition(0);
        assertEquals(4, group1.getMembers().length);
        assertTrue(group1.hasMember(0));
        assertTrue(group1.hasMember(1));
        assertTrue(group1.hasMember(2));
        assertTrue(group1.hasMember(3));

        Group group2 = this.model.getGroupByPosition(5);
        assertEquals(3, group2.getMembers().length);
        assertTrue(group2.hasMember(5));
        assertTrue(group2.hasMember(6));
        assertTrue(group2.hasMember(7));

        Group group3 = this.model.getGroupByPosition(12);
        assertEquals(2, group3.getMembers().length);
        assertTrue(group3.hasMember(12));
        assertTrue(group3.hasMember(13));
    }

    @Test
    public void shouldRemoveInMiddleOfGroup() {
        Group group = this.model.getGroupByPosition(6);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
        assertTrue(this.model.isPartOfAGroup(7));

        assertEquals(3, group.getMembers().length);
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(7));

        this.model.removePositionsFromGroup(6);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(2, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());
        assertFalse(this.model.isPartOfAGroup(7));

        assertEquals(2, group.getMembers().length);
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
    }

    @Test
    public void shouldRemoveAtEndOfGroup() {
        Group group = this.model.getGroupByPosition(5);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
        assertTrue(this.model.isPartOfAGroup(7));

        assertEquals(3, group.getMembers().length);
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(7));

        this.model.removePositionsFromGroup(7);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(2, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());
        assertFalse(this.model.isPartOfAGroup(7));

        assertEquals(2, group.getMembers().length);
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
    }

    @Test
    public void shouldRemoveAtBeginningOfGroup() {
        Group group = this.model.getGroupByPosition(7);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
        assertTrue(this.model.isPartOfAGroup(7));

        assertEquals(3, group.getMembers().length);
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(7));

        this.model.removePositionsFromGroup(5);

        assertEquals(6, group.getStartIndex());
        assertEquals(6, group.getVisibleStartIndex());
        assertEquals(6, group.getVisibleStartPosition());
        assertEquals(2, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());
        assertFalse(this.model.isPartOfAGroup(5));
        assertTrue(this.model.isPartOfAGroup(7));

        assertEquals(2, group.getMembers().length);
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(7));
    }

    @Test
    public void shouldNotRemoveForUnbreakableGroup() {
        this.model.setGroupUnbreakable(5, true);

        assertTrue(this.model.isPartOfAnUnbreakableGroup(6));

        Group group = this.model.getGroupByPosition(5);
        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
        assertTrue(this.model.isPartOfAGroup(7));

        assertEquals(3, group.getMembers().length);
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(7));

        this.model.removePositionsFromGroup(7);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
        assertTrue(this.model.isPartOfAGroup(7));

        assertEquals(3, group.getMembers().length);
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(7));
    }

    @Test
    public void shouldNotRemoveNotContainingPosition() {
        Group group = this.model.getGroupByPosition(0);
        group.setUnbreakable(true);

        assertEquals(0, group.getStartIndex());
        assertEquals(4, group.getOriginalSpan());

        this.model.removePositionsFromGroup(10);

        assertEquals(0, group.getStartIndex());
        assertEquals(4, group.getOriginalSpan());
    }

    @Test
    public void shouldRemoveFromMultipleGroups() {
        Group group1 = this.model.getGroupByPosition(0);
        Group group2 = this.model.getGroupByPosition(5);
        Group group3 = this.model.getGroupByPosition(12);

        assertEquals(0, group1.getStartIndex());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(5, group2.getStartIndex());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(12, group3.getStartIndex());
        assertEquals(2, group3.getOriginalSpan());

        // remove first item in first group
        // remove middle item in second group
        // remove last item in last group
        this.model.removePositionsFromGroup(0, 6, 13);

        assertEquals(1, group1.getStartIndex());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(5, group2.getStartIndex());
        assertEquals(2, group2.getOriginalSpan());
        assertEquals(12, group3.getStartIndex());
        assertEquals(1, group3.getOriginalSpan());
    }

    @Test
    public void shouldNotRemoveFromUnbreakableOnMultipleGroups() {
        Group group1 = this.model.getGroupByPosition(0);
        Group group2 = this.model.getGroupByPosition(5);
        Group group3 = this.model.getGroupByPosition(12);

        group2.setUnbreakable(true);

        assertEquals(0, group1.getStartIndex());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(5, group2.getStartIndex());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(12, group3.getStartIndex());
        assertEquals(2, group3.getOriginalSpan());

        // remove first item in first group
        // remove middle item in second group - this should not work
        // remove last item in last group
        this.model.removePositionsFromGroup(0, 6, 13);

        assertEquals(1, group1.getStartIndex());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(5, group2.getStartIndex());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(12, group3.getStartIndex());
        assertEquals(1, group3.getOriginalSpan());
    }

    @Test
    public void shouldNotRemoveNotContainingOnMultipleGroups() {
        Group group1 = this.model.getGroupByPosition(0);
        Group group2 = this.model.getGroupByPosition(5);
        Group group3 = this.model.getGroupByPosition(12);

        assertEquals(0, group1.getStartIndex());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(5, group2.getStartIndex());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(12, group3.getStartIndex());
        assertEquals(2, group3.getOriginalSpan());

        // remove first item in first group
        // remove middle item in second group - this should not work
        // remove last item in last group
        this.model.removePositionsFromGroup(0, 10, 13);

        assertEquals(1, group1.getStartIndex());
        assertEquals(3, group1.getOriginalSpan());
        assertEquals(5, group2.getStartIndex());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(12, group3.getStartIndex());
        assertEquals(1, group3.getOriginalSpan());
    }

    @Test
    public void shouldRemoveFromSingleGroup() {
        Group group = this.model.getGroupByPosition(0);

        assertEquals(0, group.getStartIndex());
        assertEquals(4, group.getOriginalSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
        assertTrue(group.hasMember(2));
        assertTrue(group.hasMember(3));

        this.model.removePositionsFromGroup(group, 2);

        assertEquals(0, group.getStartIndex());
        assertEquals(3, group.getOriginalSpan());

        assertEquals(3, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
        assertTrue(group.hasMember(2));
    }

    @Test
    public void shouldRemoveMultipleFromSingleGroup() {
        Group group = this.model.getGroupByPosition(0);

        assertEquals(0, group.getStartIndex());
        assertEquals(4, group.getOriginalSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
        assertTrue(group.hasMember(2));
        assertTrue(group.hasMember(3));

        this.model.removePositionsFromGroup(group, 0, 3);

        assertEquals(1, group.getStartIndex());
        assertEquals(2, group.getOriginalSpan());

        assertEquals(2, group.getMembers().length);
        assertTrue(group.hasMember(1));
        assertTrue(group.hasMember(2));
    }

    @Test
    public void shouldRemoveMultipleFromSingleGroup2() {
        Group group = this.model.getGroupByPosition(0);

        assertEquals(0, group.getStartIndex());
        assertEquals(4, group.getOriginalSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
        assertTrue(group.hasMember(2));
        assertTrue(group.hasMember(3));

        this.model.removePositionsFromGroup(0, 3);

        assertEquals(1, group.getStartIndex());
        assertEquals(2, group.getOriginalSpan());

        assertEquals(2, group.getMembers().length);
        assertTrue(group.hasMember(1));
        assertTrue(group.hasMember(2));
    }

    @Test
    public void shouldRemoveAllFromGroup() {
        Group group = this.model.getGroupByPosition(0);

        assertEquals(0, group.getStartIndex());
        assertEquals(4, group.getOriginalSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
        assertTrue(group.hasMember(2));
        assertTrue(group.hasMember(3));

        this.model.removePositionsFromGroup(0, 1, 2, 3);

        assertEquals(-1, group.getStartIndex());
        assertEquals(-1, group.getVisibleStartIndex());
        assertEquals(-1, group.getVisibleStartPosition());
        assertEquals(0, group.getOriginalSpan());
        assertEquals(0, group.getVisibleSpan());

        assertEquals(0, group.getMembers().length);

        assertNull(this.model.getGroupByPosition(0));
        assertNull(this.model.getGroupByPosition(1));
        assertNull(this.model.getGroupByPosition(2));
        assertNull(this.model.getGroupByPosition(3));

        assertNull(this.model.getGroupByPosition(4));
        assertNotNull(this.model.getGroupByPosition(5));
    }

    @Test
    public void shouldRemoveAllFromGroup2() {
        Group group = this.model.getGroupByPosition(0);

        assertEquals(0, group.getStartIndex());
        assertEquals(4, group.getOriginalSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(0));
        assertTrue(group.hasMember(1));
        assertTrue(group.hasMember(2));
        assertTrue(group.hasMember(3));

        this.model.removePositionsFromGroup(group, 0, 1, 2, 3);

        assertEquals(-1, group.getStartIndex());
        assertEquals(-1, group.getVisibleStartIndex());
        assertEquals(-1, group.getVisibleStartPosition());
        assertEquals(0, group.getOriginalSpan());
        assertEquals(0, group.getVisibleSpan());

        assertEquals(0, group.getMembers().length);

        assertNull(this.model.getGroupByPosition(0));
        assertNull(this.model.getGroupByPosition(1));
        assertNull(this.model.getGroupByPosition(2));
        assertNull(this.model.getGroupByPosition(3));

        assertNull(this.model.getGroupByPosition(4));
        assertNotNull(this.model.getGroupByPosition(5));
    }

    @Test
    public void shouldNotRemoveFromUnbreakableSingleGroup() {
        Group group = this.model.getGroupByPosition(0);
        group.setUnbreakable(true);

        assertEquals(0, group.getStartIndex());
        assertEquals(4, group.getOriginalSpan());

        this.model.removePositionsFromGroup(group, 0, 4);

        assertEquals(0, group.getStartIndex());
        assertEquals(4, group.getOriginalSpan());
    }

    @Test
    public void shouldNotRemoveNotContainingPositionFromSingleGroup() {
        Group group = this.model.getGroupByPosition(0);
        group.setUnbreakable(true);

        assertEquals(0, group.getStartIndex());
        assertEquals(4, group.getOriginalSpan());

        this.model.removePositionsFromGroup(group, 5);

        assertEquals(0, group.getStartIndex());
        assertEquals(4, group.getOriginalSpan());
    }

    @Test
    public void shouldAddPositionAtEndOfGroup() {
        Group group = this.model.getGroupByPosition(5);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertEquals(3, group.getMembers().length);
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(7));

        this.model.addPositionsToGroup(group, 8);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(7));
        assertTrue(group.hasMember(8));
    }

    @Test
    public void shouldAddMultiplePositionsAtEndOfGroup() {
        Group group = this.model.getGroupByPosition(5);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertEquals(3, group.getMembers().length);
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(7));

        this.model.addPositionsToGroup(group, 8, 9, 10);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(6, group.getOriginalSpan());
        assertEquals(6, group.getVisibleSpan());

        assertEquals(6, group.getMembers().length);
        assertTrue(group.hasMember(5));
        assertTrue(group.hasMember(6));
        assertTrue(group.hasMember(7));
        assertTrue(group.hasMember(8));
        assertTrue(group.hasMember(9));
        assertTrue(group.hasMember(10));
    }

    @Test
    public void shouldNotAddPositionAtEndOfGroupWithGap() {
        Group group = this.model.getGroupByPosition(5);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        this.model.addPositionsToGroup(group, 10);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
    }

    @Test
    public void shouldNotAddMultiplePositionsAfterGapAtEndOfGroup() {
        Group group = this.model.getGroupByPosition(5);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        this.model.addPositionsToGroup(group, 8, 9, 11, 12);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(5, group.getOriginalSpan());
        assertEquals(5, group.getVisibleSpan());
    }

    @Test
    public void shouldAddPositionAtBeginningOfGroup() {
        Group group = this.model.getGroupByPosition(12);

        assertEquals(12, group.getStartIndex());
        assertEquals(12, group.getVisibleStartIndex());
        assertEquals(12, group.getVisibleStartPosition());
        assertEquals(2, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        assertEquals(2, group.getMembers().length);
        assertTrue(group.hasMember(12));
        assertTrue(group.hasMember(13));

        this.model.addPositionsToGroup(group, 11);

        assertEquals(11, group.getStartIndex());
        assertEquals(11, group.getVisibleStartIndex());
        assertEquals(11, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        assertEquals(3, group.getMembers().length);
        assertTrue(group.hasMember(11));
        assertTrue(group.hasMember(12));
        assertTrue(group.hasMember(13));
    }

    @Test
    public void shouldAddMultiplePositionsAtBeginningOfGroup() {
        Group group = this.model.getGroupByPosition(12);

        assertEquals(12, group.getStartIndex());
        assertEquals(12, group.getVisibleStartIndex());
        assertEquals(12, group.getVisibleStartPosition());
        assertEquals(2, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        assertEquals(2, group.getMembers().length);
        assertTrue(group.hasMember(12));
        assertTrue(group.hasMember(13));

        this.model.addPositionsToGroup(group, 11, 10);

        assertEquals(10, group.getStartIndex());
        assertEquals(10, group.getVisibleStartIndex());
        assertEquals(10, group.getVisibleStartPosition());
        assertEquals(4, group.getOriginalSpan());
        assertEquals(4, group.getVisibleSpan());

        assertEquals(4, group.getMembers().length);
        assertTrue(group.hasMember(10));
        assertTrue(group.hasMember(11));
        assertTrue(group.hasMember(12));
        assertTrue(group.hasMember(13));
    }

    @Test
    public void shouldNotAddPositionAtBeginningOfGroupWithGap() {
        Group group = this.model.getGroupByPosition(12);

        assertEquals(12, group.getStartIndex());
        assertEquals(12, group.getVisibleStartIndex());
        assertEquals(12, group.getVisibleStartPosition());
        assertEquals(2, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        this.model.addPositionsToGroup(group, 10);

        assertEquals(12, group.getStartIndex());
        assertEquals(12, group.getVisibleStartIndex());
        assertEquals(12, group.getVisibleStartPosition());
        assertEquals(2, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());
    }

    @Test
    public void shouldNotAddMultiplePositionsAfterGapAtBeginningOfGroup() {
        Group group = this.model.getGroupByPosition(12);

        assertEquals(12, group.getStartIndex());
        assertEquals(12, group.getVisibleStartIndex());
        assertEquals(12, group.getVisibleStartPosition());
        assertEquals(2, group.getOriginalSpan());
        assertEquals(2, group.getVisibleSpan());

        this.model.addPositionsToGroup(group, 11, 9);

        assertEquals(11, group.getStartIndex());
        assertEquals(11, group.getVisibleStartIndex());
        assertEquals(11, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
    }

    @Test
    public void shouldNotAddPositionToUnbreakableGroup() {
        Group group = this.model.getGroupByPosition(5);
        group.setUnbreakable(true);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        this.model.addPositionsToGroup(group, 9);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
    }

    @Test
    public void shouldNotAddAlreadyContainingPositionToGroup() {
        Group group = this.model.getGroupByPosition(5);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        this.model.addPositionsToGroup(group, 6);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        // check this even for the start position of the group
        this.model.addPositionsToGroup(group, 5);

        assertEquals(5, group.getStartIndex());
        assertEquals(5, group.getVisibleStartIndex());
        assertEquals(5, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());
    }

    @Test
    public void shouldAddStaticPositions() {
        Group group1 = this.model.getGroupByPosition(0);
        this.model.addStaticIndexesToGroup(group1, 1, 2);

        Group group2 = this.model.getGroupByPosition(5);
        this.model.addStaticIndexesToGroup(group2, 5, 6);

        assertEquals(2, group1.getStaticIndexes().length);
        assertEquals(2, group2.getStaticIndexes().length);

        assertTrue(this.model.isStatic(1));
        assertTrue(this.model.isStatic(2));
        assertTrue(this.model.isStatic(5));
        assertTrue(this.model.isStatic(6));
        assertFalse(this.model.isStatic(0));
    }

    @Test
    public void shouldNotAddStaticPositionThatIsNotInGroup() {
        Group group = this.model.getGroupByPosition(0);
        this.model.addStaticIndexesToGroup(group, 4);

        assertEquals(0, group.getStaticIndexes().length);

        assertFalse(this.model.isStatic(4));
    }

    @Test
    public void shouldOnlyAddGroupPositionsToGroup() {
        Group group = this.model.getGroupByPosition(0);
        this.model.addStaticIndexesToGroup(group, 3, 4);

        assertEquals(1, group.getStaticIndexes().length);

        assertTrue(this.model.isStatic(3));
        assertFalse(this.model.isStatic(4));
    }

    @Test
    public void shouldRemoveStaticIndexIfRemovedFromGroupEnd() {
        Group group = this.model.getGroupByPosition(0);
        // set last two columns as static
        this.model.addStaticIndexesToGroup(group, 2, 3);

        assertEquals(2, group.getStaticIndexes().length);

        assertTrue(this.model.isStatic(2));
        assertTrue(this.model.isStatic(3));

        // remove last position from group
        this.model.removePositionsFromGroup(3);

        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        int[] members = group.getMembers();
        assertEquals(3, members.length);
        assertEquals(0, members[0]);
        assertEquals(1, members[1]);
        assertEquals(2, members[2]);

        assertEquals(1, group.getStaticIndexes().length);

        assertTrue(this.model.isStatic(2));
        assertFalse(this.model.isStatic(3));
    }

    @Test
    public void shouldRemoveStaticIndexIfRemovedFromGroupStart() {
        Group group = this.model.getGroupByPosition(0);
        // set first and last column as static
        this.model.addStaticIndexesToGroup(group, 0, 3);

        assertEquals(2, group.getStaticIndexes().length);

        assertTrue(this.model.isStatic(0));
        assertFalse(this.model.isStatic(1));
        assertFalse(this.model.isStatic(2));
        assertTrue(this.model.isStatic(3));

        // remove first position from group
        this.model.removePositionsFromGroup(0);

        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(1, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        int[] members = group.getMembers();
        assertEquals(3, members.length);
        assertEquals(1, members[0]);
        assertEquals(2, members[1]);
        assertEquals(3, members[2]);

        assertEquals(1, group.getStaticIndexes().length);

        assertFalse(this.model.isStatic(0));
        assertFalse(this.model.isStatic(1));
        assertFalse(this.model.isStatic(2));
        assertTrue(this.model.isStatic(3));
    }

    @Test
    public void shouldRemoveStaticIndexIfRemovedFromGroupEnd2() {
        Group group = this.model.getGroupByPosition(0);
        // set last two columns as static
        this.model.addStaticIndexesToGroup(group, 2, 3);

        assertEquals(2, group.getStaticIndexes().length);

        assertTrue(this.model.isStatic(2));
        assertTrue(this.model.isStatic(3));

        // remove last position from group
        this.model.removePositionsFromGroup(group, 3);

        assertEquals(0, group.getStartIndex());
        assertEquals(0, group.getVisibleStartIndex());
        assertEquals(0, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        int[] members = group.getMembers();
        assertEquals(3, members.length);
        assertEquals(0, members[0]);
        assertEquals(1, members[1]);
        assertEquals(2, members[2]);

        assertEquals(1, group.getStaticIndexes().length);

        assertTrue(this.model.isStatic(2));
        assertFalse(this.model.isStatic(3));
    }

    @Test
    public void shouldRemoveStaticIndexIfRemovedFromGroupStart2() {
        Group group = this.model.getGroupByPosition(0);
        // set first and last column as static
        this.model.addStaticIndexesToGroup(group, 0, 3);

        assertEquals(2, group.getStaticIndexes().length);

        assertTrue(this.model.isStatic(0));
        assertFalse(this.model.isStatic(1));
        assertFalse(this.model.isStatic(2));
        assertTrue(this.model.isStatic(3));

        // remove first position from group
        this.model.removePositionsFromGroup(group, 0);

        assertEquals(1, group.getStartIndex());
        assertEquals(1, group.getVisibleStartIndex());
        assertEquals(1, group.getVisibleStartPosition());
        assertEquals(3, group.getOriginalSpan());
        assertEquals(3, group.getVisibleSpan());

        int[] members = group.getMembers();
        assertEquals(3, members.length);
        assertEquals(1, members[0]);
        assertEquals(2, members[1]);
        assertEquals(3, members[2]);

        assertEquals(1, group.getStaticIndexes().length);

        assertFalse(this.model.isStatic(0));
        assertFalse(this.model.isStatic(1));
        assertFalse(this.model.isStatic(2));
        assertTrue(this.model.isStatic(3));
    }

    @Test
    public void shouldReturnVisiblePositionCollection() {
        int[] positions1 = this.model.getGroupByPosition(0).getVisiblePositions();
        int[] positions2 = this.model.getGroupByPosition(5).getVisiblePositions();
        int[] positions3 = this.model.getGroupByPosition(12).getVisiblePositions();

        assertTrue(Arrays.stream(positions1).anyMatch(x -> x == 0));
        assertTrue(Arrays.stream(positions1).anyMatch(x -> x == 1));
        assertTrue(Arrays.stream(positions1).anyMatch(x -> x == 2));
        assertTrue(Arrays.stream(positions1).anyMatch(x -> x == 3));

        assertTrue(Arrays.stream(positions2).anyMatch(x -> x == 5));
        assertTrue(Arrays.stream(positions2).anyMatch(x -> x == 6));
        assertTrue(Arrays.stream(positions2).anyMatch(x -> x == 7));

        assertTrue(Arrays.stream(positions3).anyMatch(x -> x == 12));
        assertTrue(Arrays.stream(positions3).anyMatch(x -> x == 13));
    }

    @Test
    public void shouldReturnVisibleIndexCollection() {
        int[] indexes1 = this.model.getGroupByPosition(0).getVisibleIndexes();
        int[] indexes2 = this.model.getGroupByPosition(5).getVisibleIndexes();
        int[] indexes3 = this.model.getGroupByPosition(12).getVisibleIndexes();

        assertTrue(Arrays.stream(indexes1).anyMatch(x -> x == 0));
        assertTrue(Arrays.stream(indexes1).anyMatch(x -> x == 1));
        assertTrue(Arrays.stream(indexes1).anyMatch(x -> x == 2));
        assertTrue(Arrays.stream(indexes1).anyMatch(x -> x == 3));

        assertTrue(Arrays.stream(indexes2).anyMatch(x -> x == 5));
        assertTrue(Arrays.stream(indexes2).anyMatch(x -> x == 6));
        assertTrue(Arrays.stream(indexes2).anyMatch(x -> x == 7));

        assertTrue(Arrays.stream(indexes3).anyMatch(x -> x == 12));
        assertTrue(Arrays.stream(indexes3).anyMatch(x -> x == 13));
    }

    @Test
    public void shouldKnowLeftEdgeOfGroup() {
        Group group1 = this.model.getGroupByPosition(0);
        Group group2 = this.model.getGroupByPosition(5);
        Group group3 = this.model.getGroupByPosition(12);

        assertTrue(group1.isGroupStart(0));
        assertFalse(group1.isGroupStart(1));
        assertFalse(group1.isGroupStart(2));
        assertFalse(group1.isGroupStart(3));

        assertFalse(group1.isGroupStart(4));
        assertFalse(group2.isGroupStart(4));

        assertTrue(group2.isGroupStart(5));
        assertFalse(group2.isGroupStart(6));
        assertFalse(group2.isGroupStart(7));

        assertTrue(group3.isGroupStart(12));
        assertFalse(group3.isGroupStart(13));
        assertFalse(group3.isGroupStart(14));
    }

    @Test
    public void shouldKnowRightEdgeOfGroup() {
        Group group1 = this.model.getGroupByPosition(0);
        Group group2 = this.model.getGroupByPosition(5);
        Group group3 = this.model.getGroupByPosition(12);

        assertFalse(group1.isGroupEnd(0));
        assertFalse(group1.isGroupEnd(1));
        assertFalse(group1.isGroupEnd(2));
        assertTrue(group1.isGroupEnd(3));

        assertFalse(group1.isGroupEnd(4));
        assertFalse(group2.isGroupEnd(4));

        assertFalse(group2.isGroupEnd(5));
        assertFalse(group2.isGroupEnd(6));
        assertTrue(group2.isGroupEnd(7));

        assertFalse(group3.isGroupEnd(12));
        assertTrue(group3.isGroupEnd(13));
        assertFalse(group3.isGroupEnd(14));
    }

    @Test
    public void shouldFindGroupByMember() {
        Group group1 = this.model.getGroupByPosition(0);
        Group group2 = this.model.getGroupByPosition(5);
        Group group3 = this.model.getGroupByPosition(12);

        assertNull(this.model.findGroupByMemberIndex(-1));

        assertEquals(group1, this.model.findGroupByMemberIndex(0));
        assertEquals(group1, this.model.findGroupByMemberIndex(1));
        assertEquals(group1, this.model.findGroupByMemberIndex(2));
        assertEquals(group1, this.model.findGroupByMemberIndex(3));

        assertNull(this.model.findGroupByMemberIndex(4));

        assertEquals(group2, this.model.findGroupByMemberIndex(5));
        assertEquals(group2, this.model.findGroupByMemberIndex(6));
        assertEquals(group2, this.model.findGroupByMemberIndex(7));

        assertEquals(group3, this.model.findGroupByMemberIndex(12));
        assertEquals(group3, this.model.findGroupByMemberIndex(13));

        assertNull(this.model.findGroupByMemberIndex(15));
    }

    @Test
    public void shouldFindGroupByMemberInCollapsed() {
        Group group1 = this.model.getGroupByPosition(0);
        Group group2 = this.model.getGroupByPosition(5);
        Group group3 = this.model.getGroupByPosition(12);

        // collapsed state should not have an effect
        group1.setCollapsed(true);
        group2.setCollapsed(true);
        group3.setCollapsed(true);

        assertNull(this.model.findGroupByMemberIndex(-1));

        assertEquals(group1, this.model.findGroupByMemberIndex(0));
        assertEquals(group1, this.model.findGroupByMemberIndex(1));
        assertEquals(group1, this.model.findGroupByMemberIndex(2));
        assertEquals(group1, this.model.findGroupByMemberIndex(3));

        assertNull(this.model.findGroupByMemberIndex(4));

        assertEquals(group2, this.model.findGroupByMemberIndex(5));
        assertEquals(group2, this.model.findGroupByMemberIndex(6));
        assertEquals(group2, this.model.findGroupByMemberIndex(7));

        assertEquals(group3, this.model.findGroupByMemberIndex(12));
        assertEquals(group3, this.model.findGroupByMemberIndex(13));

        assertNull(this.model.findGroupByMemberIndex(15));
    }

    @Test
    public void shouldSaveState() {
        Group group1 = this.model.getGroupByPosition(0);
        Group group2 = this.model.getGroupByPosition(5);
        Group group3 = this.model.getGroupByPosition(12);

        group1.setCollapseable(false);
        group2.setUnbreakable(true);
        group3.setCollapsed(true);

        this.model.addStaticIndexesToGroup(group1, 1, 2);
        group2.setVisibleStartIndex(6);
        group2.setVisibleSpan(2);

        Properties properties = new Properties();
        this.model.saveState("prefix", properties);

        assertEquals(1, properties.size());
        assertEquals(
                "testGroupName=0:0:0:4:4:expanded:uncollapseable:breakable:1,2|"
                        + "testGroupName2=5:6:5:3:2:expanded:collapseable:unbreakable|"
                        + "testGroupName3=12:12:12:2:2:collapsed:collapseable:breakable|",
                properties.getProperty("prefix.groupModel"));
    }

    @Test
    public void shouldLoadState() {
        Properties properties = new Properties();
        properties.setProperty(
                "prefix.groupModel",
                "testGroupName=0:0:0:4:4:expanded:uncollapseable:breakable:1,2,|"
                        + "testGroupName2=5:6:5:3:2:expanded:collapseable:unbreakable|"
                        + "testGroupName3=12:12:12:2:2:collapsed:collapseable:breakable|");

        GroupModel tempModel = new GroupModel();
        tempModel.loadState("prefix", properties);

        assertTrue(tempModel.isPartOfAGroup(0));
        assertTrue(tempModel.isPartOfAGroup(5));
        assertTrue(tempModel.isPartOfAGroup(12));

        Group group1 = tempModel.getGroupByPosition(0);
        Group group2 = tempModel.getGroupByPosition(5);
        Group group3 = tempModel.getGroupByPosition(12);

        assertEquals("testGroupName", group1.getName());
        assertEquals(0, group1.getStartIndex());
        assertEquals(0, group1.getVisibleStartIndex());
        assertEquals(0, group1.getVisibleStartPosition());
        assertEquals(4, group1.getOriginalSpan());
        assertEquals(4, group1.getVisibleSpan());
        assertFalse(group1.isCollapsed());
        assertFalse(group1.isCollapseable());
        assertFalse(group1.isUnbreakable());
        assertEquals(2, group1.getStaticIndexes().length);
        assertTrue(group1.containsStaticIndex(1));
        assertTrue(group1.containsStaticIndex(2));

        assertEquals("testGroupName2", group2.getName());
        assertEquals(5, group2.getStartIndex());
        assertEquals(6, group2.getVisibleStartIndex());
        assertEquals(5, group2.getVisibleStartPosition());
        assertEquals(3, group2.getOriginalSpan());
        assertEquals(2, group2.getVisibleSpan());
        assertFalse(group2.isCollapsed());
        assertTrue(group2.isCollapseable());
        assertTrue(group2.isUnbreakable());
        assertEquals(0, group2.getStaticIndexes().length);

        assertEquals("testGroupName3", group3.getName());
        assertEquals(12, group3.getStartIndex());
        assertEquals(12, group3.getVisibleStartIndex());
        assertEquals(12, group3.getVisibleStartPosition());
        assertEquals(2, group3.getOriginalSpan());
        assertEquals(2, group3.getVisibleSpan());
        assertTrue(group3.isCollapsed());
        assertTrue(group3.isCollapseable());
        assertFalse(group3.isUnbreakable());
        assertEquals(0, group3.getStaticIndexes().length);
    }
}
