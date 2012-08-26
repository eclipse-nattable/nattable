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
package org.eclipse.nebula.widgets.nattable.test.fixture.group;

import java.util.List;


import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.junit.Assert;

public class ColumnGroupModelFixture extends ColumnGroupModel {

	public static final String TEST_GROUP_1 = "G1";
	public static final String TEST_GROUP_2 = "G2";
	public static final String TEST_GROUP_3 = "G3";

	/*
	 *        0   1    2    3     4    5    6    7   ...  10  11  12
	 * ------------------------------------------------------------------
	 *     |<- G1 ->|     |<-- G2 -->|                   |<--- G3 --->|
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
		Assert.assertEquals(2, columnIndexesInGroup.size());
		Assert.assertTrue(columnIndexesInGroup.contains(Integer.valueOf(0)));
		Assert.assertTrue(columnIndexesInGroup.contains(Integer.valueOf(1)));

		columnIndexesInGroup = getColumnGroupByIndex(3).getMembers();
		Assert.assertEquals(2, columnIndexesInGroup.size());
		Assert.assertTrue(columnIndexesInGroup.contains(Integer.valueOf(3)));
		Assert.assertTrue(columnIndexesInGroup.contains(Integer.valueOf(4)));
		
		columnIndexesInGroup = getColumnGroupByIndex(10).getMembers();
		Assert.assertEquals(3, columnIndexesInGroup.size());
		Assert.assertTrue(columnIndexesInGroup.contains(Integer.valueOf(10)));
		Assert.assertTrue(columnIndexesInGroup.contains(Integer.valueOf(11)));
		Assert.assertTrue(columnIndexesInGroup.contains(Integer.valueOf(12)));
	}
	
	public void assertTestGroup3IsUnchanged() {
		List<Integer> columnIndexesInGroup = getColumnGroupByIndex(10).getMembers();
		Assert.assertEquals(3, columnIndexesInGroup.size());

		Assert.assertTrue(columnIndexesInGroup.contains(Integer.valueOf(10)));
		Assert.assertTrue(columnIndexesInGroup.contains(Integer.valueOf(11)));
		Assert.assertTrue(columnIndexesInGroup.contains(Integer.valueOf(12)));
	}

}
