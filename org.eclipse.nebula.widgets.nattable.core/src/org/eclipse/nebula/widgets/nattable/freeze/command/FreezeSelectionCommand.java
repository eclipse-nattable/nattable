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

import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Will inform the handler to use the selection layer for its freeze coordinates.
 *
 */
public class FreezeSelectionCommand implements IFreezeCommand {

	private boolean toggle;

	public FreezeSelectionCommand() {
		this(false);
	}
	
	public FreezeSelectionCommand(boolean toggle) {
		this.toggle = toggle;
	}
	
	/**
	 * Indicates whether this command should toggle the frozen state between
	 * frozen and unfrozen, or if it should always result in a frozen state.
	 */
	public boolean isToggle() {
		return toggle;
	}
	
	public boolean convertToTargetLayer(ILayer targetLayer) {
		return true;
	}
	
	public FreezeSelectionCommand cloneCommand() {
		return this;
	}
	
}
