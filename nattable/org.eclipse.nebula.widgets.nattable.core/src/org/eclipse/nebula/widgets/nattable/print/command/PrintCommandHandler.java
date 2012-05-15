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
package org.eclipse.nebula.widgets.nattable.print.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.print.GridLayerPrinter;

public class PrintCommandHandler extends AbstractLayerCommandHandler<PrintCommand> {

	private final GridLayer gridLayer;

	public PrintCommandHandler(GridLayer defaultGridLayer) {
		this.gridLayer = defaultGridLayer;
	}

	public boolean doCommand(PrintCommand command) {
		new GridLayerPrinter(gridLayer, command.getConfigRegistry()).print(command.getShell());
		return true;
	}

	public Class<PrintCommand> getCommandClass() {
		return PrintCommand.class;
	}

}
