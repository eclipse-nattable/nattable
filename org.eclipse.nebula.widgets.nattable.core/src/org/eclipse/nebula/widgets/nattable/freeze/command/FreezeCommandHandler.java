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
package org.eclipse.nebula.widgets.nattable.freeze.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.event.FreezeEvent;
import org.eclipse.nebula.widgets.nattable.freeze.event.UnfreezeEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class FreezeCommandHandler extends AbstractLayerCommandHandler<IFreezeCommand> {

	private final FreezeLayer freezeLayer;
	
	private final ViewportLayer viewportLayer;
	
	private final SelectionLayer selectionLayer;

	public FreezeCommandHandler(FreezeLayer freezeLayer, ViewportLayer viewportLayer, SelectionLayer selectionLayer) {
		this.freezeLayer = freezeLayer;
		this.viewportLayer = viewportLayer;
		this.selectionLayer = selectionLayer;
	}
	
	public Class<IFreezeCommand> getCommandClass() {
		return IFreezeCommand.class;
	}
	
	public boolean doCommand(IFreezeCommand command) {
		
		if (command instanceof FreezeColumnCommand) {
			FreezeColumnCommand freezeColumnCommand = (FreezeColumnCommand)command;
			IFreezeCoordinatesProvider coordinatesProvider = new FreezeColumnStrategy(freezeLayer, freezeColumnCommand.getColumnPosition());
			handleFreezeCommand(coordinatesProvider, freezeColumnCommand.isToggle());
			return true;
		} else if (command instanceof FreezePositionCommand) {
			FreezePositionCommand freezePositionCommand = (FreezePositionCommand) command;
			IFreezeCoordinatesProvider coordinatesProvider = 
				new FreezePositionStrategy(freezeLayer, freezePositionCommand.getColumnPosition(), freezePositionCommand.getRowPosition());
			handleFreezeCommand(coordinatesProvider, freezePositionCommand.isToggle());
			return true;
		} else if (command instanceof FreezeSelectionCommand) {
			IFreezeCoordinatesProvider coordinatesProvider = new FreezeSelectionStrategy(freezeLayer, viewportLayer, selectionLayer);
			handleFreezeCommand(coordinatesProvider, ((FreezeSelectionCommand) command).isToggle());
			return true;
		} else if (command instanceof UnFreezeGridCommand) {
			handleUnfreeze();
			return true;
		}
		
		return false;
	}

	protected void handleFreezeCommand(IFreezeCoordinatesProvider coordinatesProvider, boolean toggle) {
		if (!freezeLayer.isFrozen()) {  // if not already frozen
			final PositionCoordinate topLeftPosition = coordinatesProvider.getTopLeftPosition();
			final PositionCoordinate bottomRightPosition = coordinatesProvider.getBottomRightPosition();
	
			if (topLeftPosition != null && bottomRightPosition != null) {
				freezeLayer.setTopLeftPosition(topLeftPosition.columnPosition, topLeftPosition.rowPosition);
				freezeLayer.setBottomRightPosition(bottomRightPosition.columnPosition, bottomRightPosition.rowPosition);
		
				viewportLayer.setMinimumOriginPosition(bottomRightPosition.columnPosition + 1, bottomRightPosition.rowPosition + 1);
				
				viewportLayer.fireLayerEvent(new FreezeEvent(viewportLayer));
			}
		} else if (toggle) {  // if frozen and toggle = true
			handleUnfreeze();
		}
	}
	
	protected void handleUnfreeze() {
		resetFrozenArea();
		viewportLayer.fireLayerEvent(new UnfreezeEvent(viewportLayer));
	}

	private void resetFrozenArea() {
		PositionCoordinate topLeftPosition = freezeLayer.getTopLeftPosition();
		
		freezeLayer.setTopLeftPosition(-1, -1);
		freezeLayer.setBottomRightPosition(-1, -1);
		
		viewportLayer.resetOrigin(Math.max(0, topLeftPosition.columnPosition), Math.max(0,topLeftPosition.rowPosition));
	}
	
}
