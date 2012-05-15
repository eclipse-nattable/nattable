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

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;


/**
 * This is a vanilla ColumnHideShowLayer to be used for testing Events.
 */
public class BaseColumnHideShowLayerFixture extends ColumnHideShowLayer {

	public BaseColumnHideShowLayerFixture(IUniqueIndexLayer underlyingLayer) {
		super(underlyingLayer);
	}
	
	@Override
	public void hideColumnPositions(Collection<Integer> columnPositions) {
		super.hideColumnPositions(columnPositions);
	}
	
	@Override
	public void showColumnIndexes(Collection<Integer> columnIndexes) {
		super.showColumnIndexes(columnIndexes);
	}

	public void showAllColumns() {
		super.showAllColumns();
	}
}
