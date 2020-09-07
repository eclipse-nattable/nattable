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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.BlinkingRowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.LayerListenerFixture;
import org.eclipse.nebula.widgets.nattable.layer.event.PropertyUpdateEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.RowStructuralRefreshEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

public class GlazedListsEventLayerTest {

    private EventList<RowDataFixture> listFixture;
    private GlazedListsEventLayer<RowDataFixture> layerUnderTest;
    private LayerListenerFixture listenerFixture;

    @BeforeEach
    public void setup() {
        this.listFixture = GlazedLists.eventList(RowDataListFixture.getList());

        this.layerUnderTest = new GlazedListsEventLayer<>(new DataLayerFixture(), this.listFixture);
        this.layerUnderTest.setTestMode(true);

        this.listenerFixture = new LayerListenerFixture();
        this.layerUnderTest.addLayerListener(this.listenerFixture);
    }

    @AfterEach
    public void tearDown() {
        this.layerUnderTest.doCommand(new DisposeResourcesCommand());
    }

    @Test
    public void shouldConflateEvents() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        this.listenerFixture.setCountDownLatch(countDownLatch);
        this.listFixture.add(RowDataFixture.getInstance("T1", "A"));
        countDownLatch.await(500, TimeUnit.MILLISECONDS);

        countDownLatch = new CountDownLatch(1);
        this.listenerFixture.setCountDownLatch(countDownLatch);
        this.listFixture.add(RowDataFixture.getInstance("T2", "A"));
        countDownLatch.await(500, TimeUnit.MILLISECONDS);

        assertNotNull(this.listenerFixture.getReceivedEvent(RowStructuralRefreshEvent.class));
    }

    @Test
    public void shouldShutConflaterThreadDownWhenNatTableIsDisposed() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        this.listenerFixture.setCountDownLatch(countDownLatch);

        assertFalse(this.layerUnderTest.isDisposed());

        this.listFixture.add(RowDataFixture.getInstance("T1", "A"));
        countDownLatch.await(500, TimeUnit.MILLISECONDS);

        countDownLatch = new CountDownLatch(1);
        this.listenerFixture.setCountDownLatch(countDownLatch);
        this.listFixture.add(RowDataFixture.getInstance("T2", "A"));
        countDownLatch.await(500, TimeUnit.MILLISECONDS);

        this.layerUnderTest.doCommand(new DisposeResourcesCommand());
        assertTrue(this.layerUnderTest.isDisposed());
    }

    @Test
    public void propertyChangeEventshouldBePropagatedImmediately() throws Exception {
        List<BlinkingRowDataFixture> list = BlinkingRowDataFixture.getList(this.layerUnderTest);
        list.get(0).setAsk_price(100.0F);

        assertNotNull(this.listenerFixture.getReceivedEvent(PropertyUpdateEvent.class));
    }
}
