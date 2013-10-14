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
package org.eclipse.nebula.widgets.nattable.viewport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.graphics.Rectangle;

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
		viewportLayer = new ViewportLayerFixture();
		assertEquals(0, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(1, viewportLayer.getColumnIndexByPosition(1));
		assertEquals(2, viewportLayer.getColumnIndexByPosition(2));

		viewportLayer.moveColumnPositionIntoViewport(3);
		assertEquals(1, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(2, viewportLayer.getColumnIndexByPosition(1));
		assertEquals(3, viewportLayer.getColumnIndexByPosition(2));

		viewportLayer.moveColumnPositionIntoViewport(0);
		assertEquals(0, viewportLayer.getColumnIndexByPosition(0));
	}

	@Test
	public void testMoveColumnPositionIntoViewportForAColumnAlreadyInTheViewport() {
		viewportLayer = new ViewportLayerFixture(new Rectangle(0, 0, 285, 100));
		viewportLayer.moveColumnPositionIntoViewport(2);
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
		// width of each column = 80
		// total width = 800
		// client area width = 200 = 80 + 80 + 40
		viewportLayer = new ViewportLayerFixture(10, 5, 80, 40);
		assertEquals(200, viewportLayer.getClientAreaWidth());
		assertEquals(3, viewportLayer.getColumnCount());

		viewportLayer.setOriginX(600);
		assertEquals(3, viewportLayer.getColumnCount());
		assertEquals(200, viewportLayer.getWidth());
		assertEquals(7, viewportLayer.getColumnIndexByPosition(0));

		//Keep moving left by 1 col
		viewportLayer.moveColumnPositionIntoViewport(9);
		assertEquals(7, viewportLayer.getColumnIndexByPosition(0));

		viewportLayer.moveColumnPositionIntoViewport(8);
		assertEquals(7, viewportLayer.getColumnIndexByPosition(0));

		viewportLayer.moveColumnPositionIntoViewport(7);
		assertEquals(7, viewportLayer.getColumnIndexByPosition(0));

		viewportLayer.moveColumnPositionIntoViewport(6);
		assertEquals(6, viewportLayer.getColumnIndexByPosition(0));

		viewportLayer.moveColumnPositionIntoViewport(5);
		assertEquals(5, viewportLayer.getColumnIndexByPosition(0));

		//Move right
		viewportLayer.moveColumnPositionIntoViewport(7); //partially displayed
		assertEquals(5, viewportLayer.getColumnIndexByPosition(0));

		viewportLayer.moveColumnPositionIntoViewport(8);
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

		viewportLayer.setOriginY(viewportLayer.getStartYOfRowPosition(3));
		assertEquals(3, viewportLayer.getRowIndexByPosition(0));

		//Keep moving up by 1 row
		viewportLayer.moveRowPositionIntoViewport(2);
		assertEquals(2, viewportLayer.getRowIndexByPosition(0));

		viewportLayer.moveRowPositionIntoViewport(1);
		assertEquals(1, viewportLayer.getRowIndexByPosition(0));

		//Move down
		viewportLayer.moveRowPositionIntoViewport(3);
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

		viewportLayer.moveColumnPositionIntoViewport(2);
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

		viewportLayer.setOriginX(viewportLayer.getStartXOfColumnPosition(3));
		assertEquals(3, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(4, viewportLayer.getColumnIndexByPosition(1));
		assertEquals(5, viewportLayer.getColumnIndexByPosition(2));
	}

	@Test
	public void testMoveRowPositionIntoViewport() {
		viewportLayer.moveRowPositionIntoViewport(3);
		assertEquals(1, viewportLayer.getRowIndexByPosition(0));

		viewportLayer.moveRowPositionIntoViewport(0);
		assertEquals(0, viewportLayer.getRowIndexByPosition(0));
	}

	@Test
	public void testMoveCellPositionIntoViewport() {
		viewportLayer.moveCellPositionIntoViewport(3, 3);
		assertEquals(1, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(1, viewportLayer.getRowIndexByPosition(0));

		viewportLayer.moveCellPositionIntoViewport(2, 0);
		assertEquals(1, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(0, viewportLayer.getRowIndexByPosition(0));

		viewportLayer.moveCellPositionIntoViewport(0, 3);
		assertEquals(0, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(1, viewportLayer.getRowIndexByPosition(0));

		viewportLayer.moveCellPositionIntoViewport(0, 0);
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
		assertEquals(200, viewportLayer.getWidth());

		viewportLayer.setOriginX(viewportLayer.getStartXOfColumnPosition(2));
		assertEquals(200, viewportLayer.getClientAreaWidth());
		assertEquals(3,viewportLayer.getColumnCount());
		assertEquals(200, viewportLayer.getWidth());
	}

	/**
	 * Height is calculated by adding of heights of all visible columns
	 */
	@Test
	public void getHeight() throws Exception {
		assertEquals(100, viewportLayer.getClientAreaHeight());
		assertEquals(2,viewportLayer.getRowCount());
		assertEquals(100, viewportLayer.getHeight());

		viewportLayer.setOriginY(viewportLayer.getStartYOfRowPosition(3));
		assertEquals(100, viewportLayer.getClientAreaHeight());
		assertEquals(3,viewportLayer.getRowCount());
		assertEquals(100, viewportLayer.getHeight());
	}

	/**
	 * Scrolling Events fired when origin changes
	 */
	@Test
	public void testMoveColumnPositionIntoViewportFiresEvent() throws Exception {
		viewportLayer.addLayerListener(layerListener);
		viewportLayer.moveColumnPositionIntoViewport(4);
		ILayerEvent event = layerListener.getReceivedEvents().get(0);

		assertTrue(event instanceof IVisualChangeEvent);
	}

	@Test
	public void setViewportOriginColumnPosition() throws Exception {
		//Position count starts from 0
		viewportLayer.setOriginX(viewportLayer.getStartXOfColumnPosition(2));
		assertEquals(2, viewportLayer.getColumnIndexByPosition(0));
	}

	@Test
	public void settingViewportColumnOriginFiresEvent() throws Exception {
		viewportLayer.addLayerListener(layerListener);
		viewportLayer.setOriginX(viewportLayer.getStartXOfColumnPosition(2));

		ILayerEvent event = layerListener.getReceivedEvents().get(0);
		assertTrue(event instanceof IVisualChangeEvent);
	}

	@Test
	public void setViewportMinOriginColumnPosition() throws Exception {
		viewportLayer = new ViewportLayerFixture(10, 20, 50, 20);
		
		viewportLayer.setMinimumOriginX(100);
		assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
		assertEquals(100, viewportLayer.getOrigin().getX());
		assertEquals(2, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(4, viewportLayer.getColumnCount());
		
		viewportLayer.setMinimumOriginX(200);
		assertEquals(4, viewportLayer.getMinimumOriginColumnPosition());
		assertEquals(200, viewportLayer.getOrigin().getX());
		assertEquals(4, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(4, viewportLayer.getColumnCount());
		
		viewportLayer.setMinimumOriginX(100);
		assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
		assertEquals(100, viewportLayer.getOrigin().getX());
		
		viewportLayer.setOriginX(150);
		viewportLayer.setMinimumOriginX(200);
		assertEquals(4, viewportLayer.getMinimumOriginColumnPosition());
		assertEquals(250, viewportLayer.getOrigin().getX());
		
		viewportLayer.setMinimumOriginX(100);
		assertEquals(2, viewportLayer.getMinimumOriginColumnPosition());
		assertEquals(150, viewportLayer.getOrigin().getX());
		
		viewportLayer.setMinimumOriginX(450);
		assertEquals(9, viewportLayer.getMinimumOriginColumnPosition());
		assertEquals(450, viewportLayer.getOrigin().getX());
		assertEquals(9, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(1, viewportLayer.getColumnCount());
		
		viewportLayer.setMinimumOriginX(500);
		assertEquals(-1, viewportLayer.getMinimumOriginColumnPosition());
		assertEquals(500, viewportLayer.getOrigin().getX());
		assertEquals(-1, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(0, viewportLayer.getColumnCount());
	}
	
	@Test
	public void setViewportOriginRowPosition() throws Exception {
		//Position count starts from 0
		viewportLayer.setOriginY(viewportLayer.getStartYOfRowPosition(4));
		assertEquals(4, viewportLayer.getRowIndexByPosition(0));
	}

	@Test
	public void settingViewportRowOriginFireEvent() throws Exception {
		viewportLayer.addLayerListener(layerListener);
		viewportLayer.setOriginY(viewportLayer.getStartYOfRowPosition(1));
		assertEquals(1, viewportLayer.getRowIndexByPosition(0));

		ILayerEvent event = layerListener.getReceivedEvents().get(0);
		assertTrue(event instanceof IVisualChangeEvent);
	}

	@Test
	public void setViewportMinOriginRowPosition() throws Exception {
		viewportLayer = new ViewportLayerFixture(10, 20, 50, 20);
		
		viewportLayer.setMinimumOriginY(40);
		assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
		assertEquals(40, viewportLayer.getOrigin().getY());
		assertEquals(2, viewportLayer.getRowIndexByPosition(0));
		assertEquals(5, viewportLayer.getRowCount());
		
		viewportLayer.setMinimumOriginY(80);
		assertEquals(4, viewportLayer.getMinimumOriginRowPosition());
		assertEquals(80, viewportLayer.getOrigin().getY());
		assertEquals(4, viewportLayer.getRowIndexByPosition(0));
		assertEquals(5, viewportLayer.getRowCount());
		
		viewportLayer.setMinimumOriginY(40);
		assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
		assertEquals(40, viewportLayer.getOrigin().getY());
		
		viewportLayer.setOriginY(60);
		viewportLayer.setMinimumOriginY(80);
		assertEquals(4, viewportLayer.getMinimumOriginRowPosition());
		assertEquals(100, viewportLayer.getOrigin().getY());
		
		viewportLayer.setMinimumOriginY(40);
		assertEquals(2, viewportLayer.getMinimumOriginRowPosition());
		assertEquals(60, viewportLayer.getOrigin().getY());
		
		viewportLayer.setMinimumOriginY(380);
		assertEquals(19, viewportLayer.getMinimumOriginRowPosition());
		assertEquals(380, viewportLayer.getOrigin().getY());
		assertEquals(19, viewportLayer.getRowIndexByPosition(0));
		assertEquals(1, viewportLayer.getRowCount());
		
		viewportLayer.setMinimumOriginY(400);
		assertEquals(-1, viewportLayer.getMinimumOriginRowPosition());
		assertEquals(400, viewportLayer.getOrigin().getY());
		assertEquals(-1, viewportLayer.getRowIndexByPosition(0));
		assertEquals(0, viewportLayer.getRowCount());
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

		assertEquals(200, viewportLayer.getStartYOfRowPosition(10));
		assertEquals(200, viewportLayer.adjustOriginY(viewportLayer.getStartYOfRowPosition(10)));

		//Increase view port height
		viewportLayer.getClientAreaProvider().getClientArea().height = 400;
		assertEquals(0, viewportLayer.adjustOriginY(viewportLayer.getStartYOfRowPosition(10)));
	}

	@Test
	public void adjustColOrigin() throws Exception {
		viewportLayer = new ViewportLayerFixture(10, 20, 50, 20);
		// Default client area: width 200, height 100
		assertEquals(4, viewportLayer.getColumnCount());

		viewportLayer.setOriginX(viewportLayer.getStartXOfColumnPosition(4));
		assertEquals(4, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(200, viewportLayer.getStartXOfColumnPosition(4));
		assertEquals(200, viewportLayer.adjustOriginX(viewportLayer.getStartXOfColumnPosition(4)));
		
		// Try to scroll off the end
		viewportLayer.setOriginX(viewportLayer.getStartXOfColumnPosition(9));
		assertEquals(6, viewportLayer.getColumnIndexByPosition(0));
		assertEquals(450, viewportLayer.getStartXOfColumnPosition(9));
		assertEquals(300, viewportLayer.adjustOriginX(viewportLayer.getStartXOfColumnPosition(9)));

		//Increase viewport width
		viewportLayer.getClientAreaProvider().getClientArea().width = 500;
		assertEquals(450, viewportLayer.getStartXOfColumnPosition(9));
		assertEquals(0, viewportLayer.adjustOriginX(viewportLayer.getStartXOfColumnPosition(9)));
	}

	@Test
	public void shouldPickUpTheDefaultClientAreaIfItHasNotBeenSet() throws Exception {
		ViewportLayer viewportLayer = new ViewportLayer(new DataLayerFixture());
		assertEquals(IClientAreaProvider.DEFAULT, viewportLayer.getClientAreaProvider());
	}
}
