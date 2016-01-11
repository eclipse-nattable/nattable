/*******************************************************************************
 * Copyright (c) 2014 Frank Mosebach.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Mosebach <mosebach@patronas.de> - regression test for bug 447942
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.viewport;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public final class ViewportLayerDisposalTest {

    private Shell shell;

    @Before
    public void init() {
        this.shell = new Shell(Display.getDefault());
    }

    @After
    public void dispose() {
        this.shell.dispose();
    }

    /**
     * Tests that an {@link IStructuralChangeEvent} will be safely handled by a
     * table's {@link ViewportLayer} after the table has been disposed (see: bug
     * <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=447942">Bug
     * 447942</a>).
     *
     * @throws InterruptedException
     *             on multi-threading issues
     */
    @Test
    public void testPostEventToDisposedLayer() throws InterruptedException {
        final DataLayer dataLayer = new TestDataLayer();
        final ViewportLayer viewportLayer = new ViewportLayer(dataLayer);
        final NatTable table = new NatTable(this.shell, viewportLayer);

        // Setting the table's size will cause the viewport layer to connect
        // itself to the table's scrollbars.
        table.setSize(500, 500);

        // Change a value in the data layer on a background thread.
        final Thread backgroundUpdater = new Thread(new Runnable() {
            @Override
            public void run() {
                // Calling "setDataValue" on a background thread will cause the
                // actual update to be executed asnchronously on the ui thread.
                dataLayer.setDataValue(4, 49, "VALUE");
            }
        }, "LayerDisposalTest.backgroundUpdater");

        backgroundUpdater.start();
        backgroundUpdater.join();

        // Disposing the table will also dispose all of its layers.
        table.dispose();

        // Process all pending ui tasks. This is supposed to disptach an event
        // to the (disposed) viewport layer.
        while (this.shell.getDisplay().readAndDispatch()) {}

        // Test that the background thread has successfully updated the table's
        // data layer.
        Assert.assertEquals("The table's data layer should have been updated.", "VALUE", dataLayer.getDataValue(4, 49));
    }

    private static final class TestDataLayer extends DataLayer {

        private static IDataProvider DATA_PROVIDER = new IDataProvider() {

            private final String[][] data = new String[100][10];

            @Override
            public Object getDataValue(final int columnIndex, final int rowIndex) {
                return this.data[rowIndex][columnIndex];
            }

            @Override
            public void setDataValue(final int columnIndex, final int rowIndex, final Object newValue) {
                this.data[rowIndex][columnIndex] = (newValue == null) ? null : newValue.toString();
            }

            @Override
            public int getRowCount() {
                return 100;
            }

            @Override
            public int getColumnCount() {
                return 10;
            }
        };

        private TestDataLayer() {
            super(DATA_PROVIDER);
        }

        @Override
        public void setDataValue(final int columnIndex, final int rowIndex, final Object newValue) {
            if (Display.getCurrent() == null) {
                // We are on a background thread: Schedule an update task to be
                // executed on the ui thread.

                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        setDataValue(columnIndex, rowIndex, newValue);
                    }
                });
            } else {
                // We are on the ui thread: Update the respective data value and
                // fire a refresh event.

                super.setDataValue(columnIndex, rowIndex, newValue);

                fireLayerEvent(new StructuralRefreshEvent(this));
            }
        }
    }

}
