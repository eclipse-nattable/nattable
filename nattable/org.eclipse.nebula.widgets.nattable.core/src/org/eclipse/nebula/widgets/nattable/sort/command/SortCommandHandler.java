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
package org.eclipse.nebula.widgets.nattable.sort.command;


import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.event.SortColumnEvent;
import org.eclipse.swt.custom.BusyIndicator;

/**
 * Handle sort commands
 */
public class SortCommandHandler<T> extends AbstractLayerCommandHandler<SortColumnCommand> {

	private final ISortModel sortModel;
	private final SortHeaderLayer<T> sortHeaderLayer;

	public SortCommandHandler(ISortModel sortModel, SortHeaderLayer<T> sortHeaderLayer) {
		this.sortModel = sortModel;
		this.sortHeaderLayer = sortHeaderLayer;
	}

	@Override
	public boolean doCommand(final SortColumnCommand command) {

		final int columnIndex = command.getLayer().getColumnIndexByPosition(command.getColumnPosition());
		final SortDirectionEnum newSortDirection = sortModel.getSortDirection(columnIndex).getNextSortDirection();

		// Fire command - with busy indicator
		Runnable sortRunner = new Runnable() {
			public void run() {
				sortModel.sort(columnIndex, newSortDirection, command.isAccumulate());
			}
		};
		BusyIndicator.showWhile(null, sortRunner);

		// Fire event
		SortColumnEvent sortEvent = new SortColumnEvent(sortHeaderLayer, command.getColumnPosition());
		sortHeaderLayer.fireLayerEvent(sortEvent);

		return true;
	}

	public Class<SortColumnCommand> getCommandClass() {
		return SortColumnCommand.class;
	}

}
