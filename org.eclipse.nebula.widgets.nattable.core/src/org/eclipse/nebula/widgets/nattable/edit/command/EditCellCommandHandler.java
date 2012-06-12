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

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.InlineCellEditController;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.swt.widgets.Composite;

public class EditCellCommandHandler extends AbstractLayerCommandHandler<EditCellCommand> {

	public Class<EditCellCommand> getCommandClass() {
		return EditCellCommand.class;
	}
	
	@Override
	public boolean doCommand(EditCellCommand command) {
		ILayerCell cell = command.getCell();
		Composite parent = command.getParent();
		IConfigRegistry configRegistry = command.getConfigRegistry();
		
		return InlineCellEditController.editCellInline(cell, null, parent, configRegistry);
	}

}
