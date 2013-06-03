/*******************************************************************************
 * Copyright (c) 2013 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;


/**
 * A RowHideShowLayer for use in unit tests with a pre-canned set of hidden
 * rows. Row indexes by positions: 4 1 2 5 6
 */
public class RowHideShowLayerFixture extends RowHideShowLayer {

	private ILayerCommand lastCommand;

	@SuppressWarnings("boxing")
	public RowHideShowLayerFixture() {
		// Row reorder fixture index positions: 4 1 0 2 3 5 6
		super(new RowReorderLayerFixture());

		List<Integer> rowPositions = Arrays.asList(2, 4);
		hideRowPositions(rowPositions);
	}

	public RowHideShowLayerFixture(IUniqueIndexLayer underlyingLayerFixture) {
		super(underlyingLayerFixture);
	}

	public RowHideShowLayerFixture(int...rowPositionsToHide) {
		super(new DataLayerFixture(10, 10, 20, 5));

		Collection<Integer> rowPositions = new HashSet<Integer>();
		for (int rowPosition : rowPositionsToHide) {
			rowPositions.add(Integer.valueOf(rowPosition));
		}

		hideRowPositions(rowPositions);
	}

	@Override
	public boolean doCommand(ILayerCommand command) {
		lastCommand = command;
		return super.doCommand(command);
	}

	public ILayerCommand getLastCommand() {
		return lastCommand;
	}

}
