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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataLayerCommandHandlingTest {

    private static final String TEST_VALUE = "New Value";

    private DataLayer dataLayer;
    private UpdateDataCommand command;

    @BeforeEach
    public void setup() {
        this.dataLayer = new DataLayerFixture();
        this.command = new UpdateDataCommand(this.dataLayer, 2, 2, TEST_VALUE);
    }

    @Test
    public void handleUpdateDataCommand() throws Exception {
        this.dataLayer.doCommand(this.command);
        assertEquals(TEST_VALUE, this.dataLayer.getDataProvider().getDataValue(2, 2));
    }

    @Test
    public void handleUpdateDataCommandRaisesEvents() throws Exception {
        LayerListenerFixture listener = new LayerListenerFixture();
        this.dataLayer.addLayerListener(listener);
        this.dataLayer.doCommand(this.command);
        assertTrue(listener.getReceivedEvents().get(0) instanceof CellVisualChangeEvent);
    }

    @Test
    public void handleSameUpdateDataCommandRaisesNoEvents() throws Exception {
        LayerListenerFixture listener = new LayerListenerFixture();
        this.dataLayer.addLayerListener(listener);
        this.dataLayer.doCommand(this.command);
        assertTrue(listener.getReceivedEvents().size() == 1);
        assertTrue(listener.getReceivedEvents().get(0) instanceof CellVisualChangeEvent);

        // as calling the UpdateCommand with the same value should not trigger
        // any event
        // the size of the received events will stay 1 (the one event from
        // before which is cached)
        this.dataLayer.doCommand(this.command);
        assertTrue(listener.getReceivedEvents().size() == 1);
    }
}
