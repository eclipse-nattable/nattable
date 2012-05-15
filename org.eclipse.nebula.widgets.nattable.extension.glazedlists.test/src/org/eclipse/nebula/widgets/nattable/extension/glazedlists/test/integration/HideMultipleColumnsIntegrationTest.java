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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.test.integration;


import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.Assert;
import org.junit.Test;

import ca.odell.glazedlists.GlazedLists;

public class HideMultipleColumnsIntegrationTest {

	/**
	 * Exposing bug: http://nattable.org/jira/browse/NTBL-471
	 */
	@Test
	public void hideAllColumnsWithColumnGroupsEnabled() throws Exception {
		BodyLayerStackFixture<RowDataFixture> bodyLayerStackFixture =
			new BodyLayerStackFixture<RowDataFixture>(
				GlazedLists.eventList(RowDataListFixture.getList()),
				new ReflectiveColumnPropertyAccessor<RowDataFixture>(RowDataListFixture.getPropertyNames()),
				new ConfigRegistry());

		NatTableFixture natTableFixture = new NatTableFixture(bodyLayerStackFixture);
		LayerListenerFixture listenerFixture = new LayerListenerFixture();
		natTableFixture.addLayerListener(listenerFixture);

		Assert.assertEquals(37, bodyLayerStackFixture.getBodyDataProvider().getColumnCount());
		Assert.assertEquals(6, natTableFixture.getColumnCount());

		MultiColumnHideCommand hideAllCommand = new MultiColumnHideCommand(natTableFixture,
				new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
							21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,	32, 33, 34, 35, 36});

		natTableFixture.doCommand(hideAllCommand);
		Assert.assertEquals(1, listenerFixture.getEventsCount());

		ILayerEvent receivedEvent = listenerFixture.getReceivedEvent(HideColumnPositionsEvent.class);
		Assert.assertNotNull(receivedEvent);
	}
}
