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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.nebula.widgets.nattable.command.DisposeResourcesCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.LayerEventFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.command.AnyCommandHandlerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NatTableTest {

    private NatTable natTable;
    private DataLayerFixture underlyingLayerFixture;

    @BeforeEach
    public void setup() {
        this.underlyingLayerFixture = new DataLayerFixture(10, 5, 100, 20);
        this.natTable = new NatTable(new Shell(Display.getDefault()),
                this.underlyingLayerFixture);
    }

    @Test
    public void shouldPassOnLayerEventsToListeners() throws Exception {
        LayerListenerFixture listener = new LayerListenerFixture();

        this.natTable.addLayerListener(listener);
        this.natTable.handleLayerEvent(new LayerEventFixture());

        assertTrue(listener.containsInstanceOf(LayerEventFixture.class));
    }

    @Test
    public void shouldFireDisposeCommandOnDisposal() throws Exception {
        AnyCommandHandlerFixture commandHandler = new AnyCommandHandlerFixture();
        this.underlyingLayerFixture.registerCommandHandler(commandHandler);

        this.natTable.dispose();

        assertEquals(1, commandHandler.getNumberOfCommandsHandled());
        assertTrue(commandHandler.getCommadHandled() instanceof DisposeResourcesCommand);
    }
}
