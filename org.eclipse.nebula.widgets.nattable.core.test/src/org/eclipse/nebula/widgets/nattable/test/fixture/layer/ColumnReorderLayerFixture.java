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

import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;

/**
 * A ColumnReorderLayer for use in unit tests with a pre-canned set of column re-orderings:
 * column indexes: 4 1 0 2 3
 */
public class ColumnReorderLayerFixture extends ColumnReorderLayer {

	public ColumnReorderLayerFixture() {
		super(new DataLayerFixture());  // 0 1 2 3 4
		reorderColumns();
	}

	public ColumnReorderLayerFixture(IUniqueIndexLayer underlyingLayer) {
		super(underlyingLayer);  // 0 1 2 3 4
		reorderColumns();
	}

	private void reorderColumns() {
		reorderColumnPosition(4, 0);  // 4 0 1 2 3
		reorderColumnPosition(1, 4);  // 4 1 2 0 3
		reorderColumnPosition(3, 2);  // 4 1 0 2 3
	}

}
