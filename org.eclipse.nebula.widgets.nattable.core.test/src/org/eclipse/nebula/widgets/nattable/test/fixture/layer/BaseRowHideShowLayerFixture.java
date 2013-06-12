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

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.hideshow.RowHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;


/**
 * This is a vanilla RowHideShowLayer to be used for testing Events.
 */
public class BaseRowHideShowLayerFixture extends RowHideShowLayer {

	public BaseRowHideShowLayerFixture(IUniqueIndexLayer underlyingLayer) {
		super(underlyingLayer);
	}
	
	@Override
	public void hideRowPositions(Collection<Integer> rowPositions) {
		super.hideRowPositions(rowPositions);
	}
	
	@Override
	public void showRowIndexes(Collection<Integer> rowIndexes) {
		super.showRowIndexes(rowIndexes);
	}

	@Override
	public void showAllRows() {
		super.showAllRows();
	}
}
