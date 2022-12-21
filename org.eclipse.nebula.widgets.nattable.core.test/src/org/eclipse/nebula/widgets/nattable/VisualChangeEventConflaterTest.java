/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.nebula.widgets.nattable.conflation.EventConflaterChain;
import org.eclipse.nebula.widgets.nattable.conflation.VisualChangeEventConflater;
import org.eclipse.nebula.widgets.nattable.test.fixture.LayerEventFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.junit.jupiter.api.Test;

public class VisualChangeEventConflaterTest {

    @Test
    public void shouldAccumulateEvents() throws Exception {
        NatTableFixture natTable = new NatTableFixture();
        VisualChangeEventConflater conflater = new VisualChangeEventConflater(natTable);
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
