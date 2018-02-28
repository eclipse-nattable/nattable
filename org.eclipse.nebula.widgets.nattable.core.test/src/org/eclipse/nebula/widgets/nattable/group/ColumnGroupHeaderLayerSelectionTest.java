/*******************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultGridLayer;
import org.eclipse.nebula.widgets.nattable.group.command.ViewportSelectColumnGroupCommand;
import org.eclipse.nebula.widgets.nattable.group.command.ViewportSelectColumnGroupCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.config.DefaultColumnGroupHeaderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.GridLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class ColumnGroupHeaderLayerSelectionTest {

    public static final String TEST_GROUP_NAME_3 = "testGroupName3";
    public static final String TEST_GROUP_NAME_2 = "testGroupName2";
    public static final String TEST_GROUP_NAME_1 = "testGroupName";
    public static final String NO_GROUP_NAME = "";
    public ColumnGroupHeaderLayer columnGroupLayer;
    private ColumnGroupModel model;
    private DefaultGridLayer gridLayer;

    @Before
    public void setup() {
        this.gridLayer = new GridLayerFixture();
        this.model = new ColumnGroupModel();
        // 10 columns in header
        this.columnGroupLayer = new ColumnGroupHeaderLayer(
                this.gridLayer.getColumnHeaderLayer(),
                this.gridLayer.getBodyLayer().getSelectionLayer(),
                this.model,
                false);

        this.columnGroupLayer.addConfiguration(new DefaultColumnGroupHeaderLayerConfiguration(this.model, true));
        this.gridLayer.getBodyLayer().getViewportLayer().registerCommandHandler(
                new ViewportSelectColumnGroupCommandHandler(this.gridLayer.getBodyLayer().getViewportLayer(), this.columnGroupLayer));

        this.columnGroupLayer.addColumnsIndexesToGroup(TEST_GROUP_NAME_1, 0, 1, 2);
        this.columnGroupLayer.addColumnsIndexesToGroup(TEST_GROUP_NAME_2, 5, 6);
        this.columnGroupLayer.addColumnsIndexesToGroup(TEST_GROUP_NAME_3, 8, 9);

        this.gridLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 1050, 250);
            }

        });
        this.gridLayer.doCommand(new ClientAreaResizeCommand(new Shell(Display.getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));
    }

    @Test
    public void shouldSelectAllCellsInGroup() {
        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 2, 0, false, false));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));
    }

    @Test
    public void shouldDeselectAndSelectAllCellsInGroup() {
        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 2, 0, false, false));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));

        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 6, 0, false, false));

        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertFalse(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(5));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(6));
    }

    @Test
    public void shouldSelectAllCellsInGroupWithCtrl() {
        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 2, 0, false, false));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));

        this.gridLayer.doCommand(new ViewportSelectColumnGroupCommand(this.gridLayer, 6, 0, false, true));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(0));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(1));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(2));

        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(5));
        assertTrue(this.gridLayer.getBodyLayer().getSelectionLayer().isColumnPositionFullySelected(6));
    }

}
