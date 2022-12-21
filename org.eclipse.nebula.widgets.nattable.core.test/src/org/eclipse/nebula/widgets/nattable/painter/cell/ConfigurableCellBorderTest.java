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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConfigurableCellBorderTest {

    private NatTable natTable;
    private ConfigRegistry configRegistry;

    @BeforeEach
    public void setUp() throws Exception {
        this.natTable = new NatTableFixture();
        this.configRegistry = (ConfigRegistry) this.natTable.getConfigRegistry();
    }

    @Test
    public void shouldReturnASelectedCellWithDottedLineStyling() {
        Style cellStyle = new Style();
        final BorderStyle defaultBorderStyle = new BorderStyle(13, GUIHelper.COLOR_YELLOW, LineStyleEnum.DOTTED);
        cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, defaultBorderStyle);

        // Register line styling for body cells in selection mode
        this.configRegistry.registerConfigAttribute(
                CellConfigAttributes.CELL_STYLE,
                cellStyle,
                DisplayMode.SELECT,
                SelectionStyleLabels.SELECTION_ANCHOR_STYLE);

        // Select and access cell
        this.natTable.doCommand(new SelectCellCommand(this.natTable, 2, 2, false, false));
        ILayerCell cell = this.natTable.getCellByPosition(2, 2);
        assertEquals(DisplayMode.SELECT, cell.getDisplayMode());

        // Check for line styling
        assertEquals(
                defaultBorderStyle,
                this.configRegistry.getConfigAttribute(
                        CellConfigAttributes.CELL_STYLE,
                        cell.getDisplayMode(),
                        cell.getConfigLabels())
                        .getAttributeValue(CellStyleAttributes.BORDER_STYLE));
    }

}
