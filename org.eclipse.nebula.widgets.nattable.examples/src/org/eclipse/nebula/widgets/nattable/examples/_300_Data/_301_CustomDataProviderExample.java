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
package org.eclipse.nebula.widgets.nattable.examples._300_Data;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.examples.AbstractNatExample;
import org.eclipse.nebula.widgets.nattable.examples.runner.StandaloneNatExampleRunner;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Example showing how to implement and use a custom IDataProvider.
 *
 * @author Dirk Fauth
 *
 */
public class _301_CustomDataProviderExample extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(600, 400,
                new _301_CustomDataProviderExample());
    }

    @Override
    public String getDescription() {
        return "This is an example to show how to implement a custom IDataProvider that"
                + " operates on a two-dimensional array of Strings.";
    }

    @Override
    public Control createExampleControl(Composite parent) {

        String[][] testData = new String[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                testData[i][j] = "" + i + "/" + j;
            }
        }

        final DataLayer bodyDataLayer = new DataLayer(
                new TwoDimensionalArrayDataProvider(testData));
        bodyDataLayer.setDefaultColumnWidth(30);

        // use different style bits to avoid rendering of inactive scrollbars
        // for small table
        // Note: The enabling/disabling and showing of the scrollbars is handled
        // by the ViewportLayer.
        // Without the ViewportLayer the scrollbars will always be visible with
        // the default
        // style bits of NatTable.
        final NatTable natTable = new NatTable(parent, SWT.NO_BACKGROUND
                | SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED, bodyDataLayer);

        return natTable;
    }

    /**
     * Sample IDataProvider that operates on a two-dimensional array of Strings,
     * where the first dimension are the columns and the second dimension the
     * rows.
     *
     * @author Dirk Fauth
     *
     */
    class TwoDimensionalArrayDataProvider implements IDataProvider {

        private String[][] data;

        public TwoDimensionalArrayDataProvider(String[][] data) {
            this.data = data;
        }

        @Override
        public Object getDataValue(int columnIndex, int rowIndex) {
            return this.data[columnIndex][rowIndex];
        }

        @Override
        public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
            this.data[columnIndex][rowIndex] = newValue != null ? newValue
                    .toString() : null;
        }

        @Override
        public int getColumnCount() {
            return this.data.length;
        }

        @Override
        public int getRowCount() {
            return this.data[0] != null ? this.data[0].length : 0;
        }

    }
}
