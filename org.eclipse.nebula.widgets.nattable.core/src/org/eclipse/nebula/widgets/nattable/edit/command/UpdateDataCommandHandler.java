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
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;

public class UpdateDataCommandHandler extends AbstractLayerCommandHandler<UpdateDataCommand> {

	private final DataLayer dataLayer;

	public UpdateDataCommandHandler(DataLayer dataLayer) {
		this.dataLayer = dataLayer;
	}
	
	public Class<UpdateDataCommand> getCommandClass() {
		return UpdateDataCommand.class;
	}

	@Override
	protected boolean doCommand(UpdateDataCommand command) {
		try {
			int columnPosition = command.getColumnPosition();
			int rowPosition = command.getRowPosition();
			dataLayer.getDataProvider().setDataValue(columnPosition, rowPosition, command.getNewValue());
			dataLayer.fireLayerEvent(new CellVisualChangeEvent(dataLayer, columnPosition, rowPosition));
			return true;
		} catch (UnsupportedOperationException e) {
			e.printStackTrace(System.err);
			System.err.println("Failed to update value to: "+command.getNewValue()); //$NON-NLS-1$
			return false;
		}
	}

}
