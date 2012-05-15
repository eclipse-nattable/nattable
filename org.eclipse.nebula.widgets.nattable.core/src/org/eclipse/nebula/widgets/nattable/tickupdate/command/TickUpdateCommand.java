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
package org.eclipse.nebula.widgets.nattable.tickupdate.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class TickUpdateCommand implements ILayerCommand {

	private final IConfigRegistry configRegistry;
	private final boolean increment;

	public TickUpdateCommand(IConfigRegistry configRegistry, boolean increment) {
		this.configRegistry = configRegistry;
		this.increment = increment;
	}

	protected TickUpdateCommand(TickUpdateCommand command) {
		this.configRegistry = command.configRegistry;
		this.increment = command.increment;
	}
	
	public TickUpdateCommand cloneCommand() {
		return new TickUpdateCommand(this);
	}

	public boolean convertToTargetLayer(ILayer targetLayer) {
		// No op.
		return true;
	}
	
	public IConfigRegistry getConfigRegistry() {
		return configRegistry;
	}
	
	public boolean isIncrement() {
		return increment;
	}
}
