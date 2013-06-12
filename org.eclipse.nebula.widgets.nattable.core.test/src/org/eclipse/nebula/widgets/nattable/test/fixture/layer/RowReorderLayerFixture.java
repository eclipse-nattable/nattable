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

import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;

/**
 * A RowReorderLayer for use in unit tests with a pre-canned set of row re-orderings:
 * row indexes: 4 1 0 2 3 5 6
 */
public class RowReorderLayerFixture extends RowReorderLayer {

	public RowReorderLayerFixture() {
		super(new DataLayerFixture());  // 0 1 2 3 4 5 6
		reorderRows();
	}

	public RowReorderLayerFixture(IUniqueIndexLayer underlyingLayer) {
		super(underlyingLayer);  // 0 1 2 3 4 5 6
		reorderRows();
	}

	private void reorderRows() {
		reorderRowPosition(4, 0);  // 4 0 1 2 3 5 6
		reorderRowPosition(1, 4);  // 4 1 2 0 3 5 6
		reorderRowPosition(3, 2);  // 4 1 0 2 3 5 6
	}

}
