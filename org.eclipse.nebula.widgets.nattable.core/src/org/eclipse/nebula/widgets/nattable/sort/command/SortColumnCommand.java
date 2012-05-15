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
package org.eclipse.nebula.widgets.nattable.sort.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractColumnCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

public class SortColumnCommand extends AbstractColumnCommand {

	private boolean accumulate;
	
	public SortColumnCommand(ILayer layer, int columnPosition, boolean accumulate) {
		super(layer, columnPosition);
		this.accumulate = accumulate;
	}
	
	protected SortColumnCommand(SortColumnCommand command) {
		super(command);
		this.accumulate = command.accumulate;
	}
	
	public boolean isAccumulate() {
		return accumulate;
	}
	
	public SortColumnCommand cloneCommand() {
		return new SortColumnCommand(this);
	}
	
}
