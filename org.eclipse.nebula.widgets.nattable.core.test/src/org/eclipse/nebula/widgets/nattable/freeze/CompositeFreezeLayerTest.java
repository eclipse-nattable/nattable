/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.freeze;

import java.util.Arrays;

import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeColumnCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeSelectionCommand;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.test.LayerAssert;
import org.eclipse.nebula.widgets.nattable.test.fixture.InitializeClientAreaCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.TestLayer;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Before;
import org.junit.Test;

public class CompositeFreezeLayerTest {

    private ColumnReorderLayer reorderLayer;
    private ColumnHideShowLayer hideShowLayer;
    private SelectionLayer selectionLayer;
    private ViewportLayer viewportLayer;
    private FreezeLayer freezeLayer;
    private CompositeFreezeLayer compositeFreezeLayer;

    @Before
    public void setup() {
        TestLayer dataLayer = new TestLayer(4, 4,
                "0:0;100 | 1:1;100 | 2:2;100 | 3:3;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40", "A0 | B0 | C0 | D0 \n"
                        + "A1 | B1 | C1 | D1 \n" + "A2 | B2 | C2 | D2 \n"
                        + "A3 | B3 | C3 | D3 \n");

        this.reorderLayer = new ColumnReorderLayer(dataLayer);
        this.hideShowLayer = new ColumnHideShowLayer(this.reorderLayer);
        this.selectionLayer = new SelectionLayer(this.hideShowLayer);
        this.viewportLayer = new ViewportLayer(this.selectionLayer);
        this.freezeLayer = new FreezeLayer(this.selectionLayer);

        this.compositeFreezeLayer = new CompositeFreezeLayer(this.freezeLayer,
                this.viewportLayer, this.selectionLayer);
        this.compositeFreezeLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 400, 160);
            }

        });
        this.compositeFreezeLayer
                .doCommand(new InitializeClientAreaCommandFixture());
    }

    @Test
    public void testNotFrozen() {
        TestLayer expectedLayer = new TestLayer(
                4,
                4,
                "0:0;100 | 1:1;100 | 2:2;100 | 3:3;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
                "A0~:NONFROZEN_REGION | B0~:NONFROZEN_REGION | C0~:NONFROZEN_REGION | D0~:NONFROZEN_REGION \n"
                        + "A1~:NONFROZEN_REGION | B1~:NONFROZEN_REGION | C1~:NONFROZEN_REGION | D1~:NONFROZEN_REGION \n"
                        + "A2~:NONFROZEN_REGION | B2~:NONFROZEN_REGION | C2~:NONFROZEN_REGION | D2~:NONFROZEN_REGION \n"
                        + "A3~:NONFROZEN_REGION | B3~:NONFROZEN_REGION | C3~:NONFROZEN_REGION | D3~:NONFROZEN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

    // Freeze column

    @Test
    public void testFreezeAllColumns() {
        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                this.compositeFreezeLayer, 3));

        TestLayer expectedLayer = new TestLayer(
                4,
                4,
                "0:0;100 | 1:1;100 | 2:2;100 | 3:3;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
                "A0~:FROZEN_COLUMN_REGION | B0~:FROZEN_COLUMN_REGION | C0~:FROZEN_COLUMN_REGION | D0~:FROZEN_COLUMN_REGION \n"
                        + "A1~:FROZEN_COLUMN_REGION | B1~:FROZEN_COLUMN_REGION | C1~:FROZEN_COLUMN_REGION | D1~:FROZEN_COLUMN_REGION \n"
                        + "A2~:FROZEN_COLUMN_REGION | B2~:FROZEN_COLUMN_REGION | C2~:FROZEN_COLUMN_REGION | D2~:FROZEN_COLUMN_REGION \n"
                        + "A3~:FROZEN_COLUMN_REGION | B3~:FROZEN_COLUMN_REGION | C3~:FROZEN_COLUMN_REGION | D3~:FROZEN_COLUMN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

    @Test
    public void testFreezeColumns() {
        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                this.compositeFreezeLayer, 1));

        TestLayer expectedLayer = new TestLayer(
                4,
                4,
                "0:0;100 | 1:1;100 | 2:0;100 | 3:1;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
                "A0~:FROZEN_COLUMN_REGION | B0~:FROZEN_COLUMN_REGION | C0~:NONFROZEN_REGION | D0~:NONFROZEN_REGION \n"
                        + "A1~:FROZEN_COLUMN_REGION | B1~:FROZEN_COLUMN_REGION | C1~:NONFROZEN_REGION | D1~:NONFROZEN_REGION \n"
                        + "A2~:FROZEN_COLUMN_REGION | B2~:FROZEN_COLUMN_REGION | C2~:NONFROZEN_REGION | D2~:NONFROZEN_REGION \n"
                        + "A3~:FROZEN_COLUMN_REGION | B3~:FROZEN_COLUMN_REGION | C3~:NONFROZEN_REGION | D3~:NONFROZEN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

    // Freeze selection

    @Test
    public void testFreezeSelectionAtBeginning() {
        this.selectionLayer.setSelectedCell(2, 2);
        this.compositeFreezeLayer.doCommand(new FreezeSelectionCommand());

        TestLayer expectedLayer = new TestLayer(
                4,
                4,
                "0:0;100 | 1:1;100 | 2:0;100 | 3:1;100",
                "0:0;40  | 1:1;40  | 2:0;40  | 3:1;40",
                "A0~:FROZEN_REGION        | B0~:FROZEN_REGION        | C0~:FROZEN_ROW_REGION                      | D0~:FROZEN_ROW_REGION \n"
                        + "A1~:FROZEN_REGION        | B1~:FROZEN_REGION        | C1~:FROZEN_ROW_REGION                      | D1~:FROZEN_ROW_REGION \n"
                        + "A2~:FROZEN_COLUMN_REGION | B2~:FROZEN_COLUMN_REGION | C2~SELECT:selectionAnchor,NONFROZEN_REGION | D2~:NONFROZEN_REGION \n"
                        + "A3~:FROZEN_COLUMN_REGION | B3~:FROZEN_COLUMN_REGION | C3~:NONFROZEN_REGION                       | D3~:NONFROZEN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

    @Test
    public void testFreezeSelectionInMiddle() {
        this.compositeFreezeLayer.setClientAreaProvider(new IClientAreaProvider() {

            @Override
            public Rectangle getClientArea() {
                return new Rectangle(0, 0, 300, 120);
            }

        });

        this.selectionLayer.setSelectedCell(2, 2);
        this.viewportLayer.setOriginX(this.viewportLayer.getStartXOfColumnPosition(1));
        this.viewportLayer.setOriginY(this.viewportLayer.getStartYOfRowPosition(1));
        this.compositeFreezeLayer.doCommand(new FreezeSelectionCommand());

        TestLayer expectedLayer = new TestLayer(
                3,
                3,
                "1:0;100 | 2:0;100 | 3:1;100",
                "1:0;40  | 2:0;40  | 3:1;40",
                "B1~:FROZEN_REGION        | C1~:FROZEN_ROW_REGION                      | D1~:FROZEN_ROW_REGION \n"
                        + "B2~:FROZEN_COLUMN_REGION | C2~SELECT:selectionAnchor,NONFROZEN_REGION | D2~:NONFROZEN_REGION \n"
                        + "B3~:FROZEN_COLUMN_REGION | C3~:NONFROZEN_REGION                       | D3~:NONFROZEN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

    // Reorder

    @Test
    public void testReorderNonFrozenColumnToMiddleOfFrozenArea() {
        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                this.compositeFreezeLayer, 1));

        this.reorderLayer.reorderColumnPosition(3, 1);

        TestLayer expectedLayer = new TestLayer(
                4,
                4,
                "0:0;100 | 3:1;100 | 1:2;100 | 2:0;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
                "A0~:FROZEN_COLUMN_REGION | D0~:FROZEN_COLUMN_REGION | B0~:FROZEN_COLUMN_REGION | C0~:NONFROZEN_REGION \n"
                        + "A1~:FROZEN_COLUMN_REGION | D1~:FROZEN_COLUMN_REGION | B1~:FROZEN_COLUMN_REGION | C1~:NONFROZEN_REGION \n"
                        + "A2~:FROZEN_COLUMN_REGION | D2~:FROZEN_COLUMN_REGION | B2~:FROZEN_COLUMN_REGION | C2~:NONFROZEN_REGION \n"
                        + "A3~:FROZEN_COLUMN_REGION | D3~:FROZEN_COLUMN_REGION | B3~:FROZEN_COLUMN_REGION | C3~:NONFROZEN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

    @Test
    public void testReorderNonFrozenColumnToEndOfFrozenArea() {
        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                this.compositeFreezeLayer, 1));

        this.reorderLayer.reorderColumnPosition(3, 2);

        TestLayer expectedLayer = new TestLayer(
                4,
                4,
                "0:0;100 | 1:1;100 | 3:0;100 | 2:1;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
                "A0~:FROZEN_COLUMN_REGION | B0~:FROZEN_COLUMN_REGION | D0~:NONFROZEN_REGION | C0~:NONFROZEN_REGION \n"
                        + "A1~:FROZEN_COLUMN_REGION | B1~:FROZEN_COLUMN_REGION | D1~:NONFROZEN_REGION | C1~:NONFROZEN_REGION \n"
                        + "A2~:FROZEN_COLUMN_REGION | B2~:FROZEN_COLUMN_REGION | D2~:NONFROZEN_REGION | C2~:NONFROZEN_REGION \n"
                        + "A3~:FROZEN_COLUMN_REGION | B3~:FROZEN_COLUMN_REGION | D3~:NONFROZEN_REGION | C3~:NONFROZEN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

    @Test
    public void testReorderNonFrozenColumnToBeginningOfFrozenArea() {
        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                this.compositeFreezeLayer, 1));

        this.reorderLayer.reorderColumnPosition(3, 0);

        TestLayer expectedLayer = new TestLayer(
                4,
                4,
                "3:0;100 | 0:1;100 | 1:2;100 | 2:0;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
                "D0~:FROZEN_COLUMN_REGION | A0~:FROZEN_COLUMN_REGION | B0~:FROZEN_COLUMN_REGION | C0~:NONFROZEN_REGION \n"
                        + "D1~:FROZEN_COLUMN_REGION | A1~:FROZEN_COLUMN_REGION | B1~:FROZEN_COLUMN_REGION | C1~:NONFROZEN_REGION \n"
                        + "D2~:FROZEN_COLUMN_REGION | A2~:FROZEN_COLUMN_REGION | B2~:FROZEN_COLUMN_REGION | C2~:NONFROZEN_REGION \n"
                        + "D3~:FROZEN_COLUMN_REGION | A3~:FROZEN_COLUMN_REGION | B3~:FROZEN_COLUMN_REGION | C3~:NONFROZEN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

    @Test
    public void testReorderMiddleFrozenColumnToMiddleOfFrozenArea() {
        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                this.compositeFreezeLayer, 3));

        this.reorderLayer.reorderColumnPosition(2, 1);

        TestLayer expectedLayer = new TestLayer(
                4,
                4,
                "0:0;100 | 2:1;100 | 1:2;100 | 3:3;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
                "A0~:FROZEN_COLUMN_REGION | C0~:FROZEN_COLUMN_REGION | B0~:FROZEN_COLUMN_REGION | D0~:FROZEN_COLUMN_REGION \n"
                        + "A1~:FROZEN_COLUMN_REGION | C1~:FROZEN_COLUMN_REGION | B1~:FROZEN_COLUMN_REGION | D1~:FROZEN_COLUMN_REGION \n"
                        + "A2~:FROZEN_COLUMN_REGION | C2~:FROZEN_COLUMN_REGION | B2~:FROZEN_COLUMN_REGION | D2~:FROZEN_COLUMN_REGION \n"
                        + "A3~:FROZEN_COLUMN_REGION | C3~:FROZEN_COLUMN_REGION | B3~:FROZEN_COLUMN_REGION | D3~:FROZEN_COLUMN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

    @Test
    public void testReorderMiddleFrozenColumnToEndOfFrozenArea() {
        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                this.compositeFreezeLayer, 2));

        this.reorderLayer.reorderColumnPosition(1, 3);

        TestLayer expectedLayer = new TestLayer(
                4,
                4,
                "0:0;100 | 2:1;100 | 1:2;100 | 3:0;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
                "A0~:FROZEN_COLUMN_REGION | C0~:FROZEN_COLUMN_REGION | B0~:FROZEN_COLUMN_REGION | D0~:NONFROZEN_REGION \n"
                        + "A1~:FROZEN_COLUMN_REGION | C1~:FROZEN_COLUMN_REGION | B1~:FROZEN_COLUMN_REGION | D1~:NONFROZEN_REGION \n"
                        + "A2~:FROZEN_COLUMN_REGION | C2~:FROZEN_COLUMN_REGION | B2~:FROZEN_COLUMN_REGION | D2~:NONFROZEN_REGION \n"
                        + "A3~:FROZEN_COLUMN_REGION | C3~:FROZEN_COLUMN_REGION | B3~:FROZEN_COLUMN_REGION | D3~:NONFROZEN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

    @Test
    public void testReorderMiddleFrozenColumnToBeginningOfFrozenArea() {
        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                this.compositeFreezeLayer, 2));

        this.reorderLayer.reorderColumnPosition(1, 0);

        TestLayer expectedLayer = new TestLayer(
                4,
                4,
                "1:0;100 | 0:1;100 | 2:2;100 | 3:0;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
                "B0~:FROZEN_COLUMN_REGION | A0~:FROZEN_COLUMN_REGION | C0~:FROZEN_COLUMN_REGION | D0~:NONFROZEN_REGION \n"
                        + "B1~:FROZEN_COLUMN_REGION | A1~:FROZEN_COLUMN_REGION | C1~:FROZEN_COLUMN_REGION | D1~:NONFROZEN_REGION \n"
                        + "B2~:FROZEN_COLUMN_REGION | A2~:FROZEN_COLUMN_REGION | C2~:FROZEN_COLUMN_REGION | D2~:NONFROZEN_REGION \n"
                        + "B3~:FROZEN_COLUMN_REGION | A3~:FROZEN_COLUMN_REGION | C3~:FROZEN_COLUMN_REGION | D3~:NONFROZEN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

    @Test
    public void testReorderBeginningFrozenColumnToMiddleOfFrozenArea() {
        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                this.compositeFreezeLayer, 2));

        this.reorderLayer.reorderColumnPosition(0, 2);

        TestLayer expectedLayer = new TestLayer(
                4,
                4,
                "1:0;100 | 0:1;100 | 2:2;100 | 3:0;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
                "B0~:FROZEN_COLUMN_REGION | A0~:FROZEN_COLUMN_REGION | C0~:FROZEN_COLUMN_REGION | D0~:NONFROZEN_REGION \n"
                        + "B1~:FROZEN_COLUMN_REGION | A1~:FROZEN_COLUMN_REGION | C1~:FROZEN_COLUMN_REGION | D1~:NONFROZEN_REGION \n"
                        + "B2~:FROZEN_COLUMN_REGION | A2~:FROZEN_COLUMN_REGION | C2~:FROZEN_COLUMN_REGION | D2~:NONFROZEN_REGION \n"
                        + "B3~:FROZEN_COLUMN_REGION | A3~:FROZEN_COLUMN_REGION | C3~:FROZEN_COLUMN_REGION | D3~:NONFROZEN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

    @Test
    public void testReorderBeginningFrozenColumnToEndOfFrozenArea() {
        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                this.compositeFreezeLayer, 2));

        this.reorderLayer.reorderColumnPosition(0, 3);

        TestLayer expectedLayer = new TestLayer(
                4,
                4,
                "1:0;100 | 2:1;100 | 0:2;100 | 3:0;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
                "B0~:FROZEN_COLUMN_REGION | C0~:FROZEN_COLUMN_REGION | A0~:FROZEN_COLUMN_REGION | D0~:NONFROZEN_REGION \n"
                        + "B1~:FROZEN_COLUMN_REGION | C1~:FROZEN_COLUMN_REGION | A1~:FROZEN_COLUMN_REGION | D1~:NONFROZEN_REGION \n"
                        + "B2~:FROZEN_COLUMN_REGION | C2~:FROZEN_COLUMN_REGION | A2~:FROZEN_COLUMN_REGION | D2~:NONFROZEN_REGION \n"
                        + "B3~:FROZEN_COLUMN_REGION | C3~:FROZEN_COLUMN_REGION | A3~:FROZEN_COLUMN_REGION | D3~:NONFROZEN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

    @Test
    public void testReorderEndFrozenColumnToMiddleOfFrozenArea() {
        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                this.compositeFreezeLayer, 2));

        this.reorderLayer.reorderColumnPosition(2, 1);

        TestLayer expectedLayer = new TestLayer(
                4,
                4,
                "0:0;100 | 2:1;100 | 1:2;100 | 3:0;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
                "A0~:FROZEN_COLUMN_REGION | C0~:FROZEN_COLUMN_REGION | B0~:FROZEN_COLUMN_REGION | D0~:NONFROZEN_REGION \n"
                        + "A1~:FROZEN_COLUMN_REGION | C1~:FROZEN_COLUMN_REGION | B1~:FROZEN_COLUMN_REGION | D1~:NONFROZEN_REGION \n"
                        + "A2~:FROZEN_COLUMN_REGION | C2~:FROZEN_COLUMN_REGION | B2~:FROZEN_COLUMN_REGION | D2~:NONFROZEN_REGION \n"
                        + "A3~:FROZEN_COLUMN_REGION | C3~:FROZEN_COLUMN_REGION | B3~:FROZEN_COLUMN_REGION | D3~:NONFROZEN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

    @Test
    public void testReorderEndFrozenColumnToBeginningOfFrozenArea() {
        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                this.compositeFreezeLayer, 2));

        this.reorderLayer.reorderColumnPosition(2, 0);

        TestLayer expectedLayer = new TestLayer(
                4,
                4,
                "2:0;100 | 0:1;100 | 1:2;100 | 3:0;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
                "C0~:FROZEN_COLUMN_REGION | A0~:FROZEN_COLUMN_REGION | B0~:FROZEN_COLUMN_REGION | D0~:NONFROZEN_REGION \n"
                        + "C1~:FROZEN_COLUMN_REGION | A1~:FROZEN_COLUMN_REGION | B1~:FROZEN_COLUMN_REGION | D1~:NONFROZEN_REGION \n"
                        + "C2~:FROZEN_COLUMN_REGION | A2~:FROZEN_COLUMN_REGION | B2~:FROZEN_COLUMN_REGION | D2~:NONFROZEN_REGION \n"
                        + "C3~:FROZEN_COLUMN_REGION | A3~:FROZEN_COLUMN_REGION | B3~:FROZEN_COLUMN_REGION | D3~:NONFROZEN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

    // Hide/show

    @Test
    public void testHideMiddleFrozenColumn() {
        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                this.compositeFreezeLayer, 2));

        this.hideShowLayer.hideColumnPositions(Arrays.asList(new Integer[] { 1 }));

        TestLayer expectedLayer = new TestLayer(
                3,
                4,
                "0:0;100 | 2:1;100 | 3:0;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
                "A0~:FROZEN_COLUMN_REGION | C0~:FROZEN_COLUMN_REGION | D0~:NONFROZEN_REGION \n"
                        + "A1~:FROZEN_COLUMN_REGION | C1~:FROZEN_COLUMN_REGION | D1~:NONFROZEN_REGION \n"
                        + "A2~:FROZEN_COLUMN_REGION | C2~:FROZEN_COLUMN_REGION | D2~:NONFROZEN_REGION \n"
                        + "A3~:FROZEN_COLUMN_REGION | C3~:FROZEN_COLUMN_REGION | D3~:NONFROZEN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

    @Test
    public void testHideBeginningFrozenColumn() {
        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                this.compositeFreezeLayer, 2));

        this.hideShowLayer.hideColumnPositions(Arrays.asList(new Integer[] { 0 }));

        TestLayer expectedLayer = new TestLayer(
                3,
                4,
                "1:0;100 | 2:1;100 | 3:0;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
                "B0~:FROZEN_COLUMN_REGION | C0~:FROZEN_COLUMN_REGION | D0~:NONFROZEN_REGION \n"
                        + "B1~:FROZEN_COLUMN_REGION | C1~:FROZEN_COLUMN_REGION | D1~:NONFROZEN_REGION \n"
                        + "B2~:FROZEN_COLUMN_REGION | C2~:FROZEN_COLUMN_REGION | D2~:NONFROZEN_REGION \n"
                        + "B3~:FROZEN_COLUMN_REGION | C3~:FROZEN_COLUMN_REGION | D3~:NONFROZEN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

    @Test
    public void testHideEndFrozenColumn() {
        this.compositeFreezeLayer.doCommand(new FreezeColumnCommand(
                this.compositeFreezeLayer, 2));

        this.hideShowLayer.hideColumnPositions(Arrays.asList(new Integer[] { 2 }));

        TestLayer expectedLayer = new TestLayer(
                3,
                4,
                "0:0;100 | 1:1;100 | 3:0;100",
                "0:0;40  | 1:1;40  | 2:2;40  | 3:3;40",
                "A0~:FROZEN_COLUMN_REGION | B0~:FROZEN_COLUMN_REGION | D0~:NONFROZEN_REGION \n"
                        + "A1~:FROZEN_COLUMN_REGION | B1~:FROZEN_COLUMN_REGION | D1~:NONFROZEN_REGION \n"
                        + "A2~:FROZEN_COLUMN_REGION | B2~:FROZEN_COLUMN_REGION | D2~:NONFROZEN_REGION \n"
                        + "A3~:FROZEN_COLUMN_REGION | B3~:FROZEN_COLUMN_REGION | D3~:NONFROZEN_REGION \n");

        LayerAssert.assertLayerEquals(expectedLayer, this.compositeFreezeLayer);
    }

}
