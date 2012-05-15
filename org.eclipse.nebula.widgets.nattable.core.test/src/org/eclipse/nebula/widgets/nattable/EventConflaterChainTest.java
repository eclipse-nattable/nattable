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
package org.eclipse.nebula.widgets.nattable;

import static org.junit.Assert.assertEquals;

import org.eclipse.nebula.widgets.nattable.conflation.EventConflaterChain;
import org.eclipse.nebula.widgets.nattable.conflation.VisualChangeEventConflater;
import org.eclipse.nebula.widgets.nattable.test.fixture.LayerEventFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EventConflaterChainTest {

	private EventConflaterChain conflaterChain;
	private VisualChangeEventConflater conflater1;
	private VisualChangeEventConflater conflater2;
	private NatTableFixture natTableFixture;

	@Before
	public void setup(){
		conflaterChain = new EventConflaterChain(10, 10);
		natTableFixture = new NatTableFixture();
		conflater1 = new VisualChangeEventConflater(natTableFixture);
		conflater2 = new VisualChangeEventConflater(natTableFixture);

		conflaterChain.add(conflater1);
		conflaterChain.add(conflater2);
	}
	
	@Test
	public void shouldAddEventsToAllChildren() throws Exception {
		conflaterChain.addEvent(new LayerEventFixture());
		conflaterChain.addEvent(new LayerEventFixture());
		
		assertEquals(2, conflater1.getCount());
		assertEquals(2, conflater2.getCount());
	}
	
	@Test
	public void shouldStartUpAllConflaterTasksAtTheEndOfTheInterval() throws Exception {
		conflaterChain.start();

		conflaterChain.addEvent(new LayerEventFixture());
		conflaterChain.addEvent(new LayerEventFixture());
		
		Thread.sleep(100);
		
		assertEquals(0, conflater1.getCount());
		assertEquals(0, conflater2.getCount());
	}
	
	@After
	public void teardown(){
		conflaterChain.stop();
	}
}
