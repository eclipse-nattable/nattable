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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EventConflaterChainTest {

    private EventConflaterChain conflaterChain;
    private VisualChangeEventConflater conflater1;
    private VisualChangeEventConflater conflater2;
    private NatTableFixture natTableFixture;

    @BeforeEach
    public void setup() {
        this.conflaterChain = new EventConflaterChain(10, 10);
        this.natTableFixture = new NatTableFixture();
        this.conflater1 = new VisualChangeEventConflater(this.natTableFixture);
        this.conflater2 = new VisualChangeEventConflater(this.natTableFixture);

        this.conflaterChain.add(this.conflater1);
        this.conflaterChain.add(this.conflater2);
    }

    @Test
    public void shouldAddEventsToAllChildren() throws Exception {
        this.conflaterChain.addEvent(new LayerEventFixture());
        this.conflaterChain.addEvent(new LayerEventFixture());

        assertEquals(2, this.conflater1.getCount());
        assertEquals(2, this.conflater2.getCount());
    }

    @Test
    public void shouldStartUpAllConflaterTasksAtTheEndOfTheInterval()
            throws Exception {
        this.conflaterChain.start();

        this.conflaterChain.addEvent(new LayerEventFixture());
        this.conflaterChain.addEvent(new LayerEventFixture());

        Thread.sleep(100);

        assertEquals(0, this.conflater1.getCount());
        assertEquals(0, this.conflater2.getCount());
    }

    @AfterEach
    public void teardown() {
        this.conflaterChain.stop();
    }
}
