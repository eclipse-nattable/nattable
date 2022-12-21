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
package org.eclipse.nebula.widgets.nattable.command;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.command.CommandHandlerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.command.LayerCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GenericLayerFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//Test AbstractLayer implementations of command propagation
public class CommandPropagationTest {

    private DataLayer underlyingLayer = new DataLayerFixture(10, 5, 100, 20);
    private ILayer layer = new GenericLayerFixture(this.underlyingLayer);

    @BeforeEach
    public void setUp() {
        this.underlyingLayer.registerCommandHandler(new CommandHandlerFixture());
    }

    @Test
    public void shouldHandleGenericLayerCommand() {
        assertTrue(this.layer.doCommand(new LayerCommandFixture()));
    }

    @Test
    public void shouldPropagateToUnderlyingLayer() {
        LayerCommandFixture command = new LayerCommandFixture();
        this.layer.doCommand(command);
        assertTrue(command.getTargetLayer() instanceof DataLayerFixture);
    }

}
