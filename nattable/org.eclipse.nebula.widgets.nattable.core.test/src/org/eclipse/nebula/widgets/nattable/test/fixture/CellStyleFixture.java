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
package org.eclipse.nebula.widgets.nattable.test.fixture;


import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class CellStyleFixture extends Style {

	public static final Color TEST_BG_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
	public static final Color TEST_FG_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	public static final Color TEST_BORDER_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
	public static final BorderStyle TEST_BORDER_STYLE = new BorderStyle(2, TEST_BORDER_COLOR, LineStyleEnum.DOTTED);
	public static final Font TEST_FONT = Display.getDefault().getSystemFont();

	public CellStyleFixture() {
		setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, TEST_BG_COLOR);
		setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, TEST_FG_COLOR);

		setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
		setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);

		setAttributeValue(CellStyleAttributes.FONT, TEST_FONT);
		setAttributeValue(CellStyleAttributes.BORDER_STYLE, TEST_BORDER_STYLE);
	}

	/**
	 * Creates a Style with just the horizonal align attribute set.
	 */
	public CellStyleFixture(HorizontalAlignmentEnum hAlign) {
		setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, hAlign);
	}
}
