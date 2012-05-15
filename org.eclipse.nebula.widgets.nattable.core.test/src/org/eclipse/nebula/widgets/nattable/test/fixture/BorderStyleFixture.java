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
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;

public class BorderStyleFixture extends BorderStyle {

	public static int THICKNESS = 2;
	public static Color COLOR = GUIHelper.COLOR_GREEN;
	public static LineStyleEnum LINE_STYLE = LineStyleEnum.DASHDOT;
	
	public BorderStyleFixture() {
		super(2, COLOR, LINE_STYLE);
	}
}
