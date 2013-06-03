/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder.event;

import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiRowReorderCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseRowReorderLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReorderRowEventTest {
	private BaseRowReorderLayerFixture rowReorderLayer;

	@Before
	public void setUp() {
		rowReorderLayer = new BaseRowReorderLayerFixture(new DataLayerFixture());
	}

	@Test
	public void shouldThrowAReorderRowEvent() {
		LayerListenerFixture listenerFixture = new LayerListenerFixture();
		rowReorderLayer.addLayerListener(listenerFixture);
		rowReorderLayer.reorderRowPosition(3, 1);

		Assert.assertEquals(1, listenerFixture.getEventsCount());
		Assert.assertNotNull(listenerFixture.getReceivedEvent(RowReorderEvent.class));
	}

	@Test
	public void reorderEventMustPropagateToTheTop() throws Exception {
		DefaultBodyLayerStack underlyingLayer = new DefaultBodyLayerStack(
				new RowReorderLayer(new DataLayerFixture(10, 10, 100, 20)));
		NatTableFixture natTableFixture = new NatTableFixture(underlyingLayer);

		// Add listener
		LayerListenerFixture listenerFixture = new LayerListenerFixture();
		natTableFixture.addLayerListener(listenerFixture);

		Assert.assertEquals(10, natTableFixture.getRowCount());
		Assert.assertEquals(1, natTableFixture.getRowIndexByPosition(1));

		// Move to outside the visible range
		List<Integer> rowsToMove = Arrays.asList(1, 2, 3);
		int destinationPosition = 10;
		natTableFixture.doCommand(new MultiRowReorderCommand(natTableFixture, rowsToMove, destinationPosition));

		// Ensure that the event propagates to the top
		Assert.assertEquals(1, listenerFixture.getEventsCount());
		Assert.assertNotNull(listenerFixture.getReceivedEvent(RowReorderEvent.class));
		Assert.assertEquals(4, natTableFixture.getRowIndexByPosition(1));
	}
}
