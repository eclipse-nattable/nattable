/*****************************************************************************
 * Copyright (c) 2015, 2020 CEA LIST.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.examples.e4.part;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.e4.AbstractE4NatExamplePart;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DummyColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class PercentageExample extends AbstractE4NatExamplePart {

    @PostConstruct
    public void postConstruct(Composite parent) {
        // Setup the layer stack
        final MyDataProvider myDataProvider = new MyDataProvider();
        SelectionLayer selectionLayer = new SelectionLayer(new DataLayer(myDataProvider));
        ILayer columnHeaderLayer = new ColumnHeaderLayer(
                new DataLayer(
                        new DummyColumnHeaderDataProvider(myDataProvider)),
                selectionLayer,
                selectionLayer);

        CompositeLayer compositeLayer = new CompositeLayer(1, 2);
        compositeLayer.setChildLayer(GridRegion.COLUMN_HEADER, columnHeaderLayer, 0, 0);
        compositeLayer.setChildLayer(GridRegion.BODY, selectionLayer, 0, 1);

        NatTable natTable = new NatTable(parent, compositeLayer);

        natTable.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, "percentage");

        parent.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        showSourceLinks(parent, getClass().getName());
    }

    /**
     * Provides decimal numbers < 1 as cell values.
     */
    class MyDataProvider implements IDataProvider {

        @Override
        public int getColumnCount() {
            return 10;
        }

        @Override
        public int getRowCount() {
            return 10;
        }

        @Override
        public Object getDataValue(int columnIndex, int rowIndex) {
            // Create a decimal value
            return Double.valueOf((rowIndex * 10 + columnIndex) / 100D);
        }

        @Override
        public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
            // Do nothing
        }
    }
}