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
package org.eclipse.nebula.widgets.nattable.columnCategories;

import static org.eclipse.nebula.widgets.nattable.util.ArrayUtil.asIntegerList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;


import org.eclipse.nebula.widgets.nattable.columnCategories.ChooseColumnsFromCategoriesCommandHandler;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.test.fixture.ColumnCategoriesModelFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ColumnHeaderLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ColumnHideShowLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Before;
import org.junit.Test;

public class ChooseColumnsFromCategoriesCommandHandlerTest {

	private ChooseColumnsFromCategoriesCommandHandler commandHandler;
	private ColumnHideShowLayerFixture hideShowLayerFixture;

	@Before
	public void setup() {
		hideShowLayerFixture = new ColumnHideShowLayerFixture();
		commandHandler = new ChooseColumnsFromCategoriesCommandHandler(hideShowLayerFixture,
				new ColumnHeaderLayerFixture(), new DataLayerFixture(), new ColumnCategoriesModelFixture());
	}

	@Test
	public void shouldFireCorrectMoveCommandsOnTable() throws Exception {
		commandHandler.itemsMoved(MoveDirectionEnum.DOWN, Arrays.asList(1));
		assertTrue(hideShowLayerFixture.getLastCommand() instanceof ColumnReorderCommand);

		commandHandler.itemsMoved(MoveDirectionEnum.DOWN, Arrays.asList(1, 2, 3));
		assertTrue(hideShowLayerFixture.getLastCommand() instanceof MultiColumnReorderCommand);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void calculateDestinationPositionsForMovingUp() throws Exception {
		List<Integer> destinationPositions = commandHandler.getDestinationPositions(
				MoveDirectionEnum.UP,
				Arrays.asList(asIntegerList(1,2,3), asIntegerList(7), asIntegerList(12)));

		assertEquals(3, destinationPositions.size());
		assertEquals(0, destinationPositions.get(0).intValue());
		assertEquals(6, destinationPositions.get(1).intValue());
		assertEquals(11, destinationPositions.get(2).intValue());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void calculateDestinationPositionsForMovingDown() throws Exception {
		List<Integer> destinationPositions = commandHandler.getDestinationPositions(
				MoveDirectionEnum.DOWN,
				Arrays.asList(asIntegerList(1,2,3), asIntegerList(7), asIntegerList(12)));

		assertEquals(3, destinationPositions.size());
		assertEquals(5, destinationPositions.get(0).intValue());
		assertEquals(9, destinationPositions.get(1).intValue());
		assertEquals(14, destinationPositions.get(2).intValue());
	}

}
