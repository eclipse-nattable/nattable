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
package org.eclipse.nebula.widgets.nattable.hideshow.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;

public class MultiRowShowCommand extends AbstractContextFreeCommand {

	private final int[] rowIndexes;

	public MultiRowShowCommand(int[] rowIndexes) {
		this.rowIndexes = rowIndexes;
	}

	protected MultiRowShowCommand(MultiRowShowCommand command) {
		rowIndexes = new int[command.rowIndexes.length];
		System.arraycopy(command.rowIndexes, 0, rowIndexes, 0, command.rowIndexes.length);
	}

	public int[] getRowIndexes() {
		return rowIndexes;
	}
	
}
