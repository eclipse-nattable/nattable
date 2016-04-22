/*******************************************************************************
 * Copyright (c) 2012, 2016 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.test.performance;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.junit.BeforeClass;
import org.junit.Test;

public class BodyStackLayerPerformanceTest extends AbstractLayerPerformanceTest {

    static IDataProvider provider;

    @BeforeClass
    public static void init() {
        provider = new DummyBodyDataProvider(100000, 100000);
    }

    @Test
    public void testDataLayerPerformance() {
        this.layer = new DataLayer(provider);
    }

    @Test
    public void testReorderDataLayerPerformance() {
        this.layer = new ColumnReorderLayer(new DataLayer(provider));
    }

    @Test
    public void testHideShowReorderDataLayerPerformance() {
        this.layer = new ColumnHideShowLayer(
                new ColumnReorderLayer(new DataLayer(provider)));
    }

    @Test
    public void testSelectionHideShowReorderLayerPerformance() {
        this.layer = new SelectionLayer(
                new ColumnHideShowLayer(
                        new ColumnReorderLayer(new DataLayer(provider))));
    }

    @Test
    public void testViewportHideShowReorderDataLayerPerformance() {
        this.layer = new ViewportLayer(
                new ColumnHideShowLayer(
                        new ColumnReorderLayer(new DataLayer(provider))));
    }

    @Test
    public void testViewportSelectionHideShowReorderDataLayerPerformance() {
        this.layer = new ViewportLayer(
                new SelectionLayer(
                        new ColumnHideShowLayer(
                                new ColumnReorderLayer(new DataLayer(provider)))));
    }

    @Test
    public void testDefaultBodyLayerStackPerformance() {
        this.layer = new DefaultBodyLayerStack(new DataLayer(provider));
    }

}
