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
package org.eclipse.nebula.widgets.nattable.fillhandle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.EditableRule;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataToClipboardCommand;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommand;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommand.FillHandleOperation;
import org.eclipse.nebula.widgets.nattable.fillhandle.config.FillHandleConfigAttributes;
import org.eclipse.nebula.widgets.nattable.fillhandle.config.FillHandleConfiguration;
import org.eclipse.nebula.widgets.nattable.formula.TwoDimensionalArrayDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FillHandleSeriesTest {

    IDataProvider dataProvider = new TwoDimensionalArrayDataProvider(new Object[10][10]);
    SelectionLayer selectionLayer = new SelectionLayer(new DataLayer(this.dataProvider));
    NatTable natTable = new NatTableFixture(this.selectionLayer, false);

    @BeforeEach
    public void setup() {
        this.natTable.addConfiguration(new FillHandleConfiguration(this.selectionLayer));
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
    public void testSingleCellIntegerValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Integer.valueOf("1"));

        this.selectionLayer.setSelectedCell(4, 4);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 4));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Integer.valueOf("2"), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Integer.valueOf("3"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(Integer.valueOf("4"), this.selectionLayer.getDataValueByPosition(4, 7));
    }

    @Test
    public void testSingleCellIntegerValueDragUp() {
        this.dataProvider.setDataValue(4, 4, Integer.valueOf("1"));

        this.selectionLayer.setSelectedCell(4, 4);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 1, 1, 4));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.UP, this.natTable.getConfigRegistry()));

        assertEquals(Integer.valueOf("-2"), this.selectionLayer.getDataValueByPosition(4, 1));
        assertEquals(Integer.valueOf("-1"), this.selectionLayer.getDataValueByPosition(4, 2));
        assertEquals(Integer.valueOf("0"), this.selectionLayer.getDataValueByPosition(4, 3));
        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
    }

    @Test
    public void testSingleCellIntegerValueDragRight() {
        this.dataProvider.setDataValue(4, 4, Integer.valueOf("1"));

        this.selectionLayer.setSelectedCell(4, 4);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 4, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Integer.valueOf("2"), this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals(Integer.valueOf("3"), this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals(Integer.valueOf("4"), this.selectionLayer.getDataValueByPosition(7, 4));
    }

    @Test
    public void testSingleCellIntegerValueDragLeft() {
        this.dataProvider.setDataValue(4, 4, Integer.valueOf("1"));

        this.selectionLayer.setSelectedCell(4, 4);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(1, 4, 4, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.LEFT, this.natTable.getConfigRegistry()));

        assertEquals(Integer.valueOf("-2"), this.selectionLayer.getDataValueByPosition(1, 4));
        assertEquals(Integer.valueOf("-1"), this.selectionLayer.getDataValueByPosition(2, 4));
        assertEquals(Integer.valueOf("0"), this.selectionLayer.getDataValueByPosition(3, 4));
        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
    }

    @Test
    public void testSingleCellByteValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Byte.valueOf("1"));

        this.selectionLayer.setSelectedCell(4, 4);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 4));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Byte.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Byte.valueOf("2"), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Byte.valueOf("3"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(Byte.valueOf("4"), this.selectionLayer.getDataValueByPosition(4, 7));
    }

    @Test
    public void testSingleCellShortValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Short.valueOf("1"));

        this.selectionLayer.setSelectedCell(4, 4);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 4));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Short.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Short.valueOf("2"), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Short.valueOf("3"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(Short.valueOf("4"), this.selectionLayer.getDataValueByPosition(4, 7));
    }

    @Test
    public void testSingleCellLongValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Long.valueOf("1"));

        this.selectionLayer.setSelectedCell(4, 4);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 4));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Long.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Long.valueOf("2"), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Long.valueOf("3"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(Long.valueOf("4"), this.selectionLayer.getDataValueByPosition(4, 7));
    }

    @Test
    public void testSingleCellFloatValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Float.valueOf("1.4"));

        this.selectionLayer.setSelectedCell(4, 4);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 4));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Float.valueOf("1.4"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Float.valueOf("2.4"), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Float.valueOf("3.4"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(Float.valueOf("4.4"), this.selectionLayer.getDataValueByPosition(4, 7));
    }

    @Test
    public void testSingleCellDoubleValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Double.valueOf("1.6"));

        this.selectionLayer.setSelectedCell(4, 4);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 4));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Double.valueOf("1.6"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Double.valueOf("2.6"), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Double.valueOf("3.6"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(Double.valueOf("4.6"), this.selectionLayer.getDataValueByPosition(4, 7));
    }

    @Test
    public void testSingleCellBigIntegerValueDragDown() {
        this.dataProvider.setDataValue(4, 4, BigInteger.valueOf(100));

        this.selectionLayer.setSelectedCell(4, 4);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 4));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(BigInteger.valueOf(100), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(BigInteger.valueOf(101), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(BigInteger.valueOf(102), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(BigInteger.valueOf(103), this.selectionLayer.getDataValueByPosition(4, 7));
    }

    @Test
    public void testSingleCellBigDecimalValueDragDown() {
        this.dataProvider.setDataValue(4, 4, BigDecimal.valueOf(13.8d));

        this.selectionLayer.setSelectedCell(4, 4);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 4));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(BigDecimal.valueOf(13.8d), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(BigDecimal.valueOf(14.8d), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(BigDecimal.valueOf(15.8d), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(BigDecimal.valueOf(16.8d), this.selectionLayer.getDataValueByPosition(4, 7));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSingleCellDateValueDragDown() {
        this.dataProvider.setDataValue(4, 4, new Date(2015, 9, 29));

        this.selectionLayer.setSelectedCell(4, 4);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 4));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(new Date(2015, 9, 29), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(new Date(2015, 9, 30), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(new Date(2015, 9, 31), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(new Date(2015, 10, 1), this.selectionLayer.getDataValueByPosition(4, 7));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSingleCellDateValueDragUp() {
        this.dataProvider.setDataValue(4, 4, new Date(2015, 9, 2));

        this.selectionLayer.setSelectedCell(4, 4);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 1, 1, 4));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.UP, this.natTable.getConfigRegistry()));

        assertEquals(new Date(2015, 8, 29), this.selectionLayer.getDataValueByPosition(4, 1));
        assertEquals(new Date(2015, 8, 30), this.selectionLayer.getDataValueByPosition(4, 2));
        assertEquals(new Date(2015, 9, 1), this.selectionLayer.getDataValueByPosition(4, 3));
        assertEquals(new Date(2015, 9, 2), this.selectionLayer.getDataValueByPosition(4, 4));
    }

    @Test
    public void testMultiCellSameDiffIntegerValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Integer.valueOf("1"));
        this.dataProvider.setDataValue(4, 5, Integer.valueOf("3"));
        this.dataProvider.setDataValue(4, 6, Integer.valueOf("5"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);
        this.selectionLayer.selectCell(4, 6, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Integer.valueOf("3"), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Integer.valueOf("5"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(Integer.valueOf("7"), this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(Integer.valueOf("9"), this.selectionLayer.getDataValueByPosition(4, 8));
        assertEquals(Integer.valueOf("11"), this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @Test
    public void testMultiCellSameDiffIntegerValueDragRight() {
        this.dataProvider.setDataValue(4, 4, Integer.valueOf("1"));
        this.dataProvider.setDataValue(5, 4, Integer.valueOf("3"));
        this.dataProvider.setDataValue(6, 4, Integer.valueOf("5"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(5, 4, false, true);
        this.selectionLayer.selectCell(6, 4, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 6, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Integer.valueOf("3"), this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals(Integer.valueOf("5"), this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals(Integer.valueOf("7"), this.selectionLayer.getDataValueByPosition(7, 4));
        assertEquals(Integer.valueOf("9"), this.selectionLayer.getDataValueByPosition(8, 4));
        assertEquals(Integer.valueOf("11"), this.selectionLayer.getDataValueByPosition(9, 4));
    }

    @Test
    public void testMultiCellSameDiffIntegerValueDragUp() {
        this.dataProvider.setDataValue(4, 4, Integer.valueOf("1"));
        this.dataProvider.setDataValue(4, 5, Integer.valueOf("3"));
        this.dataProvider.setDataValue(4, 6, Integer.valueOf("5"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);
        this.selectionLayer.selectCell(4, 6, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 1, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.UP, this.natTable.getConfigRegistry()));

        assertEquals(Integer.valueOf("-5"), this.selectionLayer.getDataValueByPosition(4, 1));
        assertEquals(Integer.valueOf("-3"), this.selectionLayer.getDataValueByPosition(4, 2));
        assertEquals(Integer.valueOf("-1"), this.selectionLayer.getDataValueByPosition(4, 3));
        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Integer.valueOf("3"), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Integer.valueOf("5"), this.selectionLayer.getDataValueByPosition(4, 6));
    }

    @Test
    public void testMultiCellSameDiffIntegerValueDragLeft() {
        this.dataProvider.setDataValue(4, 4, Integer.valueOf("1"));
        this.dataProvider.setDataValue(5, 4, Integer.valueOf("3"));
        this.dataProvider.setDataValue(6, 4, Integer.valueOf("5"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(5, 4, false, true);
        this.selectionLayer.selectCell(6, 4, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(1, 4, 6, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.LEFT, this.natTable.getConfigRegistry()));

        assertEquals(Integer.valueOf("-5"), this.selectionLayer.getDataValueByPosition(1, 4));
        assertEquals(Integer.valueOf("-3"), this.selectionLayer.getDataValueByPosition(2, 4));
        assertEquals(Integer.valueOf("-1"), this.selectionLayer.getDataValueByPosition(3, 4));
        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Integer.valueOf("3"), this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals(Integer.valueOf("5"), this.selectionLayer.getDataValueByPosition(6, 4));
    }

    @Test
    public void testMultiCellSameDiffByteValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Byte.valueOf("1"));
        this.dataProvider.setDataValue(4, 5, Byte.valueOf("3"));
        this.dataProvider.setDataValue(4, 6, Byte.valueOf("5"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);
        this.selectionLayer.selectCell(4, 6, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Byte.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Byte.valueOf("3"), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Byte.valueOf("5"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(Byte.valueOf("7"), this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(Byte.valueOf("9"), this.selectionLayer.getDataValueByPosition(4, 8));
        assertEquals(Byte.valueOf("11"), this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @Test
    public void testMultiCellSameDiffShortValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Short.valueOf("1"));
        this.dataProvider.setDataValue(4, 5, Short.valueOf("3"));
        this.dataProvider.setDataValue(4, 6, Short.valueOf("5"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);
        this.selectionLayer.selectCell(4, 6, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Short.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Short.valueOf("3"), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Short.valueOf("5"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(Short.valueOf("7"), this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(Short.valueOf("9"), this.selectionLayer.getDataValueByPosition(4, 8));
        assertEquals(Short.valueOf("11"), this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @Test
    public void testMultiCellSameDiffLongValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Long.valueOf("1"));
        this.dataProvider.setDataValue(4, 5, Long.valueOf("3"));
        this.dataProvider.setDataValue(4, 6, Long.valueOf("5"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);
        this.selectionLayer.selectCell(4, 6, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Long.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Long.valueOf("3"), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Long.valueOf("5"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(Long.valueOf("7"), this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(Long.valueOf("9"), this.selectionLayer.getDataValueByPosition(4, 8));
        assertEquals(Long.valueOf("11"), this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @Test
    public void testMultiCellSameDiffFloatValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Float.valueOf("1.3"));
        this.dataProvider.setDataValue(4, 5, Float.valueOf("2.6"));
        this.dataProvider.setDataValue(4, 6, Float.valueOf("3.9"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);
        this.selectionLayer.selectCell(4, 6, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Float.valueOf("1.3"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Float.valueOf("2.6"), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Float.valueOf("3.9"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(Float.valueOf("5.2"), this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(Float.valueOf("6.5"), this.selectionLayer.getDataValueByPosition(4, 8));
        assertEquals(Float.valueOf("7.8"), this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @Test
    public void testMultiCellSameDiffDoubleValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Double.valueOf("1.3"));
        this.dataProvider.setDataValue(4, 5, Double.valueOf("2.6"));
        this.dataProvider.setDataValue(4, 6, Double.valueOf("3.9"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);
        this.selectionLayer.selectCell(4, 6, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Double.valueOf("1.3"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Double.valueOf("2.6"), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Double.valueOf("3.9"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(Double.valueOf("5.2"), this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(Double.valueOf("6.5"), this.selectionLayer.getDataValueByPosition(4, 8));
        assertEquals(Double.valueOf("7.8"), this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @Test
    public void testMultiCellSameDiffBigIntegerValueDragDown() {
        this.dataProvider.setDataValue(4, 4, BigInteger.valueOf(1));
        this.dataProvider.setDataValue(4, 5, BigInteger.valueOf(3));
        this.dataProvider.setDataValue(4, 6, BigInteger.valueOf(5));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);
        this.selectionLayer.selectCell(4, 6, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(BigInteger.valueOf(1), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(BigInteger.valueOf(3), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(BigInteger.valueOf(5), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(BigInteger.valueOf(7), this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(BigInteger.valueOf(9), this.selectionLayer.getDataValueByPosition(4, 8));
        assertEquals(BigInteger.valueOf(11), this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @Test
    public void testMultiCellSameDiffBigDecimalValueDragDown() {
        this.dataProvider.setDataValue(4, 4, BigDecimal.valueOf(1.2d));
        this.dataProvider.setDataValue(4, 5, BigDecimal.valueOf(2.4d));
        this.dataProvider.setDataValue(4, 6, BigDecimal.valueOf(3.6d));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);
        this.selectionLayer.selectCell(4, 6, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(BigDecimal.valueOf(1.2d), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(BigDecimal.valueOf(2.4d), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(BigDecimal.valueOf(3.6d), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(BigDecimal.valueOf(4.8d), this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(BigDecimal.valueOf(6d), this.selectionLayer.getDataValueByPosition(4, 8));
        assertEquals(BigDecimal.valueOf(7.2d), this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testMultiCellSameDiffDateValueDragDown() {
        this.dataProvider.setDataValue(4, 4, new Date(2015, 9, 2));
        this.dataProvider.setDataValue(4, 5, new Date(2015, 9, 5));
        this.dataProvider.setDataValue(4, 6, new Date(2015, 9, 8));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);
        this.selectionLayer.selectCell(4, 6, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(new Date(2015, 9, 2), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(new Date(2015, 9, 5), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(new Date(2015, 9, 8), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(new Date(2015, 9, 11), this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(new Date(2015, 9, 14), this.selectionLayer.getDataValueByPosition(4, 8));
        assertEquals(new Date(2015, 9, 17), this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testMultiCellSameDiffDateYearValueDragDown() {
        this.dataProvider.setDataValue(4, 4, new Date(2002, 9, 2));
        this.dataProvider.setDataValue(4, 5, new Date(2004, 9, 5));
        this.dataProvider.setDataValue(4, 6, new Date(2006, 9, 8));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);
        this.selectionLayer.selectCell(4, 6, false, true);

        this.natTable.getConfigRegistry().registerConfigAttribute(
                FillHandleConfigAttributes.INCREMENT_DATE_FIELD,
                Calendar.YEAR);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(new Date(2002, 9, 2), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(new Date(2004, 9, 5), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(new Date(2006, 9, 8), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(new Date(2008, 9, 2), this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(new Date(2010, 9, 5), this.selectionLayer.getDataValueByPosition(4, 8));
        assertEquals(new Date(2012, 9, 8), this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @Test
    public void testMultiCellSameDiffByteValueDragRight() {
        this.dataProvider.setDataValue(4, 4, Byte.valueOf("1"));
        this.dataProvider.setDataValue(5, 4, Byte.valueOf("3"));
        this.dataProvider.setDataValue(6, 4, Byte.valueOf("5"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(5, 4, false, true);
        this.selectionLayer.selectCell(6, 4, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 6, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        assertEquals(Byte.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Byte.valueOf("3"), this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals(Byte.valueOf("5"), this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals(Byte.valueOf("7"), this.selectionLayer.getDataValueByPosition(7, 4));
        assertEquals(Byte.valueOf("9"), this.selectionLayer.getDataValueByPosition(8, 4));
        assertEquals(Byte.valueOf("11"), this.selectionLayer.getDataValueByPosition(9, 4));
    }

    @Test
    public void testMultiCellSameDiffShortValueDragRight() {
        this.dataProvider.setDataValue(4, 4, Short.valueOf("1"));
        this.dataProvider.setDataValue(5, 4, Short.valueOf("3"));
        this.dataProvider.setDataValue(6, 4, Short.valueOf("5"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(5, 4, false, true);
        this.selectionLayer.selectCell(6, 4, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 6, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        assertEquals(Short.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Short.valueOf("3"), this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals(Short.valueOf("5"), this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals(Short.valueOf("7"), this.selectionLayer.getDataValueByPosition(7, 4));
        assertEquals(Short.valueOf("9"), this.selectionLayer.getDataValueByPosition(8, 4));
        assertEquals(Short.valueOf("11"), this.selectionLayer.getDataValueByPosition(9, 4));
    }

    @Test
    public void testMultiCellSameDiffLongValueDragRight() {
        this.dataProvider.setDataValue(4, 4, Long.valueOf("1"));
        this.dataProvider.setDataValue(5, 4, Long.valueOf("3"));
        this.dataProvider.setDataValue(6, 4, Long.valueOf("5"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(5, 4, false, true);
        this.selectionLayer.selectCell(6, 4, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 6, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        assertEquals(Long.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Long.valueOf("3"), this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals(Long.valueOf("5"), this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals(Long.valueOf("7"), this.selectionLayer.getDataValueByPosition(7, 4));
        assertEquals(Long.valueOf("9"), this.selectionLayer.getDataValueByPosition(8, 4));
        assertEquals(Long.valueOf("11"), this.selectionLayer.getDataValueByPosition(9, 4));
    }

    @Test
    public void testMultiCellSameDiffFloatValueDragRight() {
        this.dataProvider.setDataValue(4, 4, Float.valueOf("1.3"));
        this.dataProvider.setDataValue(5, 4, Float.valueOf("2.6"));
        this.dataProvider.setDataValue(6, 4, Float.valueOf("3.9"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(5, 4, false, true);
        this.selectionLayer.selectCell(6, 4, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 6, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        assertEquals(Float.valueOf("1.3"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Float.valueOf("2.6"), this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals(Float.valueOf("3.9"), this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals(Float.valueOf("5.2"), this.selectionLayer.getDataValueByPosition(7, 4));
        assertEquals(Float.valueOf("6.5"), this.selectionLayer.getDataValueByPosition(8, 4));
        assertEquals(Float.valueOf("7.8"), this.selectionLayer.getDataValueByPosition(9, 4));
    }

    @Test
    public void testMultiCellSameDiffDoubleValueDragRight() {
        this.dataProvider.setDataValue(4, 4, Double.valueOf("1.3"));
        this.dataProvider.setDataValue(5, 4, Double.valueOf("2.6"));
        this.dataProvider.setDataValue(6, 4, Double.valueOf("3.9"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(5, 4, false, true);
        this.selectionLayer.selectCell(6, 4, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 6, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        assertEquals(Double.valueOf("1.3"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Double.valueOf("2.6"), this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals(Double.valueOf("3.9"), this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals(Double.valueOf("5.2"), this.selectionLayer.getDataValueByPosition(7, 4));
        assertEquals(Double.valueOf("6.5"), this.selectionLayer.getDataValueByPosition(8, 4));
        assertEquals(Double.valueOf("7.8"), this.selectionLayer.getDataValueByPosition(9, 4));
    }

    @Test
    public void testMultiCellSameDiffBigIntegerValueDragRight() {
        this.dataProvider.setDataValue(4, 4, BigInteger.valueOf(1));
        this.dataProvider.setDataValue(5, 4, BigInteger.valueOf(3));
        this.dataProvider.setDataValue(6, 4, BigInteger.valueOf(5));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(5, 4, false, true);
        this.selectionLayer.selectCell(6, 4, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 6, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        assertEquals(BigInteger.valueOf(1), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(BigInteger.valueOf(3), this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals(BigInteger.valueOf(5), this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals(BigInteger.valueOf(7), this.selectionLayer.getDataValueByPosition(7, 4));
        assertEquals(BigInteger.valueOf(9), this.selectionLayer.getDataValueByPosition(8, 4));
        assertEquals(BigInteger.valueOf(11), this.selectionLayer.getDataValueByPosition(9, 4));
    }

    @Test
    public void testMultiCellSameDiffBigDecimalValueDragRight() {
        this.dataProvider.setDataValue(4, 4, BigDecimal.valueOf(1.2d));
        this.dataProvider.setDataValue(5, 4, BigDecimal.valueOf(2.4d));
        this.dataProvider.setDataValue(6, 4, BigDecimal.valueOf(3.6d));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(5, 4, false, true);
        this.selectionLayer.selectCell(6, 4, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 6, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        assertEquals(BigDecimal.valueOf(1.2d), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(BigDecimal.valueOf(2.4d), this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals(BigDecimal.valueOf(3.6d), this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals(BigDecimal.valueOf(4.8d), this.selectionLayer.getDataValueByPosition(7, 4));
        assertEquals(BigDecimal.valueOf(6d), this.selectionLayer.getDataValueByPosition(8, 4));
        assertEquals(BigDecimal.valueOf(7.2d), this.selectionLayer.getDataValueByPosition(9, 4));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testMultiCellSameDiffDateValueDragRight() {
        this.dataProvider.setDataValue(4, 4, new Date(2002, 9, 2));
        this.dataProvider.setDataValue(5, 4, new Date(2002, 9, 5));
        this.dataProvider.setDataValue(6, 4, new Date(2002, 9, 8));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(5, 4, false, true);
        this.selectionLayer.selectCell(6, 4, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 6, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        assertEquals(new Date(2002, 9, 2), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(new Date(2002, 9, 5), this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals(new Date(2002, 9, 8), this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals(new Date(2002, 9, 11), this.selectionLayer.getDataValueByPosition(7, 4));
        assertEquals(new Date(2002, 9, 14), this.selectionLayer.getDataValueByPosition(8, 4));
        assertEquals(new Date(2002, 9, 17), this.selectionLayer.getDataValueByPosition(9, 4));
    }

    @Test
    public void testMultiCellDifferentIntegerValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Integer.valueOf("1"));
        this.dataProvider.setDataValue(4, 5, Integer.valueOf("2"));
        this.dataProvider.setDataValue(4, 6, Integer.valueOf("4"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);
        this.selectionLayer.selectCell(4, 6, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        // as there is no common diff value in the cells we simply perform a
        // copy operation
        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Integer.valueOf("2"), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Integer.valueOf("4"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(Integer.valueOf("2"), this.selectionLayer.getDataValueByPosition(4, 8));
        assertEquals(Integer.valueOf("4"), this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @Test
    public void testMultiCellIntegerValueDragDown() {
        // column 4 simple series
        this.dataProvider.setDataValue(4, 4, Integer.valueOf("1"));
        this.dataProvider.setDataValue(4, 5, Integer.valueOf("2"));
        this.dataProvider.setDataValue(4, 6, Integer.valueOf("3"));
        // column 5 calculated series
        this.dataProvider.setDataValue(5, 4, Integer.valueOf("2"));
        this.dataProvider.setDataValue(5, 5, Integer.valueOf("4"));
        this.dataProvider.setDataValue(5, 6, Integer.valueOf("6"));
        // column 6 incontiguous
        this.dataProvider.setDataValue(6, 4, Integer.valueOf("1"));
        this.dataProvider.setDataValue(6, 5, Integer.valueOf("2"));
        this.dataProvider.setDataValue(6, 6, Integer.valueOf("4"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(6, 6, true, false);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 3, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        // simple series
        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Integer.valueOf("2"), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Integer.valueOf("3"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(Integer.valueOf("4"), this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(Integer.valueOf("5"), this.selectionLayer.getDataValueByPosition(4, 8));
        assertEquals(Integer.valueOf("6"), this.selectionLayer.getDataValueByPosition(4, 9));

        // series with 2 difference
        assertEquals(Integer.valueOf("2"), this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals(Integer.valueOf("4"), this.selectionLayer.getDataValueByPosition(5, 5));
        assertEquals(Integer.valueOf("6"), this.selectionLayer.getDataValueByPosition(5, 6));
        assertEquals(Integer.valueOf("8"), this.selectionLayer.getDataValueByPosition(5, 7));
        assertEquals(Integer.valueOf("10"), this.selectionLayer.getDataValueByPosition(5, 8));
        assertEquals(Integer.valueOf("12"), this.selectionLayer.getDataValueByPosition(5, 9));

        // as there is no common diff value in the cells we simply perform a
        // copy operation
        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals(Integer.valueOf("2"), this.selectionLayer.getDataValueByPosition(6, 5));
        assertEquals(Integer.valueOf("4"), this.selectionLayer.getDataValueByPosition(6, 6));
        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(6, 7));
        assertEquals(Integer.valueOf("2"), this.selectionLayer.getDataValueByPosition(6, 8));
        assertEquals(Integer.valueOf("4"), this.selectionLayer.getDataValueByPosition(6, 9));
    }

    @Test
    public void testMultiCellIntegerValueDragRight() {
        // column 4 simple series
        this.dataProvider.setDataValue(4, 4, Integer.valueOf("1"));
        this.dataProvider.setDataValue(5, 4, Integer.valueOf("2"));
        this.dataProvider.setDataValue(6, 4, Integer.valueOf("3"));
        // column 5 calculated series
        this.dataProvider.setDataValue(4, 5, Integer.valueOf("2"));
        this.dataProvider.setDataValue(5, 5, Integer.valueOf("4"));
        this.dataProvider.setDataValue(6, 5, Integer.valueOf("6"));
        // column 6 incontiguous
        this.dataProvider.setDataValue(4, 6, Integer.valueOf("1"));
        this.dataProvider.setDataValue(5, 6, Integer.valueOf("2"));
        this.dataProvider.setDataValue(6, 6, Integer.valueOf("4"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(6, 6, true, false);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 6, 3));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        // simple series
        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Integer.valueOf("2"), this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals(Integer.valueOf("3"), this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals(Integer.valueOf("4"), this.selectionLayer.getDataValueByPosition(7, 4));
        assertEquals(Integer.valueOf("5"), this.selectionLayer.getDataValueByPosition(8, 4));
        assertEquals(Integer.valueOf("6"), this.selectionLayer.getDataValueByPosition(9, 4));

        // series with 2 difference
        assertEquals(Integer.valueOf("2"), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Integer.valueOf("4"), this.selectionLayer.getDataValueByPosition(5, 5));
        assertEquals(Integer.valueOf("6"), this.selectionLayer.getDataValueByPosition(6, 5));
        assertEquals(Integer.valueOf("8"), this.selectionLayer.getDataValueByPosition(7, 5));
        assertEquals(Integer.valueOf("10"), this.selectionLayer.getDataValueByPosition(8, 5));
        assertEquals(Integer.valueOf("12"), this.selectionLayer.getDataValueByPosition(9, 5));

        // as there is no common diff value in the cells we simply perform a
        // copy operation
        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(Integer.valueOf("2"), this.selectionLayer.getDataValueByPosition(5, 6));
        assertEquals(Integer.valueOf("4"), this.selectionLayer.getDataValueByPosition(6, 6));
        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(7, 6));
        assertEquals(Integer.valueOf("2"), this.selectionLayer.getDataValueByPosition(8, 6));
        assertEquals(Integer.valueOf("4"), this.selectionLayer.getDataValueByPosition(9, 6));
    }

    @Test
    public void testByteNullCellValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Byte.valueOf("1"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Byte.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Byte.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(Byte.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 8));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @Test
    public void testShortNullCellValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Short.valueOf("1"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Short.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Short.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(Short.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 8));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @Test
    public void testIntegerNullCellValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Integer.valueOf("1"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 8));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @Test
    public void testLongNullCellValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Long.valueOf("1"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Long.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Long.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(Long.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 8));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @Test
    public void testFloatNullCellValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Float.valueOf("1"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Float.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Float.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(Float.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 8));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @Test
    public void testDoubleNullCellValueDragDown() {
        this.dataProvider.setDataValue(4, 4, Double.valueOf("1"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(Double.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Double.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(Double.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 8));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @Test
    public void testBigIntegerNullCellValueDragDown() {
        this.dataProvider.setDataValue(4, 4, BigInteger.valueOf(1));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(BigInteger.valueOf(1), this.selectionLayer.getDataValueByPosition(4, 4));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(BigInteger.valueOf(1), this.selectionLayer.getDataValueByPosition(4, 6));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(BigInteger.valueOf(1), this.selectionLayer.getDataValueByPosition(4, 8));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @Test
    public void testBigDecimalNullCellValueDragDown() {
        this.dataProvider.setDataValue(4, 4, BigDecimal.valueOf(1));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(BigDecimal.valueOf(1), this.selectionLayer.getDataValueByPosition(4, 4));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(BigDecimal.valueOf(1), this.selectionLayer.getDataValueByPosition(4, 6));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(BigDecimal.valueOf(1), this.selectionLayer.getDataValueByPosition(4, 8));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDateNullCellValueDragDown() {
        this.dataProvider.setDataValue(4, 4, new Date(2015, 9, 13));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 6));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals(new Date(2015, 9, 13), this.selectionLayer.getDataValueByPosition(4, 4));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(new Date(2015, 9, 13), this.selectionLayer.getDataValueByPosition(4, 6));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals(new Date(2015, 9, 13), this.selectionLayer.getDataValueByPosition(4, 8));
        assertNull(this.selectionLayer.getDataValueByPosition(4, 9));
    }

    @Test
    public void testMultiCellDifferentTypesDragDown() {
        this.dataProvider.setDataValue(4, 4, Float.valueOf("3.5"));
        this.dataProvider.setDataValue(4, 5, BigInteger.valueOf(1));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 4));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        // result should be a copy because of different data types
        assertEquals(Float.valueOf("3.5"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(BigInteger.valueOf(1), this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals(Float.valueOf("3.5"), this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals(BigInteger.valueOf(1), this.selectionLayer.getDataValueByPosition(4, 7));
    }

    @Test
    public void testMultiCellDifferentTypesDragRight() {
        this.dataProvider.setDataValue(4, 4, Integer.valueOf("1"));
        this.dataProvider.setDataValue(5, 4, Float.valueOf("3.5"));

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(5, 4, false, true);

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 4, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.SERIES, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        // result should be a copy because of different data types
        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals(Float.valueOf("3.5"), this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals(Integer.valueOf("1"), this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals(Float.valueOf("3.5"), this.selectionLayer.getDataValueByPosition(7, 4));
    }

}
