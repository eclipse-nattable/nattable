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
package org.eclipse.nebula.widgets.nattable.examples.fixtures;


import org.eclipse.nebula.widgets.nattable.examples.examples._104_Styling._000_Styled_grid;
import org.eclipse.nebula.widgets.nattable.layer.config.DefaultRowHeaderStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Customize the default row header style. This has to be add back to the table.
 *
 * @see _000_Styled_grid
 */
public class StyledRowHeaderConfiguration extends DefaultRowHeaderStyleConfiguration {

	public StyledRowHeaderConfiguration() {
		font = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL));

		Image bgImage = new Image(Display.getDefault(), getClass().getResourceAsStream("row_header_bg.png"));
		TextPainter txtPainter = new TextPainter(false, false);
		ICellPainter bgImagePainter = new BackgroundImagePainter(txtPainter, bgImage, null);
		cellPainter = bgImagePainter;
	}
}
