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
import org.eclipse.nebula.widgets.nattable.resize.command.AutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;

/**
 * This command is triggered by the {@link InitializeAutoResizeColumnsCommand}.
 * The selected columns picked from the {@link SelectionLayer} by the above command.
 * This handler runs as a second step. This <i>must</i> run at the {@link GridLayer} level
 * since we need to pick up all the region labels which are applied at the grid level.
 * 
 * Additionally running at the grid layer level ensures that we include cells from the
 * headers in the width calculations.
 */
public class AutoResizeColumnCommandHandler implements ILayerCommandHandler<AutoResizeColumnsCommand> {

	private final GridLayer gridLayer;

	public AutoResizeColumnCommandHandler(GridLayer gridLayer) {
		this.gridLayer = gridLayer;
	}

	public Class<AutoResizeColumnsCommand> getCommandClass() {
		return AutoResizeColumnsCommand.class;
	}

	public boolean doCommand(ILayer targetLayer, AutoResizeColumnsCommand command) {
		// Need to resize selected columns even if they are outside the viewport
		targetLayer.doCommand(new TurnViewportOffCommand());

		int[] columnPositions = ObjectUtils.asIntArray(command.getColumnPositions());
		int[] gridColumnPositions = convertFromSelectionToGrid(columnPositions);

		int[] gridColumnWidths = MaxCellBoundsHelper.getPreferedColumnWidths(
                                                         command.getConfigRegistry(), 
                                                         command.getGCFactory(), 
                                                         gridLayer,
                                                         gridColumnPositions);

		gridLayer.doCommand(new MultiColumnResizeCommand(gridLayer, gridColumnPositions, gridColumnWidths));
		targetLayer.doCommand(new TurnViewportOnCommand());

		return true;
	}

	private int[] convertFromSelectionToGrid(int[] columnPositions) {
		int[] gridColumnPositions = new int[columnPositions.length];

		for (int i = 0; i < columnPositions.length; i++) {
			// Since the viewport is turned off - body layer can be used as the underlying layer
			gridColumnPositions[i] = gridLayer.underlyingToLocalColumnPosition(gridLayer.getBodyLayer(), columnPositions[i]);
		}
		return gridColumnPositions;
	}

}
