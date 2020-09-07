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
package org.eclipse.nebula.widgets.nattable.examples.examples._100_Layers._900_test.viewportSelection;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.grid.data.DummySpanningBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.SpanningDataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ViewportSelectionSpanningDataLayerExample extends
        AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner
                .run(new ViewportSelectionSpanningDataLayerExample());
    }

    @Override
    public Control createExampleControl(Composite parent) {
        return new NatTable(parent, new ViewportLayer(new SelectionLayer(
                new SpanningDataLayer(new DummySpanningBodyDataProvider(
                        1000000, 1000000)))));
    }

}
