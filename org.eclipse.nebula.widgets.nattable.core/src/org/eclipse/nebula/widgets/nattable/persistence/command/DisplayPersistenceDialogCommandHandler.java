/*******************************************************************************
 * Copyright (c) 2012 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.nebula.widgets.nattable.persistence.command;

import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.persistence.gui.PersistenceDialog;

/**
 * Command handler implementation for handling {@link DisplayPersistenceDialogCommand}s.
 * It is used to open the corresponding dialog for save/load operations regarding the 
 * NatTable state. Will also serve as some kind of storage for the Properties instance
 * holding the states.
 * 
 * @author Dirk Fauth
 *
 */
public class DisplayPersistenceDialogCommandHandler extends AbstractLayerCommandHandler<DisplayPersistenceDialogCommand> {

	/**
	 * The Properties instance that should be used for saving and loading.
	 */
	private Properties properties;

	/**
	 * Create a new DisplayPersistenceDialogCommandHandler. Using this constructor
	 * the Properties instance used for save and load operations will be created.
	 * It can be accessed via getProperties() for further usage.
	 */
	public DisplayPersistenceDialogCommandHandler() {
		this(new Properties(), null);
	}

	/**
	 * Create a new DisplayPersistenceDialogCommandHandler. Using this constructor
	 * the Properties instance used for save and load operations will be created.
	 * It can be accessed via getProperties() for further usage. The current state
	 * of the given NatTable instance will be used to store a default configuration.
	 * @param natTable The NatTable instance for which this handler is registered. If it is 
	 * 			not <code>null</code>, the current state of that NatTable will be stored as
	 * 			default configuration. This default configuration can't be modified anymore
	 * 			in the opened dialog.
	 */
	public DisplayPersistenceDialogCommandHandler(NatTable natTable) {
		this(new Properties(), natTable);
	}

	/**
	 * Create a new DisplayPersistenceDialogCommandHandler using the specified Properties
	 * instance.
	 * @param properties The Properties instance that should be used for saving and loading.
	 */
	public DisplayPersistenceDialogCommandHandler(Properties properties) {
		this(properties, null);
	}

	/**
	 * Create a new DisplayPersistenceDialogCommandHandler using the specified Properties
	 * instance. The current state of the given NatTable instance will be used to store a 
	 * default configuration.
	 * @param properties The Properties instance that should be used for saving and loading.
	 * @param natTable The NatTable instance for which this handler is registered. If it is 
	 * 			not <code>null</code>, the current state of that NatTable will be stored as
	 * 			default configuration. This default configuration can't be modified anymore
	 * 			in the opened dialog.
	 */
	public DisplayPersistenceDialogCommandHandler(Properties properties, NatTable natTable) {
		if (properties == null) {
			throw new IllegalArgumentException("properties can not be null!"); //$NON-NLS-1$
		}
		this.properties = properties;
		
		if (natTable != null) {
			natTable.saveState("", this.properties); //$NON-NLS-1$
			this.properties.setProperty(PersistenceDialog.ACTIVE_VIEW_CONFIGURATION_KEY, ""); //$NON-NLS-1$
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler#doCommand(org.eclipse.nebula.widgets.nattable.command.ILayerCommand)
	 */
	@Override
	protected boolean doCommand(DisplayPersistenceDialogCommand command) {
		PersistenceDialog dialog = new PersistenceDialog(
				command.getNatTable().getShell(), command.getNatTable(), this.properties);
		dialog.open();
		return true;
	}
	
	/**
	 * @return The Properties instance that is used for saving and loading.
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @param properties The Properties instance that should be used for saving and loading.
	 */
	public void setProperties(Properties properties) {
		if (properties == null) {
			throw new IllegalArgumentException("properties can not be null!"); //$NON-NLS-1$
		}
		this.properties = properties;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler#getCommandClass()
	 */
	public Class<DisplayPersistenceDialogCommand> getCommandClass() {
		return DisplayPersistenceDialogCommand.class;
	}

}
