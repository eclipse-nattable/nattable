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
package org.eclipse.nebula.widgets.nattable.grid.command;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOffCommand;
import org.eclipse.nebula.widgets.nattable.print.command.TurnViewportOnCommand;
import org.eclipse.nebula.widgets.nattable.resize.MaxCellBoundsHelper;
import org.eclipse.nebula.widgets.nattable.resize.command.AutoResizeRowsCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiRowResizeCommand;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;

/**
 * @see AutoResizeColumnCommandHandler
 */
public class AutoResizeRowCommandHandler implements ILayerCommandHandler<AutoResizeRowsCommand> {

	private final GridLayer gridLayer;

	public AutoResizeRowCommandHandler(GridLayer gridLayer) {
		this.gridLayer = gridLayer;
	}

	public Class<AutoResizeRowsCommand> getCommandClass() {
		return AutoResizeRowsCommand.class;
	}

	public boolean doCommand(ILayer targetLayer, AutoResizeRowsCommand command) {
		// Need to resize selected rows even if they are outside the viewport
		targetLayer.doCommand(new TurnViewportOffCommand());

		int[] rowPositions = ObjectUtils.asIntArray(command.getRowPositions());
		int[] gridRowPositions = convertFromSelectionToGrid(rowPositions);
		
		int[] gridRowHeights = MaxCellBoundsHelper.getPreferedRowHeights(
                                                    command.getConfigRegistry(), 
                                                    command.getGCFactory(), 
                                                    gridLayer,
                                                    gridRowPositions);

		gridLayer.doCommand(new MultiRowResizeCommand(gridLayer, gridRowPositions, gridRowHeights));
		
		targetLayer.doCommand(new TurnViewportOnCommand());

		return true;
	}

	private int[] convertFromSelectionToGrid(int[] rowPositions) {
		int[] gridRowPositions = new int[rowPositions.length];

		for (int i = 0; i < rowPositions.length; i++) {
			// Since the viewport is turned off - body layer can be used as the underlying layer
			gridRowPositions[i] = gridLayer.underlyingToLocalRowPosition(gridLayer.getBodyLayer(), rowPositions[i]);
		}
		return gridRowPositions;
	}
}
