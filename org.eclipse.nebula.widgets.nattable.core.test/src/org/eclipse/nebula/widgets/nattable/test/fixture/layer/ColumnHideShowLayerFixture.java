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
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;


/**
 * A ColumnHideShowLayer for use in unit tests with a pre-canned set of hidden
 * columns. Column indexes by positions: 4 1 2
 */
public class ColumnHideShowLayerFixture extends ColumnHideShowLayer {

	private ILayerCommand lastCommand;

	@SuppressWarnings("boxing")
	public ColumnHideShowLayerFixture() {
		// Column reorder fixture index positions: 4 1 0 2 3
		super(new ColumnReorderLayerFixture());

		List<Integer> columnPositions = Arrays.asList(2, 4);
		hideColumnPositions(columnPositions);
	}

	public ColumnHideShowLayerFixture(IUniqueIndexLayer underlyingLayerFixture) {
		super(underlyingLayerFixture);
	}

	public ColumnHideShowLayerFixture(int...columnPositionsToHide) {
		super(new DataLayerFixture(10, 10, 20, 5));

		Collection<Integer> columnPositions = new HashSet<Integer>();
		for (int columnPosition : columnPositionsToHide) {
			columnPositions.add(Integer.valueOf(columnPosition));
		}

		hideColumnPositions(columnPositions);
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
