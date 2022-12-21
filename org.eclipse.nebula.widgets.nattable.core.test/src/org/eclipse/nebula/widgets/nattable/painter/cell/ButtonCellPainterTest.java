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
package org.eclipse.nebula.widgets.nattable.painter.cell;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.test.fixture.CellPainterFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.MouseActionFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.integration.SWTUtils;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ButtonCellPainterTest {

    private ButtonCellPainter buttonCellPainter;
    private CellPainterFixture buttonRaisedPainter;
    private CellPainterFixture buttonPressedPainter;
    private NatTableFixture natTable;
    private ILayerCell cellFixture;
    private GC gcFixture;
    private MouseEvent mouseClickEvent;

    @BeforeEach
    public void setup() {
        this.buttonRaisedPainter = new CellPainterFixture();
        this.buttonPressedPainter = new CellPainterFixture();
        this.buttonCellPainter = new ButtonCellPainter(this.buttonRaisedPainter,
                this.buttonPressedPainter);
        this.buttonCellPainter.setButtonFlashTime(500);

        this.natTable = new NatTableFixture();
        this.cellFixture = new LayerCell(this.natTable, 1, 5);
        this.gcFixture = new GC(this.natTable);

        this.mouseClickEvent = new MouseEvent(SWTUtils.getLeftClickEvent(100, 100,
                0, this.natTable));
        this.mouseClickEvent.data = NatEventData
                .createInstanceFromEvent(this.mouseClickEvent);
    }

    @Test
    public void shouldPaintButtonPressedOnMouseClick() throws Exception {
        // Initial paint call
        this.buttonCellPainter.paintCell(this.cellFixture, this.gcFixture,
                this.cellFixture.getBounds(), this.natTable.getConfigRegistry());

        assertTrue(this.buttonRaisedPainter.isPainted());
        assertFalse(this.buttonPressedPainter.isPainted());

        // Mouse clicked
        this.buttonCellPainter.run(this.natTable, this.mouseClickEvent);
        this.buttonCellPainter.paintCell(this.cellFixture, this.gcFixture,
                this.cellFixture.getBounds(), this.natTable.getConfigRegistry());

        // Should be painted in pressed state
        assertTrue(this.buttonPressedPainter.isPainted());
    }

    @Test
    public void shouldNotifyListeners() throws Exception {
        MouseActionFixture mouseAction = new MouseActionFixture();
        this.buttonCellPainter.addClickListener(mouseAction);

        this.buttonCellPainter.run(this.natTable, this.mouseClickEvent);
        assertTrue(mouseAction.isActionInvoked());

        this.buttonCellPainter.removeClickListener(mouseAction);
        mouseAction.reset();

        this.buttonCellPainter.run(this.natTable, this.mouseClickEvent);
        assertFalse(mouseAction.isActionInvoked());
    }
}
