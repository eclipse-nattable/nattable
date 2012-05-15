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
package org.eclipse.nebula.widgets.nattable.resize.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;

public class MultiRowResizeCommandHandler extends AbstractLayerCommandHandler<MultiRowResizeCommand> {

	private final DataLayer dataLayer;

	public MultiRowResizeCommandHandler(DataLayer dataLayer) {
		this.dataLayer = dataLayer;
	}
	
	public Class<MultiRowResizeCommand> getCommandClass() {
		return MultiRowResizeCommand.class;
	}

	@Override
	protected boolean doCommand(MultiRowResizeCommand command) {
		for (int rowPosition : command.getRowPositions()) {
			dataLayer.setRowHeightByPosition(rowPosition, command.getRowHeight(rowPosition));
		}
		return true;
	}

}
