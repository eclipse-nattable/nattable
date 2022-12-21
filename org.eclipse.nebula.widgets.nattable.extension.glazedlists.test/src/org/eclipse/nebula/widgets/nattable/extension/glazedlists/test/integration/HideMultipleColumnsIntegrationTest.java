/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
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
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.test.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataFixture;
import org.eclipse.nebula.widgets.nattable.dataset.fixture.data.RowDataListFixture;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.LayerListenerFixture;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.hideshow.command.MultiColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.event.HideColumnPositionsEvent;
import org.eclipse.nebula.widgets.nattable.layer.NoScalingDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.odell.glazedlists.GlazedLists;

public class HideMultipleColumnsIntegrationTest {

    BodyLayerStackFixture<RowDataFixture> bodyLayerStackFixture;
    NatTableFixture natTableFixture;
    LayerListenerFixture listenerFixture;

    @BeforeEach
    public void setup() {
        this.bodyLayerStackFixture = new BodyLayerStackFixture<>(
                GlazedLists.eventList(RowDataListFixture.getList()),
                new ReflectiveColumnPropertyAccessor<RowDataFixture>(
                        RowDataListFixture.getPropertyNames()),
                new ConfigRegistry());

        this.natTableFixture = new NatTableFixture(this.bodyLayerStackFixture);
        this.natTableFixture.doCommand(new ConfigureScalingCommand(new NoScalingDpiConverter()));
        this.listenerFixture = new LayerListenerFixture();
        this.natTableFixture.addLayerListener(this.listenerFixture);
    }

    // Exposing bug: http://nattable.org/jira/browse/NTBL-471
    @Test
    public void hideAllColumnsWithColumnGroupsEnabled() throws Exception {
        assertEquals(37, this.bodyLayerStackFixture.getBodyDataProvider().getColumnCount());
        assertEquals(6, this.natTableFixture.getColumnCount());

        MultiColumnHideCommand hideAllCommand = new MultiColumnHideCommand(
                this.natTableFixture, new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                        11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
                        25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36 });

        this.natTableFixture.doCommand(hideAllCommand);
        assertEquals(1, this.listenerFixture.getEventsCount());

        ILayerEvent receivedEvent = this.listenerFixture.getReceivedEvent(HideColumnPositionsEvent.class);
        assertNotNull(receivedEvent);

        this.natTableFixture.doCommand(new DisposeResourcesCommand());
    }

    @AfterEach
    public void tearDown() {
        this.natTableFixture.doCommand(new DisposeResourcesCommand());
    }
}
