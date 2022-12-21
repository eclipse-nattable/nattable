/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.test.fixture.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;

public class ColumnGroupModelFixture extends ColumnGroupModel {

    public static final String TEST_GROUP_1 = "G1";
    public static final String TEST_GROUP_2 = "G2";
    public static final String TEST_GROUP_3 = "G3";

    /*
     * 0 1 2 3 4 5 6 7 ... 10 11 12
     * ------------------------------------------------------------------ |<- G1
     * ->| |<-- G2 -->| |<--- G3 --->|
     */
    public ColumnGroupModelFixture() {
        super();
        addColumnsIndexesToGroup(TEST_GROUP_1, 0, 1);
        addColumnsIndexesToGroup(TEST_GROUP_2, 3, 4);
        addColumnsIndexesToGroup(TEST_GROUP_3, 10, 11, 12);
    }

    public void assertUnchanged() {
        List<Integer> columnIndexesInGroup;

        columnIndexesInGroup = getColumnGroupByIndex(0).getMembers();
        assertEquals(2, columnIndexesInGroup.size());
        assertTrue(columnIndexesInGroup.contains(Integer.valueOf(0)));
        assertTrue(columnIndexesInGroup.contains(Integer.valueOf(1)));

        columnIndexesInGroup = getColumnGroupByIndex(3).getMembers();
        assertEquals(2, columnIndexesInGroup.size());
        assertTrue(columnIndexesInGroup.contains(Integer.valueOf(3)));
        assertTrue(columnIndexesInGroup.contains(Integer.valueOf(4)));

        columnIndexesInGroup = getColumnGroupByIndex(10).getMembers();
        assertEquals(3, columnIndexesInGroup.size());
        assertTrue(columnIndexesInGroup.contains(Integer.valueOf(10)));
        assertTrue(columnIndexesInGroup.contains(Integer.valueOf(11)));
        assertTrue(columnIndexesInGroup.contains(Integer.valueOf(12)));
    }

    public void assertTestGroup3IsUnchanged() {
        List<Integer> columnIndexesInGroup = getColumnGroupByIndex(10).getMembers();
        assertEquals(3, columnIndexesInGroup.size());

        assertTrue(columnIndexesInGroup.contains(Integer.valueOf(10)));
        assertTrue(columnIndexesInGroup.contains(Integer.valueOf(11)));
        assertTrue(columnIndexesInGroup.contains(Integer.valueOf(12)));
    }

}
