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
package org.eclipse.nebula.widgets.nattable.painter.cell;

import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.cell.LayerCell;
import org.eclipse.nebula.widgets.nattable.test.fixture.CellPainterFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.MouseActionFixture;
import org.eclipse.nebula.widgets.nattable.test.fixture.NatTableFixture;
import org.eclipse.nebula.widgets.nattable.test.integration.SWTUtils;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ButtonCellPainterTest {

	private ButtonCellPainter buttonCellPainter;
	private CellPainterFixture buttonRaisedPainter;
	private CellPainterFixture buttonPressedPainter;
	private NatTableFixture natTable;
	private ILayerCell cellFixture;
	private GC gcFixture;
	private MouseEvent mouseClickEvent;

	@Before
	public void setup() {
		buttonRaisedPainter = new CellPainterFixture();
		buttonPressedPainter = new CellPainterFixture();
		buttonCellPainter = new ButtonCellPainter(buttonRaisedPainter, buttonPressedPainter);
		buttonCellPainter.setButtonFlashTime(500);

		natTable = new NatTableFixture();
		cellFixture = new LayerCell(natTable, 1, 5);
		gcFixture = new GC(natTable);

		mouseClickEvent = new MouseEvent(SWTUtils.getLeftClickEvent(100, 100, 0, natTable));
		mouseClickEvent.data = NatEventData.createInstanceFromEvent(mouseClickEvent);
	}

	@Test
	public void shouldPaintButtonPressedOnMouseClick() throws Exception {
		// Initial paint call
		buttonCellPainter.paintCell(cellFixture, gcFixture, cellFixture.getBounds(), natTable.getConfigRegistry());

		Assert.assertTrue(buttonRaisedPainter.isPainted());
		Assert.assertFalse(buttonPressedPainter.isPainted());

		// Mouse clicked
		buttonCellPainter.run(natTable, mouseClickEvent);
		buttonCellPainter.paintCell(cellFixture, gcFixture, cellFixture.getBounds(), natTable.getConfigRegistry());

		// Should be painted in pressed state
		Assert.assertTrue(buttonPressedPainter.isPainted());
	}

	@Test
	public void shouldNotifyListeners() throws Exception {
		MouseActionFixture mouseAction = new MouseActionFixture();
		buttonCellPainter.addClickListener(mouseAction);

		buttonCellPainter.run(natTable, mouseClickEvent);
		Assert.assertTrue(mouseAction.isActionInvoked());

		buttonCellPainter.removeClickListener(mouseAction);
		mouseAction.reset();

		buttonCellPainter.run(natTable, mouseClickEvent);
		Assert.assertFalse(mouseAction.isActionInvoked());
	}
}
