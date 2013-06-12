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
 * This fixture provides a base vanilla RowReorderLayer.  In order to keep a test's scope narrow, this class exposes the  
 * reorderRowPositions method, this way we can reorder during testing without having to use commands.
 */
public class BaseRowReorderLayerFixture extends RowReorderLayer {

	public BaseRowReorderLayerFixture(IUniqueIndexLayer underlyingLayer) {
		super(underlyingLayer);
	}
	
	@Override
	public void reorderRowPosition(int fromRowPosition, int toRowPosition) {
		super.reorderRowPosition(fromRowPosition, toRowPosition);
	}
}
