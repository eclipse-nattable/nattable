/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.resize.command;

import static org.eclipse.nebula.widgets.nattable.coordinate.Orientation.HORIZONTAL;

import org.eclipse.nebula.widgets.nattable.command.AbstractDimPositionsCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;


/**
 * Command indicating that all selected columns have to be auto resized i.e made
 * wide enough to just fit the widest cell. This should also take the column
 * header into account
 * 
 * Note: The {@link InitializeAutoResizeColumnsCommand} has to be fired first
 * when autoresizing columns.
 */
public class AutoResizeColumnsCommand extends AbstractDimPositionsCommand {
	
	private final IConfigRegistry configRegistry;
	private final GCFactory gcFactory;
	
	
	public AutoResizeColumnsCommand(InitializeAutoResizeColumnsCommand initCommand) {
		super(initCommand.getSourceLayer().getDim(HORIZONTAL), initCommand.getColumnPositions());
		this.configRegistry = initCommand.getConfigRegistry();
		this.gcFactory = initCommand.getGCFactory();
	}
	
	protected AutoResizeColumnsCommand(AutoResizeColumnsCommand command) {
		super(command);
		this.configRegistry = command.configRegistry;
		this.gcFactory = command.gcFactory;
	}
	
	@Override
	public ILayerCommand cloneCommand() {
		return new AutoResizeColumnsCommand(this);
	}

	// Accessors

	public GCFactory getGCFactory() {
		return gcFactory;
	}

	public IConfigRegistry getConfigRegistry() {
		return configRegistry;
	}
	
}
