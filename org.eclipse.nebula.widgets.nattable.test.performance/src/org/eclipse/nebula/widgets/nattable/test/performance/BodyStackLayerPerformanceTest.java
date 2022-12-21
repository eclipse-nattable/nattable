/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
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

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BodyStackLayerPerformanceTest extends AbstractLayerPerformanceTest {

    static IDataProvider provider;

    @BeforeAll
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
