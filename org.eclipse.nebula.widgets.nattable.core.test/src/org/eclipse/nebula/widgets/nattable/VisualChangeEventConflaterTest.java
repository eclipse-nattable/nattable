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
import org.junit.Test;

public class VisualChangeEventConflaterTest {

	@Test
	public void shouldAccumulateEvents() throws Exception {
		NatTableFixture natTable = new NatTableFixture();
		VisualChangeEventConflater conflater = 	new VisualChangeEventConflater(natTable);
		EventConflaterChain chain = new EventConflaterChain();
		chain.add(conflater);
		
		conflater.addEvent(new LayerEventFixture());
		conflater.addEvent(new LayerEventFixture());
		assertEquals(2, conflater.getCount());

		chain.start();
		Thread.sleep(EventConflaterChain.DEFAULT_INITIAL_DELAY + 100);

		assertEquals(0, conflater.getCount());
	}
}
