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
package org.eclipse.nebula.widgets.nattable.freeze.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class FreezeColumnCommand extends AbstractColumnCommand implements IFreezeCommand {
	
	private boolean toggle;
	
	public FreezeColumnCommand(ILayer layer, int columnPosition) {
		this(layer, columnPosition, false);
	}
	
	public FreezeColumnCommand(ILayer layer, int columnPosition, boolean toggle) {
		super(layer, columnPosition);
		this.toggle = toggle;
	}
	
	/**
	 * Indicates whether this command should toggle the frozen state between
	 * frozen and unfrozen, or if it should always result in a frozen state.
	 */
	public boolean isToggle() {
		return toggle;
	}
	
	protected FreezeColumnCommand(FreezeColumnCommand command) {
		super(command);
		this.toggle = command.toggle;
	}
	
	public ILayerCommand cloneCommand() {
		return new FreezeColumnCommand(this);
	}
	
}
