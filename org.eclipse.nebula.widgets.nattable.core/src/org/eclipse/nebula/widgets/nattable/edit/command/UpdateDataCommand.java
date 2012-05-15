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
package org.eclipse.nebula.widgets.nattable.edit.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractPositionCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class UpdateDataCommand extends AbstractPositionCommand {

	private final Object newValue;

	public UpdateDataCommand(ILayer layer, int columnPosition, int rowPosition, Object newValue) {
		super(layer, columnPosition, rowPosition);
		this.newValue = newValue;
	}
	
	protected UpdateDataCommand(UpdateDataCommand command) {
		super(command);
		this.newValue = command.newValue;
	}
	
	public Object getNewValue() {
		return newValue;
	}
	
	public UpdateDataCommand cloneCommand() {
		return new UpdateDataCommand(this);
	}

}
