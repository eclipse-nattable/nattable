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
package org.eclipse.nebula.widgets.nattable.viewport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.command.MoveSelectionCommand;
import org.eclipse.nebula.widgets.nattable.selection.command.ScrollSelectionCommand;
import org.eclipse.nebula.widgets.nattable.test.fixture.command.ScrollSelectionCommandFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.DataLayerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.LayerListenerFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.layer.ViewportLayerFixture;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ViewportLayerTest {
    protected ViewportLayer viewportLayer;
    protected LayerListenerFixture layerListener;

    @Before
    public void setup() {
        this.viewportLayer = new ViewportLayerFixture();
        this.layerListener = new LayerListenerFixture();
    }

    @After
    public void resertStaticFieldsInViewportFixture() {
        this.viewportLayer.getClientAreaProvider().getClientArea().x = 0;
        this.viewportLayer.getClientAreaProvider().getClientArea().y = 0;
        this.viewportLayer.getClientAreaProvider().getClientArea().width = 200;
        this.viewportLayer.getClientAreaProvider().getClientArea().height = 100;
    }

    @Test
    public void testMoveColumnPositionIntoViewportSimpleCase() {
        this.viewportLayer = new ViewportLayerFixture();
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.viewportLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.viewportLayer.getColumnIndexByPosition(2));

        this.viewportLayer.moveColumnPositionIntoViewport(3);
        assertEquals(1, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(2, this.viewportLayer.getColumnIndexByPosition(1));
        assertEquals(3, this.viewportLayer.getColumnIndexByPosition(2));

        this.viewportLayer.moveColumnPositionIntoViewport(0);
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));
    }

    @Test
    public void testMoveColumnPositionIntoViewportForAColumnAlreadyInTheViewport() {
        this.viewportLayer = new ViewportLayerFixture(new Rectangle(0, 0, 285, 100));
        this.viewportLayer.moveColumnPositionIntoViewport(2);
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.viewportLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.viewportLayer.getColumnIndexByPosition(2));
    }

    /*
     * 0 1 2 3 4 5 6 7 8 9 |----|----|----|----|----|----|----|----|----|----|
     */
    @Test
    public void moveColumnIntoViewportByMovingLeftAndRight() throws Exception {
        // width of each column = 80
        // total width = 800
        // client area width = 200 = 80 + 80 + 40
        this.viewportLayer = new ViewportLayerFixture(10, 5, 80, 40);
        assertEquals(200, this.viewportLayer.getClientAreaWidth());
        assertEquals(3, this.viewportLayer.getColumnCount());

        this.viewportLayer.setOriginX(600);
        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(200, this.viewportLayer.getWidth());
        assertEquals(7, this.viewportLayer.getColumnIndexByPosition(0));

        // Keep moving left by 1 col
        this.viewportLayer.moveColumnPositionIntoViewport(9);
        assertEquals(7, this.viewportLayer.getColumnIndexByPosition(0));

        this.viewportLayer.moveColumnPositionIntoViewport(8);
        assertEquals(7, this.viewportLayer.getColumnIndexByPosition(0));

        this.viewportLayer.moveColumnPositionIntoViewport(7);
        assertEquals(7, this.viewportLayer.getColumnIndexByPosition(0));

        this.viewportLayer.moveColumnPositionIntoViewport(6);
        assertEquals(6, this.viewportLayer.getColumnIndexByPosition(0));

        this.viewportLayer.moveColumnPositionIntoViewport(5);
        assertEquals(5, this.viewportLayer.getColumnIndexByPosition(0));

        // Move right
        this.viewportLayer.moveColumnPositionIntoViewport(7); // partially
                                                              // displayed
        assertEquals(5, this.viewportLayer.getColumnIndexByPosition(0));

        this.viewportLayer.moveColumnPositionIntoViewport(8);
        assertEquals(6, this.viewportLayer.getColumnIndexByPosition(0));
    }

    /*
     * Rows 0 1 2 3 4 |----|----|----|----|----|
     */
    @Test
    public void moveRowIntoViewportByMovingUpAndDown() throws Exception {
        this.viewportLayer = new ViewportLayerFixture(10, 5, 80, 80);
        assertEquals(100, this.viewportLayer.getClientAreaHeight());
        assertEquals(3, this.viewportLayer.getColumnCount());

        this.viewportLayer.setOriginY(this.viewportLayer.getStartYOfRowPosition(3));
        assertEquals(3, this.viewportLayer.getRowIndexByPosition(0));

        // Keep moving up by 1 row
        this.viewportLayer.moveRowPositionIntoViewport(2);
        assertEquals(2, this.viewportLayer.getRowIndexByPosition(0));

        this.viewportLayer.moveRowPositionIntoViewport(1);
        assertEquals(1, this.viewportLayer.getRowIndexByPosition(0));

        // Move down
        this.viewportLayer.moveRowPositionIntoViewport(3);
        assertEquals(2, this.viewportLayer.getRowIndexByPosition(0)); // partially
        // visible
        assertEquals(3, this.viewportLayer.getRowIndexByPosition(1));
        assertEquals(4, this.viewportLayer.getRowIndexByPosition(2));
    }

    @Test
    public void moveIntoViewportForAColPartiallyDisplayedAtTheRightEdge()
            throws Exception {
        this.viewportLayer = new ViewportLayerFixture(new Rectangle(0, 0, 260, 100));
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.viewportLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.viewportLayer.getColumnIndexByPosition(2)); // Partially
        // visible

        this.viewportLayer.moveColumnPositionIntoViewport(2);
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0)); // no
        // movement
        assertEquals(1, this.viewportLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.viewportLayer.getColumnIndexByPosition(2));
    }

    @Test
    public void getColumnIndexByPositionForAColumnOusideTheViewport() {
        assertEquals(2, this.viewportLayer.getColumnCount());

        // Does not check bounds. They get restricted by the column count.
        // Done for performance reasons
        assertEquals(3, this.viewportLayer.getColumnIndexByPosition(3));
        assertEquals(4, this.viewportLayer.getColumnIndexByPosition(4));
    }

    /*
     * 0 1 2 3 4 5 6 7 8 9 |----|----|----|----|----|----|----|----|----|----|
     */
    @Test
    public void getColumnIndexByPosition() {
        this.viewportLayer = new ViewportLayerFixture(10, 5, 80, 40);

        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.viewportLayer.getColumnIndexByPosition(1));
        assertEquals(2, this.viewportLayer.getColumnIndexByPosition(2));

        this.viewportLayer.setOriginX(this.viewportLayer.getStartXOfColumnPosition(3));
        assertEquals(3, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(4, this.viewportLayer.getColumnIndexByPosition(1));
        assertEquals(5, this.viewportLayer.getColumnIndexByPosition(2));
    }

    @Test
    public void testMoveRowPositionIntoViewport() {
        this.viewportLayer.moveRowPositionIntoViewport(3);
        assertEquals(1, this.viewportLayer.getRowIndexByPosition(0));

        this.viewportLayer.moveRowPositionIntoViewport(0);
        assertEquals(0, this.viewportLayer.getRowIndexByPosition(0));
    }

    @Test
    public void testMoveCellPositionIntoViewport() {
        this.viewportLayer.moveCellPositionIntoViewport(3, 3);
        assertEquals(1, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.viewportLayer.getRowIndexByPosition(0));

        this.viewportLayer.moveCellPositionIntoViewport(2, 0);
        assertEquals(1, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(0, this.viewportLayer.getRowIndexByPosition(0));

        this.viewportLayer.moveCellPositionIntoViewport(0, 3);
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.viewportLayer.getRowIndexByPosition(0));

        this.viewportLayer.moveCellPositionIntoViewport(0, 0);
        assertEquals(0, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(0, this.viewportLayer.getRowIndexByPosition(0));
    }

    @Test
    public void getColumnCount() throws Exception {
        assertEquals(2, this.viewportLayer.getColumnCount());
    }

    /*
     * Width is calculated by adding of widths of all visible columns
     */
    @Test
    public void getWidth() throws Exception {
        assertEquals(200, this.viewportLayer.getClientAreaWidth());
        assertEquals(2, this.viewportLayer.getColumnCount());
        assertEquals(200, this.viewportLayer.getWidth());

        this.viewportLayer.setOriginX(this.viewportLayer.getStartXOfColumnPosition(2));
        assertEquals(200, this.viewportLayer.getClientAreaWidth());
        assertEquals(3, this.viewportLayer.getColumnCount());
        assertEquals(200, this.viewportLayer.getWidth());
    }

    /*
     * Height is calculated by adding of heights of all visible columns
     */
    @Test
    public void getHeight() throws Exception {
        assertEquals(100, this.viewportLayer.getClientAreaHeight());
        assertEquals(2, this.viewportLayer.getRowCount());
        assertEquals(100, this.viewportLayer.getHeight());

        this.viewportLayer.setOriginY(this.viewportLayer.getStartYOfRowPosition(3));
        assertEquals(100, this.viewportLayer.getClientAreaHeight());
        assertEquals(3, this.viewportLayer.getRowCount());
        assertEquals(100, this.viewportLayer.getHeight());
    }

    /*
     * Scrolling Events fired when origin changes
     */
    @Test
    public void testMoveColumnPositionIntoViewportFiresEvent() throws Exception {
        this.viewportLayer.addLayerListener(this.layerListener);
        this.viewportLayer.moveColumnPositionIntoViewport(4);
        ILayerEvent event = this.layerListener.getReceivedEvents().get(0);

        assertTrue(event instanceof IVisualChangeEvent);
    }

    @Test
    public void setViewportOriginColumnPosition() throws Exception {
        // Position count starts from 0
        this.viewportLayer.setOriginX(this.viewportLayer.getStartXOfColumnPosition(2));
        assertEquals(2, this.viewportLayer.getColumnIndexByPosition(0));
    }

    @Test
    public void settingViewportColumnOriginFiresEvent() throws Exception {
        this.viewportLayer.addLayerListener(this.layerListener);
        this.viewportLayer.setOriginX(this.viewportLayer.getStartXOfColumnPosition(2));

        ILayerEvent event = this.layerListener.getReceivedEvents().get(0);
        assertTrue(event instanceof IVisualChangeEvent);
    }

    @Test
    public void setViewportMinOriginColumnPosition() throws Exception {
        this.viewportLayer = new ViewportLayerFixture(10, 20, 50, 20);

        this.viewportLayer.setMinimumOriginX(100);
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(100, this.viewportLayer.getOrigin().getX());
        assertEquals(2, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(4, this.viewportLayer.getColumnCount());

        this.viewportLayer.setMinimumOriginX(200);
        assertEquals(4, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(200, this.viewportLayer.getOrigin().getX());
        assertEquals(4, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(4, this.viewportLayer.getColumnCount());

        this.viewportLayer.setMinimumOriginX(100);
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(100, this.viewportLayer.getOrigin().getX());

        this.viewportLayer.setOriginX(150);
        this.viewportLayer.setMinimumOriginX(200);
        assertEquals(4, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(250, this.viewportLayer.getOrigin().getX());

        this.viewportLayer.setMinimumOriginX(100);
        assertEquals(2, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(150, this.viewportLayer.getOrigin().getX());

        this.viewportLayer.setMinimumOriginX(450);
        assertEquals(9, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(450, this.viewportLayer.getOrigin().getX());
        assertEquals(9, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(1, this.viewportLayer.getColumnCount());

        this.viewportLayer.setMinimumOriginX(500);
        assertEquals(-1, this.viewportLayer.getMinimumOriginColumnPosition());
        assertEquals(500, this.viewportLayer.getOrigin().getX());
        assertEquals(-1, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(0, this.viewportLayer.getColumnCount());
    }

    @Test
    public void setViewportOriginRowPosition() throws Exception {
        // Position count starts from 0
        this.viewportLayer.setOriginY(this.viewportLayer.getStartYOfRowPosition(4));
        assertEquals(4, this.viewportLayer.getRowIndexByPosition(0));
    }

    @Test
    public void settingViewportRowOriginFireEvent() throws Exception {
        this.viewportLayer.addLayerListener(this.layerListener);
        this.viewportLayer.setOriginY(this.viewportLayer.getStartYOfRowPosition(1));
        assertEquals(1, this.viewportLayer.getRowIndexByPosition(0));

        ILayerEvent event = this.layerListener.getReceivedEvents().get(0);
        assertTrue(event instanceof IVisualChangeEvent);
    }

    @Test
    public void setViewportMinOriginRowPosition() throws Exception {
        this.viewportLayer = new ViewportLayerFixture(10, 20, 50, 20);

        this.viewportLayer.setMinimumOriginY(40);
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(40, this.viewportLayer.getOrigin().getY());
        assertEquals(2, this.viewportLayer.getRowIndexByPosition(0));
        assertEquals(5, this.viewportLayer.getRowCount());

        this.viewportLayer.setMinimumOriginY(80);
        assertEquals(4, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(80, this.viewportLayer.getOrigin().getY());
        assertEquals(4, this.viewportLayer.getRowIndexByPosition(0));
        assertEquals(5, this.viewportLayer.getRowCount());

        this.viewportLayer.setMinimumOriginY(40);
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(40, this.viewportLayer.getOrigin().getY());

        this.viewportLayer.setOriginY(60);
        this.viewportLayer.setMinimumOriginY(80);
        assertEquals(4, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(100, this.viewportLayer.getOrigin().getY());

        this.viewportLayer.setMinimumOriginY(40);
        assertEquals(2, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(60, this.viewportLayer.getOrigin().getY());

        this.viewportLayer.setMinimumOriginY(380);
        assertEquals(19, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(380, this.viewportLayer.getOrigin().getY());
        assertEquals(19, this.viewportLayer.getRowIndexByPosition(0));
        assertEquals(1, this.viewportLayer.getRowCount());

        this.viewportLayer.setMinimumOriginY(400);
        assertEquals(-1, this.viewportLayer.getMinimumOriginRowPosition());
        assertEquals(400, this.viewportLayer.getOrigin().getY());
        assertEquals(-1, this.viewportLayer.getRowIndexByPosition(0));
        assertEquals(0, this.viewportLayer.getRowCount());
    }

    @Test
    public void scrollVerticallyByAPageCommand() throws Exception {
        ScrollSelectionCommand scrollCommand = new ScrollSelectionCommandFixture();
        MoveSelectionCommand command = new MoveSelectionCommand(
                scrollCommand.getDirection(), this.viewportLayer.getRowCount(),
                scrollCommand.isShiftMask(), scrollCommand.isControlMask());

        assertEquals(MoveDirectionEnum.DOWN, command.getDirection());
        assertEquals(2, command.getStepSize().intValue());
    }

    @Test
    public void adjustRowOrigin() throws Exception {
        this.viewportLayer = new ViewportLayerFixture(10, 20, 100, 20);
        // Default client area: width 200, height 100
        assertEquals(5, this.viewportLayer.getRowCount());

        assertEquals(200, this.viewportLayer.getStartYOfRowPosition(10));
        assertEquals(200, this.viewportLayer.adjustOriginY(this.viewportLayer
                .getStartYOfRowPosition(10)));

        // Increase view port height
        this.viewportLayer.getClientAreaProvider().getClientArea().height = 400;
        assertEquals(0, this.viewportLayer.adjustOriginY(this.viewportLayer
                .getStartYOfRowPosition(10)));
    }

    @Test
    public void adjustColOrigin() throws Exception {
        this.viewportLayer = new ViewportLayerFixture(10, 20, 50, 20);
        // Default client area: width 200, height 100
        assertEquals(4, this.viewportLayer.getColumnCount());

        this.viewportLayer.setOriginX(this.viewportLayer.getStartXOfColumnPosition(4));
        assertEquals(4, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(200, this.viewportLayer.getStartXOfColumnPosition(4));
        assertEquals(200, this.viewportLayer.adjustOriginX(this.viewportLayer
                .getStartXOfColumnPosition(4)));

        // Try to scroll off the end
        this.viewportLayer.setOriginX(this.viewportLayer.getStartXOfColumnPosition(9));
        assertEquals(6, this.viewportLayer.getColumnIndexByPosition(0));
        assertEquals(450, this.viewportLayer.getStartXOfColumnPosition(9));
        assertEquals(300, this.viewportLayer.adjustOriginX(this.viewportLayer
                .getStartXOfColumnPosition(9)));

        // Increase viewport width
        this.viewportLayer.getClientAreaProvider().getClientArea().width = 500;
        assertEquals(450, this.viewportLayer.getStartXOfColumnPosition(9));
        assertEquals(0, this.viewportLayer.adjustOriginX(this.viewportLayer
                .getStartXOfColumnPosition(9)));
    }

    @Test
    public void shouldPickUpTheDefaultClientAreaIfItHasNotBeenSet()
            throws Exception {
        ViewportLayer viewportLayer = new ViewportLayer(new DataLayerFixture());
        assertEquals(IClientAreaProvider.DEFAULT,
                viewportLayer.getClientAreaProvider());
    }
}
