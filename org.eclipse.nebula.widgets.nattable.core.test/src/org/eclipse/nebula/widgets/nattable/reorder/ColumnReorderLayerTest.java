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
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 455949
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.reorder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.AggregateConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.reorder.command.ColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.reorder.command.ResetColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.command.LayerCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.BaseDataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ColumnReorderLayerTest {

    private IUniqueIndexLayer underlyingLayer;
    private ColumnReorderLayer columnReorderLayer;

    @Before
    public void setUp() {
        this.underlyingLayer = new BaseDataLayerFixture(4, 4);
        this.columnReorderLayer = new ColumnReorderLayer(this.underlyingLayer);
    }

    @Test
    public void reorderViewableColumnsRightToLeft() throws Exception {
        // 0 1 2 3
        assertEquals(0, this.columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(3, this.columnReorderLayer.getColumnIndexByPosition(3));

        // 3 0 1 2
        this.columnReorderLayer.reorderColumnPosition(3, 0);
        assertEquals(1, this.columnReorderLayer.getColumnPositionByIndex(0));
        assertEquals(0, this.columnReorderLayer.getColumnPositionByIndex(3));

        assertEquals(3, this.columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(2, this.columnReorderLayer.getColumnIndexByPosition(3));

        // 0 1 3 2
        this.columnReorderLayer.reorderColumnPosition(0, 3);
        assertEquals(0, this.columnReorderLayer.getColumnPositionByIndex(0));
        assertEquals(1, this.columnReorderLayer.getColumnPositionByIndex(1));
        assertEquals(2, this.columnReorderLayer.getColumnPositionByIndex(3));
        assertEquals(3, this.columnReorderLayer.getColumnPositionByIndex(2));

        assertEquals(0, this.columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(3, this.columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(2, this.columnReorderLayer.getColumnIndexByPosition(3));
    }

    @Test
    /**
     * 	Index		1	2	3	0
     *          --------------------
     *  Position 	0 	1	2	3
     */
    public void reorderViewableColumnsLeftToRightByPosition() throws Exception {
        // Moving to the end
        this.columnReorderLayer.reorderColumnPosition(0, 4);

        assertEquals(2, this.columnReorderLayer.getColumnPositionByIndex(3));
        assertEquals(3, this.columnReorderLayer.getColumnPositionByIndex(0));

        assertEquals(0, this.columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(1, this.columnReorderLayer.getColumnIndexByPosition(0));
    }

    @Test
    /**
     * 	Index		2 	0	1	3
     *          --------------------
     *  Position 	0 	1	2	3
     */
    public void reorderMultipleColumnsLeftToRight() throws Exception {
        this.columnReorderLayer.reorderMultipleColumnPositions(new int[] { 0, 1 }, 3);

        assertEquals(2, this.columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(0, this.columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(1, this.columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(3, this.columnReorderLayer.getColumnIndexByPosition(3));
    }

    @Test
    /**
     * 	Index		2 	3	0	1
     *          --------------------
     *  Position 	0 	1	2	3
     */
    public void reorderMultipleColumnsLeftToRightToTheEnd() throws Exception {
        this.columnReorderLayer.reorderMultipleColumnPositions(new int[] { 0, 1 }, 4);

        assertEquals(2, this.columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(3, this.columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(0, this.columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(1, this.columnReorderLayer.getColumnIndexByPosition(3));
    }

    @Test
    /**
     * 	Index		0	1	3	2
     *          --------------------
     *  Position 	0 	1	2	3
     */
    public void reorderViewableColumnsRightToLeftByPosition() throws Exception {
        this.columnReorderLayer.reorderColumnPosition(3, 2);

        assertEquals(2, this.columnReorderLayer.getColumnPositionByIndex(3));
        assertEquals(0, this.columnReorderLayer.getColumnPositionByIndex(0));

        assertEquals(2, this.columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(0, this.columnReorderLayer.getColumnIndexByPosition(0));
    }

    @Test
    /**
     * 	Index		2	3	0	1
     *          --------------------
     *  Position 	0 	1	2	3
     */
    public void reorderMultipleColumnsRightToLeft() throws Exception {
        List<Integer> fromColumnPositions = Arrays.asList(new Integer[] { 2, 3 });

        this.columnReorderLayer.reorderMultipleColumnPositions(fromColumnPositions, 0);

        assertEquals(2, this.columnReorderLayer.getColumnIndexByPosition(0));
        assertEquals(3, this.columnReorderLayer.getColumnIndexByPosition(1));
        assertEquals(0, this.columnReorderLayer.getColumnIndexByPosition(2));
        assertEquals(1, this.columnReorderLayer.getColumnIndexByPosition(3));
    }

    @Test
    /**
     * 	Index		2	3	0	1 ... 20
     *          --------------------
     *  Position 	0 	1	2	3 ... 20
     */
    public void reorderMultipleColumnsLargeArrayToEdges() throws Exception {

        ColumnReorderLayer reorderLayer = new ColumnReorderLayer(new BaseDataLayerFixture(20, 20));

        List<Integer> fromColumnPositions = Arrays.asList(new Integer[] { 10, 11, 12, 13 });

        reorderLayer.reorderMultipleColumnPositions(fromColumnPositions, 0);

        assertEquals(10, reorderLayer.getColumnIndexByPosition(0));
        assertEquals(11, reorderLayer.getColumnIndexByPosition(1));
        assertEquals(12, reorderLayer.getColumnIndexByPosition(2));
        assertEquals(13, reorderLayer.getColumnIndexByPosition(3));
        assertEquals(0, reorderLayer.getColumnIndexByPosition(4));

        fromColumnPositions = Arrays.asList(new Integer[] { 8, 9, 10, 11 });

        reorderLayer.reorderMultipleColumnPositions(fromColumnPositions, 8);

        assertEquals(4, reorderLayer.getColumnIndexByPosition(8));
        assertEquals(5, reorderLayer.getColumnIndexByPosition(9));
        assertEquals(6, reorderLayer.getColumnIndexByPosition(10));
        assertEquals(7, reorderLayer.getColumnIndexByPosition(11));

        fromColumnPositions = Arrays.asList(new Integer[] { 8, 9, 10, 11 });

        reorderLayer.reorderMultipleColumnPositions(fromColumnPositions, reorderLayer.getColumnCount());

        /*
         * System.out.println("\n"); // See output for idea on what is going on
         * for (int i = 0; i < reorderLayer.getColumnCount(); i++)
         * System.out.println(i + "\t" +
         * reorderLayer.getColumnIndexByPosition(i));
         */

        assertEquals(7, reorderLayer.getColumnIndexByPosition(19));
        assertEquals(6, reorderLayer.getColumnIndexByPosition(18));
        assertEquals(5, reorderLayer.getColumnIndexByPosition(17));
        assertEquals(4, reorderLayer.getColumnIndexByPosition(16));
    }

    @Test
    public void commandPassedOnToParentIfCannotBeHandled() throws Exception {
        ColumnReorderLayer reorderLayer = new ColumnReorderLayer(new DataLayerFixture());
        assertFalse(reorderLayer.doCommand(new LayerCommandFixture()));
    }

    @Test
    public void canHandleColumnReorderCommand() throws Exception {
        ColumnReorderLayer reorderLayer = new ColumnReorderLayer(new DataLayerFixture());
        ColumnReorderCommand reorderCommand = new ColumnReorderCommand(reorderLayer, 0, 2);
        assertTrue(reorderLayer.doCommand(reorderCommand));
    }

    @Test
    public void getWidthForReorderedColumns() throws Exception {
        this.underlyingLayer = new DataLayerFixture();
        this.columnReorderLayer = new ColumnReorderLayer(this.underlyingLayer);

        // 0 1 2 3 4 - see DataLayerFixture
        this.columnReorderLayer.reorderColumnPosition(0, 5);

        // 1 2 3 4 0
        Assert.assertEquals(100, this.columnReorderLayer.getColumnWidthByPosition(0));
        Assert.assertEquals(35, this.columnReorderLayer.getColumnWidthByPosition(1));
        Assert.assertEquals(100, this.columnReorderLayer.getColumnWidthByPosition(2));
        Assert.assertEquals(80, this.columnReorderLayer.getColumnWidthByPosition(3));
        Assert.assertEquals(150, this.columnReorderLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void getWidthForMultipleColumnsReordering() throws Exception {
        this.underlyingLayer = new DataLayerFixture();
        this.columnReorderLayer = new ColumnReorderLayer(this.underlyingLayer);

        // 0 1 2 3 4 - see DataLayerFixture
        this.columnReorderLayer.reorderMultipleColumnPositions(Arrays.asList(1, 2), 5);

        // 0 3 4 1 2
        assertEquals(150, this.columnReorderLayer.getColumnWidthByPosition(0));
        assertEquals(100, this.columnReorderLayer.getColumnWidthByPosition(1));
        assertEquals(80, this.columnReorderLayer.getColumnWidthByPosition(2));
        assertEquals(100, this.columnReorderLayer.getColumnWidthByPosition(3));
        assertEquals(35, this.columnReorderLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void getStartXForReorderedColumn() throws Exception {
        this.underlyingLayer = new DataLayerFixture();
        this.columnReorderLayer = new ColumnReorderLayer(this.underlyingLayer);

        // 0 1 2 3 4 - see DataLayerFixture
        this.columnReorderLayer.reorderColumnPosition(0, 5);

        // Index: 1 2 3 4 0 Width: 100 35 100 80 150
        assertEquals(0, this.columnReorderLayer.getStartXOfColumnPosition(0));
        assertEquals(100, this.columnReorderLayer.getStartXOfColumnPosition(1));
        assertEquals(135, this.columnReorderLayer.getStartXOfColumnPosition(2));
        assertEquals(235, this.columnReorderLayer.getStartXOfColumnPosition(3));
        assertEquals(315, this.columnReorderLayer.getStartXOfColumnPosition(4));
    }

    @Test
    public void getConfigLabelsByPosition() throws Exception {
        DataLayer underlyingLayer = new DataLayerFixture();
        this.columnReorderLayer = new ColumnReorderLayer(underlyingLayer);
        ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(underlyingLayer);
        registerCellStyleAccumulators(underlyingLayer, columnLabelAccumulator);

        columnLabelAccumulator.registerColumnOverrides(4, "INDEX_4_LABEL");

        List<String> labelsForIndex4 = this.columnReorderLayer.getConfigLabelsByPosition(4, 0);
        assertEquals(2, labelsForIndex4.size());
        assertEquals("INDEX_4_LABEL", labelsForIndex4.get(0));
        assertEquals("EVEN_BODY", labelsForIndex4.get(1));

        // 0 1 2 3 4 - see DataLayerFixture
        this.columnReorderLayer.reorderColumnPosition(0, 5);

        // Index: 1 2 3 4 0 Width: 100 35 100 80 150
        labelsForIndex4 = this.columnReorderLayer.getConfigLabelsByPosition(3, 0);
        assertEquals(2, labelsForIndex4.size());
        assertEquals("INDEX_4_LABEL", labelsForIndex4.get(0));
        assertEquals("EVEN_BODY", labelsForIndex4.get(1));
    }

    @Test
    public void shouldResetReordering() {
        this.columnReorderLayer.reorderColumnPosition(0, 4);

        assertEquals(2, this.columnReorderLayer.getColumnPositionByIndex(3));
        assertEquals(3, this.columnReorderLayer.getColumnPositionByIndex(0));

        assertEquals(0, this.columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(1, this.columnReorderLayer.getColumnIndexByPosition(0));

        this.columnReorderLayer.resetReorder();

        assertEquals(3, this.columnReorderLayer.getColumnPositionByIndex(3));
        assertEquals(0, this.columnReorderLayer.getColumnPositionByIndex(0));

        assertEquals(3, this.columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(0, this.columnReorderLayer.getColumnIndexByPosition(0));
    }

    @Test
    public void shouldResetReorderingViaCommand() {
        this.columnReorderLayer.reorderColumnPosition(0, 4);

        assertEquals(2, this.columnReorderLayer.getColumnPositionByIndex(3));
        assertEquals(3, this.columnReorderLayer.getColumnPositionByIndex(0));

        assertEquals(0, this.columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(1, this.columnReorderLayer.getColumnIndexByPosition(0));

        this.columnReorderLayer.doCommand(new ResetColumnReorderCommand());

        assertEquals(3, this.columnReorderLayer.getColumnPositionByIndex(3));
        assertEquals(0, this.columnReorderLayer.getColumnPositionByIndex(0));

        assertEquals(3, this.columnReorderLayer.getColumnIndexByPosition(3));
        assertEquals(0, this.columnReorderLayer.getColumnIndexByPosition(0));
    }

    private void registerCellStyleAccumulators(DataLayer bodyDataLayer, ColumnOverrideLabelAccumulator columnLabelAccumulator) {
        AggregateConfigLabelAccumulator aggregrateConfigLabelAccumulator = new AggregateConfigLabelAccumulator();
        aggregrateConfigLabelAccumulator.add(columnLabelAccumulator, new AlternatingRowConfigLabelAccumulator());
        bodyDataLayer.setConfigLabelAccumulator(aggregrateConfigLabelAccumulator);
    }

}
