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
package org.eclipse.nebula.widgets.nattable.blink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executors;


import org.eclipse.nebula.widgets.nattable.blink.CellKeyStrategyImpl;
import org.eclipse.nebula.widgets.nattable.blink.UpdateEventsCache;
import org.eclipse.nebula.widgets.nattable.layer.event.PropertyUpdateEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Before;
import org.junit.Test;

public class UpdateEventsCacheTest {
	
	private static final String ASK_PRICE = RowDataListFixture.ASK_PRICE_PROP_NAME;
	UpdateEventsCache<RowDataFixture> cache;
	private PropertyUpdateEvent<RowDataFixture> testEvent1;
	private PropertyUpdateEvent<RowDataFixture> testEvent2;
	private RowDataFixture bean1;
	private RowDataFixture bean2;
	private DataLayerFixture layerFixture = new DataLayerFixture();

	@Before
	public void setup(){
		cache = new UpdateEventsCache<RowDataFixture>(RowDataFixture.rowIdAccessor, new CellKeyStrategyImpl(), Executors.newSingleThreadScheduledExecutor());
		
		bean1 = RowDataListFixture.getList().get(0);
		testEvent1 = new PropertyUpdateEvent<RowDataFixture>(layerFixture, bean1, ASK_PRICE, Integer.valueOf(10), Integer.valueOf(15));

		bean2 = RowDataListFixture.getList().get(1);
		testEvent2 = new PropertyUpdateEvent<RowDataFixture>(layerFixture, bean2, ASK_PRICE, Integer.valueOf(20), Integer.valueOf(25));
	}
	
	@Test
	public void shouldAddUpdateEvents() throws Exception {
		cache.put(testEvent1);
		cache.put(testEvent2);
		assertEquals(2, cache.getCount());
	}
	
	@Test
	public void shouldUpdateEventDataForMultipleUpdatesToBean() throws Exception {
		// Update bean1
		cache.put(testEvent1);
		assertEquals(1, cache.getCount());

		// Update bean1, again
		PropertyUpdateEvent<RowDataFixture> bean1Update = new PropertyUpdateEvent<RowDataFixture>(layerFixture, bean1, ASK_PRICE, Integer.valueOf(15), Integer.valueOf(20));
		cache.put(bean1Update);
		assertEquals(1, cache.getCount());

		// Update must accumulate - just one update in all
		String key = cache.getKey(bean1Update);
		PropertyUpdateEvent<RowDataFixture> event = cache.getEvent(key);
		assertEquals(String.valueOf(15), event.getOldValue().toString());
		assertEquals(String.valueOf(20), event.getNewValue().toString());
	}
	
	@Test
	public void shouldConstructTheKeyUsingTheColumnIndexAndRowId() throws Exception {
		assertEquals("ask_price-B Ford Motor", cache.getKey(testEvent1));
	}
	
	@Test
	public void keyGeneration() throws Exception {
		String key = cache.getKey(ASK_PRICE, "100");
		assertTrue(key.startsWith("ask_price-100"));
	}
	
	@Test
	public void shouldCleanUpStaleEventsAterTTLExpires() throws Exception {
		cache.put(testEvent1);
		cache.put(testEvent2);
		assertEquals(2, cache.getCount());
		
		Thread.sleep(UpdateEventsCache.INITIAL_DELAY + UpdateEventsCache.TIME_TO_LIVE + 100);
		
		assertEquals(0, cache.getCount());
	}
}
