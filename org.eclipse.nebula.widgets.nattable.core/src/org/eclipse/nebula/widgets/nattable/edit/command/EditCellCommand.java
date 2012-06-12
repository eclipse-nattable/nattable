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
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.swt.widgets.Composite;

public class EditCellCommand extends AbstractContextFreeCommand {
	
	private final IConfigRegistry configRegistry;
	private final Composite parent;
	private ILayerCell cell;

	public EditCellCommand(Composite parent, IConfigRegistry configRegistry, ILayerCell cell) {
		this.configRegistry = configRegistry;
		this.parent = parent;
		this.cell = cell;
	}

	public IConfigRegistry getConfigRegistry() {
		return configRegistry;
	}
	
	public Composite getParent() {
		return parent;
	}

	public ILayerCell getCell() {
		return cell;
	}
	
}
