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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupUtils;
import org.eclipse.nebula.widgets.nattable.layer.stack.ColumnGroupBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.group.ColumnGroupModelFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Before;
import org.junit.Test;

public class ColumnGroupUtilsTest {

	private ColumnGroupModel model;
	private ColumnGroupBodyLayerStack bodyStack;

	/*
	 * Test fixture
	 *
	 *       0    1    2    3     4    5    6 ...  8   9   10  11  12
	 * ------------------------------------------------------------------
	 *     |<- G1 ->|     |<-- G2 -->|           |<- G4 ->|<--- G3 --->|
	 */
	@Before
	public void setup(){
		model = new ColumnGroupModelFixture();
		model.addColumnsIndexesToGroup("G4", 8, 9);

		bodyStack = new ColumnGroupBodyLayerStack(new DataLayerFixture(20, 10, 10, 20), model);

		new NatTableFixture(bodyStack); // Inits client area
	}

	@Test
	public void isRightEdgeOfAColumnGroup() throws Exception {
		assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(bodyStack, 0, 0, model));

		// 1
		assertTrue(ColumnGroupUtils.isRightEdgeOfAColumnGroup(bodyStack, 1, 1, model));

		assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(bodyStack, 2, 2, model));
		assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(bodyStack, 3, 3, model));

		// 4
		assertTrue(ColumnGroupUtils.isRightEdgeOfAColumnGroup(bodyStack, 4, 4, model));

		assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(bodyStack, 5, 5, model));
		assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(bodyStack, 6, 6, model));
		assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(bodyStack, 7, 7, model));
		assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(bodyStack, 8, 8, model));

		// 9
		assertTrue(ColumnGroupUtils.isRightEdgeOfAColumnGroup(bodyStack, 9, 9, model));

		assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(bodyStack, 10, 10, model));
		assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(bodyStack, 11, 11, model));

		// 12
		assertTrue(ColumnGroupUtils.isRightEdgeOfAColumnGroup(bodyStack, 12, 12, model));

		assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(bodyStack, 13, 13, model));
		assertFalse(ColumnGroupUtils.isRightEdgeOfAColumnGroup(bodyStack, 14, 14, model));
	}

	@Test
	public void isLeftEdgeOfAColumnGroup() throws Exception {
		// 0
		assertTrue(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(bodyStack, 0, 0, model));

		assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(bodyStack, 1, 1, model));
		assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(bodyStack, 2, 2, model));

		// 3
		assertTrue(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(bodyStack, 3, 3, model));

		assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(bodyStack, 4, 4, model));
		assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(bodyStack, 5, 5, model));
		assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(bodyStack, 6, 6, model));
		assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(bodyStack, 7, 7, model));

		// 8
		assertTrue(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(bodyStack, 8, 8, model));

		assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(bodyStack, 9, 9, model));

		// 10
		assertTrue(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(bodyStack, 10, 10, model));

		assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(bodyStack, 11, 11, model));
		assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(bodyStack, 12, 12, model));
		assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(bodyStack, 13, 13, model));
		assertFalse(ColumnGroupUtils.isLeftEdgeOfAColumnGroup(bodyStack, 14, 14, model));
	}
}
