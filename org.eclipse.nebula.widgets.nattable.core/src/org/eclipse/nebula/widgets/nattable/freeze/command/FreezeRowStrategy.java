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
package org.eclipse.nebula.widgets.nattable.freeze.command;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;

/**
 * @author Dirk Fauth
 *
 */
public class FreezeRowStrategy implements IFreezeCoordinatesProvider {

	private final FreezeLayer freezeLayer;
	
	private final int rowPosition;

	public FreezeRowStrategy(FreezeLayer freezeLayer, int rowPosition) {
		this.freezeLayer = freezeLayer;
		this.rowPosition = rowPosition;
	}

	@Override
	public PositionCoordinate getTopLeftPosition() {
		return new PositionCoordinate(freezeLayer, -1, 0);
	}

	@Override
	public PositionCoordinate getBottomRightPosition() {
		return new PositionCoordinate(freezeLayer, -1, rowPosition);
	}

}
