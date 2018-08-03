/*****************************************************************************
 * Copyright (c) 2017, 2018 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hideshow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.dataset.person.Person;
import org.eclipse.nebula.widgets.nattable.dataset.person.PersonService;
import org.eclipse.nebula.widgets.nattable.grid.command.ClientAreaResizeCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.indicator.HideIndicatorConstants;
import org.eclipse.nebula.widgets.nattable.layer.AbstractDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IDpiConverter;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.command.ConfigureScalingCommand;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEvent;
import org.eclipse.nebula.widgets.nattable.search.strategy.ISearchStrategy;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Before;
import org.junit.Test;

public class ResizeColumnHideShowLayerTest {

    IRowDataProvider<Person> bodyDataProvider =
            new ListDataProvider<>(
                    PersonService.getPersons(10),
                    new ReflectiveColumnPropertyAccessor<Person>(
                            new String[] { "firstName", "lastName", "gender", "married", "birthday" }));
    DataLayer bodyDataLayer = new DataLayer(this.bodyDataProvider);

    ColumnReorderLayer reorderLayer;
    ResizeColumnHideShowLayer hideShowLayer;

    LayerListenerFixture listener;

    @Before
    public void setup() {
        this.reorderLayer = new ColumnReorderLayer(this.bodyDataLayer);
        this.hideShowLayer = new ResizeColumnHideShowLayer(this.reorderLayer, this.bodyDataLayer);
        this.listener = new LayerListenerFixture();
        this.hideShowLayer.addLayerListener(this.listener);
    }

    // test hide with default only set columns
    @Test
    public void testHideShowDefaultSizedColumns() {
        assertEquals(500, this.hideShowLayer.getWidth());

        assertTrue(this.bodyDataLayer.isColumnPositionResizable(1));
        assertTrue(this.bodyDataLayer.isColumnPositionResizable(4));

        this.hideShowLayer.hideColumnPositions(1, 4);

        assertEquals(2, this.listener.getReceivedEvents().size());
        assertEquals(new Range(1, 2), ((ColumnResizeEvent) this.listener.getReceivedEvents().get(0)).getColumnDiffs().iterator().next().getAfterPositionRange());
        assertEquals(new Range(4, 5), ((ColumnResizeEvent) this.listener.getReceivedEvents().get(1)).getColumnDiffs().iterator().next().getAfterPositionRange());

        assertEquals(300, this.hideShowLayer.getWidth());
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(4));
        assertEquals(2, this.hideShowLayer.getHiddenColumnIndexes().size());
        assertTrue("1 is not contained in hidden column indexes", this.hideShowLayer.getHiddenColumnIndexes().contains(Integer.valueOf(1)));
        assertTrue("4 is not contained in hidden column indexes", this.hideShowLayer.getHiddenColumnIndexes().contains(Integer.valueOf(4)));

        assertFalse(this.bodyDataLayer.isColumnPositionResizable(1));
        assertFalse(this.bodyDataLayer.isColumnPositionResizable(4));

        this.hideShowLayer.showAllColumns();

        assertEquals(4, this.listener.getReceivedEvents().size());
        assertEquals(new Range(1, 2), ((ColumnResizeEvent) this.listener.getReceivedEvents().get(2)).getColumnDiffs().iterator().next().getAfterPositionRange());
        assertEquals(new Range(4, 5), ((ColumnResizeEvent) this.listener.getReceivedEvents().get(3)).getColumnDiffs().iterator().next().getAfterPositionRange());

        assertEquals(500, this.hideShowLayer.getWidth());
        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(4));
        assertTrue("hidden column indexes are not empty", this.hideShowLayer.getHiddenColumnIndexes().isEmpty());

        assertTrue(this.bodyDataLayer.isColumnPositionResizable(1));
        assertTrue(this.bodyDataLayer.isColumnPositionResizable(4));
    }

    @Test
    public void testColumnPositionAfterHide() {
        this.bodyDataLayer.setColumnPercentageSizing(true);

        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 1000, 100));
        this.hideShowLayer.doCommand(cmd);

        assertEquals(1000, this.hideShowLayer.getWidth());
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.hideColumnPositions(1, 2, 3);

        assertEquals(1000, this.hideShowLayer.getWidth());
        assertEquals(500, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(500, this.hideShowLayer.getColumnWidthByPosition(4));

        assertEquals(0, this.hideShowLayer.getColumnPositionByX(480));
    }

    // test hide with custom sized columns
    @Test
    public void testHideShowCustomSizedColumns() {
        this.bodyDataLayer.setColumnWidthByPosition(0, 10);
        this.bodyDataLayer.setColumnWidthByPosition(1, 20);
        this.bodyDataLayer.setColumnWidthByPosition(2, 30);
        this.bodyDataLayer.setColumnWidthByPosition(3, 40);
        this.bodyDataLayer.setColumnWidthByPosition(4, 50);

        assertEquals(150, this.hideShowLayer.getWidth());

        this.hideShowLayer.hideColumnPositions(1, 4);

        assertEquals(80, this.hideShowLayer.getWidth());
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showAllColumns();

        assertEquals(150, this.hideShowLayer.getWidth());
        assertEquals(20, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(50, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    // test hide with not resizable column
    @Test
    public void testHideShowNotResizableColumns() {
        assertEquals(500, this.hideShowLayer.getWidth());

        this.bodyDataLayer.setColumnPositionResizable(1, false);
        this.bodyDataLayer.setColumnPositionResizable(4, false);

        this.hideShowLayer.hideColumnPositions(1, 4);

        assertEquals(300, this.hideShowLayer.getWidth());
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showAllColumns();

        assertEquals(500, this.hideShowLayer.getWidth());
        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(4));

        assertFalse(this.bodyDataLayer.isColumnPositionResizable(1));
        assertFalse(this.bodyDataLayer.isColumnPositionResizable(4));
    }

    // test persistence
    @Test
    public void testHideShowSaveState() {
        assertEquals(500, this.hideShowLayer.getWidth());

        this.bodyDataLayer.setColumnWidthByPosition(1, 50);
        this.bodyDataLayer.setColumnPositionResizable(4, false);

        assertTrue(this.bodyDataLayer.isColumnPositionResizable(1));
        assertFalse(this.bodyDataLayer.isColumnPositionResizable(4));

        this.hideShowLayer.hideColumnPositions(1, 4);

        assertEquals(300, this.hideShowLayer.getWidth());
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(4));

        assertFalse(this.bodyDataLayer.isColumnPositionResizable(1));
        assertFalse(this.bodyDataLayer.isColumnPositionResizable(4));

        // test save state
        Properties props = new Properties();
        this.hideShowLayer.saveState("hidden", props);

        String hidden = props.getProperty("hidden" + ResizeColumnHideShowLayer.PERSISTENCE_KEY_HIDDEN_COLUMNS);
        assertNotNull(hidden);
        assertEquals("1:[50|-1|true|false|-1.0],4:[-1|-1|false|false|-1.0],", hidden);
    }

    @Test
    public void testHideShowSaveStatePercentageSizing() {
        // enable percentage sizing
        this.bodyDataLayer.setColumnPercentageSizing(true);
        this.bodyDataLayer.setDefaultMinColumnWidth(10);
        this.bodyDataLayer.setMinColumnWidth(4, 20);

        // trigger percentage calculation
        ClientAreaResizeCommand resizeCommand = new ClientAreaResizeCommand(null);
        resizeCommand.setCalcArea(new Rectangle(0, 0, 500, 500));
        this.hideShowLayer.doCommand(resizeCommand);

        assertEquals(500, this.hideShowLayer.getWidth());

        // this causes the calculation of percentage values for all columns
        // (20%) then column 1 gets 10 percent and column 2 gets 30%
        this.bodyDataLayer.setColumnWidthByPosition(1, 50);
        this.bodyDataLayer.setColumnPositionResizable(4, false);

        assertTrue(this.bodyDataLayer.isColumnPositionResizable(1));
        assertFalse(this.bodyDataLayer.isColumnPositionResizable(4));

        this.hideShowLayer.hideColumnPositions(1, 4);

        assertEquals(500, this.hideShowLayer.getWidth());
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(4));

        assertFalse(this.bodyDataLayer.isColumnPositionResizable(1));
        assertFalse(this.bodyDataLayer.isColumnPositionResizable(4));

        // test save state
        Properties props = new Properties();
        this.hideShowLayer.saveState("hidden", props);

        String hidden = props.getProperty("hidden" + ResizeColumnHideShowLayer.PERSISTENCE_KEY_HIDDEN_COLUMNS);
        assertNotNull(hidden);
        assertEquals("1:[-1|-1|true|true|10.0],4:[-1|20|false|true|20.0],", hidden);
    }

    @Test
    public void testHideShowSaveStatePercentageSizingWithoutFixOnResize() {
        // enable percentage sizing
        this.bodyDataLayer.setColumnPercentageSizing(true);
        this.bodyDataLayer.setFixColumnPercentageValuesOnResize(false);
        this.bodyDataLayer.setDefaultMinColumnWidth(10);
        this.bodyDataLayer.setMinColumnWidth(4, 20);

        // trigger percentage calculation
        ClientAreaResizeCommand resizeCommand = new ClientAreaResizeCommand(null);
        resizeCommand.setCalcArea(new Rectangle(0, 0, 500, 500));
        this.hideShowLayer.doCommand(resizeCommand);

        assertEquals(500, this.hideShowLayer.getWidth());

        // this results in 10 percent
        this.bodyDataLayer.setColumnWidthByPosition(1, 50);
        this.bodyDataLayer.setColumnPositionResizable(4, false);

        assertTrue(this.bodyDataLayer.isColumnPositionResizable(1));
        assertFalse(this.bodyDataLayer.isColumnPositionResizable(4));

        this.hideShowLayer.hideColumnPositions(1, 4);

        assertEquals(500, this.hideShowLayer.getWidth());
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(4));

        assertFalse(this.bodyDataLayer.isColumnPositionResizable(1));
        assertFalse(this.bodyDataLayer.isColumnPositionResizable(4));

        // test save state
        Properties props = new Properties();
        this.hideShowLayer.saveState("hidden", props);

        String hidden = props.getProperty("hidden" + ResizeColumnHideShowLayer.PERSISTENCE_KEY_HIDDEN_COLUMNS);
        assertNotNull(hidden);
        assertEquals("1:[-1|-1|true|true|10.0],4:[-1|20|false|true|-1.0],", hidden);
    }

    @Test
    public void testHideShowLoadState() {
        this.bodyDataLayer.setColumnWidthByPosition(1, 50);
        this.bodyDataLayer.setColumnPositionResizable(4, false);
        this.hideShowLayer.hideColumnPositions(1, 4);

        // save the state
        Properties props = new Properties();
        this.hideShowLayer.saveState("hidden", props);

        // show all columns again
        this.hideShowLayer.showAllColumns();

        assertEquals(450, this.hideShowLayer.getWidth());
        assertEquals(50, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(4));
        assertTrue("hidden column indexes are not empty", this.hideShowLayer.getHiddenColumnIndexes().isEmpty());

        assertTrue(this.bodyDataLayer.isColumnPositionResizable(1));
        assertFalse(this.bodyDataLayer.isColumnPositionResizable(4));

        // apply the state and verify if the columns are hidden again
        this.hideShowLayer.loadState("hidden", props);

        assertEquals(300, this.hideShowLayer.getWidth());
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(4));
        assertEquals(2, this.hideShowLayer.getHiddenColumnIndexes().size());
        assertTrue("1 is not contained in hidden column indexes", this.hideShowLayer.getHiddenColumnIndexes().contains(Integer.valueOf(1)));
        assertTrue("4 is not contained in hidden column indexes", this.hideShowLayer.getHiddenColumnIndexes().contains(Integer.valueOf(4)));

        assertFalse(this.bodyDataLayer.isColumnPositionResizable(1));
        assertFalse(this.bodyDataLayer.isColumnPositionResizable(4));

        // show all columns again
        this.hideShowLayer.showAllColumns();

        // verify that the base configuration is restored correctly
        assertEquals(450, this.hideShowLayer.getWidth());
        assertEquals(50, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(4));
        assertTrue("hidden column indexes are not empty", this.hideShowLayer.getHiddenColumnIndexes().isEmpty());

        assertTrue(this.bodyDataLayer.isColumnPositionResizable(1));
        assertFalse(this.bodyDataLayer.isColumnPositionResizable(4));
    }

    @Test
    public void testHideShowLoadStatePercentageSizing() {
        // enable percentage sizing
        this.bodyDataLayer.setColumnPercentageSizing(true);

        // trigger percentage calculation
        ClientAreaResizeCommand resizeCommand = new ClientAreaResizeCommand(null);
        resizeCommand.setCalcArea(new Rectangle(0, 0, 600, 600));
        this.hideShowLayer.doCommand(resizeCommand);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(4));

        Properties defaultState = new Properties();
        this.hideShowLayer.saveState("default", defaultState);

        this.hideShowLayer.hideColumnPositions(1, 4);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(4));

        assertEquals(2, this.hideShowLayer.getHiddenColumnIndexes().size());
        assertTrue("1 is not contained in hidden column indexes", this.hideShowLayer.getHiddenColumnIndexes().contains(Integer.valueOf(1)));
        assertTrue("4 is not contained in hidden column indexes", this.hideShowLayer.getHiddenColumnIndexes().contains(Integer.valueOf(4)));

        // save the state
        Properties hiddenState = new Properties();
        this.hideShowLayer.saveState("hidden", hiddenState);

        // load default state again
        this.hideShowLayer.loadState("default", defaultState);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(4));
        assertTrue("hidden column indexes are not empty", this.hideShowLayer.getHiddenColumnIndexes().isEmpty());

        this.hideShowLayer.loadState("hidden", hiddenState);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(4));

        assertEquals(2, this.hideShowLayer.getHiddenColumnIndexes().size());
        assertTrue("1 is not contained in hidden column indexes", this.hideShowLayer.getHiddenColumnIndexes().contains(Integer.valueOf(1)));
        assertTrue("4 is not contained in hidden column indexes", this.hideShowLayer.getHiddenColumnIndexes().contains(Integer.valueOf(4)));
    }

    // test hide with mixed sized where last column
    // should take remaining space
    @Test
    public void testHideShowDefaultColumnWithPercentageLastColumn() {
        // the last column should take the remaining space
        this.bodyDataLayer.setColumnPercentageSizing(4, true);

        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 600, 100));

        this.hideShowLayer.doCommand(cmd);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.hideColumnPositions(1);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(300, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showAllColumns();

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void testHideShowCustomColumnWithPercentageLastColumn() {
        // change width of the column that gets hidden
        this.bodyDataLayer.setColumnWidthByPosition(1, 50);
        // the last column should take the remaining space
        this.bodyDataLayer.setColumnPercentageSizing(4, true);

        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 600, 100));
        this.hideShowLayer.doCommand(cmd);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(250, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.hideColumnPositions(1);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(300, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showAllColumns();

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(250, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    // test hide with percentage sized columns (not fixed values)
    @Test
    public void testHideShowPercentageSizedColumns() {
        this.bodyDataLayer.setColumnPercentageSizing(true);

        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 600, 100));
        this.hideShowLayer.doCommand(cmd);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.hideColumnPositions(2);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showAllColumns();

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    // test hide with all percentage with fixed percentage values
    @Test
    public void testHideShowFixedPercentageSizedColumns() {
        this.bodyDataLayer.setColumnWidthPercentageByPosition(0, 15);
        this.bodyDataLayer.setColumnWidthPercentageByPosition(1, 15);
        this.bodyDataLayer.setColumnWidthPercentageByPosition(2, 15);
        this.bodyDataLayer.setColumnWidthPercentageByPosition(3, 15);
        this.bodyDataLayer.setColumnWidthPercentageByPosition(4, 40);

        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 600, 100));
        this.hideShowLayer.doCommand(cmd);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(240, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.hideColumnPositions(1, 2);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(129, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(129, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(342, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showAllColumns();

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(240, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    // test showing specific columns again
    @Test
    public void testShowSpecificColumnsFixedWidth() {
        assertEquals(500, this.hideShowLayer.getWidth());

        assertTrue(this.bodyDataLayer.isColumnPositionResizable(1));
        assertTrue(this.bodyDataLayer.isColumnPositionResizable(4));

        this.hideShowLayer.hideColumnPositions(1, 4);

        assertEquals(2, this.listener.getReceivedEvents().size());
        assertEquals(new Range(1, 2), ((ColumnResizeEvent) this.listener.getReceivedEvents().get(0)).getColumnDiffs().iterator().next().getAfterPositionRange());
        assertEquals(new Range(4, 5), ((ColumnResizeEvent) this.listener.getReceivedEvents().get(1)).getColumnDiffs().iterator().next().getAfterPositionRange());

        assertEquals(300, this.hideShowLayer.getWidth());
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(4));

        assertFalse(this.bodyDataLayer.isColumnPositionResizable(1));
        assertFalse(this.bodyDataLayer.isColumnPositionResizable(4));

        this.hideShowLayer.showColumnIndexes(1);

        assertEquals(3, this.listener.getReceivedEvents().size());
        assertEquals(new Range(1, 2), ((ColumnResizeEvent) this.listener.getReceivedEvents().get(2)).getColumnDiffs().iterator().next().getAfterPositionRange());

        assertEquals(400, this.hideShowLayer.getWidth());
        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(4));

        assertTrue(this.bodyDataLayer.isColumnPositionResizable(1));
        assertFalse(this.bodyDataLayer.isColumnPositionResizable(4));
    }

    @Test
    public void testShowSpecificColumnsPercentageWidth() {
        this.bodyDataLayer.setColumnPercentageSizing(true);

        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 1500, 100));
        this.hideShowLayer.doCommand(cmd);

        assertEquals(1500, this.hideShowLayer.getWidth());
        assertEquals(300, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(300, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(300, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(300, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(300, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.hideColumnPositions(1, 4);

        assertEquals(1500, this.hideShowLayer.getWidth());
        assertEquals(500, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(500, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(500, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showColumnIndexes(1);

        assertEquals(1500, this.hideShowLayer.getWidth());
        assertEquals(375, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(375, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(375, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(375, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void testHideShowFixedPercentageSizedColumnsByIndex() {
        this.bodyDataLayer.setColumnWidthPercentageByPosition(0, 15);
        this.bodyDataLayer.setColumnWidthPercentageByPosition(1, 15);
        this.bodyDataLayer.setColumnWidthPercentageByPosition(2, 15);
        this.bodyDataLayer.setColumnWidthPercentageByPosition(3, 15);
        this.bodyDataLayer.setColumnWidthPercentageByPosition(4, 40);

        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 600, 100));
        this.hideShowLayer.doCommand(cmd);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(240, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.hideColumnPositions(1, 2);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(129, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(129, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(342, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showColumnIndexes(1, 2);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(240, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void testHideShowPercentageSizedColumnsAndDefaultMinWidth() {
        this.bodyDataLayer.setColumnPercentageSizing(true);
        this.bodyDataLayer.setDefaultMinColumnWidth(10);

        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 600, 100));
        this.hideShowLayer.doCommand(cmd);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.hideColumnPositions(2);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showAllColumns();

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void testHideShowPercentageSizedColumnsAndSpecificMinWidth() {
        this.bodyDataLayer.setColumnPercentageSizing(true);
        this.bodyDataLayer.setMinColumnWidth(2, 20);

        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 600, 100));
        this.hideShowLayer.doCommand(cmd);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.hideColumnPositions(2);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showAllColumns();

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void testHideResizeShowSpecificColumn() {
        // change width of the column that gets hidden
        this.bodyDataLayer.setColumnWidthByPosition(1, 50);
        // the last column should take the remaining space
        this.bodyDataLayer.setColumnPercentageSizing(4, true);

        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 600, 100));
        this.hideShowLayer.doCommand(cmd);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(250, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.hideColumnPositions(1);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(300, this.hideShowLayer.getColumnWidthByPosition(4));

        // resize a column
        this.bodyDataLayer.setColumnWidthByPosition(0, 50);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(350, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showAllColumns();

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(300, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void testHideResizeShowSpecificColumn2() {
        // the last two columns should take the remaining space
        this.bodyDataLayer.setColumnPercentageSizing(3, true);
        this.bodyDataLayer.setColumnPercentageSizing(4, true);

        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 600, 100));
        this.hideShowLayer.doCommand(cmd);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.hideColumnPositions(1);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(4));

        // resize a column
        this.bodyDataLayer.setColumnWidthByPosition(0, 40);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(260, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showAllColumns();

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(203, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(157, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void testHideResizeShowSpecificColumn3() {
        // specify a default min width
        this.bodyDataLayer.setDefaultMinColumnWidth(25);
        // the last two columns should take the remaining space
        this.bodyDataLayer.setColumnPercentageSizing(3, true);
        this.bodyDataLayer.setColumnPercentageSizing(4, true);

        this.bodyDataLayer.setColumnWidthByPosition(1, 300);

        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 600, 100));
        this.hideShowLayer.doCommand(cmd);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(50, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(50, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.hideColumnPositions(1);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(4));

        // resize a column
        this.bodyDataLayer.setColumnWidthByPosition(0, 200);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showAllColumns();

        assertEquals(650, this.hideShowLayer.getWidth());
        assertEquals(25, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(25, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void testHideResizeShowSpecificColumn4() {
        // specify a default min width
        this.bodyDataLayer.setDefaultMinColumnWidth(25);
        // the last two columns should take the remaining space
        this.bodyDataLayer.setColumnPercentageSizing(3, true);
        this.bodyDataLayer.setColumnPercentageSizing(4, true);

        this.bodyDataLayer.setColumnWidthByPosition(1, 300);

        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 600, 100));
        this.hideShowLayer.doCommand(cmd);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(50, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(50, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.hideColumnPositions(1);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(4));

        // resize a column
        this.bodyDataLayer.setColumnWidthByPosition(0, 180);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(200, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showAllColumns();

        assertEquals(630, this.hideShowLayer.getWidth());
        assertEquals(25, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(25, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void testHideResizeShowFixedPercentageSizedColumns() {
        this.bodyDataLayer.setColumnWidthPercentageByPosition(0, 15);
        this.bodyDataLayer.setColumnWidthPercentageByPosition(1, 15);
        this.bodyDataLayer.setColumnWidthPercentageByPosition(2, 15);
        this.bodyDataLayer.setColumnWidthPercentageByPosition(3, 15);
        this.bodyDataLayer.setColumnWidthPercentageByPosition(4, 40);

        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 600, 100));
        this.hideShowLayer.doCommand(cmd);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(240, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.hideColumnPositions(1, 2);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(129, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(129, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(342, this.hideShowLayer.getColumnWidthByPosition(4));

        // resize a column - minor rounding issues in double to int
        this.bodyDataLayer.setColumnWidthByPosition(0, 99);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(159, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(341, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showAllColumns();

        // with some rounding issues and corrections this will be the result
        // at least all columns in an appropriate ratio are visible again
        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(77, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(70, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(69, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(122, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(262, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void testHideResizeShowDynamicPercentageSizedColumns() {
        this.bodyDataLayer.setColumnPercentageSizing(true);

        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 600, 100));
        this.hideShowLayer.doCommand(cmd);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.hideColumnPositions(2);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(4));

        // resize first column to 100
        this.bodyDataLayer.setColumnWidthByPosition(0, 100);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(101, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(199, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showAllColumns();

        // because of the resize the current visible percentage columns are
        // fixed, showing the hidden column again makes it visible, but because
        // of a fixed percentage value it will only have the size of 1 pixel
        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(199, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(1, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void testHideResizeShowDynamicPercentageSizedColumnsWithMinSize() {
        this.bodyDataLayer.setColumnPercentageSizing(true);
        this.bodyDataLayer.setDefaultMinColumnWidth(25);

        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 600, 100));
        this.hideShowLayer.doCommand(cmd);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(120, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.hideColumnPositions(2);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(4));

        // resize first column to 100
        this.bodyDataLayer.setColumnWidthByPosition(0, 100);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(101, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(199, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(150, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showAllColumns();

        // because of the resize the current visible percentage columns are
        // fixed, showing the hidden column again makes it visible, and because
        // of the min size config, the other columns are updated to not exceed
        // 600
        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(96, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(191, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(25, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(144, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(144, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void testHideResizeShowMixedPercentageSizedColumns() {
        this.bodyDataLayer.setColumnPercentageSizing(true);
        this.bodyDataLayer.setColumnWidthPercentageByPosition(0, 15);
        this.bodyDataLayer.setColumnWidthPercentageByPosition(1, 15);
        this.bodyDataLayer.setColumnWidthPercentageByPosition(3, 15);
        this.bodyDataLayer.setColumnWidthPercentageByPosition(4, 15);

        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 600, 100));
        this.hideShowLayer.doCommand(cmd);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(240, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.hideColumnPositions(3);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(330, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(4));

        // resize a column
        this.bodyDataLayer.setColumnWidthByPosition(0, 100);

        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(80, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(330, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(90, this.hideShowLayer.getColumnWidthByPosition(4));

        this.hideShowLayer.showAllColumns();

        // with some rounding issues and corrections this will be the result
        // at least all columns in an appropriate ratio are visible again
        assertEquals(600, this.hideShowLayer.getWidth());
        assertEquals(87, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(70, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(287, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(78, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(78, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void testHiddenColumnsHaveSkipSearchLabel() {
        LabelStack labels = this.hideShowLayer.getConfigLabelsByPosition(1, 0);
        assertTrue("LabelStack not empty", labels.getLabels().isEmpty());

        this.hideShowLayer.hideColumnPositions(1, 4);

        labels = this.hideShowLayer.getConfigLabelsByPosition(1, 0);
        assertFalse("LabelStack not empty", labels.getLabels().isEmpty());
        assertTrue("skip search label not contained", labels.hasLabel(ISearchStrategy.SKIP_SEARCH_RESULT_LABEL));

        this.hideShowLayer.showAllColumns();

        labels = this.hideShowLayer.getConfigLabelsByPosition(1, 0);
        assertTrue("LabelStack not empty", labels.getLabels().isEmpty());
    }

    @Test
    public void testHideShowPercentageSizedColumnsAndSpecificMinWidthOnScaling() {
        // column width configuration
        this.bodyDataLayer.setColumnPercentageSizing(true);
        this.bodyDataLayer.setMinColumnWidth(2, 20);

        // enable scaling
        IDpiConverter dpiConverter = new AbstractDpiConverter() {

            @Override
            protected void readDpiFromDisplay() {
                this.dpi = 144;
            }

        };
        this.hideShowLayer.doCommand(new ConfigureScalingCommand(dpiConverter, dpiConverter));

        // trigger client area calculations
        ClientAreaResizeCommand cmd = new ClientAreaResizeCommand(null);
        cmd.setCalcArea(new Rectangle(0, 0, 900, 100));
        this.hideShowLayer.doCommand(cmd);

        assertEquals(900, this.hideShowLayer.getWidth());
        assertEquals(180, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(180, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(180, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(180, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(180, this.hideShowLayer.getColumnWidthByPosition(4));

        assertEquals(30, this.bodyDataLayer.getMinColumnWidth(2));

        this.hideShowLayer.hideColumnPositions(2);

        assertEquals(900, this.hideShowLayer.getWidth());
        assertEquals(225, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(225, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(225, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(225, this.hideShowLayer.getColumnWidthByPosition(4));

        assertEquals(0, this.bodyDataLayer.getMinColumnWidth(2));

        this.hideShowLayer.showAllColumns();

        assertEquals(900, this.hideShowLayer.getWidth());
        assertEquals(180, this.hideShowLayer.getColumnWidthByPosition(0));
        assertEquals(180, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(180, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(180, this.hideShowLayer.getColumnWidthByPosition(3));
        assertEquals(180, this.hideShowLayer.getColumnWidthByPosition(4));

        assertEquals(30, this.bodyDataLayer.getMinColumnWidth(2));
    }

    @Test
    public void shouldContainHideIndicatorLabels() {
        assertEquals(5, this.hideShowLayer.getColumnCount());

        this.hideShowLayer.hideColumnPositions(0);

        LabelStack configLabels = this.hideShowLayer.getConfigLabelsByPosition(1, 0);
        assertTrue(configLabels.hasLabel(HideIndicatorConstants.COLUMN_LEFT_HIDDEN));
        assertFalse(configLabels.hasLabel(HideIndicatorConstants.COLUMN_RIGHT_HIDDEN));

        this.hideShowLayer.hideColumnPositions(2);

        configLabels = this.hideShowLayer.getConfigLabelsByPosition(1, 0);
        assertTrue(configLabels.hasLabel(HideIndicatorConstants.COLUMN_LEFT_HIDDEN));
        assertTrue(configLabels.hasLabel(HideIndicatorConstants.COLUMN_RIGHT_HIDDEN));

        this.hideShowLayer.showColumnIndexes(0);

        configLabels = this.hideShowLayer.getConfigLabelsByPosition(1, 0);
        assertFalse(configLabels.hasLabel(HideIndicatorConstants.COLUMN_LEFT_HIDDEN));
        assertTrue(configLabels.hasLabel(HideIndicatorConstants.COLUMN_RIGHT_HIDDEN));
    }

    @Test
    public void shouldShowColumnPosition() {
        prepareReorderAndHide();

        this.hideShowLayer.showColumnPosition(3, true, false);
        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(2));

        this.hideShowLayer.showColumnPosition(3, false, false);
        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void showAllColumnPositionsOnLeftSide() {
        prepareReorderAndHide();

        // hide additional column
        this.hideShowLayer.hideColumnPositions(1);

        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));

        this.hideShowLayer.showColumnPosition(3, true, false);

        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(2));

        // hide again
        this.hideShowLayer.hideColumnPositions(2);

        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));

        // now show all columns to the left
        this.hideShowLayer.showColumnPosition(3, true, true);

        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(2));
    }

    @Test
    public void showAllColumnPositionsOnRightSide() {
        prepareReorderAndHide();

        // hide additional column
        this.hideShowLayer.hideColumnPositions(1);

        this.hideShowLayer.showColumnPosition(0, false, false);

        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));

        // hide again
        this.hideShowLayer.hideColumnPositions(1);

        // now show all columns to the right
        this.hideShowLayer.showColumnPosition(0, false, true);

        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(1));
        assertEquals(100, this.hideShowLayer.getColumnWidthByPosition(2));
    }

    @Test
    public void doNotShowIfNoDirectColumnHiddenToLeft() {
        prepareReorderAndHide();

        this.hideShowLayer.showColumnPosition(4, true, false);

        assertEquals(4, this.hideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.hideShowLayer.getColumnIndexByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnIndexByPosition(2));
        assertEquals(2, this.hideShowLayer.getColumnIndexByPosition(3));
        assertEquals(3, this.hideShowLayer.getColumnIndexByPosition(4));

        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    @Test
    public void doNotShowIfNoDirectColumnHiddenToRight() {
        prepareReorderAndHide();

        this.hideShowLayer.showColumnPosition(0, false, false);

        assertEquals(4, this.hideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.hideShowLayer.getColumnIndexByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnIndexByPosition(2));
        assertEquals(2, this.hideShowLayer.getColumnIndexByPosition(3));
        assertEquals(3, this.hideShowLayer.getColumnIndexByPosition(4));

        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(4));
    }

    private void prepareReorderAndHide() {
        // trigger reordering
        this.reorderLayer.reorderColumnPosition(4, 0); // 4 0 1 2 3
        this.reorderLayer.reorderColumnPosition(1, 4); // 4 1 2 0 3
        this.reorderLayer.reorderColumnPosition(3, 2); // 4 1 0 2 3

        // initial hide
        this.hideShowLayer.hideColumnPositions(2, 4);

        assertEquals(4, this.hideShowLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.hideShowLayer.getColumnIndexByPosition(1));
        assertEquals(0, this.hideShowLayer.getColumnIndexByPosition(2));
        assertEquals(2, this.hideShowLayer.getColumnIndexByPosition(3));
        assertEquals(3, this.hideShowLayer.getColumnIndexByPosition(4));

        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(2));
        assertEquals(0, this.hideShowLayer.getColumnWidthByPosition(4));
    }
}
