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


import org.eclipse.nebula.widgets.nattable.command.AbstractContextFreeCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.swt.widgets.Composite;

public class EditSelectionCommand extends AbstractContextFreeCommand {
	
	private final IConfigRegistry configRegistry;
	
	private final Character character;

	private final Composite parent;

    private final boolean useAdjustOnMultiCellEdit;

	public EditSelectionCommand(Composite parent, IConfigRegistry configRegistry, Character character, boolean useAdjustOnMultiCellEdit) {
		this.parent = parent;
		this.configRegistry = configRegistry;
		this.character = character;
        this.useAdjustOnMultiCellEdit = useAdjustOnMultiCellEdit;
	}

	public IConfigRegistry getConfigRegistry() {
		return configRegistry;
	}
	
	public Character getCharacter() {
		return character;
	}
	
	public Composite getParent() {
		return parent;
	}
	
	public boolean isUseAdjustOnMultiCellEdit() {
	    return useAdjustOnMultiCellEdit;
	}
	
}
