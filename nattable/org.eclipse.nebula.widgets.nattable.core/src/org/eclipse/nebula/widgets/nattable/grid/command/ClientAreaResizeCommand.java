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
package org.eclipse.nebula.widgets.nattable.grid.command;


import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.swt.widgets.Scrollable;

/**
 * Command that gives the layers access to ClientArea and the Scrollable 
 */
public class ClientAreaResizeCommand extends AbstractContextFreeCommand {
	private Scrollable scrollable;
	
	public ClientAreaResizeCommand(Scrollable scrollable) {
		super();
		this.scrollable = scrollable;
	}

	public Scrollable getScrollable() {
		return scrollable;
	}
}
