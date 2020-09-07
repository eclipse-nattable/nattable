/*******************************************************************************
 * Copyright (c) 2013, 2020 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;

import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;

/**
 * A RowReorderLayer for use in unit tests with a pre-canned set of row
 * re-orderings: row indexes: 4 1 0 2 3 5 6
 */
public class RowReorderLayerFixture extends RowReorderLayer {

    public RowReorderLayerFixture() {
        super(new DataLayerFixture()); // 0 1 2 3 4 5 6
        reorderRows();
    }

    public RowReorderLayerFixture(IUniqueIndexLayer underlyingLayer) {
        super(underlyingLayer); // 0 1 2 3 4 5 6
        reorderRows();
    }

    private void reorderRows() {
        reorderRowPosition(4, 0); // 4 0 1 2 3 5 6
        reorderRowPosition(1, 4); // 4 1 2 0 3 5 6
        reorderRowPosition(3, 2); // 4 1 0 2 3 5 6
    }

}
