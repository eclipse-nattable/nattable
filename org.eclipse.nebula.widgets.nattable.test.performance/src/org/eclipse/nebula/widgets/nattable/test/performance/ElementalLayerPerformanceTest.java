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
package org.eclipse.nebula.widgets.nattable.test.performance;

import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.junit.Test;

public class ElementalLayerPerformanceTest extends AbstractLayerPerformanceTest {

    @Test
    public void testNormalDataLayerPerformance() {
        this.layer = new DataLayer(new DummyBodyDataProvider(10, 50));
    }

    @Test
    public void testBigDataLayerPerformance() {
        this.layer = new DataLayer(new DummyBodyDataProvider(50, 100));
        setExpectedTimeInMillis(250);
    }

    @Test
    public void testReorderDataLayerPerformance() {
        this.layer = new ColumnReorderLayer(new DataLayer(new DummyBodyDataProvider(
                10, 50)));
    }

    @Test
    public void testHideShowDataLayerPerformance() {
        this.layer = new ColumnHideShowLayer(new DataLayer(
                new DummyBodyDataProvider(10, 50)));
    }

    @Test
    public void testSelectionDataLayerPerformance() {
        this.layer = new SelectionLayer(new DataLayer(new DummyBodyDataProvider(10,
                50)));
    }

    @Test
    public void testCompositeDataLayerPerformance() {
        CompositeLayer compositeLayer = new CompositeLayer(1, 1);
        compositeLayer.setChildLayer(GridRegion.BODY, new DataLayer(
                new DummyBodyDataProvider(10, 50)), 0, 0);

        this.layer = compositeLayer;
    }

}
