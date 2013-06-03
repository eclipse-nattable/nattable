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
package org.eclipse.nebula.widgets.nattable.selection;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.command.MoveSelectionCommand;

/**
 * Abstraction of the selection behavior during navigation in the grid.
 * Implementations of this class specify what to select when the selection moves
 * by responding to the {@link MoveSelectionCommand}.
 *
 * @param <T> an instance of the {@link MoveSelectionCommand}
 * @see MoveCellSelectionCommandHandler
 * @see MoveRowSelectionCommandHandler
 */
public abstract class MoveSelectionCommandHandler<T extends MoveSelectionCommand> implements ILayerCommandHandler<T> {

	protected final SelectionLayer selectionLayer;

	public MoveSelectionCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}

	public boolean doCommand(ILayer targetLayer, T command) {
		if (command.convertToTargetLayer(selectionLayer)) {
			moveSelection(command.getDirection(), command.getStepSize(), command.isShiftMask(), command.isControlMask());
			return true;
		}
		return false;
	}

	protected void moveSelection(MoveDirectionEnum moveDirection, int stepSize, boolean withShiftMask, boolean withControlMask) {
		switch (moveDirection) {
		case UP:
			moveLastSelectedUp(stepSize, withShiftMask, withControlMask);
			break;
		case DOWN:
			moveLastSelectedDown(stepSize, withShiftMask, withControlMask);
			break;
		case LEFT:
			moveLastSelectedLeft(stepSize, withShiftMask, withControlMask);
			break;
		case RIGHT:
			moveLastSelectedRight(stepSize, withShiftMask, withControlMask);
			break;
		default:
			break;
		}
	}

	protected abstract void moveLastSelectedRight(int stepSize, boolean withShiftMask, boolean withControlMask);
	protected abstract void moveLastSelectedLeft(int stepSize, boolean withShiftMask, boolean withControlMask);
	protected abstract void moveLastSelectedUp(int stepSize, boolean withShiftMask, boolean withControlMask);
	protected abstract void moveLastSelectedDown(int stepSize, boolean withShiftMask, boolean withControlMask);

}
