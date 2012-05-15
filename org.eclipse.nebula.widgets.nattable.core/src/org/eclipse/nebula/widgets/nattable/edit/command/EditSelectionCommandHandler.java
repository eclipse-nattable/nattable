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
import org.eclipse.nebula.widgets.nattable.edit.MultiCellEditController;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.swt.widgets.Composite;

public class EditSelectionCommandHandler extends AbstractLayerCommandHandler<EditSelectionCommand> {

	private SelectionLayer selectionLayer;
	
	public EditSelectionCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}
	
	public Class<EditSelectionCommand> getCommandClass() {
		return EditSelectionCommand.class;
	}
	
	@Override
    public boolean doCommand(EditSelectionCommand command) {
		Composite parent = command.getParent();
		IConfigRegistry configRegistry = command.getConfigRegistry();
		Character initialValue = command.getCharacter();
		
		return MultiCellEditController.editSelectedCells(selectionLayer, initialValue, parent, configRegistry, command.isUseAdjustOnMultiCellEdit());
	}
}
