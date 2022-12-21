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
package org.eclipse.nebula.widgets.nattable.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.PersistableFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.PropertiesFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.command.CommandHandlerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.command.LayerCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AbstractLayerTest {

    private DataLayerFixture dataLayer;
    private LayerListenerFixture firstListener;

    @BeforeEach
    public void setup() {
        this.dataLayer = new DataLayerFixture();

        this.firstListener = new LayerListenerFixture();
        this.dataLayer.addLayerListener(this.firstListener);
    }

    @Test
    public void testFireOriginalEventIfOnlyOneListener() {
        ILayerEvent event = new ColumnResizeEvent(this.dataLayer, 2);
        this.dataLayer.fireLayerEvent(event);

        List<ILayerEvent> receivedEvents = this.firstListener.getReceivedEvents();
        assertNotNull(receivedEvents);
        assertEquals(1, receivedEvents.size());
        assertSame(event, receivedEvents.get(0));
    }

    @Test
    public void testFireClonedEventIfMultipleListeners() {
        LayerListenerFixture secondListener = new LayerListenerFixture();
        this.dataLayer.addLayerListener(secondListener);

        ILayerEvent event = new ColumnResizeEvent(this.dataLayer, 2);
        this.dataLayer.fireLayerEvent(event);

        List<ILayerEvent> receivedEvents = this.firstListener.getReceivedEvents();
        assertNotNull(receivedEvents);
        assertEquals(1, receivedEvents.size());
        assertNotSame(event, receivedEvents.get(0));

        receivedEvents = secondListener.getReceivedEvents();
        assertNotNull(receivedEvents);
        assertEquals(1, receivedEvents.size());
        assertSame(event, receivedEvents.get(0));
    }

    @Test
    public void persistablesAreSaved() throws Exception {
        PersistableFixture persistable = new PersistableFixture();
        PropertiesFixture properties = new PropertiesFixture();

        this.dataLayer.registerPersistable(persistable);
        this.dataLayer.saveState("test_prefix", properties);

        assertTrue(persistable.stateSaved);
    }

    @Test
    public void commandHandlerRegistration() throws Exception {
        LayerCommandFixture command = new LayerCommandFixture();
        CommandHandlerFixture commandHandler = new CommandHandlerFixture();

        this.dataLayer.registerCommandHandler(commandHandler);
        this.dataLayer.doCommand(command);

        assertNotNull(commandHandler.getLastCommandHandled());
        commandHandler.clearLastCommandHandled();

        this.dataLayer.unregisterCommandHandler(command.getClass());
        this.dataLayer.doCommand(command);
        assertNull(commandHandler.getLastCommandHandled());
    }
}
