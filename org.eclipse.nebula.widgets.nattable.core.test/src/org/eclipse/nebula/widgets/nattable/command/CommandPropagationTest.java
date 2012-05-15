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
package org.eclipse.nebula.widgets.nattable.command;


import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.command.CommandHandlerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.command.LayerCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GenericLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

//Test AbstractLayer implementations of command propagation
public class CommandPropagationTest {

	private DataLayer underlyingLayer = new DataLayerFixture(10, 5, 100, 20);
	private ILayer layer = new GenericLayerFixture(underlyingLayer);

	@Before
	public void setUp() {
		underlyingLayer.registerCommandHandler(new CommandHandlerFixture());
	}

	@Test
	public void shouldHandleGenericLayerCommand() {
		Assert.assertTrue(layer.doCommand(new LayerCommandFixture()));
	}
	
	@Test
	public void shouldPropagateToUnderlyingLayer() {
		LayerCommandFixture command = new LayerCommandFixture();
		layer.doCommand(command);
		Assert.assertTrue(command.getTargetLayer() instanceof DataLayerFixture);
	}
	
}
