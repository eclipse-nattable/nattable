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
package org.eclipse.nebula.widgets.nattable.group.action;


import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.action.ColumnGroupHeaderReorderDragMode;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.group.ColumnGroupModelFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ColumnGroupReorderDragModeTest {

	private ILayer testLayer;
	private ColumnGroupModel columnGroupModel;
	private ColumnGroupHeaderReorderDragMode groupReorderDragMode;

	@Before
	public void setup() {
		testLayer = new DataLayerFixture(10, 5, 100, 20);
		columnGroupModel = new ColumnGroupModelFixture();
		groupReorderDragMode = new ColumnGroupHeaderReorderDragMode(columnGroupModel);
	}

	/*  Test Fixture
	 *
	 *        0   1    2    3     4    5    6    7   ...  10  11  12
	 * ------------------------------------------------------------------
	 *     |<- G1 ->|     |<-- G2 -->|                   |<--- G3 --->|
	 */

	@Test
	public void isValidTargetColumnPositionMovingRight() throws Exception {
		Assert.assertFalse(groupReorderDragMode.isValidTargetColumnPosition(testLayer, 0, 0));
		Assert.assertFalse(groupReorderDragMode.isValidTargetColumnPosition(testLayer, 0, 1));

		Assert.assertTrue(groupReorderDragMode.isValidTargetColumnPosition(testLayer, 0, 2));

		Assert.assertFalse(groupReorderDragMode.isValidTargetColumnPosition(testLayer, 0, 3));
		Assert.assertFalse(groupReorderDragMode.isValidTargetColumnPosition(testLayer, 0, 4));

		Assert.assertTrue(groupReorderDragMode.isValidTargetColumnPosition(testLayer, 0, 5));
		Assert.assertTrue(groupReorderDragMode.isValidTargetColumnPosition(testLayer, 0, 6));
	}

	@Test
	public void isValidTargetColumnPositionMovingLeft() throws Exception {
		Assert.assertTrue(groupReorderDragMode.isValidTargetColumnPosition(testLayer, 11, 10));

		Assert.assertTrue(groupReorderDragMode.isValidTargetColumnPosition(testLayer, 11, 9));
		Assert.assertTrue(groupReorderDragMode.isValidTargetColumnPosition(testLayer, 11, 6));
		Assert.assertTrue(groupReorderDragMode.isValidTargetColumnPosition(testLayer, 11, 5));

		Assert.assertFalse(groupReorderDragMode.isValidTargetColumnPosition(testLayer, 11, 4));
		Assert.assertFalse(groupReorderDragMode.isValidTargetColumnPosition(testLayer, 11, 3));

		Assert.assertTrue(groupReorderDragMode.isValidTargetColumnPosition(testLayer, 11, 2));

		Assert.assertFalse(groupReorderDragMode.isValidTargetColumnPosition(testLayer, 11, 1));
		Assert.assertFalse(groupReorderDragMode.isValidTargetColumnPosition(testLayer, 11, 0));
	}
}
