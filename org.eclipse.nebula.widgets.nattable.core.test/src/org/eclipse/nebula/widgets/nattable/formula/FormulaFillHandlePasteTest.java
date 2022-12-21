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
package org.eclipse.nebula.widgets.nattable.formula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.EditableRule;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.copy.InternalCellClipboard;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataToClipboardCommand;
import org.eclipse.nebula.widgets.nattable.copy.command.InternalCopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommand;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommand.FillHandleOperation;
import org.eclipse.nebula.widgets.nattable.formula.command.FormulaFillHandlePasteCommandHandler;
import org.eclipse.nebula.widgets.nattable.formula.config.DefaultFormulaConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FormulaFillHandlePasteTest {

    IDataProvider dataProvider = new TwoDimensionalArrayDataProvider(new String[10][10]);
    FormulaDataProvider formulaDataProvider = new FormulaDataProvider(this.dataProvider);
    SelectionLayer selectionLayer = new SelectionLayer(new DataLayer(this.formulaDataProvider));
    NatTable natTable = new NatTableFixture(this.selectionLayer, false);

    @BeforeEach
    public void setup() {
        InternalCellClipboard clipboard = new InternalCellClipboard();
        this.selectionLayer.registerCommandHandler(
                new InternalCopyDataCommandHandler(this.selectionLayer, clipboard));
        this.selectionLayer.registerCommandHandler(
                new FormulaFillHandlePasteCommandHandler(this.selectionLayer, clipboard, this.formulaDataProvider));

        this.natTable.addConfiguration(new DefaultFormulaConfiguration(
                this.formulaDataProvider,
                this.selectionLayer,
                clipboard));

        this.natTable.addConfiguration(new AbstractRegistryConfiguration() {

            @Override
            public void configureRegistry(IConfigRegistry configRegistry) {
                configRegistry.registerConfigAttribute(
                        EditConfigAttributes.CELL_EDITABLE_RULE,
                        EditableRule.ALWAYS_EDITABLE);
            }
        });
        this.natTable.configure();
    }

    @AfterEach
    public void tearDown() {
        this.selectionLayer.clear();
        this.selectionLayer.setFillHandleRegion(null);
    }

    @Test
    public void testSingleCellDragOneDown() {
        this.dataProvider.setDataValue(4, 4, "Simpson");

        this.selectionLayer.setSelectedCell(4, 4);

        testCellStates(new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 2));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 5));

        testCellStates(new Point(4, 4), new Point(4, 5));
    }

    @Test
    public void testSingleCellSeriesStringDragOneDown() {
        this.dataProvider.setDataValue(4, 4, "Simpson");

        this.selectionLayer.setSelectedCell(4, 4);

        testCellStates(new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 2));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 5));

        testCellStates(new Point(4, 4), new Point(4, 5));
    }

    @Test
    public void testSingleNumberCellDragDown() {
        this.dataProvider.setDataValue(4, 4, "1");

        this.selectionLayer.setSelectedCell(4, 4);

        testCellStates(new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 4));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals("1", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("2", this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals("3", this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals("4", this.selectionLayer.getDataValueByPosition(4, 7));

        testCellStates(new Point(4, 4), new Point(4, 5), new Point(4, 6), new Point(4, 7));
    }

    @Test
    public void testMultiNumberCellDragDown() {
        this.dataProvider.setDataValue(4, 4, "2");
        this.dataProvider.setDataValue(4, 5, "4");

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        testCellStates(new Point(4, 4), new Point(4, 5));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 4));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals("2", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("4", this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals("6", this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals("8", this.selectionLayer.getDataValueByPosition(4, 7));

        testCellStates(new Point(4, 4), new Point(4, 5), new Point(4, 6), new Point(4, 7));
    }

    @Test
    public void testFunctionCellDragDown() {
        this.dataProvider.setDataValue(4, 4, "2");
        this.dataProvider.setDataValue(5, 4, "21");
        this.dataProvider.setDataValue(6, 4, "=E5*F5");
        this.dataProvider.setDataValue(4, 5, "3");
        this.dataProvider.setDataValue(5, 5, "22");

        this.selectionLayer.selectCell(6, 4, false, true);

        testCellStates(new Point(4, 4), new Point(5, 4), new Point(6, 4),
                new Point(4, 5), new Point(5, 5));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(6, 4, 1, 2));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals("2", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("21", this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals(new BigDecimal("42"), this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals("3", this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals("22", this.selectionLayer.getDataValueByPosition(5, 5));
        assertEquals(new BigDecimal("66"), this.selectionLayer.getDataValueByPosition(6, 5));

        testCellStates(new Point(4, 4), new Point(5, 4), new Point(6, 4),
                new Point(4, 5), new Point(5, 5), new Point(6, 5));
    }

    @Test
    public void testFullFunctionCellDragDown() {
        this.dataProvider.setDataValue(4, 4, "2");
        this.dataProvider.setDataValue(5, 4, "21");
        this.dataProvider.setDataValue(6, 4, "=E5*F5");

        assertEquals(new BigDecimal("42"), this.formulaDataProvider.getDataValue(6, 4));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(5, 4, false, true);
        this.selectionLayer.selectCell(6, 4, false, true);

        testCellStates(new Point(4, 4), new Point(5, 4), new Point(6, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 3, 3));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals("2", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("21", this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals(new BigDecimal("42"), this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals("3", this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals("22", this.selectionLayer.getDataValueByPosition(5, 5));
        assertEquals(new BigDecimal("66"), this.selectionLayer.getDataValueByPosition(6, 5));
        assertEquals("4", this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals("23", this.selectionLayer.getDataValueByPosition(5, 6));
        assertEquals(new BigDecimal("92"), this.selectionLayer.getDataValueByPosition(6, 6));

        testCellStates(new Point(4, 4), new Point(5, 4), new Point(6, 4),
                new Point(4, 5), new Point(5, 5), new Point(6, 5),
                new Point(4, 6), new Point(5, 6), new Point(6, 6));
    }

    private void testCellStates(Point... nonNull) {
        for (int i = 0; i < this.dataProvider.getColumnCount(); i++) {
            for (int j = 0; j < this.dataProvider.getRowCount(); j++) {

                boolean check = true;
                for (Point p : nonNull) {
                    if ((i == p.x) && (j == p.y)) {
                        check = false;
                        break;
                    }
                }
                if (check) {
                    assertNull(this.selectionLayer.getDataValueByPosition(i, j), "Position " + i + "/" + j + " is not null");
                } else {
                    assertNotNull(this.selectionLayer.getDataValueByPosition(i, j), "Position " + i + "/" + j + " is null");
                }
            }
        }
    }

}
