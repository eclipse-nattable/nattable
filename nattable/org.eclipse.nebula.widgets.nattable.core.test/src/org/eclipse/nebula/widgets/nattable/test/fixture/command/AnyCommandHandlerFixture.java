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

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

@SuppressWarnings("unchecked")
public class AnyCommandHandlerFixture implements ILayerCommandHandler {

	private ILayerCommand commadHandled;
	private int numberOfCommandsHandled;

	public boolean doCommand(ILayer targetLayer, ILayerCommand command) {
		this.commadHandled = command;
		this.numberOfCommandsHandled++;
		return true;
	}

	public Class getCommandClass() {
		return ILayerCommand.class;
	}

	public ILayerCommand getCommadHandled() {
		return commadHandled;
	}

	public int getNumberOfCommandsHandled() {
		return numberOfCommandsHandled;
	}
}
