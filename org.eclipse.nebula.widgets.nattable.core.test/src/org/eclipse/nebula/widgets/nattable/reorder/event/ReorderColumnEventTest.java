/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.event;

import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseColumnReorderLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReorderColumnEventTest {
	private BaseColumnReorderLayerFixture columnReorderLayer;

	@Before
	public void setUp() {
		columnReorderLayer = new BaseColumnReorderLayerFixture(new DataLayerFixture());
	}

	@Test
	public void shouldThrowAReorderColumnEvent() {
		LayerListenerFixture listenerFixture = new LayerListenerFixture();
		columnReorderLayer.addLayerListener(listenerFixture);
		columnReorderLayer.reorderColumnPosition(3, 1);

		Assert.assertEquals(1, listenerFixture.getEventsCount());
		Assert.assertNotNull(listenerFixture.getReceivedEvent(ColumnReorderEvent.class));
	}

	/**
	 * Fix for http://nattable.org/jira/browse/NTBL-476
	 */
	@Test
	public void reorderEventMustPropagateToTheTop() throws Exception {
		DefaultBodyLayerStack underlyingLayer = new DefaultBodyLayerStack(new DataLayerFixture(20, 10, 100, 20));
		NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

		// Add listener
		LayerListenerFixture listenerFixture = new LayerListenerFixture();
		natTableFixture.addLayerListener(listenerFixture);

		Assert.assertEquals(6, natTableFixture.getColumnCount());
		Assert.assertEquals(1, natTableFixture.getColumnIndexByPosition(1));

		// Move to outside the visible range
		List<Integer> columnToMove = Arrays.asList(1, 2, 3);
		int destinationPosition = 10;
		natTableFixture.doCommand(new MultiColumnReorderCommand(natTableFixture, columnToMove, destinationPosition));

		// Ensure that the event propagates to the top
		Assert.assertEquals(1, listenerFixture.getEventsCount());
		Assert.assertNotNull(listenerFixture.getReceivedEvent(ColumnReorderEvent.class));
		Assert.assertEquals(4, natTableFixture.getColumnIndexByPosition(1));
	}
}
