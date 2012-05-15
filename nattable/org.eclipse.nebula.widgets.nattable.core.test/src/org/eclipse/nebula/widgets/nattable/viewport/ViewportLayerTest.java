/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ViewportLayerTest {
	protected ViewportLayer viewportLayer;
	protected LayerListenerFixture layerListener;

	@Before
	public void setup() {
		viewportLayer = new ViewportLayerFixture();
		layerListener = new LayerListenerFixture();
	}

	@After
	public void resertStaticFieldsInViewportFixture(){
		viewportLayer.getClientAreaProvider().getClientArea().x = 0;
		viewportLayer.getClientAreaProvider().getClientArea().y = 0;
		viewportLayer.getClientAreaProvider().getClientArea().width = 200;
		viewportLayer.getClientAreaProvider().getClientArea().height = 100;
	}

	@Test
	public void testMoveColumnPositionIntoViewportSimpleCase() {
		viewportLayer = new ViewportLayerFixture(new Rectangle(0, 0, 285, 100));
		assertEquals(0, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(1, viewportLayer.getColumnIndexByPosition(1));
		assertEquals(2, viewportLayer.getColumnIndexByPosition(2));

		viewportLayer.moveColumnPositionIntoViewport(3, false);
		assertEquals(1, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(2, viewportLayer.getColumnIndexByPosition(1));
		assertEquals(3, viewportLayer.getColumnIndexByPosition(2));

		viewportLayer.moveColumnPositionIntoViewport(0, false);
		assertEquals(0, viewportLayer.getColumnIndexByPosition(0));
	}

	@Test
	public void testMoveColumnPositionIntoViewportForAColumnAlreadyInTheViewport() {
		viewportLayer = new ViewportLayerFixture(new Rectangle(0, 0, 285, 100));
		viewportLayer.moveColumnPositionIntoViewport(2, false);
		assertEquals(0, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(1, viewportLayer.getColumnIndexByPosition(1));
		assertEquals(2, viewportLayer.getColumnIndexByPosition(2));
	}

	/**
	 *    0     1    2    3   4    5    6    7    8    9
	 *	|----|----|----|----|----|----|----|----|----|----|
	 */
	@Test
	public void moveColumnIntoViewportByMovingLeftAndRight() throws Exception {
		viewportLayer = new ViewportLayerFixture(10, 5, 80, 40);
		assertEquals(200, viewportLayer.getClientAreaWidth());
		assertEquals(3, viewportLayer.getColumnCount());

		viewportLayer.setOriginColumnPosition(7);
		assertEquals(7, viewportLayer.getColumnIndexByPosition(0));

		//Keep moving left by 1 col
		viewportLayer.moveColumnPositionIntoViewport(9, false);
		assertEquals(8, viewportLayer.getColumnIndexByPosition(0));

		viewportLayer.moveColumnPositionIntoViewport(8, false);
		assertEquals(8, viewportLayer.getColumnIndexByPosition(0));

		viewportLayer.moveColumnPositionIntoViewport(7, false);
		assertEquals(7, viewportLayer.getColumnIndexByPosition(0));

		viewportLayer.moveColumnPositionIntoViewport(6, false);
		assertEquals(6, viewportLayer.getColumnIndexByPosition(0));

		viewportLayer.moveColumnPositionIntoViewport(5, false);
		assertEquals(5, viewportLayer.getColumnIndexByPosition(0));

		//Move right
		viewportLayer.moveColumnPositionIntoViewport(7, false); //partially displayed
		assertEquals(5, viewportLayer.getColumnIndexByPosition(0));

		viewportLayer.moveColumnPositionIntoViewport(8, false);
		assertEquals(6, viewportLayer.getColumnIndexByPosition(0));
	}

	/**
	 * Rows
	 *    0     1    2    3   4
	 *	|----|----|----|----|----|
	 */
	@Test
	public void moveRowIntoViewportByMovingUpAndDown() throws Exception {
		viewportLayer = new ViewportLayerFixture(10, 5, 80, 80);
		assertEquals(100, viewportLayer.getClientAreaHeight());
		assertEquals(3, viewportLayer.getColumnCount());

		viewportLayer.setOriginRowPosition(3);
		assertEquals(3, viewportLayer.getRowIndexByPosition(0));

		//Keep moving up by 1 row
		viewportLayer.moveRowPositionIntoViewport(2, false);
		assertEquals(2, viewportLayer.getRowIndexByPosition(0));

		viewportLayer.moveRowPositionIntoViewport(1, false);
		assertEquals(1, viewportLayer.getRowIndexByPosition(0));

		//Move down
		viewportLayer.moveRowPositionIntoViewport(3, false);
		assertEquals(2, viewportLayer.getRowIndexByPosition(0)); //partially visible
		assertEquals(3, viewportLayer.getRowIndexByPosition(1));
		assertEquals(4, viewportLayer.getRowIndexByPosition(2));
	}

	@Test
	public void moveIntoViewportForAColPartiallyDisplayedAtTheRightEdge() throws Exception {
		viewportLayer = new ViewportLayerFixture(new Rectangle(0, 0, 260, 100));
		assertEquals(0, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(1, viewportLayer.getColumnIndexByPosition(1));
		assertEquals(2, viewportLayer.getColumnIndexByPosition(2));	//Partially visible

		viewportLayer.moveColumnPositionIntoViewport(2, false);
		assertEquals(0, viewportLayer.getColumnIndexByPosition(0));	//no movement
		assertEquals(1, viewportLayer.getColumnIndexByPosition(1));
		assertEquals(2, viewportLayer.getColumnIndexByPosition(2));
	}

	@Test
	public void getColumnIndexByPositionForAColumnOusideTheViewport() {
		assertEquals(2, viewportLayer.getColumnCount());

		// Does not check bounds. They get restricted by the column count.
		// Done for performance reasons
		assertEquals(3, viewportLayer.getColumnIndexByPosition(3));
		assertEquals(4, viewportLayer.getColumnIndexByPosition(4));
	}

	/**
	 *    0     1    2    3   4    5    6    7    8    9
	 *	|----|----|----|----|----|----|----|----|----|----|
	 */
	@Test
	public void getColumnIndexByPosition() {
		viewportLayer = new ViewportLayerFixture(10, 5, 80, 40);

		assertEquals(3, viewportLayer.getColumnCount());
		assertEquals(0, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(1, viewportLayer.getColumnIndexByPosition(1));
		assertEquals(2, viewportLayer.getColumnIndexByPosition(2));

		viewportLayer.setOriginColumnPosition(3);
		assertEquals(3, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(4, viewportLayer.getColumnIndexByPosition(1));
		assertEquals(5, viewportLayer.getColumnIndexByPosition(2));
	}

	@Test
	public void testMoveRowPositionIntoViewport() {
		viewportLayer.moveRowPositionIntoViewport(2, false);
		assertEquals(1, viewportLayer.getRowIndexByPosition(0));

		viewportLayer.moveRowPositionIntoViewport(0, false);
		assertEquals(0, viewportLayer.getRowIndexByPosition(0));
	}

	@Test
	public void testMoveCellPositionIntoViewport() {
		viewportLayer.moveCellPositionIntoViewport(2, 2, false);
		assertEquals(1, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(1, viewportLayer.getRowIndexByPosition(0));

		viewportLayer.moveCellPositionIntoViewport(2, 0, false);
		assertEquals(1, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(0, viewportLayer.getRowIndexByPosition(0));

		viewportLayer.moveCellPositionIntoViewport(0, 2, false);
		assertEquals(0, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(1, viewportLayer.getRowIndexByPosition(0));

		viewportLayer.moveCellPositionIntoViewport(0, 0, false);
		assertEquals(0, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(0, viewportLayer.getRowIndexByPosition(0));
	}

	@Test
	public void getColumnCount() throws Exception {
		assertEquals(2, viewportLayer.getColumnCount());
	}

	/**
	 * Width is calculated by adding of widths of all visible columns
	 */
	@Test
	public void getWidth() throws Exception {
		assertEquals(200, viewportLayer.getClientAreaWidth());
		assertEquals(2,viewportLayer.getColumnCount());
		assertEquals(250, viewportLayer.getWidth());

		viewportLayer.setOriginColumnPosition(2);
		assertEquals(200, viewportLayer.getClientAreaWidth());
		assertEquals(3,viewportLayer.getColumnCount());
		assertEquals(215, viewportLayer.getWidth());
	}

	/**
	 * Height is calculated by adding of heights of all visible columns
	 */
	@Test
	public void getHeight() throws Exception {
		assertEquals(100, viewportLayer.getClientAreaHeight());
		assertEquals(2,viewportLayer.getRowCount());
		assertEquals(110, viewportLayer.getHeight());

		viewportLayer.setOriginRowPosition(3);
		assertEquals(100, viewportLayer.getClientAreaHeight());
		assertEquals(3,viewportLayer.getRowCount());
		assertEquals(130, viewportLayer.getHeight());
	}

	/**
	 * Scrolling Events fired when origin changes
	 */

	@Test
	public void testMoveColumnPositionIntoViewportFiresEvent() throws Exception {
		viewportLayer.addLayerListener(layerListener);
		viewportLayer.moveColumnPositionIntoViewport(4, false);
		ILayerEvent event = layerListener.getReceivedEvents().get(0);

		assertTrue(event instanceof IVisualChangeEvent);
	}

	@Test
	public void setViewportOriginRowPosition() throws Exception {
		//Position count starts from 0
		viewportLayer.setOriginRowPosition(4);
		assertEquals(4, viewportLayer.getRowIndexByPosition(0));
	}

	@Test
	public void settingViewportRowOriginFireEvent() throws Exception {
		viewportLayer.addLayerListener(layerListener);
		viewportLayer.setOriginRowPosition(1);
		assertEquals(1, viewportLayer.getRowIndexByPosition(0));

		ILayerEvent event = layerListener.getReceivedEvents().get(0);
		assertTrue(event instanceof IVisualChangeEvent);
	}

	@Test
	public void setViewportOriginColumnPosition() throws Exception {
		//Position count starts from 0
		viewportLayer.setOriginColumnPosition(2);
		assertEquals(2, viewportLayer.getColumnIndexByPosition(0));
	}

	@Test
	public void settingViewportColumnOriginFiresEvent() throws Exception {
		viewportLayer.addLayerListener(layerListener);
		viewportLayer.setOriginColumnPosition(2);

		ILayerEvent event = layerListener.getReceivedEvents().get(0);
		assertTrue(event instanceof IVisualChangeEvent);
	}

	@Test
	public void scrollVerticallyByAPageCommand() throws Exception {
		ScrollSelectionCommand scrollCommand  = new ScrollSelectionCommandFixture();
		MoveSelectionCommand command = new MoveSelectionCommand(scrollCommand.getDirection(),
		viewportLayer.getRowCount(),
		scrollCommand.isShiftMask(),
		scrollCommand.isControlMask());

		assertEquals(MoveDirectionEnum.DOWN, command.getDirection());
		assertEquals(2, command.getStepSize());
	}

	@Test
	public void adjustRowOrigin() throws Exception {
		viewportLayer = new ViewportLayerFixture(10, 20, 100, 20);
		// Default client area: width 200, height 100
		assertEquals(5, viewportLayer.getRowCount());

		viewportLayer.setOriginRowPosition(10);
		assertEquals(10, viewportLayer.adjustRowOrigin());

		//Increase view port height
		viewportLayer.getClientAreaProvider().getClientArea().height = 400;
		assertEquals(0, viewportLayer.adjustRowOrigin());
	}

	@Test
	public void adjustColOrigin() throws Exception {
		viewportLayer = new ViewportLayerFixture(10, 20, 50, 20);
		// Default client area: width 200, height 100
		assertEquals(4, viewportLayer.getColumnCount());

		viewportLayer.setOriginColumnPosition(4);
		assertEquals(4, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(4, viewportLayer.adjustColumnOrigin());

		//Increase viewport width
		viewportLayer.getClientAreaProvider().getClientArea().width = 500;
		assertEquals(0, viewportLayer.adjustColumnOrigin());
	}

	@Test
	public void shouldPickUpTheDefaultClientAreaIfItHasNotBeenSet() throws Exception {
		ViewportLayer viewportLayer = new ViewportLayer(new DataLayerFixture());
		assertEquals(IClientAreaProvider.DEFAULT, viewportLayer.getClientAreaProvider());
	}
}
