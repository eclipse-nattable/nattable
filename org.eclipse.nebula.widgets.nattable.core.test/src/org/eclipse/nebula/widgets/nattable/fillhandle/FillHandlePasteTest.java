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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.EditableRule;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataToClipboardCommand;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommand;
import org.eclipse.nebula.widgets.nattable.fillhandle.command.FillHandlePasteCommand.FillHandleOperation;
import org.eclipse.nebula.widgets.nattable.fillhandle.config.FillHandleConfiguration;
import org.eclipse.nebula.widgets.nattable.formula.TwoDimensionalArrayDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FillHandlePasteTest {

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
    public void testSingleCellDragOneUp() {
        this.dataProvider.setDataValue(4, 4, "Simpson");

        this.selectionLayer.setSelectedCell(4, 4);

        testCellStates(new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 3, 1, 2));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.UP, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 3));

        testCellStates(new Point(4, 4), new Point(4, 3));
    }

    @Test
    public void testSingleCellDragOneRight() {
        this.dataProvider.setDataValue(4, 4, "Simpson");

        this.selectionLayer.setSelectedCell(4, 4);

        testCellStates(new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 2, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(5, 4));

        testCellStates(new Point(4, 4), new Point(5, 4));
    }

    @Test
    public void testSingleCellDragOneLeft() {
        this.dataProvider.setDataValue(4, 4, "Simpson");

        this.selectionLayer.setSelectedCell(4, 4);

        testCellStates(new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(3, 4, 2, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.LEFT, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(3, 4));

        testCellStates(new Point(4, 4), new Point(3, 4));
    }

    @Test
    public void testSingleCellDragMultipleDown() {
        this.dataProvider.setDataValue(4, 4, "Simpson");

        this.selectionLayer.setSelectedCell(4, 4);

        testCellStates(new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 4));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 7));

        testCellStates(new Point(4, 4), new Point(4, 5), new Point(4, 6), new Point(4, 7));
    }

    @Test
    public void testSingleCellDragMultiUp() {
        this.dataProvider.setDataValue(4, 4, "Simpson");

        this.selectionLayer.setSelectedCell(4, 4);

        testCellStates(new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 0, 1, 5));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.UP, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 0));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 1));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 2));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 3));

        testCellStates(new Point(4, 4), new Point(4, 0), new Point(4, 1), new Point(4, 2), new Point(4, 3));
    }

    @Test
    public void testSingleCellDragMultiRight() {
        this.dataProvider.setDataValue(4, 4, "Simpson");

        this.selectionLayer.setSelectedCell(4, 4);

        testCellStates(new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 4, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(7, 4));

        testCellStates(new Point(4, 4), new Point(5, 4), new Point(6, 4), new Point(7, 4));
    }

    @Test
    public void testSingleCellDragMultiLeft() {
        this.dataProvider.setDataValue(4, 4, "Simpson");

        this.selectionLayer.setSelectedCell(4, 4);

        testCellStates(new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(0, 4, 5, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.LEFT, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(0, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(1, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(2, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(3, 4));

        testCellStates(new Point(4, 4), new Point(0, 4), new Point(1, 4), new Point(2, 4), new Point(3, 4));
    }

    @Test
    public void testSingleCellDragDownRight() {
        this.dataProvider.setDataValue(4, 4, "Simpson");

        this.selectionLayer.setSelectedCell(4, 4);

        testCellStates(new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 2, 2));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(5, 5));

        testCellStates(new Point(4, 4), new Point(4, 5), new Point(5, 4), new Point(5, 5));
    }

    @Test
    public void testSingleCellDragOneUpRight() {
        this.dataProvider.setDataValue(4, 4, "Simpson");

        this.selectionLayer.setSelectedCell(4, 4);

        testCellStates(new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 3, 2, 2));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.UP, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 3));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(5, 3));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(5, 4));

        testCellStates(new Point(4, 3), new Point(5, 3), new Point(4, 4), new Point(5, 4));
    }

    @Test
    public void testSingleCellDragUpLeft() {
        this.dataProvider.setDataValue(4, 4, "Simpson");

        this.selectionLayer.setSelectedCell(4, 4);

        testCellStates(new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(3, 3, 2, 2));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.LEFT, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(3, 3));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 3));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(3, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));

        testCellStates(new Point(3, 3), new Point(4, 3), new Point(3, 4), new Point(4, 4));
    }

    @Test
    public void testSingleCellDragDownLeft() {
        this.dataProvider.setDataValue(4, 4, "Simpson");

        this.selectionLayer.setSelectedCell(4, 4);

        testCellStates(new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(3, 4, 2, 2));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.LEFT, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(3, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(3, 5));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 5));

        testCellStates(new Point(3, 4), new Point(4, 4), new Point(3, 5), new Point(4, 5));
    }

    @Test
    public void testMultiVerticalCellDragRight() {
        this.dataProvider.setDataValue(4, 4, "Simpson");
        this.dataProvider.setDataValue(4, 5, "Flanders");

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        testCellStates(new Point(4, 4), new Point(4, 5));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 3, 2));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(5, 5));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(6, 5));

        testCellStates(new Point(4, 4), new Point(4, 5), new Point(5, 4), new Point(5, 5), new Point(6, 4), new Point(6, 5));
    }

    @Test
    public void testMultiVerticalCellDragLeft() {
        this.dataProvider.setDataValue(4, 4, "Simpson");
        this.dataProvider.setDataValue(4, 5, "Flanders");

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        testCellStates(new Point(4, 4), new Point(4, 5));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(2, 4, 3, 2));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.LEFT, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(2, 4));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(2, 5));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(3, 4));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(3, 5));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 5));

        testCellStates(new Point(2, 4), new Point(2, 5), new Point(3, 4), new Point(3, 5), new Point(4, 4), new Point(4, 5));
    }

    @Test
    public void testMultiVerticalCellDragDown() {
        this.dataProvider.setDataValue(4, 4, "Simpson");
        this.dataProvider.setDataValue(4, 5, "Flanders");

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        testCellStates(new Point(4, 4), new Point(4, 5));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 4, 1, 5));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 7));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 8));

        testCellStates(new Point(4, 4), new Point(4, 5), new Point(4, 6), new Point(4, 7), new Point(4, 8));
    }

    @Test
    public void testMultiVerticalCellDragUp() {
        this.dataProvider.setDataValue(4, 4, "Simpson");
        this.dataProvider.setDataValue(4, 5, "Flanders");

        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        testCellStates(new Point(4, 4), new Point(4, 5));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(4, 1, 1, 5));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.UP, this.natTable.getConfigRegistry()));

        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 1));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 2));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 3));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 5));

        testCellStates(new Point(4, 1), new Point(4, 2), new Point(4, 3), new Point(4, 4), new Point(4, 5));
    }

    @Test
    public void testMultiHorizontalCellDragRight() {
        this.dataProvider.setDataValue(3, 4, "Homer");
        this.dataProvider.setDataValue(4, 4, "Simpson");

        this.selectionLayer.selectCell(3, 4, false, true);
        this.selectionLayer.selectCell(4, 4, false, true);

        testCellStates(new Point(3, 4), new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(3, 4, 5, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(7, 4));

        testCellStates(new Point(3, 4), new Point(4, 4), new Point(5, 4), new Point(6, 4), new Point(7, 4));
    }

    @Test
    public void testMultiHorizontalCellDragLeft() {
        this.dataProvider.setDataValue(3, 4, "Homer");
        this.dataProvider.setDataValue(4, 4, "Simpson");

        this.selectionLayer.selectCell(3, 4, false, true);
        this.selectionLayer.selectCell(4, 4, false, true);

        testCellStates(new Point(3, 4), new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(0, 4, 5, 1));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.LEFT, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(0, 4));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(1, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(2, 4));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));

        testCellStates(new Point(0, 4), new Point(1, 4), new Point(2, 4), new Point(3, 4), new Point(4, 4));
    }

    @Test
    public void testMultiHorizontalCellDragDown() {
        this.dataProvider.setDataValue(3, 4, "Homer");
        this.dataProvider.setDataValue(4, 4, "Simpson");

        this.selectionLayer.selectCell(3, 4, false, true);
        this.selectionLayer.selectCell(4, 4, false, true);

        testCellStates(new Point(3, 4), new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(3, 4, 2, 3));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 5));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 6));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 6));

        testCellStates(new Point(3, 4), new Point(4, 4), new Point(3, 5), new Point(4, 5), new Point(3, 6), new Point(4, 6));
    }

    @Test
    public void testMultiHorizontalCellDragUp() {
        this.dataProvider.setDataValue(3, 4, "Homer");
        this.dataProvider.setDataValue(4, 4, "Simpson");

        this.selectionLayer.selectCell(3, 4, false, true);
        this.selectionLayer.selectCell(4, 4, false, true);

        testCellStates(new Point(3, 4), new Point(4, 4));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(3, 2, 2, 3));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.UP, this.natTable.getConfigRegistry()));

        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 2));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 2));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 3));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 3));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));

        testCellStates(new Point(3, 2), new Point(4, 2), new Point(3, 3), new Point(4, 3), new Point(3, 4), new Point(4, 4));
    }

    @Test
    public void testMultiCellDragOneDown() {
        this.dataProvider.setDataValue(3, 4, "Homer");
        this.dataProvider.setDataValue(4, 4, "Simpson");
        this.dataProvider.setDataValue(3, 5, "Ned");
        this.dataProvider.setDataValue(4, 5, "Flanders");

        this.selectionLayer.selectCell(3, 4, false, true);
        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(3, 5, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        testCellStates(new Point(3, 4), new Point(4, 4), new Point(3, 5), new Point(4, 5));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(3, 4, 2, 3));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("Ned", this.selectionLayer.getDataValueByPosition(3, 5));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 6));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 6));

        testCellStates(
                new Point(3, 4), new Point(4, 4),
                new Point(3, 5), new Point(4, 5),
                new Point(3, 6), new Point(4, 6));
    }

    @Test
    public void testMultiCellDragThreeDown() {
        this.dataProvider.setDataValue(3, 4, "Homer");
        this.dataProvider.setDataValue(4, 4, "Simpson");
        this.dataProvider.setDataValue(3, 5, "Ned");
        this.dataProvider.setDataValue(4, 5, "Flanders");

        this.selectionLayer.selectCell(3, 4, false, true);
        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(3, 5, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        testCellStates(new Point(3, 4), new Point(4, 4), new Point(3, 5), new Point(4, 5));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(3, 4, 2, 5));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.DOWN, this.natTable.getConfigRegistry()));

        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("Ned", this.selectionLayer.getDataValueByPosition(3, 5));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 6));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 6));
        assertEquals("Ned", this.selectionLayer.getDataValueByPosition(3, 5));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 6));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 6));

        testCellStates(
                new Point(3, 4), new Point(4, 4),
                new Point(3, 5), new Point(4, 5),
                new Point(3, 6), new Point(4, 6),
                new Point(3, 7), new Point(4, 7),
                new Point(3, 8), new Point(4, 8));
    }

    @Test
    public void testMultiCellDragOneUp() {
        this.dataProvider.setDataValue(3, 4, "Homer");
        this.dataProvider.setDataValue(4, 4, "Simpson");
        this.dataProvider.setDataValue(3, 5, "Ned");
        this.dataProvider.setDataValue(4, 5, "Flanders");

        this.selectionLayer.selectCell(3, 4, false, true);
        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(3, 5, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        testCellStates(new Point(3, 4), new Point(4, 4), new Point(3, 5), new Point(4, 5));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(3, 3, 2, 3));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.UP, this.natTable.getConfigRegistry()));

        assertEquals("Ned", this.selectionLayer.getDataValueByPosition(3, 3));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 3));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("Ned", this.selectionLayer.getDataValueByPosition(3, 5));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 5));

        testCellStates(
                new Point(3, 3), new Point(4, 3),
                new Point(3, 4), new Point(4, 4),
                new Point(3, 5), new Point(4, 5));
    }

    @Test
    public void testMultiCellDragThreeUp() {
        this.dataProvider.setDataValue(3, 4, "Homer");
        this.dataProvider.setDataValue(4, 4, "Simpson");
        this.dataProvider.setDataValue(3, 5, "Ned");
        this.dataProvider.setDataValue(4, 5, "Flanders");

        this.selectionLayer.selectCell(3, 4, false, true);
        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(3, 5, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        testCellStates(new Point(3, 4), new Point(4, 4), new Point(3, 5), new Point(4, 5));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(3, 1, 2, 5));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.UP, this.natTable.getConfigRegistry()));

        assertEquals("Ned", this.selectionLayer.getDataValueByPosition(3, 1));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 1));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 2));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 2));
        assertEquals("Ned", this.selectionLayer.getDataValueByPosition(3, 3));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 3));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("Ned", this.selectionLayer.getDataValueByPosition(3, 5));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 5));

        testCellStates(
                new Point(3, 1), new Point(4, 1),
                new Point(3, 2), new Point(4, 2),
                new Point(3, 3), new Point(4, 3),
                new Point(3, 4), new Point(4, 4),
                new Point(3, 5), new Point(4, 5));
    }

    @Test
    public void testMultiCellDragOneRight() {
        this.dataProvider.setDataValue(3, 4, "Homer");
        this.dataProvider.setDataValue(4, 4, "Simpson");
        this.dataProvider.setDataValue(3, 5, "Ned");
        this.dataProvider.setDataValue(4, 5, "Flanders");

        this.selectionLayer.selectCell(3, 4, false, true);
        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(3, 5, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        testCellStates(new Point(3, 4), new Point(4, 4), new Point(3, 5), new Point(4, 5));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(3, 4, 3, 2));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals("Ned", this.selectionLayer.getDataValueByPosition(3, 5));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals("Ned", this.selectionLayer.getDataValueByPosition(5, 5));

        testCellStates(
                new Point(3, 4), new Point(4, 4), new Point(5, 4),
                new Point(3, 5), new Point(4, 5), new Point(5, 5));
    }

    @Test
    public void testMultiCellDragThreeRight() {
        this.dataProvider.setDataValue(3, 4, "Homer");
        this.dataProvider.setDataValue(4, 4, "Simpson");
        this.dataProvider.setDataValue(3, 5, "Ned");
        this.dataProvider.setDataValue(4, 5, "Flanders");

        this.selectionLayer.selectCell(3, 4, false, true);
        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(3, 5, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        testCellStates(new Point(3, 4), new Point(4, 4), new Point(3, 5), new Point(4, 5));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(3, 4, 5, 2));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.RIGHT, this.natTable.getConfigRegistry()));

        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(5, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(6, 4));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(7, 4));
        assertEquals("Ned", this.selectionLayer.getDataValueByPosition(3, 5));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 5));
        assertEquals("Ned", this.selectionLayer.getDataValueByPosition(5, 5));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(6, 5));
        assertEquals("Ned", this.selectionLayer.getDataValueByPosition(7, 5));

        testCellStates(
                new Point(3, 4), new Point(4, 4), new Point(5, 4), new Point(6, 4), new Point(7, 4),
                new Point(3, 5), new Point(4, 5), new Point(5, 5), new Point(6, 5), new Point(7, 5));
    }

    @Test
    public void testMultiCellDragOneLeft() {
        this.dataProvider.setDataValue(3, 4, "Homer");
        this.dataProvider.setDataValue(4, 4, "Simpson");
        this.dataProvider.setDataValue(3, 5, "Ned");
        this.dataProvider.setDataValue(4, 5, "Flanders");

        this.selectionLayer.selectCell(3, 4, false, true);
        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(3, 5, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        testCellStates(new Point(3, 4), new Point(4, 4), new Point(3, 5), new Point(4, 5));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(2, 4, 3, 2));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.LEFT, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(2, 4));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(2, 5));
        assertEquals("Ned", this.selectionLayer.getDataValueByPosition(3, 5));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 5));

        testCellStates(
                new Point(2, 4), new Point(3, 4), new Point(4, 4),
                new Point(2, 5), new Point(3, 5), new Point(4, 5));
    }

    @Test
    public void testMultiCellDragThreeLeft() {
        this.dataProvider.setDataValue(3, 4, "Homer");
        this.dataProvider.setDataValue(4, 4, "Simpson");
        this.dataProvider.setDataValue(3, 5, "Ned");
        this.dataProvider.setDataValue(4, 5, "Flanders");

        this.selectionLayer.selectCell(3, 4, false, true);
        this.selectionLayer.selectCell(4, 4, false, true);
        this.selectionLayer.selectCell(3, 5, false, true);
        this.selectionLayer.selectCell(4, 5, false, true);

        testCellStates(new Point(3, 4), new Point(4, 4), new Point(3, 5), new Point(4, 5));

        this.natTable.doCommand(new CopyDataToClipboardCommand(
                "\t", //$NON-NLS-1$
                System.getProperty("line.separator"), //$NON-NLS-1$
                this.natTable.getConfigRegistry()));

        this.selectionLayer.setFillHandleRegion(new Rectangle(0, 4, 5, 2));
        this.natTable.doCommand(
                new FillHandlePasteCommand(FillHandleOperation.COPY, MoveDirectionEnum.LEFT, this.natTable.getConfigRegistry()));

        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(0, 4));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(1, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(2, 4));
        assertEquals("Homer", this.selectionLayer.getDataValueByPosition(3, 4));
        assertEquals("Simpson", this.selectionLayer.getDataValueByPosition(4, 4));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(0, 5));
        assertEquals("Ned", this.selectionLayer.getDataValueByPosition(1, 5));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(2, 5));
        assertEquals("Ned", this.selectionLayer.getDataValueByPosition(3, 5));
        assertEquals("Flanders", this.selectionLayer.getDataValueByPosition(4, 5));

        testCellStates(
                new Point(0, 4), new Point(1, 4), new Point(2, 4), new Point(3, 4), new Point(4, 4),
                new Point(0, 5), new Point(1, 5), new Point(2, 5), new Point(3, 5), new Point(4, 5));
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
