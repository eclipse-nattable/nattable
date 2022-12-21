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
package org.eclipse.nebula.widgets.nattable.group;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ColumnHideCommand;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.resize.command.ColumnResizeCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultBodyLayerStackTest {

    private DefaultBodyLayerStack layerStack;

    @BeforeEach
    public void setup() {
        this.layerStack = new DefaultBodyLayerStack(new DataLayerFixture(10, 5, 100,
                20));
        this.layerStack.setClientAreaProvider(new IClientAreaProvider() {
            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 2000, 250);
            }
        });
        this.layerStack.doCommand(new ClientAreaResizeCommand(new Shell(Display
                .getDefault(), SWT.V_SCROLL | SWT.H_SCROLL)));
    }

    /*
     * Data Layer: 0 1 2 3 4 5 6 7 8 9
     * -----------------------------------------------------
     */
    @Test
    public void hideColumnsAndReorder() throws Exception {
        // Hide 3, 4
        this.layerStack.doCommand(new ColumnHideCommand(this.layerStack, 3));
        this.layerStack.doCommand(new ColumnHideCommand(this.layerStack, 3));

        assertEquals(0, this.layerStack.getColumnIndexByPosition(0));
        assertEquals(1, this.layerStack.getColumnIndexByPosition(1));
        assertEquals(2, this.layerStack.getColumnIndexByPosition(2));
        assertEquals(5, this.layerStack.getColumnIndexByPosition(3));
        assertEquals(6, this.layerStack.getColumnIndexByPosition(4));
        assertEquals(7, this.layerStack.getColumnIndexByPosition(5));
        assertEquals(8, this.layerStack.getColumnIndexByPosition(6));
        assertEquals(9, this.layerStack.getColumnIndexByPosition(7));
        assertEquals(-1, this.layerStack.getColumnIndexByPosition(8));

        // Reorder 0 -> 4
        this.layerStack.doCommand(new ColumnReorderCommand(this.layerStack, 0, 4));

        assertEquals(1, this.layerStack.getColumnIndexByPosition(0));
        assertEquals(2, this.layerStack.getColumnIndexByPosition(1));
        assertEquals(5, this.layerStack.getColumnIndexByPosition(2));
        assertEquals(0, this.layerStack.getColumnIndexByPosition(3));
        assertEquals(6, this.layerStack.getColumnIndexByPosition(4));
        assertEquals(7, this.layerStack.getColumnIndexByPosition(5));
        assertEquals(8, this.layerStack.getColumnIndexByPosition(6));
        assertEquals(9, this.layerStack.getColumnIndexByPosition(7));
        assertEquals(-1, this.layerStack.getColumnIndexByPosition(8));
    }

    @Test
    public void resizeAColumnAndHideIt() throws Exception {
        assertEquals(10, this.layerStack.getColumnCount());
        assertEquals(1000, this.layerStack.getWidth());

        // Resize 2
        this.layerStack.doCommand(new ColumnResizeCommand(this.layerStack, 2, 500));
        assertEquals(1400, this.layerStack.getWidth());

        assertEquals(1, this.layerStack.getColumnIndexByPosition(1));
        assertEquals(100, this.layerStack.getColumnWidthByPosition(1));
        assertEquals(100, this.layerStack.getStartXOfColumnPosition(1));

        assertEquals(2, this.layerStack.getColumnIndexByPosition(2));
        assertEquals(500, this.layerStack.getColumnWidthByPosition(2));
        assertEquals(200, this.layerStack.getStartXOfColumnPosition(2));

        assertEquals(3, this.layerStack.getColumnIndexByPosition(3));
        assertEquals(100, this.layerStack.getColumnWidthByPosition(3));
        assertEquals(700, this.layerStack.getStartXOfColumnPosition(3));

        // Hide 2
        this.layerStack.doCommand(new ColumnHideCommand(this.layerStack, 2));
        assertEquals(9, this.layerStack.getColumnCount());

        assertEquals(1, this.layerStack.getColumnIndexByPosition(1));
        assertEquals(100, this.layerStack.getColumnWidthByPosition(1));
        assertEquals(100, this.layerStack.getStartXOfColumnPosition(1));

        assertEquals(3, this.layerStack.getColumnIndexByPosition(2));
        assertEquals(100, this.layerStack.getColumnWidthByPosition(2));
        assertEquals(200, this.layerStack.getStartXOfColumnPosition(2));

        assertEquals(4, this.layerStack.getColumnIndexByPosition(3));
        assertEquals(100, this.layerStack.getColumnWidthByPosition(3));
        assertEquals(300, this.layerStack.getStartXOfColumnPosition(3));

        assertEquals(9, this.layerStack.getColumnIndexByPosition(8));
        assertEquals(100, this.layerStack.getColumnWidthByPosition(8));
        assertEquals(800, this.layerStack.getStartXOfColumnPosition(8));
    }

    @Test
    public void resizeAColumnAndReorderIt() throws Exception {
        assertEquals(10, this.layerStack.getColumnCount());
        assertEquals(1000, this.layerStack.getWidth());

        // Resize 2
        this.layerStack.doCommand(new ColumnResizeCommand(this.layerStack, 2, 500));
        assertEquals(1400, this.layerStack.getWidth());

        // Reorder 2 -> 4
        this.layerStack.doCommand(new ColumnReorderCommand(this.layerStack, 2, 4));

        assertEquals(0, this.layerStack.getColumnIndexByPosition(0));
        assertEquals(1, this.layerStack.getColumnIndexByPosition(1));
        assertEquals(3, this.layerStack.getColumnIndexByPosition(2));
        assertEquals(2, this.layerStack.getColumnIndexByPosition(3));
        assertEquals(4, this.layerStack.getColumnIndexByPosition(4));
        assertEquals(5, this.layerStack.getColumnIndexByPosition(5));
        assertEquals(6, this.layerStack.getColumnIndexByPosition(6));
        assertEquals(7, this.layerStack.getColumnIndexByPosition(7));
        assertEquals(8, this.layerStack.getColumnIndexByPosition(8));
    }
}
