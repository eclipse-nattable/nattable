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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.group.action.ColumnHeaderReorderDragMode;
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
		model = new ColumnGroupModelFixture();
		model.addColumnsIndexesToGroup("G4", 18, 19, 20);

		model.getColumnGroupByIndex(0).setUnbreakable(true);
		model.getColumnGroupByIndex(10).setUnbreakable(true);

		testLayer = new DataLayerFixture(20, 10, 100, 20);
		dragMode = new ColumnHeaderReorderDragMode(model);
	}

	/*
	 * Test Setup
	 *
	 *        0   1    2    3     4    5    6    7   ...  10  11  12 ...   17   18  19  20
	 * ---------------------------------------------------------------------------------------
	 *     |<- G1 ->|     |<-- G2 -->|                   |<--- G3 --->|        |<--- G4 --->|
	 *     |UnBreak.|                                    |UnBreakable |
	 */
	@Test
	public void basicReordering() throws Exception {
		assertTrue(dragMode.isValidTargetColumnPosition(testLayer, 6, 9));
	}

	@Test
	public void shouldNotAllowMovingIntoAnUnbreakableGroup() throws Exception {
		assertFalse(dragMode.isValidTargetColumnPosition(testLayer, 2, 10));
	}

	@Test
	public void shouldNotAllowMovingOutOfAnUnbreakableGroup() throws Exception {
		assertFalse(dragMode.isValidTargetColumnPosition(testLayer, 0, 7));
	}

	// Reordering among column group

	@Test
	public void shouldAllowReorderingWithinAnUnbreakableGroup() throws Exception {
		assertTrue(dragMode.isValidTargetColumnPosition(testLayer, 10, 11));
	}

	@Test
	public void shouldAllowReorderingWithinARegularGroup() throws Exception {
		assertTrue(dragMode.isValidTargetColumnPosition(testLayer, 18, 19));
	}

	// Moving between groups

	@Test
	public void shouldAllowMovingBetweenRegularGroups() throws Exception {
		assertTrue(dragMode.isValidTargetColumnPosition(testLayer, 3, 19));
	}

	@Test
	public void shouldNotAllowMovingBetweenTwoUnbreakableGroups() throws Exception {
		assertFalse(dragMode.isValidTargetColumnPosition(testLayer, 0, 11));
	}

	@Test
	public void shouldNotAllowMovingFromUnbreakableGroupToRegularGroup() throws Exception {
		assertFalse(dragMode.isValidTargetColumnPosition(testLayer, 0, 3));
	}

	@Test
	public void shouldNotAllowMovingFromRegularGroupToUnbreakableGroup() throws Exception {
		assertFalse(dragMode.isValidTargetColumnPosition(testLayer, 3, 11));
	}
}
