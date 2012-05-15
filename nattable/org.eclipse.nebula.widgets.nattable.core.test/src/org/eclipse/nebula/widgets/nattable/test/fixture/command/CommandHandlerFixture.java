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
package org.eclipse.nebula.widgets.nattable.test.fixture.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;

public class CommandHandlerFixture extends AbstractLayerCommandHandler<LayerCommandFixture>{

	private LayerCommandFixture lastCommandHandled;

	@Override
	public boolean doCommand(LayerCommandFixture command) {
		this.lastCommandHandled = command;
		return true;
	}

	public Class<LayerCommandFixture> getCommandClass() {
		return LayerCommandFixture.class;
	}
	
	public LayerCommandFixture getLastCommandHandled() {
		return lastCommandHandled;
	}
	
	public void clearLastCommandHandled(){
		lastCommandHandled = null;
	}

}
