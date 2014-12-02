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
package org.eclipse.nebula.widgets.nattable.layer.stack;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyBodyDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;

public class DummyGridLayerStack extends DefaultGridLayer {

    public DummyGridLayerStack() {
        this(20, 20);
    }

    public DummyGridLayerStack(int columnCount, int rowCount) {
        this(new DummyBodyDataProvider(columnCount, rowCount));
    }

    public DummyGridLayerStack(IDataProvider bodyDataProvider) {
        super(true);
        IDataProvider columnHeaderDataProvider = new DummyColumnHeaderDataProvider(
                bodyDataProvider);
        IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(
                bodyDataProvider);
        IDataProvider cornerDataProvider = new DefaultCornerDataProvider(
                columnHeaderDataProvider, rowHeaderDataProvider);

        init(bodyDataProvider, columnHeaderDataProvider, rowHeaderDataProvider,
                cornerDataProvider);
    }
}