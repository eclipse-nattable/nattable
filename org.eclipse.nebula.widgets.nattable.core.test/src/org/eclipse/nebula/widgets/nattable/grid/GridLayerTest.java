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
package org.eclipse.nebula.widgets.nattable.grid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.test.fixture.InitializeClientAreaCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.command.LayerCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseDataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ViewportLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Before;
import org.junit.Test;

public class GridLayerTest {

    private GridLayer gridLayerUnderTest;

    private DataLayer bodyDataLayer;
    private DataLayer columnHeaderDataLayer;
    private DataLayer rowHeaderDataLayer;
    private DataLayer cornerDataLayer;

    @Before
    public void setup() {
        this.bodyDataLayer = new BaseDataLayerFixture();
        this.columnHeaderDataLayer = new BaseDataLayerFixture();
        this.rowHeaderDataLayer = new BaseDataLayerFixture();
        this.cornerDataLayer = new BaseDataLayerFixture();

        this.gridLayerUnderTest = new DefaultGridLayer(this.bodyDataLayer,
                this.columnHeaderDataLayer, this.rowHeaderDataLayer, this.cornerDataLayer);
    }

    @Test
    public void getLayers() throws Exception {
        assertNotNull(this.gridLayerUnderTest.getBodyLayer());
        assertNotNull(this.gridLayerUnderTest.getColumnHeaderLayer());
        assertNotNull(this.gridLayerUnderTest.getRowHeaderLayer());
        assertNotNull(this.gridLayerUnderTest.getCornerLayer());
    }

    @Test
    public void doCommandInvokesBodyFirst() throws Exception {
        DummyCommandHandler bodyCommandHandler = new DummyCommandHandler(true);
        DummyCommandHandler columnHeaderCommandHandler = new DummyCommandHandler(true);
        DummyCommandHandler rowHeaderCommandHandler = new DummyCommandHandler(true);
        DummyCommandHandler cornerCommandHandler = new DummyCommandHandler(true);

        this.bodyDataLayer.registerCommandHandler(bodyCommandHandler);
        this.columnHeaderDataLayer.registerCommandHandler(columnHeaderCommandHandler);
        this.rowHeaderDataLayer.registerCommandHandler(rowHeaderCommandHandler);
        this.cornerDataLayer.registerCommandHandler(cornerCommandHandler);

        final ILayerCommand command = new LayerCommandFixture();

        this.gridLayerUnderTest.doCommand(command);

        assertTrue(bodyCommandHandler.isCommandCaught());
        assertFalse(columnHeaderCommandHandler.isCommandCaught());
        assertFalse(rowHeaderCommandHandler.isCommandCaught());
        assertFalse(cornerCommandHandler.isCommandCaught());
    }

    @Test
    public void doCommandInvokesOtherLayers() throws Exception {
        DummyCommandHandler bodyCommandHandler = new DummyCommandHandler(false);
        DummyCommandHandler columnHeaderCommandHandler = new DummyCommandHandler(false);
        DummyCommandHandler rowHeaderCommandHandler = new DummyCommandHandler(false);
        DummyCommandHandler cornerCommandHandler = new DummyCommandHandler(true);

        this.bodyDataLayer.registerCommandHandler(bodyCommandHandler);
        this.columnHeaderDataLayer.registerCommandHandler(columnHeaderCommandHandler);
        this.rowHeaderDataLayer.registerCommandHandler(rowHeaderCommandHandler);
        this.cornerDataLayer.registerCommandHandler(cornerCommandHandler);

        final ILayerCommand command = new LayerCommandFixture();

        this.gridLayerUnderTest.doCommand(command);

        assertFalse(bodyCommandHandler.isCommandCaught());
        assertFalse(columnHeaderCommandHandler.isCommandCaught());
        assertFalse(rowHeaderCommandHandler.isCommandCaught());
        assertTrue(cornerCommandHandler.isCommandCaught());
    }

    // **** New tests using fixtures ****

    /**
     * @see ViewportLayerFixture#DEFAULT_CLIENT_AREA
     */
    @Test
    public void initBodyLayer() throws Exception {
        DefaultGridLayer gridLayer = new GridLayerFixture();

        ViewportLayer viewport = gridLayer.getBodyLayer().getViewportLayer();
        viewport.setClientAreaProvider(new IClientAreaProvider() {
            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 160, 80);
            }
        });

        // Client area gets init when this command is fired
        gridLayer.doCommand(new InitializeClientAreaCommandFixture());

        assertEquals(160, viewport.getClientAreaWidth());
        assertEquals(80, viewport.getClientAreaHeight());

        assertEquals(160, viewport.getWidth());
        assertEquals(80, viewport.getHeight());
    }

    @Test
    public void initRowHeaderHeight() throws Exception {
        GridLayer gridLayer = new GridLayerFixture();
        gridLayer.doCommand(new InitializeClientAreaCommandFixture());

        ILayer rowHeader = gridLayer.getRowHeaderLayer();
        // Only visible rows are counted
        assertEquals(100, rowHeader.getHeight());
        assertEquals(40, rowHeader.getWidth());
    }

    @Test
    public void initCorner() throws Exception {
        GridLayer gridLayer = new GridLayerFixture();

        ILayer colHeader = gridLayer.getCornerLayer();
        assertEquals(20, colHeader.getHeight());
        assertEquals(40, colHeader.getWidth());
    }

    class DummyCommandHandler extends AbstractLayerCommandHandler<LayerCommandFixture> {

        private final boolean catchCommand;

        private boolean caughtCommand;

        public DummyCommandHandler(boolean catchCommand) {
            this.catchCommand = catchCommand;
        }

        @Override
        public boolean doCommand(LayerCommandFixture command) {
            if (this.catchCommand) {
                this.caughtCommand = true;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Class<LayerCommandFixture> getCommandClass() {
            return LayerCommandFixture.class;
        }

        public boolean isCommandCaught() {
            return this.caughtCommand;
        }

    }

}
