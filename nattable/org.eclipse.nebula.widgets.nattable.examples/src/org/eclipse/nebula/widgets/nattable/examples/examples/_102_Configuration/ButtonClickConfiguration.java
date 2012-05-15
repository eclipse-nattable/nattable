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
package org.eclipse.nebula.widgets.nattable.examples.examples._102_Configuration;

import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.painter.cell.ButtonCellPainter;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellLabelMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;

public class ButtonClickConfiguration<T> extends AbstractUiBindingConfiguration {

	private final ButtonCellPainter buttonCellPainter;

	public ButtonClickConfiguration(ButtonCellPainter buttonCellPainter) {
		this.buttonCellPainter = buttonCellPainter;
	}

	/**
	 * Configure the UI bindings for the mouse click
	 */
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		// Match a mouse event on the body, when the left button is clicked
		// and the custom cell label is present
		CellLabelMouseEventMatcher mouseEventMatcher = new CellLabelMouseEventMatcher(
															GridRegion.BODY,
															MouseEventMatcher.LEFT_BUTTON,
															Rendereing_a_cell_as_a_button.CUSTOM_CELL_LABEL);

		// Inform the button painter of the click.
		uiBindingRegistry.registerMouseDownBinding(mouseEventMatcher, buttonCellPainter);
	}

}
