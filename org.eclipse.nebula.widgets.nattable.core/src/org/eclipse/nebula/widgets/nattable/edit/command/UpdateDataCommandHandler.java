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
package org.eclipse.nebula.widgets.nattable.edit.command;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;

/**
 * {@link ILayerCommandHandler} that handles {@link UpdateDataCommand}s by updating
 * the data model. It is usually directly registered to the {@link DataLayer} this
 * command handler is associated with.
 */
public class UpdateDataCommandHandler extends AbstractLayerCommandHandler<UpdateDataCommand> {

	private static final Log log = LogFactory.getLog(UpdateDataCommandHandler.class);

	/**
	 * The {@link DataLayer} on which the data model updates should be executed.
	 */
	private final DataLayer dataLayer;
	
	/**
	 * @param dataLayer The {@link DataLayer} on which the data model updates should be executed.
	 */
	public UpdateDataCommandHandler(DataLayer dataLayer) {
		this.dataLayer = dataLayer;
	}
	
	@Override
	public Class<UpdateDataCommand> getCommandClass() {
		return UpdateDataCommand.class;
	}

	@Override
	protected boolean doCommand(UpdateDataCommand command) {
		try {
			int columnPosition = command.getColumnPosition();
			int rowPosition = command.getRowPosition();
			if (!ObjectUtils.equals(
					dataLayer.getDataValue(columnPosition, rowPosition), command.getNewValue())) {
				dataLayer.setDataValue(columnPosition, rowPosition, command.getNewValue());
				dataLayer.fireLayerEvent(new CellVisualChangeEvent(dataLayer, columnPosition, rowPosition));
				
				//TODO implement a new event which is a mix of PropertyUpdateEvent and CellVisualChangeEvent
			}
			return true;
		} catch(Exception e) {
			log.error("Failed to update value to: "+command.getNewValue(), e); //$NON-NLS-1$
			return false;
		}
	}
}
