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
package org.eclipse.nebula.widgets.nattable.blink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.concurrent.Executors;

import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.layer.event.PropertyUpdateEvent;
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
    public void setup() {
        this.cache = new UpdateEventsCache<RowDataFixture>(
                new IRowIdAccessor<RowDataFixture>() {

                    @Override
                    public Serializable getRowId(RowDataFixture rowObject) {
                        return rowObject.getSecurity_description();
                    }
                },
                new CellKeyStrategyImpl(),
                Executors.newSingleThreadScheduledExecutor());

        this.bean1 = RowDataListFixture.getList().get(0);
        this.testEvent1 = new PropertyUpdateEvent<RowDataFixture>(this.layerFixture,
                this.bean1, ASK_PRICE, Integer.valueOf(10), Integer.valueOf(15));

        this.bean2 = RowDataListFixture.getList().get(1);
        this.testEvent2 = new PropertyUpdateEvent<RowDataFixture>(this.layerFixture,
                this.bean2, ASK_PRICE, Integer.valueOf(20), Integer.valueOf(25));
    }

    @Test
    public void shouldAddUpdateEvents() throws Exception {
        this.cache.put(this.testEvent1);
        this.cache.put(this.testEvent2);
        assertEquals(2, this.cache.getCount());
    }

    @Test
    public void shouldUpdateEventDataForMultipleUpdatesToBean()
            throws Exception {
        // Update bean1
        this.cache.put(this.testEvent1);
        assertEquals(1, this.cache.getCount());

        // Update bean1, again
        PropertyUpdateEvent<RowDataFixture> bean1Update = new PropertyUpdateEvent<RowDataFixture>(
                this.layerFixture, this.bean1, ASK_PRICE, Integer.valueOf(15),
                Integer.valueOf(20));
        this.cache.put(bean1Update);
        assertEquals(1, this.cache.getCount());

        // Update must accumulate - just one update in all
        String key = this.cache.getKey(bean1Update);
        PropertyUpdateEvent<RowDataFixture> event = this.cache.getEvent(key);
        assertEquals(String.valueOf(15), event.getOldValue().toString());
        assertEquals(String.valueOf(20), event.getNewValue().toString());
    }

    @Test
    public void shouldConstructTheKeyUsingTheColumnIndexAndRowId()
            throws Exception {
        assertEquals("ask_price-B Ford Motor", this.cache.getKey(this.testEvent1));
    }

    @Test
    public void keyGeneration() throws Exception {
        String key = this.cache.getKey(ASK_PRICE, "100");
        assertTrue(key.startsWith("ask_price-100"));
    }

    @Test
    public void shouldCleanUpStaleEventsAterTTLExpires() throws Exception {
        this.cache.put(this.testEvent1);
        this.cache.put(this.testEvent2);
        assertEquals(2, this.cache.getCount());

        Thread.sleep(UpdateEventsCache.INITIAL_DELAY
                + UpdateEventsCache.TIME_TO_LIVE + 100);

        assertEquals(0, this.cache.getCount());
    }
}
