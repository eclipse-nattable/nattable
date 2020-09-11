/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.fixture.layer;

import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

public class ColumnHeaderLayerFixture extends ColumnHeaderLayer {

    // Viewport is 400px wide
    public static final DataLayer DATA_LAYER = new DataLayerFixture();
    public static final SelectionLayer SELECTION_LAYER = new SelectionLayer(DATA_LAYER);
    public static final ViewportLayer BODY_LAYER = new ViewportLayerFixture(SELECTION_LAYER);

    public ColumnHeaderLayerFixture() {
        super(DATA_LAYER, BODY_LAYER, SELECTION_LAYER);
    }

    public ColumnHeaderLayerFixture(ViewportLayer viewportLayer) {
        super(DATA_LAYER, viewportLayer, SELECTION_LAYER);
    }

    public static DataLayer getDataLayer() {
        return DATA_LAYER;
    }
}
