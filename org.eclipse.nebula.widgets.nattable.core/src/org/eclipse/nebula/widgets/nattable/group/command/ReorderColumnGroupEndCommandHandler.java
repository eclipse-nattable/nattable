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
package org.eclipse.nebula.widgets.nattable.group.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;

public class ReorderColumnGroupEndCommandHandler extends AbstractLayerCommandHandler<ReorderColumnGroupEndCommand> {

	private final ColumnGroupReorderLayer columnGroupReorderLayer;

	public ReorderColumnGroupEndCommandHandler(ColumnGroupReorderLayer columnGroupReorderLayer) {
		this.columnGroupReorderLayer = columnGroupReorderLayer;
	}
	
	public Class<ReorderColumnGroupEndCommand> getCommandClass() {
		return ReorderColumnGroupEndCommand.class;
	}

	@Override
	protected boolean doCommand(ReorderColumnGroupEndCommand command) {
		int toColumnPosition = command.getToColumnPosition();
		
		return columnGroupReorderLayer.reorderColumnGroup(columnGroupReorderLayer.getReorderFromColumnPosition(), toColumnPosition);
	}

}
